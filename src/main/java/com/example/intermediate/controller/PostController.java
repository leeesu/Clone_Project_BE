package com.example.intermediate.controller;

import com.example.intermediate.controller.request.PostRequestDto;
import com.example.intermediate.controller.response.ResponseDto;
import com.example.intermediate.external.AwsS3UploadService;
import com.example.intermediate.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class PostController {

  private final PostService postService;
  private final AwsS3UploadService s3Service;


  // 게시글 작성
  @PostMapping( "/auth/products/new")
  public ResponseDto<?> createPost(@RequestPart(value = "data") PostRequestDto postRequestDto,
                                   @RequestPart("images") List<MultipartFile> multipartFiles,  // images라고 하는 건 어떰?
                                   HttpServletRequest request) {
    if (multipartFiles == null) {
      throw new NullPointerException("사진을 업로드해주세요");
    }
    List<String> imgPaths = s3Service.upload(multipartFiles);

    return postService.createPost(postRequestDto, imgPaths, request);
  }

  // 상세 게시글 가져오기
  @GetMapping( "/products/{productId}")
  public ResponseDto<?> getPost(@PathVariable Long productId) {
    return postService.getPost(productId);
  }

  // 전체 게시글 가져오기
  @GetMapping("/products")
  public ResponseDto<?> getAllPosts() {
    return postService.getAllPost();
  }

  // 조회수순 게시글 가져오기
  @GetMapping("/products/view")
  public ResponseDto<?> getAllViewPosts() {
    return postService.getAllPost();
  }


  // 게시글 수정
  @PutMapping( "/auth/products/{productId}")
  public ResponseDto<?> updatePost(@PathVariable Long productId,
                                   @RequestPart(value = "data") PostRequestDto postRequestDto,
                                   @RequestPart("image") List<MultipartFile> multipartFiles,
                                   HttpServletRequest request) {
    if (multipartFiles == null) {
      throw new NullPointerException("사진을 업로드해주세요");
    }
    List<String> imgPaths = s3Service.upload(multipartFiles);

    return postService.updatePost(productId, postRequestDto, imgPaths, request);
  }

  //게시글 삭제
  @DeleteMapping( "/auth/products/{productId}")
  public ResponseDto<?> deletePost(@PathVariable Long productId,
      HttpServletRequest request) {
    return postService.deletePost(productId, request);
  }

}
