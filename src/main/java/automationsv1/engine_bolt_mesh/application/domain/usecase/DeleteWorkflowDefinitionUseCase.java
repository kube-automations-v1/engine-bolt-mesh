package automationsv1.engine_bolt_mesh.application.domain.usecase;

import automationsv1.engine_bolt_mesh.application.domain.exception.ResourceNotFoundException;
import automationsv1.engine_bolt_mesh.application.domain.model.WorkflowDefinition;
import automationsv1.engine_bolt_mesh.application.ports.in.DeleteWorkflowDefinitionInputPort;
import automationsv1.engine_bolt_mesh.application.ports.out.WorkflowDefinitionOutputPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@RequiredArgsConstructor
@Component
public class DeleteWorkflowDefinitionUseCase implements DeleteWorkflowDefinitionInputPort {

    private final WorkflowDefinitionOutputPort outputPort;

    @Override
    public void delete(UUID id) {
        if (!outputPort.existsById(id)) {
            throw new ResourceNotFoundException("Workflow não encontrado(a): " + id);
        }
        outputPort.deleteById(id);
    }

}
