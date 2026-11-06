package automationsv1.engine_bolt_mesh.application.domain.usecase;

import automationsv1.engine_bolt_mesh.application.domain.model.ApiDefinition;
import automationsv1.engine_bolt_mesh.application.ports.in.RegisterApiDefinitionInputPort;
import automationsv1.engine_bolt_mesh.application.ports.out.ApiDefinitionOutputPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;

@RequiredArgsConstructor
@Component
public class RegisterApiDefinitionUseCase implements RegisterApiDefinitionInputPort {

    private final ApiDefinitionOutputPort apiDefinitionOutputPort;

    @Override
    public ApiDefinition register(ApiDefinition api) {
        return apiDefinitionOutputPort.save(new ApiDefinition(
                null, api.tenantId(), api.name(), api.baseUrl(), api.httpMethod().toUpperCase(),
                api.headersTemplate(), api.bodyTemplate(), api.extractedFields(), Instant.now()));
    }

}
