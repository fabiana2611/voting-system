package com.votacao.assembleia;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.votacao.assembleia.controller.PautaController;
import com.votacao.assembleia.repository.PautaEntity;
import com.votacao.assembleia.repository.PautaRepository;
import com.votacao.assembleia.repository.SessionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.mockito.Mockito.when;

import java.util.List;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PautaController.class)
class PautaControllerListTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private PautaRepository pautaRepository;

  @MockitoBean
  private SessionRepository sessionRepository;

  @Test
  void shouldListPautas() throws Exception {
    when(pautaRepository.findAll()).thenReturn(List.of(
      new PautaEntity("P1", "Pauta A", 0, 0)
    ));
    when(sessionRepository.existsByPautaId("P1")).thenReturn(false);

    mockMvc.perform(get("/api/v1/pautas"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$[0].id").isNotEmpty())
      .andExpect(jsonPath("$[0].name").value("Pauta A"))
      .andExpect(jsonPath("$[0].yesCount").value(0))
      .andExpect(jsonPath("$[0].noCount").value(0))
      .andExpect(jsonPath("$[0].hasSession").value(false));
  }
}
