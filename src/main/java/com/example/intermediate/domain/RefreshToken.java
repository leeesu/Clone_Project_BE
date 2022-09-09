package com.example.intermediate.domain;

import javax.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.repository.Query;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity

public class RefreshToken extends Timestamped {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @JoinColumn(name = "member_id", nullable = false)
  @OneToOne(fetch = FetchType.LAZY)
  private Member member;

  @Column()
  private String value;

  public void updateValue(String token) {
    this.value = token;
  }
}
