package com.example.intermediate.utils;

import com.example.intermediate.domain.Post;
import com.example.intermediate.repository.PostRepository;
import com.example.intermediate.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor // final 멤버 변수를 자동으로 생성합니다.
@Component // 스프링이 필요 시 자동으로 생성하는 클래스 목록에 추가합니다.
public class Scheduler {

    private final PostRepository postRepository;
    private final PostService postService;

    // 초, 분, 시, 일, 월, 주 순서
    // * 은 every 를 의미
    // 시각은 24시까지 있음 ex) 매일 15시 32분 -> "0 32 15 * * *"
    @Scheduled(cron = "0 0 1 * * *")
    public void organizePost() throws InterruptedException {
        System.out.println("게시글 정리 실행");
        List<Post> postList = postRepository.findAll();
        for (Post post : postList) {
            postService.organize(post.getId());
        }
    }
}