package automationsv1.engine_bolt_mesh.application.domain.usecase;

import automationsv1.engine_bolt_mesh.application.domain.exception.ResourceNotFoundException;
import automationsv1.engine_bolt_mesh.application.domain.model.ApiDefinition;
import automationsv1.engine_bolt_mesh.application.ports.in.GetApiDefinitionInputPort;
import automationsv1.engine_bolt_mesh.application.ports.out.ApiDefinitionOutputPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@RequiredArgsConstructor
@Component
public class GetApiDefinitionUseCase implements GetApiDefinitionInputPort {

    private final ApiDefinitionOutputPort outputPort;

    @Override
    public ApiDefinition get(UUID id) {
        return outputPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ApiDefinition não encontrado(a): " + id));
    }

}
