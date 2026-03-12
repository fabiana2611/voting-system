# Desafio Java Especialista

## Descrição

Esse projeto tem como proposito atender ao desafio de código de Java Especialista criando uma aplicação que será usada para avaliação de conhecimento e tomada de decisões

### Applicação para votação em assembléia

Essa aplicação disponibilizará um aplicativo onde, usuários cadastrados por CPF, poderão realizar votações em assembleias.

A construçõa dessa aplicação usa como suporte [GitHub Spec Kit](https://github.com/github/spec-kit/tree/main) para especificação e implementação da aplicação.

## Arquitetura

- frontend: Aplicação Next.js que consome a API do backend.
- backend: API Java 17 com Spring Boot.

O frontend se comunica exclusivamente com o backend via REST. Persistência de dados e integrações externas devem ocorrer somente no backend.

### Exemplo inicial (Hello World)

- Endpoint backend: GET /api/hello retorna {"message": "Hello World"}.
- Página inicial do frontend busca esse endpoint na primeira carga e exibe a mensagem.

### Como executar

Backend (porta 8080):

Pré-requisito: banco PostgreSQL via Docker.

1. Acesse a raiz do projeto.
2. Execute docker compose up -d.

Isso inicia o banco em localhost:5432 com as credenciais usadas pelo backend.

1. Acesse a pasta backend.
2. Execute mvn spring-boot:run. #mvn clean spring-boot:run

Frontend (porta 3000):

1. Acesse a pasta frontend.
2. Execute npm install.
3. Execute npm run dev.

### Teste de performance

Para executar o teste de performance com 100 votos concorrentes:

1. Acesse a pasta backend.
2. Execute mvn -Dtest=SessionPerformanceTest test.

### Qualidade de codigo (Checkstyle)

O backend usa Checkstyle via Maven para analise estatica de padroes de codigo Java.

Para rodar e ver o resultado no console:

1. Acesse a pasta `backend`.
2. Execute `mvn checkstyle:check`.

Se houver violacoes, o Maven finaliza com erro e exibe no terminal o arquivo, a linha e a regra violada.

### Qualidade de codigo (SpotBugs)

O backend tambem usa SpotBugs para detectar possiveis bugs em bytecode Java.

Para rodar e ver o resultado no console:

1. Acesse a pasta `backend`.
2. Execute `mvn spotbugs:spotbugs`.

Para falhar o build quando houver bugs detectados:

1. Acesse a pasta `backend`.
2. Execute `mvn spotbugs:check`.
3. Execute `mvn spotbugs:gui`. 

## Melhorias de concorrencia no backend

Para cenarios de alta concorrencia, o backend recebeu ajustes para garantir consistencia de dados:

- Restricao unica em `votes(session_id, user_id)` para impedir voto duplicado do mesmo usuario na mesma sessao.
- Restricao unica em `sessions(pauta_id)` para impedir mais de uma sessao por pauta.
- Indices para consultas mais frequentes:
	- `idx_votes_session_id` em `votes(session_id)`
	- `idx_sessions_pauta_id` em `sessions(pauta_id)`
- Incremento atomico de votos (`YES`/`NO`) com `UPDATE` direto no banco, evitando perda de atualizacao em concorrencia.
- Tratamento de conflito por violacao de unicidade retornando HTTP `409 Conflict`.

### Impacto na API

- `POST /api/v1/pautas/{pautaId}/sessions` retorna `409` se ja existir sessao para a pauta.
- `POST /api/v1/pautas/{pautaId}/sessions/{sessionId}/votes` retorna `409` para voto duplicado (mesmo usuario na mesma sessao).
