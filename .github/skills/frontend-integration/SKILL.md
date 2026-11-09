---
name: frontend-integration
description: Guia para implementar o frontend do engine-bolt-mesh (motor de workflows com SpEL). Use ao criar telas, clients HTTP, tipos TypeScript ou formulários que consomem a API de conectores e workflows.
---

# Frontend do engine-bolt-mesh

## 1. O que é o projeto

Motor de automação/orquestração de workflows em tempo de execução (estilo Power Automate / n8n). Backend: Java 21, Spring Boot 4, arquitetura hexagonal, Postgres (colunas JSON), SpEL para expressões e JsonPath para extração de dados.

O usuário monta dois tipos de recurso:

1. **Conector (`ApiDefinition`)**: descreve uma API externa (URL, método, headers, body) e quais campos da resposta devem ser capturados (`extractedFields`).
2. **Workflow (`WorkflowDefinition`)**: grafo de passos (`steps`) que encadeia chamadas de conectores e regras de validação.

Ao **disparar** um workflow (`trigger`), o motor percorre o grafo a partir de `firstStepId`, executa cada passo e devolve um **log de execução** com o resultado de cada passo.

- Base URL padrão: `http://localhost:8080`
- Prefixo: `/api/v1`
- Content-Type: `application/json`
- Sem autenticação por enquanto. CORS **não está configurado**: em dev use proxy do bundler ou peça para liberar CORS no backend.
- IDs são UUID. Datas (`Instant`) chegam como string ISO-8601.

## 2. Fluxo de uso na UI

```
[Conector]  cadastrar -> (opcional) "Test & Discover" para sugerir campos
     |
[Workflow]  montar grafo de passos usando os conectores
     |
[Trigger]   informar payload JSON -> exibir WorkflowExecutionLog (timeline de passos)
```

Telas sugeridas: lista/CRUD de conectores, editor de conector com botão "Testar e descobrir campos", lista/CRUD de workflows, editor de grafo (passos e ligações), tela de execução com payload + timeline do resultado.

## 3. Endpoints

### 3.1 Conectores — `/api/v1/connectors`

| Método | Rota | Descrição | Sucesso |
|---|---|---|---|
| POST | `/connectors` | Cria conector | 201 + `ApiDefinition` |
| GET | `/connectors` | Lista todos | 200 + `ApiDefinition[]` |
| GET | `/connectors/{id}` | Busca por id | 200 + `ApiDefinition` |
| PUT | `/connectors/{id}` | Atualiza (mesmo body do POST) | 200 + `ApiDefinition` |
| DELETE | `/connectors/{id}` | Remove (físico) | 204 |
| POST | `/connectors/discover` | Faz a chamada real e sugere campos | 200 + `ExtractedField[]` |

**Body de POST/PUT** (`CreateApiDefinitionRequest`):

| Campo | Tipo | Obrigatório | Observação |
|---|---|---|---|
| `tenantId` | UUID | sim | No PUT é ignorado (mantém o original) |
| `name` | string | sim | máx. 100 caracteres |
| `baseUrl` | string | sim | Aceita SpEL: `https://api.com/clientes/#{#inputs['cpf']}` |
| `httpMethod` | string | sim | `GET`, `POST`, `PUT`, `DELETE` ou `PATCH` (case-insensitive; salvo em maiúsculo) |
| `headersTemplate` | `Record<string,string>` | não | Valores aceitam SpEL `#{...}` |
| `bodyTemplate` | string | não | String JSON com `#{...}`. Enviado como `application/json` |
| `extractedFields` | `ExtractedField[]` | sim (pode ser `[]`) | Campos capturados da resposta |

**Resposta** (`ApiDefinition`): os campos acima mais `id` e `createdAt`.

