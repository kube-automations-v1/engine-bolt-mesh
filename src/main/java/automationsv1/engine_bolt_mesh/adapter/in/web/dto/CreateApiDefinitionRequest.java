package automationsv1.engine_bolt_mesh.adapter.in.web.dto;

import automationsv1.engine_bolt_mesh.application.domain.model.ApiDefinition;
import automationsv1.engine_bolt_mesh.application.domain.model.ExtractedField;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record CreateApiDefinitionRequest(
        @NotNull UUID tenantId,
        @NotBlank String name,
        @NotBlank String baseUrl,
        @NotBlank @Pattern(regexp = "(?i)GET|POST|PUT|DELETE|PATCH") String httpMethod,
        Map<String, String> headersTemplate,
        String bodyTemplate,
        @NotNull List<ExtractedField> extractedFields
) {

    public ApiDefinition toDomain() {
        return new ApiDefinition(null, tenantId, name, baseUrl, httpMethod, headersTemplate,
                bodyTemplate, extractedFields, null);
    }

}
