package automationsv1.engine_bolt_mesh.application.ports.in;

import java.util.UUID;

public interface DeleteWorkflowDefinitionInputPort {

    void delete(UUID id);

}
