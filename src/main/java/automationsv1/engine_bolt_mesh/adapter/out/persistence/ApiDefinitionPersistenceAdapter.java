package automationsv1.engine_bolt_mesh.adapter.out.persistence;

import automationsv1.engine_bolt_mesh.adapter.out.persistence.entity.ApiDefinitionEntity;
import automationsv1.engine_bolt_mesh.adapter.out.persistence.repository.ApiDefinitionRepository;
import automationsv1.engine_bolt_mesh.application.domain.model.ApiDefinition;
import automationsv1.engine_bolt_mesh.application.ports.out.ApiDefinitionOutputPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class ApiDefinitionPersistenceAdapter implements ApiDefinitionOutputPort {

    private final ApiDefinitionRepository repository;

    @Override
    public ApiDefinition save(ApiDefinition apiDefinition) {
        return repository.save(ApiDefinitionEntity.from(apiDefinition)).toDomain();
    }

    @Override
    public Optional<ApiDefinition> findById(UUID id) {
        return repository.findById(id).map(ApiDefinitionEntity::toDomain);
    }

    @Override
    public List<ApiDefinition> findAll() {
        return repository.findAll().stream().map(ApiDefinitionEntity::toDomain).toList();
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
