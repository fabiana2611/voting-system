# Backend API Documentation

Base URL: `http://localhost:8080/api/v1`

Swagger UI: `http://localhost:8080/swagger-ui/index.html`

OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## 1. Hello

### GET `/hello`

Returns a smoke-test message.

Response `200`

```json
{
  "message": "Hello World"
}
```

## 2. Pautas

### POST `/pautas`

Creates a new pauta.

Request body

```json
{
  "name": "Aprovar orcamento"
}
```

Response `201`

```json
{
  "id": "f4c930f8-8366-4e26-b8f4-57f9e6f7792a",
  "name": "Aprovar orcamento",
  "yesCount": 0,
  "noCount": 0,
  "hasSession": false
}
```

### GET `/pautas`

Lists all pautas.

Response `200`

```json
[
  {
    "id": "f4c930f8-8366-4e26-b8f4-57f9e6f7792a",
    "name": "Aprovar orcamento",
    "yesCount": 12,
    "noCount": 4,
    "hasSession": true
  }
]
```

### DELETE `/pautas/{pautaId}`

Deletes a pauta when it has no open session.

Path params

- `pautaId`: pauta identifier

Response `204`

No content.

Possible errors

- `404`: pauta not found
- `409`: pauta has an open session

## 3. Sessions

### POST `/pautas/{pautaId}/sessions`

Starts a voting session for a pauta.

Path params

- `pautaId`: pauta identifier

Response `201`

```json
{
  "sessionId": "ec31b272-8b87-4f87-a0dd-6c4e10fdc11c",
  "pautaId": "f4c930f8-8366-4e26-b8f4-57f9e6f7792a",
  "pautaName": "Aprovar orcamento",
  "openedAt": "2026-03-12T16:00:00Z",
  "closesAt": "2026-03-12T16:00:30Z",
  "closed": false,
  "yesCount": 0,
  "noCount": 0
}
```

Possible errors

- `404`: pauta not found
- `409`: session already exists for pauta

### GET `/pautas/{pautaId}/sessions/{sessionId}`

Returns session state and closes/persists it if it is expired.

Path params

- `pautaId`: pauta identifier
- `sessionId`: session identifier

Response `200`

```json
{
  "sessionId": "ec31b272-8b87-4f87-a0dd-6c4e10fdc11c",
  "pautaId": "f4c930f8-8366-4e26-b8f4-57f9e6f7792a",
  "pautaName": "Aprovar orcamento",
  "openedAt": "2026-03-12T16:00:00Z",
  "closesAt": "2026-03-12T16:00:30Z",
  "closed": true,
  "yesCount": 12,
  "noCount": 4
}
```

Possible errors

- `404`: session or pauta not found

## 4. Votes

### POST `/pautas/{pautaId}/sessions/{sessionId}/votes`

Submits a YES/NO vote in a session.

Path params

- `pautaId`: pauta identifier
- `sessionId`: session identifier

Request body

```json
{
  "userId": "678.990.942-75",
  "choice": "YES"
}
```

Response `200`

```json
{
  "sessionId": "ec31b272-8b87-4f87-a0dd-6c4e10fdc11c",
  "pautaId": "f4c930f8-8366-4e26-b8f4-57f9e6f7792a",
  "pautaName": "Aprovar orcamento",
  "openedAt": "2026-03-12T16:00:00Z",
  "closesAt": "2026-03-12T16:00:30Z",
  "closed": false,
  "yesCount": 13,
  "noCount": 4
}
```

Possible errors

- `400`: missing/invalid user
- `404`: session or pauta not found
- `409`: duplicate vote for same user/session

Response example `409`

```json
{
  "code": "DUPLICATE_CPF_VOTE",
  "message": "CPF ja votou nesta sessao."
}
```

- `410`: session already closed

Response example `410`

```json
{
  "code": "SESSION_EXPIRED",
  "message": "Sessao expirada. Nao e possivel registrar novos votos."
}
```

## Notes

- Session duration is currently 30 seconds.
- CORS is configured for `http://localhost:3000`.
- Vote duplication is prevented by database unique constraint on `(session_id, user_id)`.
