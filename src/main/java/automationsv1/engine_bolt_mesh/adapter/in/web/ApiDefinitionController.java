package automationsv1.engine_bolt_mesh.adapter.in.web;

import automationsv1.engine_bolt_mesh.adapter.in.web.dto.CreateApiDefinitionRequest;
import automationsv1.engine_bolt_mesh.adapter.in.web.dto.DiscoverRequest;
import automationsv1.engine_bolt_mesh.application.domain.model.ApiDefinition;
import automationsv1.engine_bolt_mesh.application.domain.model.ExtractedField;
import automationsv1.engine_bolt_mesh.application.ports.in.DiscoverApiFieldsInputPort;
import automationsv1.engine_bolt_mesh.application.ports.in.RegisterApiDefinitionInputPort;
import automationsv1.engine_bolt_mesh.application.ports.in.DeleteApiDefinitionInputPort;
import automationsv1.engine_bolt_mesh.application.ports.in.GetApiDefinitionInputPort;
import automationsv1.engine_bolt_mesh.application.ports.in.ListApiDefinitionsInputPort;
import automationsv1.engine_bolt_mesh.application.ports.in.UpdateApiDefinitionInputPort;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/connectors")
@RequiredArgsConstructor
public class ApiDefinitionController {

    private final RegisterApiDefinitionInputPort registerApiDefinition;
    private final DiscoverApiFieldsInputPort discoverApiFields;
    private final GetApiDefinitionInputPort getApiDefinition;
    private final ListApiDefinitionsInputPort listApiDefinitions;
    private final UpdateApiDefinitionInputPort updateApiDefinition;
    private final DeleteApiDefinitionInputPort deleteApiDefinition;

    /** Cadastra um novo conector (ApiDefinition). */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiDefinition create(@Valid @RequestBody CreateApiDefinitionRequest request) {
        return registerApiDefinition.register(request.toDomain());
    }

    /** "Test & Discover": executa a chamada real e sugere os campos extraíveis (caminhos folha + tipos). */
    @PostMapping("/discover")
    public List<ExtractedField> discover(@Valid @RequestBody DiscoverRequest request) {
        return discoverApiFields.discover(request.url(), request.httpMethod(), request.headers(), request.body());
    }

    @GetMapping
    public List<ApiDefinition> list() {
        return listApiDefinitions.list();
    }

    @GetMapping("/{id}")
    public ApiDefinition get(@PathVariable UUID id) {
        return getApiDefinition.get(id);
    }

    @PutMapping("/{id}")
    public ApiDefinition update(@PathVariable UUID id, @Valid @RequestBody CreateApiDefinitionRequest request) {
        return updateApiDefinition.update(id, request.toDomain());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        deleteApiDefinition.delete(id);
    }

}
