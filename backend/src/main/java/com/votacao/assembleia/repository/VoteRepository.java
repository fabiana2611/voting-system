package com.votacao.assembleia.repository;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteRepository extends JpaRepository<VoteEntity, String> {
  long countBySessionIdAndChoice(String sessionId, VoteChoice choice);
  boolean existsBySessionIdAndUserId(String sessionId, String userId);
}
