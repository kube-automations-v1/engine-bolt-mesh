package automationsv1.engine_bolt_mesh.application.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record WorkflowExecutionLog(
        UUID id,
        UUID workflowId,
        String status,
        Map<String, Object> triggerPayload,
        Map<String, Object> finalStepOutputs,
        List<StepTrace> stepTraces,
        Instant startedAt,
        Instant finishedAt
) {

    public static final String COMPLETED = "COMPLETED";
    public static final String FAILED = "FAILED";

    public record StepTrace(
            String stepId,
            String stepType,
            String status,
            Object details,
            long durationMs,
            Instant timestamp
    ) {}

}
