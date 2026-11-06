package automationsv1.engine_bolt_mesh.application.domain.usecase;

import automationsv1.engine_bolt_mesh.application.domain.model.WorkflowContext;
import automationsv1.engine_bolt_mesh.application.ports.in.EvaluateConditionInputPort;
import automationsv1.engine_bolt_mesh.application.ports.out.CreateSafeContextOutputPort;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class EvaluateConditionUseCase implements EvaluateConditionInputPort {

    private final ExpressionParser parser;

    private final Cache<String, Expression> expressionCache;

    private final CreateSafeContextOutputPort createSafeContext;

    @Override
    public boolean evaluateCondition(String expression, WorkflowContext workflowContext) {
        EvaluationContext context = createSafeContext.create(workflowContext);
        Expression compiled = expressionCache.get(expression, parser::parseExpression);
        Boolean result = compiled.getValue(context, Boolean.class);
        return Boolean.TRUE.equals(result);
    }

}
