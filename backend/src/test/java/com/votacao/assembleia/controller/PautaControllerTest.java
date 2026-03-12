package com.votacao.assembleia.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

import com.votacao.assembleia.repository.PautaEntity;
import com.votacao.assembleia.repository.PautaRepository;
import com.votacao.assembleia.repository.SessionEntity;
import com.votacao.assembleia.repository.SessionRepository;
import com.votacao.assembleia.repository.VoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Optional;

@SuppressWarnings("null")
@WebMvcTest(PautaController.class)
class PautaControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private PautaRepository pautaRepository;

  @MockitoBean
  private SessionRepository sessionRepository;

  @MockitoBean
  private VoteRepository voteRepository;

  @BeforeEach
  void setup() {
    when(pautaRepository.save(any(PautaEntity.class)))
      .thenAnswer(invocation -> invocation.getArgument(0, PautaEntity.class));
  }

  @Test
  void shouldCreatePauta() throws Exception {
    mockMvc.perform(post("/api/v1/pautas")
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .content("{\"name\":\"Pauta de Teste\"}"))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.id").isNotEmpty())
      .andExpect(jsonPath("$.name").value("Pauta de Teste"))
      .andExpect(jsonPath("$.yesCount").value(0))
      .andExpect(jsonPath("$.noCount").value(0))
      .andExpect(jsonPath("$.hasSession").value(false));
  }

  @Test
  void shouldDeletePautaWhenNoSession() throws Exception {
    when(pautaRepository.existsById("P1")).thenReturn(true);
    when(sessionRepository.findByPautaId("P1")).thenReturn(Optional.empty());
    when(sessionRepository.existsByPautaIdAndClosedFalse("P1")).thenReturn(false);

    mockMvc.perform(delete("/api/v1/pautas/P1"))
      .andExpect(status().isNoContent());

    verify(voteRepository).deleteByPautaId("P1");
    verify(sessionRepository).deleteByPautaId("P1");
    verify(pautaRepository).deleteById("P1");
  }

  @Test
  void shouldReturnConflictWhenDeletingPautaWithOpenSession() throws Exception {
    when(pautaRepository.existsById("P1")).thenReturn(true);
    SessionEntity openSession = new SessionEntity(
      "S1",
      "P1",
      "Pauta 1",
      Instant.now().minusSeconds(10),
      Instant.now().plusSeconds(10),
      false,
      false,
      1,
      0
    );
    when(sessionRepository.findByPautaId("P1")).thenReturn(Optional.of(openSession));
    when(sessionRepository.existsByPautaIdAndClosedFalse("P1")).thenReturn(true);

    mockMvc.perform(delete("/api/v1/pautas/P1"))
      .andExpect(status().isConflict());

    verify(pautaRepository, never()).deleteById("P1");
  }

  @Test
  void shouldReturnNotFoundWhenDeletingMissingPauta() throws Exception {
    when(pautaRepository.existsById("P1")).thenReturn(false);

    mockMvc.perform(delete("/api/v1/pautas/P1"))
      .andExpect(status().isNotFound());

    verify(pautaRepository, never()).deleteById("P1");
  }

  @Test
  void shouldDeletePautaWhenSessionIsExpired() throws Exception {
    when(pautaRepository.existsById("P1")).thenReturn(true);
    SessionEntity expiredSession = new SessionEntity(
      "S1",
      "P1",
      "Pauta 1",
      Instant.now().minusSeconds(120),
      Instant.now().minusSeconds(60),
      false,
      false,
      1,
      0
    );
    when(sessionRepository.findByPautaId("P1")).thenReturn(Optional.of(expiredSession));
    when(sessionRepository.existsByPautaIdAndClosedFalse("P1")).thenReturn(false);

    mockMvc.perform(delete("/api/v1/pautas/P1"))
      .andExpect(status().isNoContent());

    verify(sessionRepository).save(expiredSession);
    verify(voteRepository).deleteByPautaId("P1");
    verify(sessionRepository).deleteByPautaId("P1");
    verify(pautaRepository).deleteById("P1");
  }
}
