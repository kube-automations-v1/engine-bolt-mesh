package automationsv1.engine_bolt_mesh.application.domain.usecase;

import automationsv1.engine_bolt_mesh.application.domain.model.WorkflowContext;
import automationsv1.engine_bolt_mesh.application.ports.in.EvaluateExpressionInputPort;
import automationsv1.engine_bolt_mesh.application.ports.in.InterpolateInputPort;
import automationsv1.engine_bolt_mesh.application.ports.out.CreateSafeContextOutputPort;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class EvaluateExpressionUseCase implements EvaluateExpressionInputPort {

    private static final String TEMPLATE_PREFIX = "#{";

    private final ExpressionParser parser;

    private final Cache<String, Expression> expressionCache;

    private final InterpolateInputPort interpolate;

    private final CreateSafeContextOutputPort createSafeContext;

    // Templates (#{...}) viram String; expressões puras preservam o tipo do resultado (ex: Integer)
    @Override
    public Object evaluateExpression(String expression, WorkflowContext workflowContext) {
        if (expression == null) return null;
        if (expression.contains(TEMPLATE_PREFIX)) {
            return interpolate.interpolate(expression, workflowContext);
        }
        Expression compiled = expressionCache.get(expression, parser::parseExpression);
        return compiled.getValue(createSafeContext.create(workflowContext));
    }

}
