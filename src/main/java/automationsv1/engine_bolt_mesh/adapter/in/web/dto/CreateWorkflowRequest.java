package automationsv1.engine_bolt_mesh.adapter.in.web.dto;

import automationsv1.engine_bolt_mesh.application.domain.model.WorkflowDefinition;
import automationsv1.engine_bolt_mesh.application.domain.model.step.WorkflowStep;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

public record CreateWorkflowRequest(
        @NotNull UUID tenantId,
        @NotBlank String name,
        Boolean active,
        @NotBlank String firstStepId,
        @NotEmpty Map<String, WorkflowStep> steps
) {

    public WorkflowDefinition toDomain() {
        return new WorkflowDefinition(null, tenantId, name, active == null || active, 1,
                firstStepId, steps, null, null);
    }

}
