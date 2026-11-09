package automationsv1.engine_bolt_mesh.application.domain.usecase;

import automationsv1.engine_bolt_mesh.application.domain.exception.ResourceNotFoundException;
import automationsv1.engine_bolt_mesh.application.domain.model.ApiDefinition;
import automationsv1.engine_bolt_mesh.application.ports.in.UpdateApiDefinitionInputPort;
import automationsv1.engine_bolt_mesh.application.ports.out.ApiDefinitionOutputPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@RequiredArgsConstructor
@Component
public class UpdateApiDefinitionUseCase implements UpdateApiDefinitionInputPort {

    private final ApiDefinitionOutputPort outputPort;

    @Override
    public ApiDefinition update(UUID id, ApiDefinition data) {
        ApiDefinition existing = outputPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ApiDefinition não encontrado(a): " + id));
        return outputPort.save(new ApiDefinition(
                id, existing.tenantId(), data.name(), data.baseUrl(), data.httpMethod().toUpperCase(),
                data.headersTemplate(), data.bodyTemplate(), data.extractedFields(), existing.createdAt()));
    }

}
