# Estado da entrega

## Componentes

- **Receivables:** cadastro de recebíveis, taxas de precificação, simulação, liquidação idempotente e extrato paginado.
- **Exchange Rate:** cadastro de cotações com vigência, histórico persistido em PostgreSQL e consulta da taxa aplicável à referência. Contrato em [Exchange Rate](../exchange-rate/README.md).
- **Frontend:** pendente, incluindo painel do operador e grid de transações.
- **EDA:** proposta arquitetural; Outbox, broker e consumidores ainda não implementados.

## Pendências

| Prioridade | Pendência técnica | Próximo passo |
| --- | --- | --- |
| Alta | `SettlementConcurrencyIntegrationTest.attempt` aceita qualquer RuntimeException; a barreira começa chamadas, mas não garante que ambas leram a mesma versão. | Sincronizar a disputa após leitura, verificar conflito esperado e rollback/estado final. Testar também mesma chave simultânea. |
| Alta | `ExchangeRateHttpAdapter` não verifica vigência contra `at`. Exchange Rate seleciona a vigência correta e persiste câmbio com oito casas. | Testar cotação futura/antiga, contrato de escala e igualdade do snapshot com taxa efetivamente calculada. |
| Alta | Chamada de câmbio dentro de `SettlementApplicationService` transacional mantém conexão e lock durante retry. | Medir contenção e adotar consulta versionada com confirmação curta e revalidação. |
| Alta | Dockerfile não corresponde à pasta; Compose não inicia API; CI/linter não encontrados; IT fora de `mvn test`. | Corrigir empacotamento e tornar testes de integração críticos obrigatórios em banco descartável. |
| Alta | API sem autenticação/autorização; settlement sem proteção SQL contra edição. | Definir papéis/carteiras, credencial restrita de banco e testes de autorização/imutabilidade antes de produção. |
| Média | Alteração de SETTLED e algumas falhas de câmbio chegam como 500; retry cobre RestClientException genericamente. | Mapear conflito de domínio e indisponibilidade, evitar retry de erros permanentes. |
| Média | Frontend pendente; métricas de negócio e configuração de logs estruturados ainda não implementadas em Receivables. | Assumir cortes e seus impactos; priorizar evidências operacionais exigidas na defesa. |
| Média | Banco sem cadastro normalizado de cedentes; `assignorId` é texto externo. | Documentar origem/autorização da referência; avaliar cadastro/FK conforme escopo real. |

## Testes

172 testes de Receivables passaram, incluindo C1–C3, com agente Mockito explícito. Testes de integração `*IT`, concorrência com PostgreSQL e integração real entre Receivables e Exchange Rate ainda precisam de execução para comprovar o fluxo completo. Não há aferição de carga ou capacidade para a arquitetura de escala proposta.
