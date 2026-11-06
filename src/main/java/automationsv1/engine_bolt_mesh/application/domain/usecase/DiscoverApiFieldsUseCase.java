package automationsv1.engine_bolt_mesh.application.domain.usecase;

import automationsv1.engine_bolt_mesh.application.domain.model.ExtractedField;
import automationsv1.engine_bolt_mesh.application.ports.in.DiscoverApiFieldsInputPort;
import automationsv1.engine_bolt_mesh.application.ports.out.HttpCallOutputPort;
import automationsv1.engine_bolt_mesh.application.ports.out.JsonFieldExtractorOutputPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class DiscoverApiFieldsUseCase implements DiscoverApiFieldsInputPort {

    private final HttpCallOutputPort httpCall;
    private final JsonFieldExtractorOutputPort jsonFieldExtractor;

    @Override
    public List<ExtractedField> discover(String url, String httpMethod, Map<String, String> headers, String body) {
        String rawResponse = httpCall.call(httpMethod, url, headers, body);
        return jsonFieldExtractor.discover(rawResponse);
    }

}