```json
{
  "tenantId": "11111111-1111-1111-1111-111111111111",
  "name": "Serasa",
  "baseUrl": "https://api.externa.com/score/#{#inputs['cpf']}",
  "httpMethod": "GET",
  "headersTemplate": { "X-Request-Id": "#{#uuid()}" },
  "bodyTemplate": null,
  "extractedFields": [
    { "alias": "score_credito", "path": "data.credit.score", "type": "INTEGER" },
    { "alias": "sem_restricao", "path": "data.restricao", "type": "BOOLEAN" },
    { "alias": "email_cliente", "path": "cliente.email", "type": "STRING" }
  ]
}
```

**`ExtractedField`**
- `alias`: nome da variável disponível depois nos passos (`#steps['passo']['alias']`).
- `path`: JsonPath da resposta; o `$.` inicial é opcional (`data.credit.score` = `$.data.credit.score`). Campo ausente vira `null`, sem erro.
- `type`: `STRING | INTEGER | DECIMAL | BOOLEAN | OBJECT`. `INTEGER` vira número inteiro, `DECIMAL` vira decimal, `OBJECT` mantém objeto/array como veio.

**Discover** — body (`DiscoverRequest`): `{ "url": "...", "httpMethod": "GET", "headers": {"k":"v"}, "body": "..." }` (`url` e `httpMethod` obrigatórios). Executa a requisição **de verdade** (sem SpEL), percorre o JSON e devolve os caminhos folha com tipo detectado, por exemplo:

```json
[
  { "alias": "data_credit_score", "path": "data.credit.score", "type": "INTEGER" },
  { "alias": "cliente_email", "path": "cliente.email", "type": "STRING" }
]
```

Arrays aparecem como `lista[0].campo` (só o primeiro elemento é inspecionado). Use para pré-popular a tabela de campos e deixe o usuário renomear o `alias`, trocar o `type` e remover linhas.

### 3.2 Workflows — `/api/v1/workflows`

| Método | Rota | Descrição | Sucesso |
|---|---|---|---|
| POST | `/workflows` | Cria workflow | 201 + `WorkflowDefinition` |
| GET | `/workflows` | Lista todos | 200 + `WorkflowDefinition[]` |
| GET | `/workflows/{workflowId}` | Busca por id | 200 + `WorkflowDefinition` |
| PUT | `/workflows/{workflowId}` | Atualiza (mesmo body do POST) | 200 + `WorkflowDefinition` |
| DELETE | `/workflows/{workflowId}` | Remove (físico) | 204 |
| POST | `/workflows/{workflowId}/trigger` | Executa o workflow | 200 + `WorkflowExecutionLog` |

**Body de POST/PUT** (`CreateWorkflowRequest`):

| Campo | Tipo | Obrigatório | Observação |
|---|---|---|---|
| `tenantId` | UUID | sim | No PUT é ignorado |
| `name` | string | sim | |
| `active` | boolean | não | Padrão `true`. Workflow inativo não executa (422) |
| `firstStepId` | string | sim | Precisa ser uma chave existente em `steps` |
| `steps` | `Record<string, WorkflowStep>` | sim, não vazio | A **chave do mapa deve ser igual ao `id` do passo** |

**Resposta** (`WorkflowDefinition`): `id`, `tenantId`, `name`, `active`, `version`, `firstStepId`, `steps`, `createdAt`, `updatedAt`. O `version` começa em 1 e sobe a cada PUT.

#### Passos (`WorkflowStep`) — polimórfico pelo campo `type`

**`API_CALL`** — chama um conector:

```json
{
  "type": "API_CALL",
  "id": "step_consulta_score",
  "name": "Consulta score",
  "apiDefinitionId": "uuid-do-conector",
  "inputMappings": { "cpf": "#trigger['cpf']" },
  "retryPolicy": { "maxAttempts": 2, "backoffMillis": 500 },
  "nextStepId": "step_valida_regras",
  "onErrorStepId": null
}
```

