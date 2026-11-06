package automationsv1.engine_bolt_mesh.adapter.out.json;

import automationsv1.engine_bolt_mesh.application.domain.model.ExtractedField;
import automationsv1.engine_bolt_mesh.application.domain.model.ExtractedField.FieldType;
import automationsv1.engine_bolt_mesh.application.ports.out.JsonFieldExtractorOutputPort;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class JsonPathFieldExtractorAdapter implements JsonFieldExtractorOutputPort {

    private static final Configuration JSON_PATH_CONFIG = Configuration.builder()
            .options(Option.SUPPRESS_EXCEPTIONS, Option.DEFAULT_PATH_LEAF_TO_NULL)
            .build();

    private final JsonMapper jsonMapper;

    @Override
    public Map<String, Object> extract(String rawJson, List<ExtractedField> fields) {
        Map<String, Object> output = new LinkedHashMap<>();
        if (rawJson == null || rawJson.isBlank() || fields == null) return output;

        DocumentContext document = JsonPath.using(JSON_PATH_CONFIG).parse(rawJson);
        for (ExtractedField field : fields) {
            String path = field.path().startsWith("$") ? field.path() : "$." + field.path();
            Object value = document.read(path);
            output.put(field.alias(), convert(value, field.type()));
        }
        return output;
    }

    @Override
    public List<ExtractedField> discover(String rawJson) {
        List<ExtractedField> fields = new ArrayList<>();
        if (rawJson != null && !rawJson.isBlank()) {
            collect(jsonMapper.readTree(rawJson), "", fields);
        }
        return fields;
    }

    private void collect(JsonNode node, String path, List<ExtractedField> fields) {
        if (node.isObject() && node.size() > 0) {
            node.properties().forEach(entry ->
                    collect(entry.getValue(), path.isEmpty() ? entry.getKey() : path + "." + entry.getKey(), fields));
        } else if (node.isArray() && node.size() > 0) {
            collect(node.get(0), path + "[0]", fields);
        } else if (!path.isEmpty()) {
            fields.add(new ExtractedField(toAlias(path), path, detectType(node)));
        }
    }

    private FieldType detectType(JsonNode node) {
        if (node.isIntegralNumber()) return FieldType.INTEGER;
        if (node.isNumber()) return FieldType.DECIMAL;
        if (node.isBoolean()) return FieldType.BOOLEAN;
        if (node.isContainer()) return FieldType.OBJECT;
        return FieldType.STRING;
    }

    private String toAlias(String path) {
        return path.replaceAll("[^A-Za-z0-9]+", "_").replaceAll("^_+|_+$", "");
    }

    private Object convert(Object value, FieldType type) {
        if (value == null || type == null) return value;
        return switch (type) {
            case STRING -> value.toString();
            case INTEGER -> new BigDecimal(value.toString()).longValue();
            case DECIMAL -> new BigDecimal(value.toString());
            case BOOLEAN -> value instanceof Boolean ? value : Boolean.parseBoolean(value.toString());
            case OBJECT -> value;
        };
    }

}
