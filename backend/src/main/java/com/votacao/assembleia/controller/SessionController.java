package com.votacao.assembleia.controller;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.votacao.assembleia.repository.PautaEntity;
import com.votacao.assembleia.repository.PautaRepository;
import com.votacao.assembleia.repository.SessionEntity;
import com.votacao.assembleia.repository.SessionRepository;
import com.votacao.assembleia.repository.UserRepository;
import com.votacao.assembleia.repository.VoteChoice;
import com.votacao.assembleia.repository.VoteEntity;
import com.votacao.assembleia.repository.VoteRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/v1")
@Tag(name = "Sessions", description = "Session lifecycle and voting")
public class SessionController {

  private static final Duration SESSION_DURATION = Duration.ofSeconds(30);
  private static final Logger logger = LoggerFactory.getLogger(SessionController.class);

  private final PautaRepository pautaRepository;
  private final SessionRepository sessionRepository;
  private final VoteRepository voteRepository;
  private final UserRepository userRepository;

  public SessionController(
    PautaRepository pautaRepository,
    SessionRepository sessionRepository,
    VoteRepository voteRepository,
    UserRepository userRepository
  ) {
    this.pautaRepository = pautaRepository;
    this.sessionRepository = sessionRepository;
    this.voteRepository = voteRepository;
    this.userRepository = userRepository;
  }

