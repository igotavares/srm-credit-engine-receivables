# Mock de câmbio

Na raiz do projeto, execute `docker compose up -d mockserver`.
A aplicação usa `http://localhost:1080` por padrão; a variável
`EXCHANGE_RATE_BASE_URL` permite substituir esse endereço.
Se a aplicação estiver na mesma rede Docker, use `http://mockserver:1080`.

O arquivo `expectations.json` é carregado ao iniciar o MockServer.
Após editá-lo, execute `docker compose restart mockserver`.
As chamadas de exemplo estão em `requests.http`, executável pelo IntelliJ HTTP Client.

Os mocks exigem o parâmetro `at` não vazio e retornam taxas fictícias fixas,
independentemente da data: USD/BRL = 5.00, BRL/USD = 0.20 e pares iguais = 1.00.
O campo `validFrom` é fixo em `2000-01-01T00:00:00`; não há simulação de histórico.
Chamadas sem correspondência recebem o 404 padrão do MockServer.
