package automationsv1.engine_bolt_mesh.application.domain.usecase;

import automationsv1.engine_bolt_mesh.application.domain.model.WorkflowDefinition;
import automationsv1.engine_bolt_mesh.application.ports.in.ListWorkflowDefinitionsInputPort;
import automationsv1.engine_bolt_mesh.application.ports.out.WorkflowDefinitionOutputPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class ListWorkflowDefinitionsUseCase implements ListWorkflowDefinitionsInputPort {

    private final WorkflowDefinitionOutputPort outputPort;

    @Override
    public List<WorkflowDefinition> list() {
        return outputPort.findAll();
    }

}
