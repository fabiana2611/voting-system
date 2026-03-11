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
2. Execute mvn spring-boot:run.

Frontend (porta 3000):

1. Acesse a pasta frontend.
2. Execute npm install.
3. Execute npm run dev.

### Teste de performance

Para executar o teste de performance com 100 votos concorrentes:

1. Acesse a pasta backend.
2. Execute mvn -Dtest=SessionPerformanceTest test.
