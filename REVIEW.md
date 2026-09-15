# Revisão Completa — `settlement.controller.ts`

Segui o pipeline de `00-orquestrador.md`: rodei os 4 especialistas contra o mesmo trecho e depois consolidei. **Esta é a segunda geração do relatório**, atualizada com os casos novos incluídos na base de conhecimento (todos referenciando este exato endpoint como "Anexo A do desafio técnico"):

- `boas-praticas/001-anexo-a-float-e-excecao-engolida.md`
- `design/casos/001-anexo-a-controller-monolitico.md`
- `negocio/casos/001-golden-cases-precificacao.md`
- `negocio/casos/002-liquidacao-duplicada-nao-idempotente.md`
- `seguranca/casos/001-sql-injection-endpoint-liquidacao.md`

O achado de maior impacto desta atualização: os **golden cases** transformam a antiga suspeita "magnitude de taxa parece errada" em **certeza matemática confirmada** (ver detalhe abaixo), e revelam um bug adicional de ordem de arredondamento no câmbio que a primeira versão deste relatório não capturava.

---

## [Segurança]

### [Segurança] SQL Injection em `receivableId` e `currency`
- **Severidade:** Crítica
- **Trecho:** `settlement.controller.ts:7, 23, 26` — interpolação direta em três queries SQL (`SELECT`, `INSERT`, `UPDATE`).
- **Fonte:** `seguranca/casos/001-sql-injection-endpoint-liquidacao.md` (caso documentado especificamente para este endpoint).
- **Impacto:** Em um sistema que movimenta valores de um fundo de crédito, permite desde exfiltração de dados de outros cedentes/recebíveis até manipulação de valores e status de liquidação — liquidações fraudulentas ou fuga de dados sensíveis do fundo.
- **Correção:** Query parametrizada em todas as três chamadas, e `currency` validado contra whitelist (`enum BRL | USD`) antes de qualquer uso:
```ts
const receivable = await db.queryOne(
  `SELECT * FROM receivables WHERE id = $1`, [receivableId]
);
await db.query(
  `INSERT INTO settlements (receivable_id, amount, currency) VALUES ($1, $2, $3)`,
  [receivableId, finalAmount.toFixed(2), currency]
);
```

### [Segurança] Ausência de autenticação/autorização no endpoint de liquidação
- **Severidade:** Crítica
- **Trecho:** `settlement.controller.ts:3` — nenhuma checagem de identidade/permissão antes de liquidar um recebível.
- **Impacto:** Qualquer chamador pode liquidar recebíveis de terceiros — IDOR clássico em operação que move dinheiro.
- **Correção:** Middleware de autenticação + checagem de ownership (`receivable.ownerId === req.user.id`) antes de processar.

### [Segurança] Falta de validação de moeda/taxa de câmbio pode persistir valores inválidos
- **Severidade:** Média
- **Trecho:** `settlement.controller.ts:15-18` — `fxService.getLatestRate` sem validação de retorno (nulo, zero, negativo).
- **Impacto:** Taxa `0`/`NaN`/indisponível gera `finalAmount = Infinity/NaN`, persistido ou quebrando a query silenciosamente.
- **Correção:** Validar `rate > 0 && Number.isFinite(rate)` antes de usar; lançar erro explícito caso contrário.

---

## [Boas Práticas]

### [Boas Práticas] Payload da requisição sem validação/schema
- **Severidade:** Alta
- **Trecho:** `settlement.controller.ts:4` — `receivableId`/`currency` sem schema, efetivamente `any`.
- **Correção:** Validar com `zod`/`class-validator` antes de usar.

