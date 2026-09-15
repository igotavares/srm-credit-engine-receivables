# Uso de IA

## Atividades em que foi utilizada

A IA foi usada para configurar o projeto, criar documentação técnica e melhorar a redação dos textos de revisão.

## Especificações e prompts estratégicos

Resumo das orientações, não transcrição de sessões anteriores:

- Configuração: apoiar a estrutura inicial e a configuração do projeto, sujeitas à validação no ambiente local.
- Documentação: descrever premissas, decisões e operação com base no que está implementado; identificar propostas futuras.
- Revisão: melhorar clareza e prioridade dos problemas, preservando impacto de negócio e correção proposta.

## Exemplo de erro e verificação

Um texto produzido com apoio de IA descrevia Transactional Outbox como implementada. A comparação com `SettlementApplicationService`, a migração e as dependências de Receivables mostrou que o fluxo persiste recebível e liquidação, sem tabela Outbox ou publicação de eventos. Outbox está documentada como evolução em `DECISIONS.md`.

O caso demonstra a necessidade de validar afirmações técnicas com código e schema antes de aceitá-las. Não há registro que permita atribuir a frase a um modelo ou sessão específicos.

## Responsabilidade humana e limites da delegação

A decisão final sobre risco cambial, arredondamento, idempotência, escopo e aceite pertence ao autor. A IA pode propor alternativas e revisar textos; o autor precisa conferir as evidências e conseguir explicar e alterar o código na defesa.

As orientações acima resumem o uso da ferramenta; não são transcrições de prompts. A aprovação das decisões e a validação dos resultados permanecem sob responsabilidade humana.
