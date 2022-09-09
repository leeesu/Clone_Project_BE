package com.example.intermediate.repository;

import com.example.intermediate.domain.Likes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Likes, Long> {

    Optional<Likes> findByPostIdAndNickname(Long Id , String nickname);
    List<Likes> findAllByPostId(Long postId);

}
