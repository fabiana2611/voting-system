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
public class SessionController {

  private static final Duration SESSION_DURATION = Duration.ofSeconds(30);

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
  public ResponseEntity<SessionResponse> startSession(@PathVariable String pautaId) {
    if (sessionRepository.existsByPautaId(Objects.requireNonNull(pautaId))) {
      return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }
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
        SessionEntity saved = sessionRepository.save(session);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
      })
      .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }

  @GetMapping(value = "/pautas/{pautaId}/sessions/{sessionId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Transactional
  public ResponseEntity<SessionResponse> getSession(
    @PathVariable String pautaId,
    @PathVariable String sessionId
  ) {
    SessionEntity session = sessionRepository.findById(Objects.requireNonNull(sessionId)).orElse(null);
    if (session == null || !session.getPautaId().equals(pautaId)) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    closeIfExpiredAndPersist(session);
    return ResponseEntity.ok(toResponse(session));
  }

  @PostMapping(
    value = "/pautas/{pautaId}/sessions/{sessionId}/votes",
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @Transactional
  public ResponseEntity<SessionResponse> submitVote(
    @PathVariable String pautaId,
    @PathVariable String sessionId,
    @RequestBody VoteRequest request
  ) {
    SessionEntity session = sessionRepository.findById(Objects.requireNonNull(sessionId)).orElse(null);
    if (session == null || !session.getPautaId().equals(pautaId)) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    if (request.userId() == null || request.userId().isBlank()) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    String userId = Objects.requireNonNull(request.userId());
    if (!userRepository.existsById(userId)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    closeIfExpiredAndPersist(session);
    if (session.isClosed()) {
      return ResponseEntity.status(HttpStatus.GONE).body(toResponse(session));
    }

    if (voteRepository.existsBySessionIdAndUserId(sessionId, userId)) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body(toResponse(session));
    }

    VoteEntity vote = new VoteEntity(UUID.randomUUID().toString(), sessionId, userId, request.choice());
    voteRepository.save(vote);
    session.addVote(request.choice());
    sessionRepository.save(session);

    closeIfExpiredAndPersist(session);
    return ResponseEntity.ok(toResponse(session));
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

  private record VoteRequest(String userId, VoteChoice choice) {
  }

  private record SessionResponse(
    String sessionId,
    String pautaId,
    String pautaName,
    Instant openedAt,
    Instant closesAt,
    boolean closed,
    int yesCount,
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
