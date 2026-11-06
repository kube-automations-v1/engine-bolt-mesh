package automationsv1.engine_bolt_mesh.adapter.in.web;

import automationsv1.engine_bolt_mesh.adapter.in.web.dto.CreateWorkflowRequest;
import automationsv1.engine_bolt_mesh.application.domain.model.WorkflowDefinition;
import automationsv1.engine_bolt_mesh.application.domain.model.WorkflowExecutionLog;
import automationsv1.engine_bolt_mesh.application.ports.in.RegisterWorkflowDefinitionInputPort;
import automationsv1.engine_bolt_mesh.application.ports.in.WorkFlowEngineInputPort;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workflows")
@RequiredArgsConstructor
public class WorkflowController {

    private final RegisterWorkflowDefinitionInputPort registerWorkflow;
    private final WorkFlowEngineInputPort workFlowEngine;

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

}
