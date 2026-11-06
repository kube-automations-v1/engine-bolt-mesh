package automationsv1.engine_bolt_mesh.application.domain.usecase;

import automationsv1.engine_bolt_mesh.application.domain.exception.ResourceNotFoundException;
import automationsv1.engine_bolt_mesh.application.domain.exception.WorkflowCycleException;
import automationsv1.engine_bolt_mesh.application.domain.exception.WorkflowExecutionException;
import automationsv1.engine_bolt_mesh.application.domain.model.WorkflowContext;
import automationsv1.engine_bolt_mesh.application.domain.model.WorkflowDefinition;
import automationsv1.engine_bolt_mesh.application.domain.model.WorkflowExecutionLog;
import automationsv1.engine_bolt_mesh.application.domain.model.WorkflowExecutionLog.StepTrace;
import automationsv1.engine_bolt_mesh.application.domain.model.step.ApiCallStep;
import automationsv1.engine_bolt_mesh.application.domain.model.step.ValidationRuleStep;
import automationsv1.engine_bolt_mesh.application.domain.model.step.WorkflowStep;
import automationsv1.engine_bolt_mesh.application.ports.in.EvaluateConditionInputPort;
import automationsv1.engine_bolt_mesh.application.ports.in.ExecuteApiCallInputPort;
import automationsv1.engine_bolt_mesh.application.ports.in.WorkFlowEngineInputPort;
import automationsv1.engine_bolt_mesh.application.ports.out.WorkflowDefinitionOutputPort;
import automationsv1.engine_bolt_mesh.application.ports.out.WorkflowExecutionLogOutputPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class WorkFlowEngineUseCase implements WorkFlowEngineInputPort {

    static final int MAX_STEPS = 100;

    private static final String STEP_SUCCESS = "SUCCESS";
    private static final String STEP_ERROR = "ERROR";

    private final WorkflowDefinitionOutputPort workflowDefinitionOutputPort;
    private final WorkflowExecutionLogOutputPort executionLogOutputPort;
    private final ExecuteApiCallInputPort executeApiCall;
    private final EvaluateConditionInputPort evaluateCondition;

    @Override
    public WorkflowExecutionLog run(UUID workflowId, Map<String, Object> triggerPayload) {
        WorkflowDefinition definition = workflowDefinitionOutputPort.findById(workflowId)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow não encontrado: " + workflowId));
        if (!definition.active()) {
            throw new WorkflowExecutionException("Workflow inativo: " + workflowId);
        }

        Instant startedAt = Instant.now();
        WorkflowContext context = new WorkflowContext(triggerPayload);
        String status = WorkflowExecutionLog.COMPLETED;

        try {
            walk(definition, context);
        } catch (RuntimeException e) {
            status = WorkflowExecutionLog.FAILED;
            context.addTrace(new StepTrace("-", "ENGINE", STEP_ERROR, e.getMessage(), 0, Instant.now()));
        }

        return executionLogOutputPort.save(new WorkflowExecutionLog(
                null, workflowId, status,
                triggerPayload == null ? Map.of() : triggerPayload,
                context.getSteps(), List.copyOf(context.getTraces()),
                startedAt, Instant.now()));
    }

    private void walk(WorkflowDefinition definition, WorkflowContext context) {
        WorkflowStep current = definition.getFirstStep();
        if (current == null) {
            throw new WorkflowExecutionException("Primeiro passo não encontrado: " + definition.firstStepId());
        }

        int executed = 0;
        while (current != null) {
            if (++executed > MAX_STEPS) {
                throw new WorkflowCycleException(MAX_STEPS);
            }

            String nextStepId = switch (current) {
                case ApiCallStep apiStep -> runApiCall(apiStep, context);
                case ValidationRuleStep valStep -> runValidation(valStep, context);
            };

            current = definition.getStepById(nextStepId);
            if (nextStepId != null && current == null) {
                throw new WorkflowExecutionException("Passo referenciado não existe: " + nextStepId);
            }
        }
    }

    private String runApiCall(ApiCallStep step, WorkflowContext context) {
        long start = System.nanoTime();
        try {
            Map<String, Object> output = executeApiCall.execute(step, context);
            context.addStepOutput(step.id(), output);
            context.addTrace(trace(step, STEP_SUCCESS, output, start));
            return step.nextStepId();
        } catch (RuntimeException e) {
            context.addTrace(trace(step, STEP_ERROR, e.getMessage(), start));
            if (step.onErrorStepId() == null) {
                throw e;
            }
            return step.onErrorStepId();
        }
    }

    private String runValidation(ValidationRuleStep step, WorkflowContext context) {
        long start = System.nanoTime();
        List<Map<String, Object>> results = new ArrayList<>();
        List<Boolean> outcomes = new ArrayList<>();

        List<ValidationRuleStep.Rule> rules = step.rules() == null ? List.of() : step.rules();
        for (ValidationRuleStep.Rule rule : rules) {
            boolean passed;
            String error = null;
            try {
                passed = evaluateCondition.evaluateCondition(rule.spelExpression(), context);
            } catch (RuntimeException e) {
                passed = false;
                error = e.getMessage();
            }
            outcomes.add(passed);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("description", rule.description());
            result.put("expression", rule.spelExpression());
            result.put("passed", passed);
            if (!passed) result.put("failureMessage", error != null ? error : rule.failureMessage());
            results.add(result);
        }

        boolean allPassed = step.operator() == ValidationRuleStep.LogicalOperator.OR
                ? outcomes.stream().anyMatch(Boolean::booleanValue)
                : outcomes.stream().allMatch(Boolean::booleanValue);

        context.addTrace(trace(step, allPassed ? STEP_SUCCESS : "FAILED_RULES", results, start));
        return allPassed ? step.onPassStepId() : step.onFailStepId();
    }

    private StepTrace trace(WorkflowStep step, String status, Object details, long startNanos) {
        long durationMs = (System.nanoTime() - startNanos) / 1_000_000;
        return new StepTrace(step.id(), step.type().name(), status, details, durationMs, Instant.now());
    }

}
