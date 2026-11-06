package automationsv1.engine_bolt_mesh.application.ports.out;

import java.util.Map;

public interface HttpCallOutputPort {

    String call(String httpMethod, String url, Map<String, String> headers, String body);

}
