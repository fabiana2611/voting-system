package com.votacao.assembleia.repository;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
  name = "votes",
  uniqueConstraints = {
    @UniqueConstraint(name = "uk_votes_session_user", columnNames = { "session_id", "user_id" })
  },
  indexes = {
    @Index(name = "idx_votes_session_id", columnList = "session_id")
  }
)
public class VoteEntity {

  @Id
  private String id;

  @Column(name = "session_id", nullable = false)
  private String sessionId;

  @Column(name = "user_id", nullable = false)
  private String userId;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private VoteChoice choice;

  protected VoteEntity() {
  }

  public VoteEntity(String id, String sessionId, String userId, VoteChoice choice) {
    this.id = id;
    this.sessionId = sessionId;
    this.userId = userId;
    this.choice = choice;
  }

  public String getId() {
    return id;
  }

  public String getSessionId() {
    return sessionId;
  }

  public String getUserId() {
    return userId;
  }

  public VoteChoice getChoice() {
    return choice;
  }
}
