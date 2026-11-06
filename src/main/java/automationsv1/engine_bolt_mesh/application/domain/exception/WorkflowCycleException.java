package automationsv1.engine_bolt_mesh.application.domain.exception;

public class WorkflowCycleException extends WorkflowExecutionException {

    public WorkflowCycleException(int maxSteps) {
        super("Workflow excedeu o limite de " + maxSteps + " passos (possível loop infinito)");
    }
}
