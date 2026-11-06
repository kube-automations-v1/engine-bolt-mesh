package automationsv1.engine_bolt_mesh.application.ports.out;

import automationsv1.engine_bolt_mesh.application.domain.model.ExtractedField;

import java.util.List;
import java.util.Map;

public interface JsonFieldExtractorOutputPort {

    Map<String, Object> extract(String rawJson, List<ExtractedField> fields);

    List<ExtractedField> discover(String rawJson);

}
