package automationsv1.engine_bolt_mesh.adapter.out.http;

import automationsv1.engine_bolt_mesh.application.ports.out.HttpCallOutputPort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class RestClientHttpAdapter implements HttpCallOutputPort {

    private final RestClient restClient;

    @Override
    public String call(String httpMethod, String url, Map<String, String> headers, String body) {
        RestClient.RequestBodySpec request = restClient
                .method(HttpMethod.valueOf(httpMethod.toUpperCase()))
                .uri(URI.create(url))
                .headers(h -> {
                    if (headers != null) headers.forEach(h::set);
                });

        if (StringUtils.hasText(body)) {
            request.contentType(MediaType.APPLICATION_JSON).body(body);
        }

        return request.retrieve().body(String.class);
    }

}
