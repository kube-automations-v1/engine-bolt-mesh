package automationsv1.engine_bolt_mesh.application.domain.usecase;

import automationsv1.engine_bolt_mesh.application.domain.exception.ResourceNotFoundException;
import automationsv1.engine_bolt_mesh.application.domain.model.WorkflowDefinition;
import automationsv1.engine_bolt_mesh.application.ports.in.GetWorkflowDefinitionInputPort;
import automationsv1.engine_bolt_mesh.application.ports.out.WorkflowDefinitionOutputPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@RequiredArgsConstructor
@Component
public class GetWorkflowDefinitionUseCase implements GetWorkflowDefinitionInputPort {

    private final WorkflowDefinitionOutputPort outputPort;

    @Override
    public WorkflowDefinition get(UUID id) {
        return outputPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow não encontrado(a): " + id));
    }

}
