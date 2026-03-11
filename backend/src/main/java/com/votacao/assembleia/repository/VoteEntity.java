package com.votacao.assembleia.repository;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "votes")
public class VoteEntity {

  @Id
  private String id;

  private String sessionId;

  private String userId;

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
