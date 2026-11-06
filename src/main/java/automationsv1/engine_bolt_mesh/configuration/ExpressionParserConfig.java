package automationsv1.engine_bolt_mesh.configuration;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.SpelCompilerMode;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;

@Configuration
public class ExpressionParserConfig {

    private static final long CACHE_MAX_SIZE = 10_000;

    @Bean
    public ExpressionParser expressionParser() {
        // MIXED compila em bytecode as expressões "quentes" e cai para interpretação se a compilação não for possível
        SpelParserConfiguration config = new SpelParserConfiguration(SpelCompilerMode.MIXED, getClass().getClassLoader());
        return new SpelExpressionParser(config);
    }

    @Bean
    public TemplateParserContext templateParserContext() {
        return new TemplateParserContext("#{", "}");
    }

    @Bean
    public Cache<String, Expression> expressionCache() {
        return Caffeine.newBuilder().maximumSize(CACHE_MAX_SIZE).build();
    }

    @Bean
    public Cache<String, Expression> templateCache() {
        return Caffeine.newBuilder().maximumSize(CACHE_MAX_SIZE).build();
    }

}