- `inputMappings`: nome da variável → expressão SpEL. Ficam disponíveis no conector como `#inputs['nome']`.
- `retryPolicy` (opcional): tentativas totais e espera entre elas em ms.
- `nextStepId`: próximo passo. `null` encerra o fluxo.
- `onErrorStepId`: se a chamada falhar, vai para este passo. Se for `null`, o workflow termina como `FAILED`.

**`VALIDATION_RULE`** — avalia regras e desvia:

```json
{
  "type": "VALIDATION_RULE",
  "id": "step_valida_regras",
  "name": "Valida regras",
  "operator": "AND",
  "rules": [
    {
      "description": "Score alto",
      "spelExpression": "#steps['step_consulta_score']['score_credito'] >= 700",
      "failureMessage": "Score baixo"
    }
  ],
  "onPassStepId": "step_libera_limite",
  "onFailStepId": null
}
```

- `operator`: `AND` (todas passam) ou `OR` (ao menos uma passa).
- `onPassStepId` / `onFailStepId`: `null` encerra o fluxo (termina `COMPLETED`).
- Regra que lança erro conta como reprovada.

Qualquer `*StepId` que não exista em `steps` faz a execução falhar. Validar no editor: ids únicos, referências existentes, alertar sobre ciclos (limite de 100 passos executados por execução).

### 3.3 Trigger e resultado

`POST /workflows/{id}/trigger` com **qualquer JSON objeto** como body (vira `#trigger`), por exemplo `{ "cpf": "12345678900", "tipo": "PF" }`.

Resposta (`WorkflowExecutionLog`):

```json
{
  "id": "uuid",
  "workflowId": "uuid",
  "status": "COMPLETED",
  "triggerPayload": { "cpf": "12345678900" },
  "finalStepOutputs": {
    "step_consulta_score": { "score_credito": 780, "sem_restricao": false, "email_cliente": "joao@teste.com" }
  },
  "stepTraces": [
    {
      "stepId": "step_consulta_score",
      "stepType": "API_CALL",
      "status": "SUCCESS",
      "details": { "score_credito": 780 },
      "durationMs": 120,
      "timestamp": "2026-11-08T22:00:00Z"
    }
  ],
  "startedAt": "...",
  "finishedAt": "..."
}
```

- `status` do log: `COMPLETED` ou `FAILED`. **O HTTP é 200 mesmo quando `FAILED`**; sempre olhe `status`.
- `stepTraces[].status`: `SUCCESS`, `ERROR` ou `FAILED_RULES` (regras reprovaram; o fluxo seguiu por `onFailStepId`).
- `stepTraces[].details` depende do tipo:
  - `API_CALL` com sucesso: objeto com os campos extraídos. Em erro: string com a mensagem.
  - `VALIDATION_RULE`: lista de `{ description, expression, passed, failureMessage? }`.
- Falha geral do motor (loop, passo inexistente) aparece como trace com `stepId: "-"` e `stepType: "ENGINE"`.
- `finalStepOutputs`: saída de cada passo `API_CALL` executado, indexada pelo id do passo.
- Não existe endpoint de listagem/consulta de logs; o log só é retornado na resposta do trigger. Guarde-o no estado do cliente.

## 4. Expressões SpEL (editor de expressões)

Dois formatos aparecem nos campos de texto:

- **Template** `#{ ... }`: usado em `baseUrl`, `headersTemplate`, `bodyTemplate` e também aceito em `inputMappings`. O resultado é interpolado como string. Ex.: `https://x.com/#{#inputs['cpf']}`.
- **Expressão pura**: usada em `inputMappings` (sem `#{}`) e em `spelExpression`. Preserva o tipo (número, boolean). Ex.: `#steps['a']['score'] * 10`.

Em `bodyTemplate`, números ficam sem aspas e strings com aspas: `{"cpf": "#{#inputs['doc']}", "limite": #{#inputs['valor']}}`.

**Variáveis disponíveis**

