# Engine Bolt Mesh — Guia passo a passo e relação com o código

Pacote base: `src/main/java/automationsv1/engine_bolt_mesh` (abreviado como `…/`).
Arquitetura hexagonal: `adapter/in` (REST) → `application/ports/in` → `application/domain/usecase` → `application/ports/out` → `adapter/out`.

## 1. Conceitos

| Conceito | O que é | Código |
|---|---|---|
| **ApiDefinition** (conector) | URL, método, headers, body e campos extraídos de uma API | `application/domain/model/ApiDefinition.java` |
| **ExtractedField** | `alias` + `path` (JsonPath) + `type` de um campo da resposta | `application/domain/model/ExtractedField.java` |
| **WorkflowDefinition** | Mapa de passos + `firstStepId` | `application/domain/model/WorkflowDefinition.java` |
| **Passo `API_CALL`** | Chama uma ApiDefinition | `…/model/step/ApiCallStep.java` |
| **Passo `VALIDATION_RULE`** | Avalia regras SpEL (AND/OR) | `…/model/step/ValidationRuleStep.java` |
| **WorkflowContext** | Estado da execução: `trigger`, `steps`, `inputs`, `env`, traces | `application/domain/model/WorkflowContext.java` |
| **WorkflowExecutionLog** | Resultado (status + `stepTraces`) | `application/domain/model/WorkflowExecutionLog.java` |

O polimorfismo dos passos é resolvido pelo campo `"type"` (`WorkflowStep.java`, `@JsonSubTypes`).

## 2. Passo a passo de uso

### Passo 1 — (Opcional) Descobrir os campos de uma API
`POST /api/v1/connectors/discover`
```json
{ "url": "https://pokeapi.co/api/v2/pokemon/ditto", "httpMethod": "GET" }
```
- Controller: `adapter/in/web/ApiDefinitionController.java#discover`
- Use case: `DiscoverApiFieldsUseCase` → `HttpCallOutputPort` (`adapter/out/http/RestClientHttpAdapter.java`) → `JsonPathFieldExtractorAdapter#discover`
- Retorna os caminhos folha com `alias`/`type` sugeridos.

### Passo 2 — Cadastrar o conector (ApiDefinition)
`POST /api/v1/connectors`
```json
{
  "tenantId": "11111111-1111-1111-1111-111111111111",
  "name": "pokemon",
  "baseUrl": "https://pokeapi.co/api/v2/pokemon/#{#inputs['name']}",
  "httpMethod": "GET",
  "extractedFields": [{ "path": "name", "type": "STRING", "alias": "name" }]
}
```
- DTO: `adapter/in/web/dto/CreateApiDefinitionRequest.java`
- Use case: `RegisterApiDefinitionUseCase` (normaliza método para maiúsculo e grava `createdAt`)
- Persistência: `ApiDefinitionPersistenceAdapter` → tabela `api_definitions` (`ApiDefinitionEntity`).

`baseUrl`, `headersTemplate` e `bodyTemplate` são **templates** com `#{ expressão SpEL }`. Dentro deles use `#inputs['x']` (valores calculados em `inputMappings` do passo).

Exemplo de conector POST (corrigido):
```json
{ "baseUrl": "http://localhost:8080/test/reg",
  "httpMethod": "POST",
  "bodyTemplate": "{\"name\": \"#{#inputs['name']}\"}" }
```

### Passo 3 — Cadastrar o workflow
`POST /api/v1/workflows`
```json
{
  "tenantId": "11111111-1111-1111-1111-111111111111",
  "name": "É o ditto?",
  "firstStepId": "step_get_pokemon_dito",
  "steps": {
    "step_get_pokemon_dito": {
      "type": "API_CALL", "id": "step_get_pokemon_dito", "name": "Consulta poke",
      "apiDefinitionId": "<id do conector pokemon>",
      "inputMappings": { "name": "#trigger['name']" },
      "nextStepId": "step_valida_regras"
    },
    "step_valida_regras": {
      "type": "VALIDATION_RULE", "id": "step_valida_regras", "name": "Valida regras",
      "operator": "AND",
      "rules": [{
        "description": "Valida Nome",
        "spelExpression": "#steps['step_get_pokemon_dito']['name'] == 'ditto'",
        "failureMessage": "Não é o ditto"
      }],
      "onPassStepId": "registrar_poke"
    },
    "registrar_poke": {
      "type": "API_CALL", "id": "registrar_poke", "name": "registrar_poke",
      "apiDefinitionId": "<id do conector Registrar Poke>",
      "inputMappings": { "name": "#trigger['name']" },
      "nextStepId": null
    }
  }
}
```
- Controller: `WorkflowController#create` → `RegisterWorkflowDefinitionUseCase` (valida que `firstStepId` existe) → `WorkflowDefinitionPersistenceAdapter` (tabela `workflow_definitions`, `steps` em JSON).

### Passo 4 — Disparar o workflow
`POST /api/v1/workflows/{workflowId}/trigger` com `{ "name": "ditto" }`
- `WorkflowController#trigger` → `WorkFlowEngineUseCase#run`.

## 3. O que acontece na execução

