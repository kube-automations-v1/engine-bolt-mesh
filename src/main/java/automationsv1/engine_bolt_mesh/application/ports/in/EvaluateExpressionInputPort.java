package automationsv1.engine_bolt_mesh.application.ports.in;

import automationsv1.engine_bolt_mesh.application.domain.model.WorkflowContext;

public interface EvaluateExpressionInputPort {

    Object evaluateExpression(String expression, WorkflowContext workflowContext);

}