| Variável | Conteúdo |
|---|---|
| `#trigger` | Payload recebido no trigger |
| `#steps` | Saída por passo: `#steps['id_do_passo']['alias']` |
| `#inputs` | Resultado dos `inputMappings` do passo atual (só dentro do conector) |
| `#env` | Mapa de ambiente (hoje vazio) |

**Funções**: `#upper(s)`, `#lower(s)`, `#nowIso()`, `#uuid()`, `#coalesce(a, b)`. Ex.: `#{#uuid()}`.

**Exemplos úteis**

```text
#steps['s1']['score'] >= 700 and #steps['s1']['sem_restricao'] == false
#steps['s1']['email']?.contains('@teste.com') == true
{'PF','PJ'}.contains(#trigger['tipo'])
#trigger['cpf'] matches '^[0-9]{11}$'
#lower(#trigger['nome']).contains('silva')
#coalesce(#trigger['limite'], 1000)
```

**Restrições (modo seguro)**: o contexto é somente leitura. Não funcionam `T(java.lang.Runtime)`, `new`, nem atribuições. Métodos de instância de String/List/Map (`contains`, `startsWith`, `size`, etc.) funcionam. Use `?.` para evitar erro com `null`.

Dica de UI: ofereça autocomplete para `#trigger`, `#steps['id']['alias']` (a partir dos `extractedFields` do conector de cada passo anterior) e `#inputs`.

## 5. Erros

Respostas de erro usam `application/problem+json` (RFC 9457): `{ "type", "title", "status", "detail", "instance" }`.

| Status | Quando |
|---|---|
| 400 | Validação do body (campo obrigatório vazio, `httpMethod` inválido) ou JSON malformado |
| 404 | Conector/workflow não encontrado (GET/PUT/DELETE/trigger) |
| 422 | Workflow inativo, `firstStepId` inexistente em `steps` |
| 500 | Demais erros, inclusive falha HTTP no `/connectors/discover` (ex.: a API externa retornou 4xx/5xx) |

Mostre `detail` ao usuário. Para execuções, trate separadamente o `status: "FAILED"` do log (HTTP 200).

## 6. Tipos TypeScript

```ts
type Uuid = string;
type FieldType = 'STRING' | 'INTEGER' | 'DECIMAL' | 'BOOLEAN' | 'OBJECT';
type HttpMethod = 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH';

interface ExtractedField { alias: string; path: string; type: FieldType }

interface ApiDefinition {
  id: Uuid; tenantId: Uuid; name: string; baseUrl: string; httpMethod: HttpMethod;
  headersTemplate: Record<string, string> | null; bodyTemplate: string | null;
  extractedFields: ExtractedField[]; createdAt: string;
}
type ApiDefinitionInput = Omit<ApiDefinition, 'id' | 'createdAt'>;

interface DiscoverRequest {
  url: string; httpMethod: HttpMethod; headers?: Record<string, string>; body?: string;
}

interface RetryPolicy { maxAttempts: number; backoffMillis: number }

interface ApiCallStep {
  type: 'API_CALL'; id: string; name: string; apiDefinitionId: Uuid;
  inputMappings: Record<string, string>; retryPolicy?: RetryPolicy | null;
  nextStepId: string | null; onErrorStepId: string | null;
}

interface ValidationRule { description: string; spelExpression: string; failureMessage: string }

interface ValidationRuleStep {
  type: 'VALIDATION_RULE'; id: string; name: string; operator: 'AND' | 'OR';
  rules: ValidationRule[]; onPassStepId: string | null; onFailStepId: string | null;
}

type WorkflowStep = ApiCallStep | ValidationRuleStep;

interface WorkflowDefinition {
  id: Uuid; tenantId: Uuid; name: string; active: boolean; version: number;
  firstStepId: string; steps: Record<string, WorkflowStep>;
  createdAt: string; updatedAt: string;
}
type WorkflowInput = Pick<WorkflowDefinition, 'tenantId' | 'name' | 'firstStepId' | 'steps'> & { active?: boolean };

interface StepTrace {
  stepId: string; stepType: 'API_CALL' | 'VALIDATION_RULE' | 'ENGINE';
  status: 'SUCCESS' | 'ERROR' | 'FAILED_RULES'; details: unknown;
  durationMs: number; timestamp: string;
}

interface WorkflowExecutionLog {
  id: Uuid; workflowId: Uuid; status: 'COMPLETED' | 'FAILED';
  triggerPayload: Record<string, unknown>; finalStepOutputs: Record<string, unknown>;
  stepTraces: StepTrace[]; startedAt: string; finishedAt: string;
}

interface ProblemDetail { type?: string; title?: string; status: number; detail?: string; instance?: string }
```