`WorkFlowEngineUseCase` (`application/domain/usecase/WorkFlowEngineUseCase.java`):

1. `run()` — busca o workflow, rejeita se inativo, cria `WorkflowContext(trigger)`.
2. `walk()` — percorre os passos a partir de `firstStepId`; limite `MAX_STEPS = 100` (`WorkflowCycleException`). Termina quando o próximo id é `null`.
3. `runApiCall()` → `ExecuteApiCallUseCase#execute`:
   1. Carrega a `ApiDefinition`.
   2. Resolve `inputMappings` com `EvaluateExpressionUseCase` e publica em `context.inputs` (`#inputs`).
   3. Interpola `baseUrl`, `bodyTemplate`, `headersTemplate` com `InterpolateUseCase`.
   4. Chama a API com retry (`callWithRetry`, `retryPolicy`).
   5. Extrai os campos com `JsonPathFieldExtractorAdapter#extract` e guarda em `context.steps[stepId]` (`#steps['id']['alias']`).
   6. Em erro: usa `onErrorStepId` ou aborta o workflow (status `FAILED`).
4. `runValidation()` — avalia cada regra via `EvaluateConditionUseCase` (resultado `Boolean`). Combina com `AND`/`OR`. Passou → `onPassStepId`; falhou → `onFailStepId` (status do trace `FAILED_RULES`). Se a regra lançar exceção, `passed=false` e a mensagem do erro vai em `failureMessage`.
5. O log é salvo (`WorkflowExecutionLogPersistenceAdapter`) e devolvido com `COMPLETED` ou `FAILED`.

### Variáveis disponíveis no SpEL
Definidas em `adapter/out/spel/CreateSafeContextAdapter.java`:

| Variável | Conteúdo |
|---|---|
| `#trigger` | payload do disparo |
| `#steps` | saídas dos passos já executados |
| `#inputs` | inputs resolvidos do passo atual |
| `#env` | mapa de ambiente (vazio por padrão) |
| `#upper(s)`, `#lower(s)`, `#nowIso()`, `#uuid()`, `#coalesce(a,b)` | funções de `adapter/out/spel/WorkflowFunctions.java` |

Parser/cache: `configuration/ExpressionParserConfig.java` (templates `#{ }`, Caffeine, modo MIXED).

## 4. Erros que apareceram e a causa real

### 4.1 `EL1004E: Method contains/indexOf/matches(String) cannot be found on type String`
- Causa: `CreateSafeContextAdapter` usa `SimpleEvaluationContext.forReadOnlyDataBinding()`, que **bloqueia chamadas de método** em objetos Java (segurança). Só leitura de propriedades/Map, operadores (`==`, `>`, `and`, `or`) e as funções registradas funcionam.
- Solução: usar `==` (`#steps['step_get_pokemon_dito']['name'] == 'ditto'`).
- Se precisar de "contém" no futuro: adicionar uma função pública estática em `WorkflowFunctions` (ex.: `contains(String, String)`), que é registrada automaticamente e usada como `#contains(#steps[...]['name'], 'ditto')`.

### 4.2 `400 Bad Request` em `/test/reg` + `JSON parse error: Unexpected end-of-input in VALUE_STRING`
- Causa: o `bodyTemplate` do conector "Registrar Poke" estava com a **aspa de fechamento faltando**:
  ```
  {"name": "#{#inputs['name']}}      ← inválido
  ```
  Após interpolar vira `{"name": "ditto}`, que o `TestController` (`adapter/in/web/TestController.java`, `POST /test/reg`) não consegue desserializar.
- Correção: `{"name": "#{#inputs['name']}"}`.
- Observação: `#inputs['name']` está **correto** (o `inputMappings` do passo `registrar_poke` define `name`); `#trigger` não é necessário no template.

## 5. Como ler o resultado (`stepTraces`)

| Campo | Significado |
|---|---|
| `stepType` | `API_CALL`, `VALIDATION_RULE` ou `ENGINE` (erro geral) |
| `status` | `SUCCESS`, `ERROR`, `FAILED_RULES` |
| `details` | saída extraída (API), resultado por regra (validação) ou mensagem de erro |

`status` geral: `COMPLETED` ou `FAILED` (`WorkflowExecutionLog`). Erros de negócio são mapeados em `ApiExceptionHandler` (404 `ResourceNotFoundException`, 422 `WorkflowExecutionException`).

## 6. Mapa rápido de camadas

- **Entrada (REST):** `ApiDefinitionController`, `WorkflowController`, `ApiExceptionHandler`
- **Portas de entrada:** `application/ports/in/*`
- **Casos de uso:** `application/domain/usecase/*`
- **Portas de saída:** `application/ports/out/*`
- **Adapters de saída:** `adapter/out/http` (HTTP), `adapter/out/json` (JsonPath), `adapter/out/spel` (contexto seguro), `adapter/out/persistence` (JPA/Postgres)
- **Configuração:** `configuration/AppConfig.java` (RestClient), `ExpressionParserConfig.java`, `src/main/resources/application.yaml`
- **Teste de ponta a ponta:** `src/test/java/.../application/WorkflowEngineIntegrationTest.java`
