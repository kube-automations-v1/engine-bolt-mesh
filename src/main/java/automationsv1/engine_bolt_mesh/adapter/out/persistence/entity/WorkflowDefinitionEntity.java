package automationsv1.engine_bolt_mesh.adapter.out.persistence.entity;

import automationsv1.engine_bolt_mesh.application.domain.model.WorkflowDefinition;
import automationsv1.engine_bolt_mesh.application.domain.model.step.WorkflowStep;
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
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "workflow_definitions")
@Getter
@Setter
@NoArgsConstructor
public class WorkflowDefinitionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private String name;

    private boolean active;

    private Integer version;

    private String firstStepId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private Map<String, WorkflowStep> steps;

    private Instant createdAt;

    private Instant updatedAt;

    public static WorkflowDefinitionEntity from(WorkflowDefinition workflow) {
        WorkflowDefinitionEntity entity = new WorkflowDefinitionEntity();
        entity.id = workflow.id();
        entity.tenantId = workflow.tenantId();
        entity.name = workflow.name();
        entity.active = workflow.active();
        entity.version = workflow.version();
        entity.firstStepId = workflow.firstStepId();
        entity.steps = workflow.steps();
        entity.createdAt = workflow.createdAt();
        entity.updatedAt = workflow.updatedAt();
        return entity;
    }

    public WorkflowDefinition toDomain() {
        return new WorkflowDefinition(id, tenantId, name, active, version, firstStepId, steps, createdAt, updatedAt);
    }

}
