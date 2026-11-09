package automationsv1.engine_bolt_mesh.application.domain.usecase;

import automationsv1.engine_bolt_mesh.application.domain.exception.ResourceNotFoundException;
import automationsv1.engine_bolt_mesh.application.domain.model.ApiDefinition;
import automationsv1.engine_bolt_mesh.application.ports.in.DeleteApiDefinitionInputPort;
import automationsv1.engine_bolt_mesh.application.ports.out.ApiDefinitionOutputPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@RequiredArgsConstructor
@Component
public class DeleteApiDefinitionUseCase implements DeleteApiDefinitionInputPort {

    private final ApiDefinitionOutputPort outputPort;

    @Override
    public void delete(UUID id) {
        if (!outputPort.existsById(id)) {
            throw new ResourceNotFoundException("ApiDefinition não encontrado(a): " + id);
        }
        outputPort.deleteById(id);
    }

}
