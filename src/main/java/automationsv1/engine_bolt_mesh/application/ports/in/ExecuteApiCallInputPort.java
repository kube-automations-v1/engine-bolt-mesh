package automationsv1.engine_bolt_mesh.application.ports.in;

import automationsv1.engine_bolt_mesh.application.domain.model.WorkflowContext;
import automationsv1.engine_bolt_mesh.application.domain.model.step.ApiCallStep;

import java.util.Map;

public interface ExecuteApiCallInputPort {

    Map<String, Object> execute(ApiCallStep step, WorkflowContext workflowContext);

}
