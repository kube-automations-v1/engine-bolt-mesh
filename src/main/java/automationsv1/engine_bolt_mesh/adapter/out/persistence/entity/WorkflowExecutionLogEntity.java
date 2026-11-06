package automationsv1.engine_bolt_mesh.adapter.out.persistence.entity;

import automationsv1.engine_bolt_mesh.application.domain.model.WorkflowExecutionLog;
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
@Table(name = "workflow_execution_logs")
@Getter
@Setter
@NoArgsConstructor
public class WorkflowExecutionLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID workflowId;

    @Column(nullable = false)
    private String status;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> triggerPayload;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> finalStepOutputs;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<WorkflowExecutionLog.StepTrace> stepTraces;

    private Instant startedAt;

    private Instant finishedAt;

    public static WorkflowExecutionLogEntity from(WorkflowExecutionLog log) {
        WorkflowExecutionLogEntity entity = new WorkflowExecutionLogEntity();
        entity.id = log.id();
        entity.workflowId = log.workflowId();
        entity.status = log.status();
        entity.triggerPayload = log.triggerPayload();
        entity.finalStepOutputs = log.finalStepOutputs();
        entity.stepTraces = log.stepTraces();
        entity.startedAt = log.startedAt();
        entity.finishedAt = log.finishedAt();
        return entity;
    }

    public WorkflowExecutionLog toDomain() {
        return new WorkflowExecutionLog(id, workflowId, status, triggerPayload, finalStepOutputs,
                stepTraces, startedAt, finishedAt);
    }

}
