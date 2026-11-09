package automationsv1.engine_bolt_mesh.application.ports.out;

import automationsv1.engine_bolt_mesh.application.domain.model.ApiDefinition;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApiDefinitionOutputPort {

    ApiDefinition save(ApiDefinition apiDefinition);

    Optional<ApiDefinition> findById(UUID id);

    List<ApiDefinition> findAll();

    boolean existsById(UUID id);

    void deleteById(UUID id);

}
