package automationsv1.engine_bolt_mesh.adapter.out.persistence.entity;

import automationsv1.engine_bolt_mesh.application.domain.model.ApiDefinition;
import automationsv1.engine_bolt_mesh.application.domain.model.ExtractedField;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "api_definitions")
@Getter
@Setter
@NoArgsConstructor
public class ApiDefinitionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID tenantId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private String baseUrl;

    private String httpMethod;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, String> headersTemplate;

    @Column(columnDefinition = "text")
    private String bodyTemplate;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private List<ExtractedField> extractedFields;

    private Instant createdAt;

    public static ApiDefinitionEntity from(ApiDefinition api) {
        ApiDefinitionEntity entity = new ApiDefinitionEntity();
        entity.id = api.id();
        entity.tenantId = api.tenantId();
        entity.name = api.name();
        entity.baseUrl = api.baseUrl();
        entity.httpMethod = api.httpMethod();
        entity.headersTemplate = api.headersTemplate();
        entity.bodyTemplate = api.bodyTemplate();
        entity.extractedFields = api.extractedFields();
        entity.createdAt = api.createdAt();
        return entity;
    }

    public ApiDefinition toDomain() {
        return new ApiDefinition(id, tenantId, name, baseUrl, httpMethod, headersTemplate,
                bodyTemplate, extractedFields, createdAt);
    }

}
