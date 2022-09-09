package com.example.intermediate.domain;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Post extends Timestamped {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private String content;

  @Column(nullable = false)
  private String imgUrl;

  @JoinColumn(name = "member_id", nullable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  private Member member;

  @Column(nullable = false)
  private int likes;

  // 회원정보 검증
  public boolean validateMember(Member member) {
    return !this.member.equals(member);
  }

  // 좋아요 상태 동기화
  public void syncLikes(int num) {
    this.likes = (num);
  }

}