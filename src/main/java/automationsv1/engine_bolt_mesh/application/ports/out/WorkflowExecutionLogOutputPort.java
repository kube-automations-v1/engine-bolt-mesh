package automationsv1.engine_bolt_mesh.application.ports.out;

import automationsv1.engine_bolt_mesh.application.domain.model.WorkflowExecutionLog;

public interface WorkflowExecutionLogOutputPort {

    WorkflowExecutionLog save(WorkflowExecutionLog log);

}
