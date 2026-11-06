package automationsv1.engine_bolt_mesh.application.domain.usecase;

import automationsv1.engine_bolt_mesh.application.domain.exception.WorkflowExecutionException;
import automationsv1.engine_bolt_mesh.application.domain.model.WorkflowDefinition;
import automationsv1.engine_bolt_mesh.application.ports.in.RegisterWorkflowDefinitionInputPort;
import automationsv1.engine_bolt_mesh.application.ports.out.WorkflowDefinitionOutputPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;

@RequiredArgsConstructor
@Component
public class RegisterWorkflowDefinitionUseCase implements RegisterWorkflowDefinitionInputPort {

    private final WorkflowDefinitionOutputPort workflowDefinitionOutputPort;

    @Override
    public WorkflowDefinition register(WorkflowDefinition workflow) {
        if (workflow.getFirstStep() == null) {
            throw new WorkflowExecutionException("firstStepId não existe em steps: " + workflow.firstStepId());
        }
        Instant now = Instant.now();
        return workflowDefinitionOutputPort.save(new WorkflowDefinition(
                null, workflow.tenantId(), workflow.name(), workflow.active(),
                workflow.version() != null ? workflow.version() : 1,
                workflow.firstStepId(), workflow.steps(), now, now));
    }

}
