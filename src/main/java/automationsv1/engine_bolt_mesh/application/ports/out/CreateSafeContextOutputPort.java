package automationsv1.engine_bolt_mesh.application.ports.out;

import automationsv1.engine_bolt_mesh.application.domain.model.WorkflowContext;
import org.springframework.expression.EvaluationContext;

public interface CreateSafeContextOutputPort {

     EvaluationContext create(WorkflowContext workflowContext);

}
