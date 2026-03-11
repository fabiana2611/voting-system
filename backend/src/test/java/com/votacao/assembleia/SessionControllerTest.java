package com.votacao.assembleia;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.votacao.assembleia.controller.SessionController;
import com.votacao.assembleia.repository.PautaEntity;
import com.votacao.assembleia.repository.PautaRepository;
import com.votacao.assembleia.repository.SessionEntity;
import com.votacao.assembleia.repository.SessionRepository;
import com.votacao.assembleia.repository.UserRepository;
import com.votacao.assembleia.repository.VoteRepository;
import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SuppressWarnings("null")
@WebMvcTest(SessionController.class)
class SessionControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private PautaRepository pautaRepository;

  @MockitoBean
  private SessionRepository sessionRepository;

  @MockitoBean
  private VoteRepository voteRepository;

  @MockitoBean
  private UserRepository userRepository;

  @Test
  void shouldRejectDuplicateVote() throws Exception {
    PautaEntity pauta = new PautaEntity("P1", "Pauta Teste", 0, 0);
    SessionEntity session = new SessionEntity(
      "S1",
      pauta.getId(),
      pauta.getName(),
      Instant.now(),
      Instant.now().plusSeconds(30),
      false,
      false,
      0,
      0
    );

    when(pautaRepository.findById(anyString())).thenReturn(java.util.Optional.of(pauta));
    when(sessionRepository.save(any(SessionEntity.class)))
      .thenAnswer(invocation -> java.util.Objects.requireNonNull(invocation.getArgument(0, SessionEntity.class)));
    when(sessionRepository.findById(anyString())).thenReturn(java.util.Optional.of(session));
    String userCpf = "678.990.942-75";
    when(userRepository.existsById(userCpf)).thenReturn(true);
    when(voteRepository.existsBySessionIdAndUserId(session.getId(), userCpf))
      .thenReturn(false, true);

    String responseBody = mockMvc.perform(post("/api/v1/pautas/" + pauta.getId() + "/sessions"))
      .andExpect(status().isCreated())
      .andReturn()
      .getResponse()
      .getContentAsString();

    Map<?, ?> payloadMap = objectMapper.readValue(responseBody, Map.class);
    String sessionId = payloadMap.get("sessionId").toString();

    String payload = "{\"userId\":\"" + userCpf + "\",\"choice\":\"YES\"}";

    mockMvc.perform(post("/api/v1/pautas/" + pauta.getId() + "/sessions/" + sessionId + "/votes")
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .content(payload))
      .andExpect(status().isOk());

    mockMvc.perform(post("/api/v1/pautas/" + pauta.getId() + "/sessions/" + sessionId + "/votes")
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .content(payload))
      .andExpect(status().isConflict());
  }
}
