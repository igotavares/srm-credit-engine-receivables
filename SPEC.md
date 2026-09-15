# SPEC — SRM Credit Engine

## Escopo e premissas

A plataforma possui dois módulos: Receivables, responsável por recebíveis, precificação, liquidação e relatório interno; e [Exchange Rate](../exchange-rate/README.md), responsável por cadastrar e persistir cotações e consultar sua vigência. Ambos utilizam PostgreSQL. Evoluções estão em [DECISIONS.md](DECISIONS.md).

| Tema | Premissa adotada |
| --- | --- |
| Unidade de operação | Um recebível por requisição; sem lote atômico, liquidação parcial ou transferência bancária. |
| Prazo | Dias corridos entre aquisição e vencimento divididos por 30; não é a convenção formal 30/360. O prazo usa 12 casas HALF_EVEN. |
| Datas | Vencimento posterior à aquisição. Cadastro, alteração e simulação rejeitam vencimento passado; liquidação não repete essa checagem. Datas/hora sem offset são interpretadas como UTC. |
| Taxa base | Configurada por moeda do título e vigência; seeds BRL `0.01` e USD `0.004` ao mês. |
| Spread | Duplicata `0.015`; cheque `0.025`; configuração por tipo e vigência. Ambos usam Strategy `STANDARD`. Tipos são enum e exigem deploy para expansão. |
| Referência | Liquidação seleciona configurações no instante da operação; simulação aceita referência explícita. Simular não reserva taxa. |
| Câmbio | Cotação na referência da liquidação, expressa em unidades da moeda do título por unidade da moeda de pagamento; dividir o valor presente pela cotação. |
| Indisponibilidade | Falha de câmbio impede confirmação da liquidação; sem fallback silencioso. Exchange Rate retorna a cotação com maior início de vigência menor ou igual à referência. MockServer é uma alternativa para desenvolvimento isolado. |
| Idempotência | Chave global de até 120 caracteres; replay exige mesmo recebível e moeda, devolvendo resultado persistido. Outra chave não permite liquidar o título novamente. |
| Auditoria | Memória de cálculo gravada com liquidação; sem endpoint de alteração. Imutabilidade contra SQL direto ainda não é garantida. |

## Precisão e aferição

Aplicação usa `BigDecimal`; potência usa precisão de 34 dígitos, HALF_EVEN. Precisão intermediária finita não significa aritmética exata ilimitada. Valores monetários finais têm duas casas, HALF_EVEN. Em conversões, arredondar o valor presente na moeda do título antes de dividir pelo câmbio e arredondar a moeda de pagamento.

Banco: valores `NUMERIC(18,6)`, taxas/spreads e prazo `NUMERIC(18,12)`, câmbio `NUMERIC(18,8)`. A API serializa valores financeiros de resposta como strings. Limites de entrada devem respeitar essas escalas; o contrato de câmbio utiliza oito casas, tanto no Exchange Rate quanto no snapshot da liquidação.

`VP = face / (1 + taxaBase + spread)^prazo`. Aferição com taxa base de 1% e meses inteiros:

| Caso | Valor presente | Deságio BRL | Pagamento |
| --- | ---: | ---: | ---: |
| C1: duplicata, 100.000, 3 meses | 92.859,94 BRL | 7.140,06 | 92.859,94 BRL |
| C2: cheque, 25.000, 2 meses | 23.337,77 BRL | 1.662,23 | 23.337,77 BRL |
| C3: C1, câmbio 5,4321 | 92.859,94 BRL | 7.140,06 | 17.094,67 USD |

Testes: `PricingApplicationServiceTest` cobre C1–C3; `StandardPricingStrategyTest` cobre C1–C2. Relatórios antigos em `target` não substituem execução atual.

## Integridade e contratos

Liquidação atual abre transação, bloqueia a chave em `idempotency_keys`, verifica replay, calcula, atualiza recebível e insere settlement. `@Version` e unicidade de recebível/chave complementam a proteção. A chamada HTTP de câmbio ocorre dentro da transação: é uma limitação operacional consciente a resolver antes de ampliar carga.

Primeira liquidação retorna 201; replay, 200; conflitos de negócio, 422; conflitos de integridade/versão, 409. Falhas inesperadas retornam 500. Não há Outbox ou publicação de eventos implementada.

Snapshot guarda face, prazo, taxas/vigências, estratégia/método/versão, valor presente, câmbio/vigência, valor final, moedas, cedente e timestamps. Tipo e datas originais não estão copiados no settlement; hoje dependem do recebível relacionado.

## Perguntas ao negócio

1. Quem assume risco cambial e qual defasagem máxima da cotação é aceitável?
2. O prazo deve começar na aquisição ou na liquidação? Qual convenção de dias é contratual?
3. São permitidos títulos vencidos, liquidações parciais e lotes? Lote exige tudo ou nada?
4. Quem pode cadastrar taxas, liquidar e consultar cada cedente? Há dupla aprovação?
5. Como registrar estornos e comprovar o pagamento externo? Qual retenção de auditoria?
6. Qual SLO de latência, volume de pico e atraso aceitável no extrato?

## Critérios de aceite e pendências

- **Corretude:** C1–C3 ao centavo; testes de empate HALF_EVEN, datas e limites; nenhuma conta monetária binária.
- **Integridade:** retry preserva identidade e valor; disputa produz uma liquidação; falha de persistência reverte status e inserção. Teste concorrente deve verificar a causa do conflito, não aceitar qualquer exceção.
- **Usabilidade:** painel com simulação sem reload e erros por campo; extrato paginado com filtros. API implementada; frontend pendente.
- **Segurança:** SQL parametrizado, validação e erros sem detalhes internos; autenticação e autorização são requisito antes de produção, ausentes no case.
- **Desempenho:** relatório limitado a 200 itens; cálculo sem I/O na Strategy. Meta proposta: p95 de simulação BRL abaixo de 200 ms a 50 req/s, em ambiente documentado; ainda não aferida.
- **Operação:** testes críticos obrigatórios no CI, logs estruturados e métricas de liquidação após commit. Pendentes; Actuator isolado não comprova esses critérios.
