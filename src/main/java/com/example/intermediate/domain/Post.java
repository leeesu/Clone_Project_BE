package com.example.intermediate.domain;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
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

  @Transient
  @OneToMany(fetch = FetchType.LAZY)
  private final List<Img> imgList = new ArrayList<>();

  @Column(nullable = false)
  private int price;

  @JoinColumn(name = "member_id", nullable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  private Member member;

  @Column(nullable = false)
  private int likes;
  //조회수
  @Column(nullable = false)
  private int view;

  // 회원정보 검증
  public boolean validateMember(Member member) {
    return !this.member.equals(member);
  }

  // 좋아요 상태 동기화
  public void syncLikes(int num) {
    this.likes = (num);
  }
  //조회수 증가
  public void updateViewCount(){
    this.view++;
  }

}
