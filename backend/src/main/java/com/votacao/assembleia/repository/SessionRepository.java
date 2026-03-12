package com.votacao.assembleia.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SessionRepository extends JpaRepository<SessionEntity, String> {
	boolean existsByPautaId(String pautaId);

	boolean existsByPautaIdAndClosedFalse(String pautaId);

	Optional<SessionEntity> findByPautaId(String pautaId);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("delete from SessionEntity s where s.pautaId = :pautaId")
	int deleteByPautaId(@Param("pautaId") String pautaId);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("update SessionEntity s set s.yesCount = s.yesCount + 1 where s.id = :sessionId")
	int incrementYesCount(@Param("sessionId") String sessionId);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("update SessionEntity s set s.noCount = s.noCount + 1 where s.id = :sessionId")
	int incrementNoCount(@Param("sessionId") String sessionId);
}
