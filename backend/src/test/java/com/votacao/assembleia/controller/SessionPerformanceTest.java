package com.votacao.assembleia.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import com.votacao.assembleia.repository.PautaEntity;
import com.votacao.assembleia.repository.PautaRepository;
import com.votacao.assembleia.repository.SessionEntity;
import com.votacao.assembleia.repository.SessionRepository;
import com.votacao.assembleia.repository.UserRepository;
import com.votacao.assembleia.repository.VoteEntity;
import com.votacao.assembleia.repository.VoteRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SuppressWarnings("null")
@WebMvcTest(SessionController.class)
class SessionPerformanceTest {

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
  void shouldAccept100ConcurrentVotes() throws Exception {
    PautaEntity pauta = new PautaEntity("P1", "Pauta Performance", 0, 0);
    AtomicReference<SessionEntity> storedSession = new AtomicReference<>();
    Set<String> voted = ConcurrentHashMap.newKeySet();

    when(pautaRepository.findById(anyString())).thenReturn(java.util.Optional.of(pauta));
    when(sessionRepository.save(any(SessionEntity.class)))
      .thenAnswer(invocation -> {
        SessionEntity saved = invocation.getArgument(0, SessionEntity.class);
        storedSession.set(saved);
        return saved;
      });
    when(sessionRepository.saveAndFlush(any(SessionEntity.class)))
      .thenAnswer(invocation -> {
        SessionEntity saved = invocation.getArgument(0, SessionEntity.class);
        storedSession.set(saved);
        return saved;
      });
    when(sessionRepository.findById(anyString()))
      .thenAnswer(invocation -> java.util.Optional.ofNullable(storedSession.get()));

    Pattern cpfPattern = Pattern.compile("\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}");
    when(userRepository.existsById(anyString()))
      .thenAnswer(invocation -> cpfPattern.matcher(invocation.getArgument(0, String.class)).matches());

    when(voteRepository.existsBySessionIdAndUserId(anyString(), anyString()))
      .thenAnswer(invocation -> {
        String sessionId = invocation.getArgument(0, String.class);
        String userId = invocation.getArgument(1, String.class);
        return voted.contains(sessionId + ":" + userId);
      });

    when(voteRepository.save(any(VoteEntity.class)))
      .thenAnswer(invocation -> {
        VoteEntity vote = invocation.getArgument(0, VoteEntity.class);
        voted.add(vote.getSessionId() + ":" + vote.getUserId());
        return vote;
      });
    when(voteRepository.saveAndFlush(any(VoteEntity.class)))
      .thenAnswer(invocation -> {
        VoteEntity vote = invocation.getArgument(0, VoteEntity.class);
        voted.add(vote.getSessionId() + ":" + vote.getUserId());
        return vote;
      });

    String responseBody = mockMvc.perform(post("/api/v1/pautas/" + pauta.getId() + "/sessions"))
      .andExpect(status().isCreated())
      .andReturn()
      .getResponse()
      .getContentAsString();

    Map<?, ?> payloadMap = objectMapper.readValue(responseBody, Map.class);
    String sessionId = payloadMap.get("sessionId").toString();

    ExecutorService executor = Executors.newFixedThreadPool(20);
    CountDownLatch startGate = new CountDownLatch(1);
    CountDownLatch doneGate = new CountDownLatch(100);
    AtomicInteger successCount = new AtomicInteger(0);

    for (int i = 1; i <= 100; i++) {
      String userId = String.format("%03d.%03d.%03d-%02d", i % 1000, (i * 7) % 1000, (i * 13) % 1000, i % 100);
      executor.submit(() -> {
        try {
          startGate.await(5, TimeUnit.SECONDS);
          String payload = "{\"userId\":\"" + userId + "\",\"choice\":\"YES\"}";
          mockMvc.perform(post("/api/v1/pautas/" + pauta.getId() + "/sessions/" + sessionId + "/votes")
              .contentType(MediaType.APPLICATION_JSON_VALUE)
              .content(payload))
            .andExpect(status().isOk());
          successCount.incrementAndGet();
        } catch (Exception ignored) {
        } finally {
          doneGate.countDown();
        }
      });
    }

    startGate.countDown();
    doneGate.await(10, TimeUnit.SECONDS);
    executor.shutdownNow();

    if (successCount.get() != 100) {
      throw new AssertionError("Expected 100 successful votes, got " + successCount.get());
    }

    if (storedSession.get() == null) {
      throw new AssertionError("Session was not stored");
    }
  }
}
