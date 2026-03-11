package com.votacao.assembleia.repository;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<SessionEntity, String> {
	boolean existsByPautaId(String pautaId);
}
