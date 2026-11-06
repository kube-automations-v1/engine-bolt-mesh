package automationsv1.engine_bolt_mesh.application.domain.usecase;

import automationsv1.engine_bolt_mesh.application.domain.model.WorkflowContext;
import automationsv1.engine_bolt_mesh.application.ports.in.InterpolateInputPort;
import automationsv1.engine_bolt_mesh.application.ports.out.CreateSafeContextOutputPort;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class InterpolateUseCase implements InterpolateInputPort {

    private final ExpressionParser parser;
    private final TemplateParserContext templateContext;
    private final Cache<String, Expression> templateCache;

    private final CreateSafeContextOutputPort createSafeContext;

    @Override
    public String interpolate(String template, WorkflowContext workflowContext) {
        if (template == null) return null;
        EvaluationContext context = createSafeContext.create(workflowContext);
        Expression compiled = templateCache.get(template, t -> parser.parseExpression(t, templateContext));
        return compiled.getValue(context, String.class);
    }

}
