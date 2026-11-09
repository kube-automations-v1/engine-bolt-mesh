package automationsv1.engine_bolt_mesh.application.ports.in;

import automationsv1.engine_bolt_mesh.application.domain.model.WorkflowDefinition;

import java.util.List;

public interface ListWorkflowDefinitionsInputPort {

    List<WorkflowDefinition> list();

}
