package com.example.intermediate.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostResponseAllDto {
    private Long id;
    private String title;
    private String imgUrl;
    private String nickname;
    private int comments;
    private int likes;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
