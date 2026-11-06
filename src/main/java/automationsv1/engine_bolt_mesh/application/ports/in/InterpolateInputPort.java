package automationsv1.engine_bolt_mesh.application.ports.in;

import automationsv1.engine_bolt_mesh.application.domain.model.WorkflowContext;

public interface InterpolateInputPort {

    String interpolate(String template, WorkflowContext workflowContext);

}
