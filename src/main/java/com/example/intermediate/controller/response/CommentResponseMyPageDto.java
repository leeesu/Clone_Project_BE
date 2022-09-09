package com.example.intermediate.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponseMyPageDto {
    private Long id;
    private String nickname;
    private String content;
    private int likes;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
