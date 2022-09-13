package com.example.intermediate.controller;

import com.example.intermediate.controller.request.PostRequestDto;
import com.example.intermediate.controller.response.ResponseDto;
import com.example.intermediate.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
public class PostController {

  private final PostService postService;


  // 게시글 작성
  @PostMapping( "/api/auth/posts")
  public ResponseDto<?> createPost(@RequestPart(value = "title") PostRequestDto requestDto1,
                                   @RequestPart(value = "content") PostRequestDto  requestDto2,
                                   @RequestPart(value = "image" ,required = false) MultipartFile file,
                                   HttpServletRequest request) {
    return postService.createPost(requestDto1,requestDto2, request,file);
  }

  // 상세 게시글 가져오기
  @GetMapping( "/api/posts/{id}")
  public ResponseDto<?> getPost(@PathVariable Long id) {
    return postService.getPost(id);
  }

  // 전체 게시글 가져오기
  @GetMapping("/api/posts")
  public ResponseDto<?> getAllPosts() {
    return postService.getAllPost();
  }

  // 조회수순 게시글 가져오기
  @GetMapping("/api/posts/view")
  public ResponseDto<?> getAllViewPosts() {
    return postService.getAllPost();
  }




  // 게시글 수정
  @PutMapping( "/api/auth/posts/{id}")
  public ResponseDto<?> updatePost(@PathVariable Long id,
                                   @RequestPart(value = "title") PostRequestDto requestDto1,
                                   @RequestPart(value = "content") PostRequestDto  requestDto2,
                                   @RequestPart(value = "image" ,required = false) MultipartFile file,
                                   HttpServletRequest request) {
    return postService.updatePost(id, requestDto1, requestDto2, file, request);
  }

  //게시글 삭제
  @DeleteMapping( "/api/auth/posts/{id}")
  public ResponseDto<?> deletePost(@PathVariable Long id,
      HttpServletRequest request) {
    return postService.deletePost(id, request);
  }

}
