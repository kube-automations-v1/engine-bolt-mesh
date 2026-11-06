package automationsv1.engine_bolt_mesh.application;

import automationsv1.engine_bolt_mesh.application.domain.model.ApiDefinition;
import automationsv1.engine_bolt_mesh.application.domain.model.ExtractedField;
import automationsv1.engine_bolt_mesh.application.domain.model.ExtractedField.FieldType;
import automationsv1.engine_bolt_mesh.application.domain.model.WorkflowDefinition;
import automationsv1.engine_bolt_mesh.application.domain.model.WorkflowExecutionLog;
import automationsv1.engine_bolt_mesh.application.domain.model.step.ApiCallStep;
import automationsv1.engine_bolt_mesh.application.domain.model.step.ValidationRuleStep;
import automationsv1.engine_bolt_mesh.application.domain.model.step.ValidationRuleStep.LogicalOperator;
import automationsv1.engine_bolt_mesh.application.domain.model.step.WorkflowStep;
import automationsv1.engine_bolt_mesh.application.ports.in.RegisterApiDefinitionInputPort;
import automationsv1.engine_bolt_mesh.application.ports.in.RegisterWorkflowDefinitionInputPort;
import automationsv1.engine_bolt_mesh.application.ports.in.WorkFlowEngineInputPort;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class WorkflowEngineIntegrationTest {

    private static WireMockServer wireMock;

    @Autowired RegisterApiDefinitionInputPort registerApi;
    @Autowired RegisterWorkflowDefinitionInputPort registerWorkflow;
    @Autowired WorkFlowEngineInputPort engine;

    @BeforeAll
    static void startWireMock() {
        wireMock = new WireMockServer(options().dynamicPort());
        wireMock.start();
    }

    @AfterAll
    static void stopWireMock() {
        wireMock.stop();
    }

    @Test
    void executesChainedApiCallsWithValidation() {
        wireMock.stubFor(get(urlEqualTo("/score/12345678900")).willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("""
                        {"data": {"credit": {"score": 780}, "restricao": false},
                         "cliente": {"email": "joao@teste.com"}}""")));
        wireMock.stubFor(post(urlEqualTo("/erp/limite")).willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("{\"protocolo\": \"ABC-1\"}")));

        UUID tenant = UUID.randomUUID();
        String base = "http://localhost:" + wireMock.port();

        ApiDefinition serasa = registerApi.register(new ApiDefinition(null, tenant, "Serasa",
                base + "/score/#{#inputs['cpf']}", "GET", Map.of(), null,
                List.of(new ExtractedField("score_credito", "data.credit.score", FieldType.INTEGER),
                        new ExtractedField("sem_restricao", "data.restricao", FieldType.BOOLEAN),
                        new ExtractedField("email_cliente", "cliente.email", FieldType.STRING)),
                null));

        ApiDefinition erp = registerApi.register(new ApiDefinition(null, tenant, "ERP",
                base + "/erp/limite", "POST", Map.of("X-Request-Id", "#{#uuid()}"),
                "{\"cpf\": \"#{#inputs['doc']}\", \"limiteAprovado\": #{#inputs['valor_limite']}, \"notificar\": \"#{#inputs['email']}\"}",
                List.of(new ExtractedField("protocolo", "protocolo", FieldType.STRING)),
                null));

        Map<String, WorkflowStep> steps = Map.of(
                "step_consulta_score", new ApiCallStep("step_consulta_score", "Consulta score", serasa.id(),
                        Map.of("cpf", "#trigger['cpf']"), null, "step_valida_regras", null),
                "step_valida_regras", new ValidationRuleStep("step_valida_regras", "Valida regras", LogicalOperator.AND,
                        List.of(new ValidationRuleStep.Rule("Score alto",
                                        "#steps['step_consulta_score']['score_credito'] >= 700", "Score baixo"),
                                new ValidationRuleStep.Rule("Sem restrição",
                                        "#steps['step_consulta_score']['sem_restricao'] == false", "Possui restrição")),
                        "step_libera_limite", null),
                "step_libera_limite", new ApiCallStep("step_libera_limite", "Libera limite", erp.id(),
                        Map.of("doc", "#trigger['cpf']",
                                "valor_limite", "#steps['step_consulta_score']['score_credito'] * 10",
                                "email", "#steps['step_consulta_score']['email_cliente']"),
                        new ApiCallStep.RetryPolicy(2, 10), null, null));

        WorkflowDefinition workflow = registerWorkflow.register(new WorkflowDefinition(
                null, tenant, "Liberação de limite", true, 1, "step_consulta_score", steps, null, null));

        WorkflowExecutionLog log = engine.run(workflow.id(), Map.of("cpf", "12345678900"));

        assertThat(log.status()).isEqualTo(WorkflowExecutionLog.COMPLETED);
        assertThat(log.stepTraces()).extracting(WorkflowExecutionLog.StepTrace::stepId)
                .containsExactly("step_consulta_score", "step_valida_regras", "step_libera_limite");
        assertThat(log.stepTraces()).allMatch(t -> "SUCCESS".equals(t.status()));
        assertThat(log.finalStepOutputs()).containsKeys("step_consulta_score", "step_libera_limite");

        wireMock.verify(1, postRequestedFor(urlEqualTo("/erp/limite"))
                .withRequestBody(equalToJson("{\"cpf\": \"12345678900\", \"limiteAprovado\": 7800, \"notificar\": \"joao@teste.com\"}")));
    }

}
