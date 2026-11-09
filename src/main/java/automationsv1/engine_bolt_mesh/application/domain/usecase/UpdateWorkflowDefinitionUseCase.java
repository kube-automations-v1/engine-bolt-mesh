package automationsv1.engine_bolt_mesh.application.domain.usecase;

import automationsv1.engine_bolt_mesh.application.domain.exception.ResourceNotFoundException;
import automationsv1.engine_bolt_mesh.application.domain.exception.WorkflowExecutionException;
import automationsv1.engine_bolt_mesh.application.domain.model.WorkflowDefinition;
import automationsv1.engine_bolt_mesh.application.ports.in.UpdateWorkflowDefinitionInputPort;
import automationsv1.engine_bolt_mesh.application.ports.out.WorkflowDefinitionOutputPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class UpdateWorkflowDefinitionUseCase implements UpdateWorkflowDefinitionInputPort {

    private final WorkflowDefinitionOutputPort outputPort;

    @Override
    public WorkflowDefinition update(UUID id, WorkflowDefinition data) {
        WorkflowDefinition existing = outputPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow não encontrado(a): " + id));
        if (data.getFirstStep() == null) {
            throw new WorkflowExecutionException("firstStepId não existe em steps: " + data.firstStepId());
        }
        int nextVersion = existing.version() == null ? 1 : existing.version() + 1;
        return outputPort.save(new WorkflowDefinition(
                id, existing.tenantId(), data.name(), data.active(), nextVersion,
                data.firstStepId(), data.steps(), existing.createdAt(), Instant.now()));
    }

}
