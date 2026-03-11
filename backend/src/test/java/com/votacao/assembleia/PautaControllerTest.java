package com.votacao.assembleia;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.votacao.assembleia.controller.PautaController;
import com.votacao.assembleia.repository.PautaEntity;
import com.votacao.assembleia.repository.PautaRepository;
import com.votacao.assembleia.repository.SessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SuppressWarnings("null")
@WebMvcTest(PautaController.class)
class PautaControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private PautaRepository pautaRepository;

  @MockitoBean
  private SessionRepository sessionRepository;

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
}
