package automationsv1.engine_bolt_mesh.application.ports.in;

import automationsv1.engine_bolt_mesh.application.domain.model.ApiDefinition;

public interface RegisterApiDefinitionInputPort {

    ApiDefinition register(ApiDefinition apiDefinition);

}
