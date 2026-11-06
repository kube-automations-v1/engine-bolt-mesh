package automationsv1.engine_bolt_mesh.adapter.out.spel;

import automationsv1.engine_bolt_mesh.application.domain.model.WorkflowContext;
import automationsv1.engine_bolt_mesh.application.ports.out.CreateSafeContextOutputPort;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.support.SimpleEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class CreateSafeContextAdapter implements CreateSafeContextOutputPort {

    private static final Map<String, Method> FUNCTIONS = Arrays.stream(WorkflowFunctions.class.getDeclaredMethods())
            .filter(m -> java.lang.reflect.Modifier.isPublic(m.getModifiers()))
            .collect(Collectors.toMap(Method::getName, Function.identity()));

    @Override
    public EvaluationContext create(WorkflowContext workflowContext) {
        // Permite apenas leitura de propriedades/Maps, bloqueando chamadas a classes Java arbitrárias
        SimpleEvaluationContext context = SimpleEvaluationContext.forReadOnlyDataBinding().build();

        context.setVariable("trigger", workflowContext.getTrigger());
        context.setVariable("steps", workflowContext.getSteps());
        context.setVariable("inputs", workflowContext.getInputs());
        context.setVariable("env", workflowContext.getEnv());
        FUNCTIONS.forEach(context::setVariable);

        return context;
    }

}
