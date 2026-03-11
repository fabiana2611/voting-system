package com.votacao.assembleia.repository;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "pautas")
public class PautaEntity {

  @Id
  private String id;

  private String name;

  private int yesCount;

  private int noCount;

  protected PautaEntity() {
  }

  public PautaEntity(String id, String name, int yesCount, int noCount) {
    this.id = id;
    this.name = name;
    this.yesCount = yesCount;
    this.noCount = noCount;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public int getYesCount() {
    return yesCount;
  }

  public int getNoCount() {
    return noCount;
  }

  public void addResults(int yes, int no) {
    this.yesCount += yes;
    this.noCount += no;
  }
}
