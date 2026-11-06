package automationsv1.engine_bolt_mesh.application.domain.model.step;

import java.util.List;

public record ValidationRuleStep(
        String id,
        String name,
        LogicalOperator operator,
        List<Rule> rules,
        String onPassStepId,
        String onFailStepId
) implements WorkflowStep {

    @Override
    public StepType type() { return StepType.VALIDATION_RULE; }

    public enum LogicalOperator { AND, OR }

    public record Rule(
            String description,
            String spelExpression,
            String failureMessage
    ) {}
}
