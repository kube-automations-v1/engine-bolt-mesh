package automationsv1.engine_bolt_mesh.application.domain.usecase;

import automationsv1.engine_bolt_mesh.application.domain.model.ApiDefinition;
import automationsv1.engine_bolt_mesh.application.ports.in.ListApiDefinitionsInputPort;
import automationsv1.engine_bolt_mesh.application.ports.out.ApiDefinitionOutputPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class ListApiDefinitionsUseCase implements ListApiDefinitionsInputPort {

    private final ApiDefinitionOutputPort outputPort;

    @Override
    public List<ApiDefinition> list() {
        return outputPort.findAll();
    }

}
