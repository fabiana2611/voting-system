package com.votacao.assembleia.repository;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
  name = "sessions",
  uniqueConstraints = {
    @UniqueConstraint(name = "uk_sessions_pauta_id", columnNames = { "pauta_id" })
  },
  indexes = {
    @Index(name = "idx_sessions_pauta_id", columnList = "pauta_id")
  }
)
public class SessionEntity {

  @Id
  private String id;

  @Column(name = "pauta_id", nullable = false)
  private String pautaId;

  @Column(name = "pauta_name", nullable = false)
  private String pautaName;

  private Instant openedAt;

  private Instant closesAt;

  private boolean closed;

  private boolean persisted;

  private int yesCount;

  private int noCount;

  protected SessionEntity() {
  }

  public SessionEntity(
    String id,
    String pautaId,
    String pautaName,
    Instant openedAt,
    Instant closesAt,
    boolean closed,
    boolean persisted,
    int yesCount,
    int noCount
  ) {
    this.id = id;
    this.pautaId = pautaId;
    this.pautaName = pautaName;
    this.openedAt = openedAt;
    this.closesAt = closesAt;
    this.closed = closed;
    this.persisted = persisted;
    this.yesCount = yesCount;
    this.noCount = noCount;
  }

  public String getId() {
    return id;
  }

  public String getPautaId() {
    return pautaId;
  }

  public String getPautaName() {
    return pautaName;
  }

  public Instant getOpenedAt() {
    return openedAt;
  }

  public Instant getClosesAt() {
    return closesAt;
  }

  public boolean isClosed() {
    return closed;
  }

  public void setClosed(boolean closed) {
    this.closed = closed;
  }

  public boolean isPersisted() {
    return persisted;
  }

  public void setPersisted(boolean persisted) {
    this.persisted = persisted;
  }

  public int getYesCount() {
    return yesCount;
  }

  public int getNoCount() {
    return noCount;
  }

  public void addVote(VoteChoice choice) {
    if (choice == VoteChoice.YES) {
      this.yesCount += 1;
    } else if (choice == VoteChoice.NO) {
      this.noCount += 1;
    }
  }
}
