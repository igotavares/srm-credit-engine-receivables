# Post-mortem — exercício do Anexo B

**Cenário hipotético:** sexta-feira, 18h40; três cedentes relatam pagamento duplicado. Severidade proposta: SEV-1. Impacto monetário, intervalo afetado e quantidade de títulos ainda desconhecidos. Este documento é uma simulação, sem incidente real investigado.

## Resumo e hipótese causal

O Anexo A não verifica status nem garante unicidade/idempotência. Basta repetir uma requisição, mesmo depois de uma primeira liquidação totalmente bem-sucedida, para tentar inserir outro registro. Concorrência e falha parcial agravam o risco, mas não são necessárias para a duplicidade.

INSERT e UPDATE não aparecem em transação; catch vazio pode esconder falha parcial com 200. O trecho não contém transferência bancária: registros duplicados precisam ser conciliados com comprovantes externos para confirmar pagamentos duplicados.

## Linha do tempo hipotética

| Horário | Evento e grau de certeza |
| --- | --- |
| D−14 | Deploy do endpoint, conforme cenário. Falta confirmar revisão, testes e constraints existentes. |
| 18h32 | Hipótese: primeira chamada grava settlement e atualiza status; resposta se perde na rede. |
| 18h33 | Hipótese: cliente repete chamada. Mesmo status SETTLED não impede novo INSERT. |
| 18h36 | Hipótese: integração externa processa ambos os registros sem deduplicação. Essa integração não consta do Anexo A. |
| 18h40 | Dado do exercício: mesa reporta três cedentes com duplicidade. |
| 18h45 | Ação proposta: conter comandos e reprocessamentos, preservar evidências e iniciar conciliação. |
| 19h00 | Primeira atualização proposta: alcance conhecido, pagamentos confirmados e próxima atualização em 30 minutos. |

Alternativa a investigar: primeiro INSERT confirma e UPDATE falha; falso 200 oculta inconsistência. Não atribuir essa causa sem logs ou evidências de banco.

## Investigação e divisão do plantão

O Tech Lead coordena contenção, comunicação com a mesa e decisão de retomada. O dev pleno coleta consultas, logs de gateway/aplicação/banco e reproduz a falha em ambiente isolado. A mesa valida pagamentos e autoriza compensações.

Consultar, com acesso somente de leitura:

```sql
SELECT receivable_id, COUNT(*) AS quantidade
FROM settlements
GROUP BY receivable_id
HAVING COUNT(*) > 1;
```

Cruzar IDs com valores, moedas, status e comprovantes do pagador. Investigar as duas semanas desde o deploy e expandir a janela se houver evidência anterior. Contar registros duplicados não equivale a calcular prejuízo; somar valores por moeda e distinguir ordens criadas de pagamentos confirmados.

Correlacionar requests, retries, horários e timeouts. Inspecionar schema real: ausência de constraint no trecho não prova ausência no banco. O catch não registra erro; procurar logs do banco/gateway e documentar lacunas de observabilidade. Preservar cópias e trilha de acesso antes de qualquer reparação.

## Contenção imediata

Suspender endpoint e produtores/reprocessadores de liquidação; interromper ordens ainda pendentes no pagador quando possível. Manter consultas e simulação se independentes. Rollback de deploy só se a versão anterior tiver garantias verificadas. Não apagar settlements duplicados nem reexecutar ordens de resultado desconhecido.

Conciliar com a mesa e o pagador. Recuperações e compensações exigem registro próprio vinculado ao original e aprovação operacional. Comunicar alcance conhecido e incertezas, sem culpar operador ou IA.

## Correção e prevenção com responsáveis

| Ação proposta | Responsável | Prazo alvo | Evidência de conclusão |
| --- | --- | --- | --- |
| Contenção e inventário | TL + mesa | Imediato | Produtores suspensos e relação de títulos afetados. |
| Transação, unicidade, chave e controle de versão | Dev pleno + revisão TL | Antes da retomada | Testes com PostgreSQL: replay, disputa, payload divergente e rollback. |
| Tratamento de duplicatas existentes | TL + mesa | Antes de aplicar unicidade | Plano auditável de reconciliação/migração; não excluir evidências para criar índice. |
| Proteção do pagamento externo | TL + integração | Antes de reprocessar | ID estável no provedor; consulta de status em resultado desconhecido; conciliação. |
| Métricas e logs | Dev pleno | Antes da retomada | Alertas testados, IDs correlacionados sem dados sensíveis. |
| CI obrigatório e revisão financeira | TL | Próximo dia útil | Cálculo, rollback e corrida executados; teste ignorado não conta como aprovado. |

Ao introduzir eventos, gravar Outbox na mesma transação e deduplicar consumo atomicamente com seu efeito local. Isso não torna transferência externa atômica; o provedor ainda precisa de idempotência e conciliação.

## Retomada e encerramento

Retomar gradualmente após verificar invariantes no banco, correção dos erros silenciosos e teste de concorrência com causa de conflito conhecida. Monitorar tentativas duplicadas, latência, falhas e divergência de conciliação. Definir gatilho de nova suspensão se ocorrer duplicidade confirmada.

Encerrar quando o universo afetado estiver conciliado, compensações acordadas com a mesa, proteções verificadas e ações remanescentes tiverem responsável e prazo. Sem valor ou duração inventados para completar o relatório.
