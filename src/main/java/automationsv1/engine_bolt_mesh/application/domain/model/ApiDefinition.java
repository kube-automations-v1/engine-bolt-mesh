package automationsv1.engine_bolt_mesh.application.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ApiDefinition(
        UUID id,
        UUID tenantId,
        String name,
        String baseUrl,
        String httpMethod,
        Map<String, String> headersTemplate,
        String bodyTemplate,
        List<ExtractedField> extractedFields,
        Instant createdAt
) {
}
