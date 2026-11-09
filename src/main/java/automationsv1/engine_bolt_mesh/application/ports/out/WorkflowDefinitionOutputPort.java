package automationsv1.engine_bolt_mesh.application.ports.out;

import automationsv1.engine_bolt_mesh.application.domain.model.WorkflowDefinition;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkflowDefinitionOutputPort {

    WorkflowDefinition save(WorkflowDefinition workflowDefinition);

    Optional<WorkflowDefinition> findById(UUID id);

    List<WorkflowDefinition> findAll();

    boolean existsById(UUID id);

    void deleteById(UUID id);

}
