package automationsv1.engine_bolt_mesh.application.domain.usecase;

import automationsv1.engine_bolt_mesh.application.domain.exception.ResourceNotFoundException;
import automationsv1.engine_bolt_mesh.application.domain.exception.WorkflowExecutionException;
import automationsv1.engine_bolt_mesh.application.domain.model.ApiDefinition;
import automationsv1.engine_bolt_mesh.application.domain.model.WorkflowContext;
import automationsv1.engine_bolt_mesh.application.domain.model.step.ApiCallStep;
import automationsv1.engine_bolt_mesh.application.ports.in.EvaluateExpressionInputPort;
import automationsv1.engine_bolt_mesh.application.ports.in.ExecuteApiCallInputPort;
import automationsv1.engine_bolt_mesh.application.ports.in.InterpolateInputPort;
import automationsv1.engine_bolt_mesh.application.ports.out.ApiDefinitionOutputPort;
import automationsv1.engine_bolt_mesh.application.ports.out.HttpCallOutputPort;
import automationsv1.engine_bolt_mesh.application.ports.out.JsonFieldExtractorOutputPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class ExecuteApiCallUseCase implements ExecuteApiCallInputPort {

    private final ApiDefinitionOutputPort apiDefinitionOutputPort;
    private final EvaluateExpressionInputPort evaluateExpression;
    private final InterpolateInputPort interpolate;
    private final HttpCallOutputPort httpCall;
    private final JsonFieldExtractorOutputPort jsonFieldExtractor;

    @Override
    public Map<String, Object> execute(ApiCallStep step, WorkflowContext context) {
        ApiDefinition api = apiDefinitionOutputPort.findById(step.apiDefinitionId())
                .orElseThrow(() -> new ResourceNotFoundException("ApiDefinition não encontrada: " + step.apiDefinitionId()));

        // Os inputs são publicados no contexto antes de resolver, permitindo que um mapping use os anteriores
        Map<String, Object> resolvedInputs = new LinkedHashMap<>();
        context.setInputs(resolvedInputs);
        if (step.inputMappings() != null) {
            step.inputMappings().forEach((name, expression) ->
                    resolvedInputs.put(name, evaluateExpression.evaluateExpression(expression, context)));
        }

        String url = interpolate.interpolate(api.baseUrl(), context);
        String body = interpolate.interpolate(api.bodyTemplate(), context);
        Map<String, String> headers = new LinkedHashMap<>();
        if (api.headersTemplate() != null) {
            api.headersTemplate().forEach((name, value) -> headers.put(name, interpolate.interpolate(value, context)));
        }

        String rawResponse = callWithRetry(step, api.httpMethod(), url, headers, body);
        return jsonFieldExtractor.extract(rawResponse, api.extractedFields());
    }

    private String callWithRetry(ApiCallStep step, String method, String url, Map<String, String> headers, String body) {
        int maxAttempts = step.retryPolicy() != null ? Math.max(1, step.retryPolicy().maxAttempts()) : 1;
        long backoff = step.retryPolicy() != null ? step.retryPolicy().backoffMillis() : 0;

        RuntimeException last = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return httpCall.call(method, url, headers, body);
            } catch (RuntimeException e) {
                last = e;
                if (attempt < maxAttempts && backoff > 0) {
                    sleep(backoff);
                }
            }
        }
        throw new WorkflowExecutionException(
                "Falha ao chamar API do passo '" + step.id() + "' após " + maxAttempts + " tentativa(s): " + last.getMessage(), last);
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WorkflowExecutionException("Execução interrompida durante o backoff", e);
        }
    }

}