## 7. Exemplo de client

```ts
const BASE = '/api/v1';

async function http<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(BASE + path, {
    headers: { 'Content-Type': 'application/json' },
    ...init,
  });
  if (!res.ok) {
    const problem: ProblemDetail = await res.json().catch(() => ({ status: res.status }));
    throw problem;
  }
  return res.status === 204 ? (undefined as T) : res.json();
}

export const connectors = {
  list: () => http<ApiDefinition[]>('/connectors'),
  get: (id: Uuid) => http<ApiDefinition>(`/connectors/${id}`),
  create: (b: ApiDefinitionInput) => http<ApiDefinition>('/connectors', { method: 'POST', body: JSON.stringify(b) }),
  update: (id: Uuid, b: ApiDefinitionInput) => http<ApiDefinition>(`/connectors/${id}`, { method: 'PUT', body: JSON.stringify(b) }),
  remove: (id: Uuid) => http<void>(`/connectors/${id}`, { method: 'DELETE' }),
  discover: (b: DiscoverRequest) => http<ExtractedField[]>('/connectors/discover', { method: 'POST', body: JSON.stringify(b) }),
};

export const workflows = {
  list: () => http<WorkflowDefinition[]>('/workflows'),
  get: (id: Uuid) => http<WorkflowDefinition>(`/workflows/${id}`),
  create: (b: WorkflowInput) => http<WorkflowDefinition>('/workflows', { method: 'POST', body: JSON.stringify(b) }),
  update: (id: Uuid, b: WorkflowInput) => http<WorkflowDefinition>(`/workflows/${id}`, { method: 'PUT', body: JSON.stringify(b) }),
  remove: (id: Uuid) => http<void>(`/workflows/${id}`, { method: 'DELETE' }),
  trigger: (id: Uuid, payload: Record<string, unknown>) =>
    http<WorkflowExecutionLog>(`/workflows/${id}/trigger`, { method: 'POST', body: JSON.stringify(payload) }),
};
```

## 8. Armadilhas e boas práticas

- A chave de `steps` deve ser igual ao `id` do passo; gere os ids no cliente (ex.: `step_<slug>`) e mantenha-os estáveis ao editar.
- Todo passo precisa do campo `type`; sem ele o backend não consegue desserializar.
- Ao excluir um conector, avise se algum workflow o usa: o backend **não** bloqueia a exclusão e o passo passa a falhar na execução.
- `PUT` substitui o recurso inteiro (não é parcial): envie o objeto completo.
- Reprovação em `VALIDATION_RULE` sem `onFailStepId` termina o fluxo como `COMPLETED`; destaque na timeline o status `FAILED_RULES` e as `failureMessage`.
- Para `INTEGER`, valores grandes vêm como número JSON; cuidado com precisão acima de 2^53 no JavaScript.
- O endpoint `/connectors/discover` dispara requisição real (pode ter efeitos colaterais em `POST`/`PUT`/`DELETE`); avise o usuário antes de executar.
- Não coloque segredos fixos em `headersTemplate` se o conector for visível a outros usuários; ainda não há gestão de segredos (`#env` está vazio).
