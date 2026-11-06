package automationsv1.engine_bolt_mesh.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.Map;

public record DiscoverRequest(
        @NotBlank String url,
        @NotBlank @Pattern(regexp = "(?i)GET|POST|PUT|DELETE|PATCH") String httpMethod,
        Map<String, String> headers,
        String body
) {
}
