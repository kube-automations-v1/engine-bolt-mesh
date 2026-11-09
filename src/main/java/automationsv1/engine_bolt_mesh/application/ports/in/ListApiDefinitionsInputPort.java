package automationsv1.engine_bolt_mesh.application.ports.in;

import automationsv1.engine_bolt_mesh.application.domain.model.ApiDefinition;

import java.util.List;

public interface ListApiDefinitionsInputPort {

    List<ApiDefinition> list();

}
