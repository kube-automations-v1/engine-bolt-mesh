package automationsv1.engine_bolt_mesh.application.domain.model;

import automationsv1.engine_bolt_mesh.application.domain.model.step.WorkflowStep;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record WorkflowDefinition(
        UUID id,
        UUID tenantId,
        String name,
        boolean active,
        Integer version,
        String firstStepId,
        Map<String, WorkflowStep> steps,
        Instant createdAt,
        Instant updatedAt
) {

    @JsonIgnore
    public WorkflowStep getFirstStep() {
        return getStepById(firstStepId);
    }

    public WorkflowStep getStepById(String stepId) {
        if (stepId == null || steps == null) return null;
        return steps.get(stepId);
    }

}
