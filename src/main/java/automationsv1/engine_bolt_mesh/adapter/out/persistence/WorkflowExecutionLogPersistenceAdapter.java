package automationsv1.engine_bolt_mesh.adapter.out.persistence;

import automationsv1.engine_bolt_mesh.adapter.out.persistence.entity.WorkflowExecutionLogEntity;
import automationsv1.engine_bolt_mesh.adapter.out.persistence.repository.WorkflowExecutionLogRepository;
import automationsv1.engine_bolt_mesh.application.domain.model.WorkflowExecutionLog;
import automationsv1.engine_bolt_mesh.application.ports.out.WorkflowExecutionLogOutputPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class WorkflowExecutionLogPersistenceAdapter implements WorkflowExecutionLogOutputPort {

    private final WorkflowExecutionLogRepository repository;

    @Override
    public WorkflowExecutionLog save(WorkflowExecutionLog log) {
        return repository.save(WorkflowExecutionLogEntity.from(log)).toDomain();
    }

}
