package automationsv1.engine_bolt_mesh.application.ports.in;

import automationsv1.engine_bolt_mesh.application.domain.model.WorkflowDefinition;

public interface RegisterWorkflowDefinitionInputPort {

    WorkflowDefinition register(WorkflowDefinition workflowDefinition);

}