### [Boas Práticas] `toFixed(2)` usado como arredondamento não é half-even
- **Severidade:** Alta
- **Trecho:** `settlement.controller.ts:23, 32` — `finalAmount.toFixed(2)`.
- **Fonte:** `boas-praticas/001-anexo-a-float-e-excecao-engolida.md`.
- **Impacto:** O domínio exige **half-even (banker's rounding)**; `toFixed` arredonda com base em uma representação binária de ponto flutuante já imprecisa, podendo divergir do padrão contratual do fundo e gerar disputas com cedentes sobre o valor pago.
- **Correção:** Implementar arredondamento half-even explícito, aplicado **uma única vez**, sobre o resultado final — nunca via `toFixed` em cima de `number`.

### [Boas Práticas] Resposta sempre HTTP 200, mesmo em falha
- **Severidade:** Média
- **Trecho:** `settlement.controller.ts:32`.
- **Correção:** Propagar erro real (`5xx`) em vez de sempre `200`.

---

## [Design de Código]

### [Design de Código] Controller monolítico sem camadas nem Strategy (requisito explícito do desafio)
- **Severidade:** Alta
- **Trecho:** `settlement.controller.ts:3-33` — leitura de body, SQL cru, cálculo de PV, câmbio, persistência e resposta HTTP tudo em um handler; spread decidido por ternário (`receivable.type === "DUPLICATA" ? 1.5 : 2.5`) em vez do padrão **Strategy** pedido explicitamente na seção 4.1.2 do enunciado.
- **Fonte:** `design/casos/001-anexo-a-controller-monolitico.md`.
- **Impacto:** Impossível testar a fórmula de precificação isoladamente; adicionar um novo tipo de recebível exige editar o controller diretamente, aumentando risco de regressão em código que move dinheiro.
- **Correção:** Extrair `SettlementService`/`PricingService` com uma `Strategy` por tipo de recebível (`DuplicataMercantilStrategy`, `ChequePreDatadoStrategy`, ...), selecionada por factory/registry; extrair acesso a dados para `ReceivablesRepository`/`SettlementsRepository` com queries parametrizadas.

### [Design de Código] Ambiguidade sobre qual taxa de câmbio vale (sem vigência)
- **Severidade:** Alta
- **Trecho:** `settlement.controller.ts:16` — `fxService.getLatestRate("USD")` não recebe nenhuma referência de data (operação, vencimento ou liquidação).
- **Fonte:** `design/casos/001-anexo-a-controller-monolitico.md`.
- **Impacto:** "Qual taxa vale" fica implícito no timing da chamada em vez de ser uma regra de negócio explícita e testável — resultado da liquidação passa a depender de quando o `fxService` foi consultado, não de uma política definida.
- **Correção:** Tornar explícita no `CurrencyEngine` a política de qual taxa usar, recebendo uma data de referência como parâmetro em vez de "última taxa" implícita.

### [Design de Código] SQL cru inline em vez de repositório/ORM
- **Severidade:** Média
- **Trecho:** `settlement.controller.ts:6-8, 21-27`.
- **Correção:** Encapsular em repositório com métodos nomeados (`findReceivableById`, `createSettlement`, `markReceivableSettled`).

---

## [Negócio]

### [Negócio > Regras de Negócio Financeiras] `BASE_RATE`/spread com magnitude 100x maior que o correto — confirmado por golden cases
- **Severidade:** Crítica
- **Trecho:** `settlement.controller.ts:1, 10-12` — `BASE_RATE = 1.0`, spreads `1.5`/`2.5`.
- **Fonte:** `negocio/casos/001-golden-cases-precificacao.md`.
- **O que os golden cases provam:** taxa base é **1,00% a.m. (`0.01`)**, spread Duplicata Mercantil **1,5% (`0.015`)**, demais tipos **2,5% (`0.025`)** — reconstituindo a fórmula a partir de C1 (R$100.000, 3 meses → PV R$92.859,94) e C2 (R$25.000, 2 meses → PV R$23.337,77) confirma exatamente esses valores. O código atual usa `1.0`/`1.5`/`2.5` — cem vezes maior — o que faz `Math.pow(1 + BASE_RATE + spread, term)` explodir e o deságio ficar absurdamente agressivo.
- **Impacto:** Cada liquidação paga drasticamente menos que o devido ao cedente — perda financeira direta e risco de disputa/contencioso; o motor não bate os golden cases ao centavo.
- **Correção:** `BASE_RATE = 0.01`; spread por tipo via Strategy (ver achado de Design) retornando `0.015` (Duplicata) / `0.025` (demais). Adicionar teste automatizado que valida os três casos (C1, C2, C3) ao centavo — é requisito do desafio, não sugestão.

### [Negócio > Regras de Negócio Financeiras] Ordem de arredondamento incorreta na conversão cross-currency
- **Severidade:** Alta
- **Trecho:** `settlement.controller.ts:14-18` — `finalAmount = presentValue / rate` usa `presentValue` bruto (float, não arredondado) antes de qualquer arredondamento; o arredondamento só acontece depois, via `toFixed(2)`, na escrita/resposta.
- **Fonte:** `negocio/casos/001-golden-cases-precificacao.md` (caso C3: PV em BRL R$92.859,94 já arredondado, dividido por 5,4321 → US$17.094,67).
- **Impacto:** A regra exige arredondar o valor presente **em BRL, half-even, 2 casas** primeiro, e só então converter para USD. Converter o float bruto e arredondar só no fim inverte a ordem exigida, produzindo um valor em USD que pode divergir do golden case C3 por erro de centavos.
- **Correção:**
```ts
const valorPresente = presentValue.roundHalfEven(2); // arredonda aqui, em BRL
const valorFinal = currency === "USD" ? valorPresente.divide(rate) : valorPresente;
```

### [Negócio > Regras de Negócio Financeiras] Moeda gravada não corresponde à moeda efetiva do valor calculado
- **Severidade:** Alta
- **Trecho:** `settlement.controller.ts:14-18, 22-24` — conversão só ocorre se `currency === "USD"`; qualquer outro valor é gravado como está, com o número ainda em BRL.
- **Correção:** Validar `currency` contra lista fechada suportada, rejeitando o request se não for `BRL`/`USD`.

### [Negócio > Regras de Negócio Financeiras] Liquidação duplicada por ausência de idempotência — incidente já documentado
- **Severidade:** Crítica
- **Trecho:** `settlement.controller.ts:3-33` — nenhuma checagem de `receivable.status`, chave de idempotência ou lock antes do `INSERT`.
- **Fonte:** `negocio/casos/002-liquidacao-duplicada-nao-idempotente.md` — corresponde ao incidente do Anexo B: **"três cedentes receberam a mesma liquidação duas vezes"**, com este exato código em produção há duas semanas.
- **Impacto:** Retry de rede, duplo clique do operador, ou requisições concorrentes liquidam o mesmo recebível mais de uma vez — o fundo paga o cedente duas vezes pelo mesmo ativo. Este não é um bug ticket comum, é o tipo de bug que gera post-mortem.
- **Correção:** Checar `receivable.status !== 'SETTLED'` na mesma transação que faz o `INSERT`/`UPDATE` (idealmente com `SELECT ... FOR UPDATE` ou constraint de unicidade em `settlements(receivable_id)` como backstop), mais chave de idempotência por requisição.

---

## Consolidação

### Achados cruzados (múltiplas perspectivas no mesmo trecho)

### [Negócio > Regras de Negócio Financeiras / Boas Práticas / Segurança] Exceção da escrita de liquidação é silenciada, sem log, rollback nem trilha de auditoria
- **Severidade:** Crítica
- **Trecho:** `settlement.controller.ts:28-30` — `catch (e) { /* segue o jogo */ }`.
- **Fonte:** `boas-praticas/001-anexo-a-float-e-excecao-engolida.md` — o próprio comentário do código admite que, se o `UPDATE` falhar, o `INSERT` já foi persistido, e segue como se nada tivesse acontecido.
- **Impacto:** É o pior dos problemas do trecho: garante estado inconsistente (settlement registrado, recebível não marcado como liquidado) sem qualquer sinalização — exatamente o cenário que **agrava** o incidente de liquidação duplicada (Anexo B) descrito acima.
- **Correção:** Envolver os dois `db.query` em transação (`BEGIN`/`COMMIT`/`ROLLBACK`); no `catch`, fazer rollback explícito + logar com contexto (`receivableId`, stack) + propagar como erro HTTP — nunca "seguir o jogo".

### [Boas Práticas / Segurança] Ausência de verificação de existência do recebível antes do uso
- **Severidade:** Alta
- **Trecho:** `settlement.controller.ts:10` — `receivable.type` acessado sem checar `null`/`undefined`.
- **Impacto:** `receivableId` inexistente derruba a request com exceção não tratada (fora do `try/catch`), possível vazamento de stack trace via handler de erro padrão do Express.
- **Correção:** `if (!receivable) return res.status(404).json({ error: "Recebível não encontrado" });` logo após a busca.

### [Boas Práticas / Design de Código / Negócio > Regras de Negócio Financeiras] Uso de `number`/float para dinheiro, sem `Money`/`Currency` value object
- **Severidade:** Alta
- **Trecho:** `settlement.controller.ts:1, 11-12, 17` — `face_value`, `presentValue`, `finalAmount` todos `number`, inclusive em `Math.pow`.
- **Fonte:** `boas-praticas/001-anexo-a-float-e-excecao-engolida.md`.
- **Impacto:** Erro de ponto flutuante acumulado em cálculo financeiro encadeado pode divergir dos golden cases e gerar diferenças de caixa despercebidas até auditoria.
- **Correção:** Tipo decimal de precisão fixa (`Decimal.js`/`big.js`) desde a entrada até a persistência, encapsulado num value object `Money` com `Currency` explícito.

### [Negócio > Regras de Negócio Financeiras / Boas Práticas] `INSERT` e `UPDATE` da liquidação não são atômicos
- **Severidade:** Alta
- **Trecho:** `settlement.controller.ts:21-27` — duas queries separadas, sem transação.
- **Impacto:** Falha no `UPDATE` após `INSERT` bem-sucedido deixa settlement registrado com recebível não marcado — combinado à falta de idempotência, facilita liquidação duplicada.
- **Correção:** Ver correção do catch vazio acima — mesma transação cobre os dois problemas.

### [Boas Práticas / Negócio > Domínio] Resposta sempre HTTP 200, mesmo em falha ou inconsistência de estado
- **Severidade:** Média
- **Trecho:** `settlement.controller.ts:32`.
- **Correção:** Status HTTP deve refletir o resultado real da operação.

---

## Resumo Executivo

**Total de achados únicos (pós-merge de sobreposições): 16** *(+1 em relação à primeira versão: novo achado de ordem de arredondamento cross-currency; achado de "constantes hardcoded" foi absorvido pelo achado, agora mais preciso, de ausência de Strategy pattern)*

| Severidade | Qtde |
|---|---|
| Crítica | 5 |
| Alta | 8 |
| Média | 3 |
| Baixa | 0 |

**Críticos:** SQL Injection · Ausência de autenticação/autorização · Liquidação duplicada por falta de idempotência (incidente documentado no Anexo B) · Exceção silenciada sem log/rollback/auditoria · Magnitude de taxa 100x maior que o correto (confirmado por golden cases).

**Por macro-categoria** (achados cruzados contam nas duas):
- **Técnica** (Segurança/Boas Práticas/Design): 13 achados, incluindo 4 dos 5 críticos.
- **Negócio** (Regras Financeiras/Domínio): 8 achados, incluindo a magnitude de taxa e a liquidação duplicada — ambos agora com evidência concreta (golden cases e incidente real) em vez de suspeita.

**Veredito: bloquear merge.**

Os 5 críticos por si só já bloqueiam. Diferença chave desta segunda versão: dois achados que antes eram "suspeita, faltou contexto" (magnitude da taxa) ou "risco teórico" (liquidação duplicada) agora têm **evidência concreta e documentada** — os golden cases provam matematicamente o erro de taxa, e o Anexo B descreve um incidente real com este mesmo código em produção. Ordem de correção recomendada: parametrizar SQL → adicionar auth/ownership → transação cobrindo idempotência + rollback (resolve dois críticos de uma vez) → corrigir `BASE_RATE`/spread para os valores dos golden cases + arredondamento half-even na ordem correta → extrair Strategy/repositório/CurrencyEngine explícito.
