package automationsv1.engine_bolt_mesh.application.domain.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkflowContext {

    private final Map<String, Object> trigger = new HashMap<>();
    private final Map<String, Object> steps = new HashMap<>();
    private final Map<String, Object> env = new HashMap<>();
    private Map<String, Object> inputs = new HashMap<>();
    private final List<WorkflowExecutionLog.StepTrace> traces = new ArrayList<>();

    public WorkflowContext(Map<String, Object> triggerPayload) {
        if (triggerPayload != null) this.trigger.putAll(triggerPayload);
    }

    public void addStepOutput(String stepId, Object output) {
        this.steps.put(stepId, output);
    }

    public void addTrace(WorkflowExecutionLog.StepTrace trace) {
        this.traces.add(trace);
    }

    public void setInputs(Map<String, Object> inputs) { this.inputs = inputs; }

    public Map<String, Object> getTrigger() { return trigger; }
    public Map<String, Object> getSteps() { return steps; }
    public Map<String, Object> getEnv() { return env; }
    public Map<String, Object> getInputs() { return inputs; }
    public List<WorkflowExecutionLog.StepTrace> getTraces() { return traces; }

}
