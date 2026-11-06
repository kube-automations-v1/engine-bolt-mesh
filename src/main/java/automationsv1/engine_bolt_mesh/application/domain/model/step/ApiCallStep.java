package automationsv1.engine_bolt_mesh.application.domain.model.step;

import java.util.Map;
import java.util.UUID;

public record ApiCallStep(
        String id,
        String name,
        UUID apiDefinitionId,
        Map<String, String> inputMappings,
        RetryPolicy retryPolicy,
        String nextStepId,
        String onErrorStepId
) implements WorkflowStep {

    @Override
    public StepType type() { return StepType.API_CALL; }

    public record RetryPolicy(int maxAttempts, long backoffMillis) {}
}