  @PostMapping(value = "/pautas/{pautaId}/sessions", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Start session", description = "Creates a voting session for a pauta")
  @ApiResponses(value = {
    @ApiResponse(
      responseCode = "201",
      description = "Session created",
      content = @Content(
        mediaType = "application/json",
        examples = @ExampleObject(value = "{\"sessionId\":\"ec31b272-8b87-4f87-a0dd-6c4e10fdc11c\",\"pautaId\":\"f4c930f8-8366-4e26-b8f4-57f9e6f7792a\",\"pautaName\":\"Aprovar orcamento\",\"openedAt\":\"2026-03-12T16:00:00Z\",\"closesAt\":\"2026-03-12T16:00:30Z\",\"closed\":false,\"yesCount\":0,\"noCount\":0}")
      )
    ),
    @ApiResponse(responseCode = "404", description = "Pauta not found"),
    @ApiResponse(responseCode = "409", description = "Session already exists for pauta")
  })
  public ResponseEntity<SessionResponse> startSession(@PathVariable String pautaId) {
    logger.info("POST /api/v1/pautas/{}/sessions - starting session", pautaId);
    return pautaRepository.findById(Objects.requireNonNull(pautaId))
      .map(pauta -> {
        Instant openedAt = Instant.now();
        Instant closesAt = openedAt.plus(SESSION_DURATION);
        SessionEntity session = new SessionEntity(
          UUID.randomUUID().toString(),
          pauta.getId(),
          pauta.getName(),
          openedAt,
          closesAt,
          false,
          false,
          0,
          0
        );
        try {
          SessionEntity saved = sessionRepository.saveAndFlush(session);
          logger.info(
            "POST /api/v1/pautas/{}/sessions - session created sessionId={} closesAt={}",
            pautaId,
            saved.getId(),
            saved.getClosesAt()
          );
          return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
        } catch (DataIntegrityViolationException exception) {
          logger.warn(
            "POST /api/v1/pautas/{}/sessions - conflict while creating session (already exists)",
            pautaId
          );
          return ResponseEntity.status(HttpStatus.CONFLICT).<SessionResponse>build();
        }
      })
      .orElseGet(() -> {
        logger.warn("POST /api/v1/pautas/{}/sessions - pauta not found", pautaId);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
      });
  }

  @GetMapping(value = "/pautas/{pautaId}/sessions/{sessionId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Transactional
  @Operation(summary = "Get session", description = "Returns session data and closes/persists it if expired")
  @ApiResponses(value = {
    @ApiResponse(
      responseCode = "200",
      description = "Session returned",
      content = @Content(
        mediaType = "application/json",
        examples = @ExampleObject(value = "{\"sessionId\":\"ec31b272-8b87-4f87-a0dd-6c4e10fdc11c\",\"pautaId\":\"f4c930f8-8366-4e26-b8f4-57f9e6f7792a\",\"pautaName\":\"Aprovar orcamento\",\"openedAt\":\"2026-03-12T16:00:00Z\",\"closesAt\":\"2026-03-12T16:00:30Z\",\"closed\":true,\"yesCount\":12,\"noCount\":4}")
      )
    ),
    @ApiResponse(responseCode = "404", description = "Session or pauta not found")
  })
  public ResponseEntity<SessionResponse> getSession(
    @PathVariable String pautaId,
    @PathVariable String sessionId
  ) {
    logger.info("GET /api/v1/pautas/{}/sessions/{} - fetching session", pautaId, sessionId);
    SessionEntity session = sessionRepository.findById(Objects.requireNonNull(sessionId)).orElse(null);
    if (session == null || !session.getPautaId().equals(pautaId)) {
      logger.warn("GET /api/v1/pautas/{}/sessions/{} - session not found", pautaId, sessionId);
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    closeIfExpiredAndPersist(session);
    logger.info(
      "GET /api/v1/pautas/{}/sessions/{} - returned closed={} yesCount={} noCount={}",
      pautaId,
      sessionId,
      session.isClosed(),
      session.getYesCount(),
      session.getNoCount()
    );
    return ResponseEntity.ok(toResponse(session));
  }

  @PostMapping(
    value = "/pautas/{pautaId}/sessions/{sessionId}/votes",
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @Transactional
  @Operation(summary = "Submit vote", description = "Submits a YES/NO vote for a user in a session")
  @ApiResponses(value = {
    @ApiResponse(
      responseCode = "200",
      description = "Vote accepted",
      content = @Content(
        mediaType = "application/json",
        examples = @ExampleObject(value = "{\"sessionId\":\"ec31b272-8b87-4f87-a0dd-6c4e10fdc11c\",\"pautaId\":\"f4c930f8-8366-4e26-b8f4-57f9e6f7792a\",\"pautaName\":\"Aprovar orcamento\",\"openedAt\":\"2026-03-12T16:00:00Z\",\"closesAt\":\"2026-03-12T16:00:30Z\",\"closed\":false,\"yesCount\":13,\"noCount\":4}")
      )
    ),
    @ApiResponse(responseCode = "400", description = "Invalid user or payload"),
    @ApiResponse(responseCode = "404", description = "Session or pauta not found"),
    @ApiResponse(responseCode = "409", description = "Duplicate vote for same user/session"),
    @ApiResponse(responseCode = "410", description = "Session already closed")
  })
  public ResponseEntity<?> submitVote(
    @PathVariable String pautaId,
    @PathVariable String sessionId,
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
      required = true,
      description = "Vote payload",
      content = @Content(
        mediaType = "application/json",
        examples = @ExampleObject(value = "{\"userId\":\"678.990.942-75\",\"choice\":\"YES\"}")
      )
    )
    @RequestBody VoteRequest request
  ) {
    logger.info(
      "POST /api/v1/pautas/{}/sessions/{}/votes - vote request userId={} choice={}",
      pautaId,
      sessionId,
      maskUserId(request.userId()),
      request.choice()
    );
    SessionEntity session = sessionRepository.findById(Objects.requireNonNull(sessionId)).orElse(null);
    if (session == null || !session.getPautaId().equals(pautaId)) {
      logger.warn("POST /api/v1/pautas/{}/sessions/{}/votes - session not found", pautaId, sessionId);
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    if (request.userId() == null || request.userId().isBlank()) {
      logger.warn("POST /api/v1/pautas/{}/sessions/{}/votes - invalid userId", pautaId, sessionId);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    String userId = Objects.requireNonNull(request.userId());
    if (!userRepository.existsById(userId)) {
      logger.warn(
        "POST /api/v1/pautas/{}/sessions/{}/votes - user not found userId={}",
        pautaId,
        sessionId,
        maskUserId(userId)
      );
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    closeIfExpiredAndPersist(session);
    if (session.isClosed()) {
      logger.warn("POST /api/v1/pautas/{}/sessions/{}/votes - session is closed", pautaId, sessionId);
      return ResponseEntity.status(HttpStatus.GONE)
        .body(new VoteErrorResponse("SESSION_EXPIRED", "Sessao expirada. Nao e possivel registrar novos votos."));
    }

    if (voteRepository.existsBySessionIdAndUserId(sessionId, userId)) {
      logger.warn(
        "POST /api/v1/pautas/{}/sessions/{}/votes - duplicate vote for userId={}",
        pautaId,
        sessionId,
        maskUserId(userId)
      );
      return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(new VoteErrorResponse("DUPLICATE_CPF_VOTE", "CPF ja votou nesta sessao."));
    }

    VoteEntity vote = new VoteEntity(UUID.randomUUID().toString(), sessionId, userId, request.choice());
    try {
      voteRepository.saveAndFlush(vote);
    } catch (DataIntegrityViolationException exception) {
      logger.warn(
        "POST /api/v1/pautas/{}/sessions/{}/votes - conflict while saving vote for userId={}",
        pautaId,
        sessionId,
        maskUserId(userId)
      );
      return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(new VoteErrorResponse("DUPLICATE_CPF_VOTE", "CPF ja votou nesta sessao."));
    }

    int updatedRows;
    if (request.choice() == VoteChoice.YES) {
      updatedRows = sessionRepository.incrementYesCount(sessionId);
    } else {
      updatedRows = sessionRepository.incrementNoCount(sessionId);
    }

    if (updatedRows == 0) {
      // Fallback for tests/mocks that do not execute JPQL update methods.
      session.addVote(request.choice());
      sessionRepository.save(session);
    }

    session = sessionRepository.findById(sessionId).orElse(session);

    closeIfExpiredAndPersist(session);
    logger.info(
      "POST /api/v1/pautas/{}/sessions/{}/votes - vote accepted userId={} yesCount={} noCount={} closed={}",
      pautaId,
      sessionId,
      maskUserId(userId),
      session.getYesCount(),
      session.getNoCount(),
      session.isClosed()
    );
    return ResponseEntity.ok(toResponse(session));
  }

  private String maskUserId(String userId) {
    if (userId == null || userId.isBlank()) {
      return "<empty>";
    }
    if (userId.length() <= 4) {
      return "****";
    }
    return "****" + userId.substring(userId.length() - 4);
  }

  private void closeIfExpiredAndPersist(SessionEntity session) {
    if (!session.isClosed() && Instant.now().isAfter(session.getClosesAt())) {
      session.setClosed(true);
    }

    if (session.isClosed() && !session.isPersisted()) {
      PautaEntity pauta = pautaRepository.findById(Objects.requireNonNull(session.getPautaId())).orElse(null);
      if (pauta != null) {
        pauta.addResults(session.getYesCount(), session.getNoCount());
        pautaRepository.save(pauta);
      }
      session.setPersisted(true);
    }

    sessionRepository.save(session);
  }

  private record VoteRequest(
    @Schema(description = "User identifier (CPF)", example = "678.990.942-75")
    String userId,
    @Schema(description = "Vote choice", example = "YES")
    VoteChoice choice
  ) {
  }

  private record VoteErrorResponse(
    @Schema(description = "Machine-readable error code", example = "DUPLICATE_CPF_VOTE")
    String code,
    @Schema(description = "Human-readable error message", example = "CPF ja votou nesta sessao.")
    String message
  ) {
  }

  private record SessionResponse(
    @Schema(description = "Session identifier", example = "ec31b272-8b87-4f87-a0dd-6c4e10fdc11c")
    String sessionId,
    @Schema(description = "Pauta identifier", example = "f4c930f8-8366-4e26-b8f4-57f9e6f7792a")
    String pautaId,
    @Schema(description = "Pauta name", example = "Aprovar orcamento")
    String pautaName,
    @Schema(description = "Session start time", example = "2026-03-12T16:00:00Z")
    Instant openedAt,
    @Schema(description = "Session close time", example = "2026-03-12T16:00:30Z")
    Instant closesAt,
    @Schema(description = "Whether session is closed", example = "false")
    boolean closed,
    @Schema(description = "YES vote count", example = "13")
    int yesCount,
    @Schema(description = "NO vote count", example = "4")
    int noCount
  ) {
  }

  private SessionResponse toResponse(SessionEntity session) {
    return new SessionResponse(
      session.getId(),
      session.getPautaId(),
      session.getPautaName(),
      session.getOpenedAt(),
      session.getClosesAt(),
      session.isClosed(),
      session.getYesCount(),
      session.getNoCount()
    );
  }
}
