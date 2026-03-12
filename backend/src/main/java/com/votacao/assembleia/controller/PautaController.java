package com.votacao.assembleia.controller;

import java.util.List;
import java.util.UUID;
import java.time.Instant;

import com.votacao.assembleia.repository.PautaEntity;
import com.votacao.assembleia.repository.PautaRepository;
import com.votacao.assembleia.repository.SessionEntity;
import com.votacao.assembleia.repository.SessionRepository;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/v1")
@Tag(name = "Pautas", description = "Pauta management")
public class PautaController {

  private static final Logger logger = LoggerFactory.getLogger(PautaController.class);

  private final PautaRepository pautaRepository;
  private final SessionRepository sessionRepository;
  private final VoteRepository voteRepository;

  public PautaController(
    PautaRepository pautaRepository,
    SessionRepository sessionRepository,
    VoteRepository voteRepository
  ) {
    this.pautaRepository = pautaRepository;
    this.sessionRepository = sessionRepository;
    this.voteRepository = voteRepository;
  }

  @PostMapping(value = "/pautas", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Create pauta", description = "Creates a new pauta with zeroed vote counters")
  @ApiResponses(value = {
    @ApiResponse(
      responseCode = "201",
      description = "Pauta created",
      content = @Content(
        mediaType = "application/json",
        examples = @ExampleObject(value = "{\"id\":\"f4c930f8-8366-4e26-b8f4-57f9e6f7792a\",\"name\":\"Aprovar orcamento\",\"yesCount\":0,\"noCount\":0,\"hasSession\":false}")
      )
    )
  })
  public ResponseEntity<PautaResponse> createPauta(@RequestBody PautaRequest request) {
    logger.info("POST /api/v1/pautas - creating pauta with name={}", request.name());
    String id = UUID.randomUUID().toString();
    PautaEntity pauta = new PautaEntity(id, request.name(), 0, 0);
    PautaEntity saved = pautaRepository.save(pauta);
    logger.info("POST /api/v1/pautas - created pautaId={}", saved.getId());
    return ResponseEntity.status(HttpStatus.CREATED)
      .body(new PautaResponse(saved.getId(), saved.getName(), saved.getYesCount(), saved.getNoCount(), false));
  }

  @GetMapping(value = "/pautas", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "List pautas", description = "Lists all pautas and whether each has an opened/created session")
  @ApiResponses(value = {
    @ApiResponse(
      responseCode = "200",
      description = "Pautas returned",
      content = @Content(
        mediaType = "application/json",
        examples = @ExampleObject(value = "[{\"id\":\"f4c930f8-8366-4e26-b8f4-57f9e6f7792a\",\"name\":\"Aprovar orcamento\",\"yesCount\":12,\"noCount\":4,\"hasSession\":true}]")
      )
    )
  })
  public List<PautaResponse> listPautas() {
    List<PautaResponse> response = pautaRepository.findAll()
      .stream()
      .map(pauta -> new PautaResponse(
        pauta.getId(),
        pauta.getName(),
        pauta.getYesCount(),
        pauta.getNoCount(),
        sessionRepository.existsByPautaId(pauta.getId())
      ))
      .toList();
      logger.info("GET /api/v1/pautas - returned {} pautas", response.size());
      return response;
  }

  @DeleteMapping(value = "/pautas/{pautaId}")
  @Transactional
  @Operation(summary = "Delete pauta", description = "Deletes a pauta when it has no open session")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "204", description = "Pauta deleted"),
    @ApiResponse(responseCode = "404", description = "Pauta not found"),
    @ApiResponse(responseCode = "409", description = "Pauta has an open session and cannot be deleted")
  })
  public ResponseEntity<Void> deletePauta(@PathVariable String pautaId) {
    logger.info("DELETE /api/v1/pautas/{} - deleting pauta", pautaId);

    if (!pautaRepository.existsById(pautaId)) {
      logger.warn("DELETE /api/v1/pautas/{} - pauta not found", pautaId);
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    SessionEntity session = sessionRepository.findByPautaId(pautaId).orElse(null);
    if (session != null && !session.isClosed() && Instant.now().isAfter(session.getClosesAt())) {
      session.setClosed(true);
      sessionRepository.save(session);
      logger.info("DELETE /api/v1/pautas/{} - auto-closed expired session", pautaId);
    }

    if (sessionRepository.existsByPautaIdAndClosedFalse(pautaId)) {
      logger.warn("DELETE /api/v1/pautas/{} - conflict: pauta has open session", pautaId);
      return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    // Remove dependent data first so pauta delete succeeds when a DONE session exists.
    voteRepository.deleteByPautaId(pautaId);
    sessionRepository.deleteByPautaId(pautaId);
    pautaRepository.deleteById(pautaId);
    logger.info("DELETE /api/v1/pautas/{} - pauta deleted", pautaId);
    return ResponseEntity.noContent().build();
  }

  public record PautaRequest(
    @Schema(description = "Pauta name", example = "Aprovar orcamento")
    String name
  ) {
  }

  public record PautaResponse(
    @Schema(description = "Pauta identifier", example = "f4c930f8-8366-4e26-b8f4-57f9e6f7792a")
    String id,
    @Schema(description = "Pauta name", example = "Aprovar orcamento")
    String name,
    @Schema(description = "Accumulated YES votes", example = "12")
    int yesCount,
    @Schema(description = "Accumulated NO votes", example = "4")
    int noCount,
    @Schema(description = "Indicates whether pauta has an active/created session", example = "true")
    boolean hasSession
  ) {
  }
}
