package automationsv1.engine_bolt_mesh.adapter.in.web;

import automationsv1.engine_bolt_mesh.adapter.in.web.dto.CreateApiDefinitionRequest;
import automationsv1.engine_bolt_mesh.adapter.in.web.dto.DiscoverRequest;
import automationsv1.engine_bolt_mesh.application.domain.model.ApiDefinition;
import automationsv1.engine_bolt_mesh.application.domain.model.ExtractedField;
import automationsv1.engine_bolt_mesh.application.ports.in.DiscoverApiFieldsInputPort;
import automationsv1.engine_bolt_mesh.application.ports.in.RegisterApiDefinitionInputPort;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/connectors")
@RequiredArgsConstructor
public class ApiDefinitionController {

    private final RegisterApiDefinitionInputPort registerApiDefinition;
    private final DiscoverApiFieldsInputPort discoverApiFields;

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

}
