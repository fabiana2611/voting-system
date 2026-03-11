package com.votacao.assembleia.controller;

import java.util.List;
import java.util.UUID;

import com.votacao.assembleia.repository.PautaEntity;
import com.votacao.assembleia.repository.PautaRepository;
import com.votacao.assembleia.repository.SessionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/v1")
public class PautaController {

  private final PautaRepository pautaRepository;
  private final SessionRepository sessionRepository;

  public PautaController(PautaRepository pautaRepository, SessionRepository sessionRepository) {
    this.pautaRepository = pautaRepository;
    this.sessionRepository = sessionRepository;
  }

  @PostMapping(value = "/pautas", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<PautaResponse> createPauta(@RequestBody PautaRequest request) {
    String id = UUID.randomUUID().toString();
    PautaEntity pauta = new PautaEntity(id, request.name(), 0, 0);
    PautaEntity saved = pautaRepository.save(pauta);
    return ResponseEntity.status(HttpStatus.CREATED)
      .body(new PautaResponse(saved.getId(), saved.getName(), saved.getYesCount(), saved.getNoCount(), false));
  }

  @GetMapping(value = "/pautas", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<PautaResponse> listPautas() {
    return pautaRepository.findAll()
      .stream()
      .map(pauta -> new PautaResponse(
        pauta.getId(),
        pauta.getName(),
        pauta.getYesCount(),
        pauta.getNoCount(),
        sessionRepository.existsByPautaId(pauta.getId())
      ))
      .toList();
  }

  public record PautaRequest(String name) {
  }

  public record PautaResponse(String id, String name, int yesCount, int noCount, boolean hasSession) {
  }
}
