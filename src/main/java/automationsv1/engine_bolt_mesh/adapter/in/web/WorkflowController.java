package automationsv1.engine_bolt_mesh.adapter.in.web;

import automationsv1.engine_bolt_mesh.adapter.in.web.dto.CreateWorkflowRequest;
import automationsv1.engine_bolt_mesh.application.domain.model.WorkflowDefinition;
import automationsv1.engine_bolt_mesh.application.domain.model.WorkflowExecutionLog;
import automationsv1.engine_bolt_mesh.application.ports.in.RegisterWorkflowDefinitionInputPort;
import automationsv1.engine_bolt_mesh.application.ports.in.WorkFlowEngineInputPort;
import automationsv1.engine_bolt_mesh.application.ports.in.DeleteWorkflowDefinitionInputPort;
import automationsv1.engine_bolt_mesh.application.ports.in.GetWorkflowDefinitionInputPort;
import automationsv1.engine_bolt_mesh.application.ports.in.ListWorkflowDefinitionsInputPort;
import automationsv1.engine_bolt_mesh.application.ports.in.UpdateWorkflowDefinitionInputPort;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workflows")
@RequiredArgsConstructor
public class WorkflowController {

    private final RegisterWorkflowDefinitionInputPort registerWorkflow;
    private final WorkFlowEngineInputPort workFlowEngine;
    private final GetWorkflowDefinitionInputPort getWorkflow;
    private final ListWorkflowDefinitionsInputPort listWorkflows;
    private final UpdateWorkflowDefinitionInputPort updateWorkflow;
    private final DeleteWorkflowDefinitionInputPort deleteWorkflow;

    /** Cadastra um novo WorkflowDefinition. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkflowDefinition create(@Valid @RequestBody CreateWorkflowRequest request) {
        return registerWorkflow.register(request.toDomain());
    }

    /** Dispara o workflow com um payload arbitrário e devolve o log completo da execução. */
    @PostMapping("/{workflowId}/trigger")
    public WorkflowExecutionLog trigger(@PathVariable UUID workflowId, @RequestBody Map<String, Object> payload) {
        return workFlowEngine.run(workflowId, payload);
    }

    @GetMapping
    public List<WorkflowDefinition> list() {
        return listWorkflows.list();
    }

    @GetMapping("/{workflowId}")
    public WorkflowDefinition get(@PathVariable UUID workflowId) {
        return getWorkflow.get(workflowId);
    }

    @PutMapping("/{workflowId}")
    public WorkflowDefinition update(@PathVariable UUID workflowId, @Valid @RequestBody CreateWorkflowRequest request) {
        return updateWorkflow.update(workflowId, request.toDomain());
    }

    @DeleteMapping("/{workflowId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID workflowId) {
        deleteWorkflow.delete(workflowId);
    }

}
