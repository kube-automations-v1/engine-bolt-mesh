package automationsv1.engine_bolt_mesh.application.ports.in;

import automationsv1.engine_bolt_mesh.application.domain.model.WorkflowExecutionLog;

import java.util.Map;
import java.util.UUID;

public interface WorkFlowEngineInputPort {

    WorkflowExecutionLog run(UUID workflowId, Map<String, Object> triggerPayload);

}
