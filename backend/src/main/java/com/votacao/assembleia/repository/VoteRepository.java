package com.votacao.assembleia.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VoteRepository extends JpaRepository<VoteEntity, String> {
  long countBySessionIdAndChoice(String sessionId, VoteChoice choice);
  boolean existsBySessionIdAndUserId(String sessionId, String userId);

  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Query("delete from VoteEntity v where v.sessionId in (select s.id from SessionEntity s where s.pautaId = :pautaId)")
  int deleteByPautaId(@Param("pautaId") String pautaId);
}
