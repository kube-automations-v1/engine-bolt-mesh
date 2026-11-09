package automationsv1.engine_bolt_mesh.application;

import automationsv1.engine_bolt_mesh.application.domain.exception.ResourceNotFoundException;
import automationsv1.engine_bolt_mesh.application.domain.model.ApiDefinition;
import automationsv1.engine_bolt_mesh.application.domain.model.ExtractedField;
import automationsv1.engine_bolt_mesh.application.domain.model.ExtractedField.FieldType;
import automationsv1.engine_bolt_mesh.application.domain.model.WorkflowDefinition;
import automationsv1.engine_bolt_mesh.application.domain.model.step.ApiCallStep;
import automationsv1.engine_bolt_mesh.application.domain.model.step.WorkflowStep;
import automationsv1.engine_bolt_mesh.application.ports.in.DeleteApiDefinitionInputPort;
import automationsv1.engine_bolt_mesh.application.ports.in.DeleteWorkflowDefinitionInputPort;
import automationsv1.engine_bolt_mesh.application.ports.in.GetApiDefinitionInputPort;
import automationsv1.engine_bolt_mesh.application.ports.in.GetWorkflowDefinitionInputPort;
import automationsv1.engine_bolt_mesh.application.ports.in.ListApiDefinitionsInputPort;
import automationsv1.engine_bolt_mesh.application.ports.in.ListWorkflowDefinitionsInputPort;
import automationsv1.engine_bolt_mesh.application.ports.in.RegisterApiDefinitionInputPort;
import automationsv1.engine_bolt_mesh.application.ports.in.RegisterWorkflowDefinitionInputPort;
import automationsv1.engine_bolt_mesh.application.ports.in.UpdateApiDefinitionInputPort;
import automationsv1.engine_bolt_mesh.application.ports.in.UpdateWorkflowDefinitionInputPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class CrudIntegrationTest {

    @Autowired RegisterApiDefinitionInputPort registerApi;
    @Autowired GetApiDefinitionInputPort getApi;
    @Autowired ListApiDefinitionsInputPort listApis;
    @Autowired UpdateApiDefinitionInputPort updateApi;
    @Autowired DeleteApiDefinitionInputPort deleteApi;

    @Autowired RegisterWorkflowDefinitionInputPort registerWorkflow;
    @Autowired GetWorkflowDefinitionInputPort getWorkflow;
    @Autowired ListWorkflowDefinitionsInputPort listWorkflows;
    @Autowired UpdateWorkflowDefinitionInputPort updateWorkflow;
    @Autowired DeleteWorkflowDefinitionInputPort deleteWorkflow;

    private ApiDefinition newApi(String name) {
        return new ApiDefinition(null, UUID.randomUUID(), name, "http://x/y", "get", Map.of(), null,
                List.of(new ExtractedField("a", "a", FieldType.STRING)), null);
    }

    @Test
    void apiDefinitionCrud() {
        ApiDefinition created = registerApi.register(newApi("one"));
        assertThat(getApi.get(created.id()).name()).isEqualTo("one");
        assertThat(listApis.list()).extracting(ApiDefinition::id).contains(created.id());

        ApiDefinition updated = updateApi.update(created.id(), newApi("two"));
        assertThat(updated.name()).isEqualTo("two");
        assertThat(updated.tenantId()).isEqualTo(created.tenantId());
        assertThat(getApi.get(created.id()).name()).isEqualTo("two");

        deleteApi.delete(created.id());
        assertThatThrownBy(() -> getApi.get(created.id())).isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> deleteApi.delete(created.id())).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void workflowDefinitionCrud() {
        Map<String, WorkflowStep> steps = Map.of("s1",
                new ApiCallStep("s1", "S1", UUID.randomUUID(), Map.of(), null, null, null));
        WorkflowDefinition created = registerWorkflow.register(new WorkflowDefinition(
                null, UUID.randomUUID(), "wf", true, 1, "s1", steps, null, null));
        assertThat(getWorkflow.get(created.id()).steps()).containsKey("s1");
        assertThat(listWorkflows.list()).extracting(WorkflowDefinition::id).contains(created.id());

        WorkflowDefinition updated = updateWorkflow.update(created.id(), new WorkflowDefinition(
                null, null, "wf2", false, null, "s1", steps, null, null));
        assertThat(updated.name()).isEqualTo("wf2");
        assertThat(updated.active()).isFalse();
        assertThat(updated.version()).isEqualTo(2);

        deleteWorkflow.delete(created.id());
        assertThatThrownBy(() -> getWorkflow.get(created.id())).isInstanceOf(ResourceNotFoundException.class);
    }

}
