package automationsv1.engine_bolt_mesh.adapter.out.persistence;

import automationsv1.engine_bolt_mesh.adapter.out.persistence.entity.WorkflowDefinitionEntity;
import automationsv1.engine_bolt_mesh.adapter.out.persistence.repository.WorkflowDefinitionRepository;
import automationsv1.engine_bolt_mesh.application.domain.model.WorkflowDefinition;
import automationsv1.engine_bolt_mesh.application.ports.out.WorkflowDefinitionOutputPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class WorkflowDefinitionPersistenceAdapter implements WorkflowDefinitionOutputPort {

    private final WorkflowDefinitionRepository repository;

    @Override
    public WorkflowDefinition save(WorkflowDefinition workflowDefinition) {
        return repository.save(WorkflowDefinitionEntity.from(workflowDefinition)).toDomain();
    }

    @Override
    public Optional<WorkflowDefinition> findById(UUID id) {
        return repository.findById(id).map(WorkflowDefinitionEntity::toDomain);
    }

    @Override
    public List<WorkflowDefinition> findAll() {
        return repository.findAll().stream().map(WorkflowDefinitionEntity::toDomain).toList();
    }

    @Override
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

}
