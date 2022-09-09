package com.example.intermediate.controller;

import com.example.intermediate.controller.response.ResponseDto;
import com.example.intermediate.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Validated
@RequiredArgsConstructor
@RestController
public class LikeController {
    private final LikeService likeService;

    // 게시글 좋아요
    @PostMapping( "/api/auth/posts/like/{id}")
    public ResponseDto<?> addPostLike(@PathVariable Long id, HttpServletRequest request) {
        return likeService.addPostLike(id, request);
    }

    // 댓글 좋아요
    @PostMapping( "/api/auth/comments/like/{id}")
    public ResponseDto<?> addCommentLike(@PathVariable Long id, HttpServletRequest request) {
        return likeService.addCommentLike(id, request);
    }

}
