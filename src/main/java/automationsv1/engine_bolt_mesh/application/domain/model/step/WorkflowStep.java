package automationsv1.engine_bolt_mesh.application.domain.model.step;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ApiCallStep.class, name = "API_CALL"),
        @JsonSubTypes.Type(value = ValidationRuleStep.class, name = "VALIDATION_RULE")
})
public sealed interface WorkflowStep permits ApiCallStep, ValidationRuleStep {
    String id();
    String name();
    StepType type();
}
