package automationsv1.engine_bolt_mesh.application.ports.in;

import automationsv1.engine_bolt_mesh.application.domain.model.ExtractedField;

import java.util.List;
import java.util.Map;

public interface DiscoverApiFieldsInputPort {

    List<ExtractedField> discover(String url, String httpMethod, Map<String, String> headers, String body);

}
