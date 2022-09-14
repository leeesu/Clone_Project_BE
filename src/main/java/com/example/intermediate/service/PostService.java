package com.example.intermediate.service;

import com.example.intermediate.controller.request.PostRequestDto;
import com.example.intermediate.controller.response.PostResponseAllDto;
import com.example.intermediate.controller.response.PostResponseDto;
import com.example.intermediate.controller.response.ResponseDto;
import com.example.intermediate.domain.Img;
import com.example.intermediate.domain.Member;
import com.example.intermediate.domain.Post;
import com.example.intermediate.external.AwsS3UploadService;
import com.example.intermediate.jwt.TokenProvider;
import com.example.intermediate.repository.ImgRepository;
import com.example.intermediate.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class PostService {

  private final PostRepository postRepository;
  private final ImgRepository imgRepository;
  private final TokenProvider tokenProvider;
  private final AwsS3UploadService awsS3UploadService;

  // 게시글 작성
  @Transactional
  public ResponseDto<?> createPost(PostRequestDto requestDto,
                                   List<String> imgPaths,
                                   HttpServletRequest request) {

    if (null == request.getHeader("RefreshToken")) {
      return ResponseDto.fail("MEMBER_NOT_FOUND",
              "로그인이 필요합니다.");
    }

    if (null == request.getHeader("Authorization")) {
      return ResponseDto.fail("MEMBER_NOT_FOUND",
              "로그인이 필요합니다.");
    }

    Member member = validateMember(request);
    if (null == member) {
      return ResponseDto.fail("INVALID_TOKEN", "Token이 유효하지 않습니다.");
    }

    Post post = Post.builder()
            .title(requestDto.getTitle())
            .content(requestDto.getContent())
            .price(requestDto.getPrice())
            .likes(0)
            .view(0)
            .member(member)
            .build();

    postRepository.save(post);

    postBlankCheck(imgPaths);

    List<String> imgList = new ArrayList<>();
    for (String imgUrl : imgPaths) {
      Img img = new Img(imgUrl, post);
      imgRepository.save(img);
      imgList.add(img.getImgUrl());
    }

    return ResponseDto.success(
        PostResponseDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .imgList(imgList)
                .price(post.getPrice())
                .likes(post.getLikes())
                .view(post.getView())
                .nickname(post.getMember().getNickname())
                .createdAt(post.getCreatedAt())
                .modifiedAt(post.getModifiedAt())
                .build()
    );
  }

  private void postBlankCheck(List<String> imgPaths) {
    if(imgPaths == null || imgPaths.isEmpty()){ //.isEmpty()도 되는지 확인해보기
      throw new NullPointerException("이미지를 등록해주세요(Blank Check)");
    }
  }

  // 게시글 단건 조회
  @Transactional// readOnly설정시 데이터가 Mapping되지 않는문제로 해제
  public ResponseDto<?> getPost(Long postId) {
    Post post = isPresentPost(postId);
    if (null == post) {
      return ResponseDto.fail("NOT_FOUND", "존재하지 않는 게시글 id 입니다.");
    }
    //단건조회 조회수 증가
    post.updateViewCount();

    List<Img> findImgList = imgRepository.findByPost_Id(post.getId());
    List<String> imgList = new ArrayList<>();
    for (Img img : findImgList) {
      imgList.add(img.getImgUrl());
    }

    return ResponseDto.success(
        PostResponseDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .imgList(imgList)
                .price(post.getPrice())
                .likes(post.getLikes())
                .view(post.getView())
                .nickname(post.getMember().getNickname())
                .createdAt(post.getCreatedAt())
                .modifiedAt(post.getModifiedAt())
                .build()
    );
  }

  // 전체 게시글 조회
  @Transactional(readOnly = true)
  public ResponseDto<?> getAllPost() {
    List<Post> postList = postRepository.findAllByOrderByModifiedAtDesc();
    List<PostResponseAllDto> postResponseAllDto = new ArrayList<>();

    for (Post post : postList) {
      List<Img> findImgList = imgRepository.findByPost_Id(post.getId());
      List<String> imgList = new ArrayList<>();
      for (Img img : findImgList) {
        imgList.add(img.getImgUrl());
      }

      postResponseAllDto.add(
              PostResponseAllDto.builder()
                      .id(post.getId())
                      .title(post.getTitle())
                      .imgUrl(imgList.get(0))
                      .price(post.getPrice())
                      .likes(post.getLikes())
                      .view(post.getView())
                      .nickname(post.getMember().getNickname())
                      .createdAt(post.getCreatedAt())
                      .modifiedAt(post.getModifiedAt())
                      .build()
      );
    }

    return ResponseDto.success(postResponseAllDto);

  }

  // 조회수순 정렬 게시글 조회
  @Transactional(readOnly = true)
  public ResponseDto<?> getAllViewPost() {
    List<Post> postList = postRepository.findAllByOrderByViewDesc();
    List<PostResponseAllDto> postResponseAllDto = new ArrayList<>();

    for (Post post : postList) {
      List<Img> findImgList = imgRepository.findByPost_Id(post.getId());
      List<String> imgList = new ArrayList<>();
      for (Img img : findImgList) {
        imgList.add(img.getImgUrl());
      }

      postResponseAllDto.add(
              PostResponseAllDto.builder()
                      .id(post.getId())
                      .title(post.getTitle())
                      .price(post.getPrice())
                      .imgUrl(imgList.get(0))
                      .likes(post.getLikes())
                      .view(post.getView())
                      .nickname(post.getMember().getNickname())
                      .createdAt(post.getCreatedAt())
                      .modifiedAt(post.getModifiedAt())
                      .build()
      );
    }

    return ResponseDto.success(postResponseAllDto);
  }

  // 게시글 수정
  @Transactional
  public ResponseDto<?> updatePost(Long id,
                                   PostRequestDto requestDto,
                                   List<String> imgPaths,
                                   HttpServletRequest request
                                   ) {

    if (null == request.getHeader("RefreshToken")) {
      return ResponseDto.fail("MEMBER_NOT_FOUND",
          "로그인이 필요합니다.");
    }

    if (null == request.getHeader("Authorization")) {
      return ResponseDto.fail("MEMBER_NOT_FOUND",
          "로그인이 필요합니다.");
    }

    Member member = validateMember(request);
    if (null == member) {
      return ResponseDto.fail("INVALID_TOKEN", "Token이 유효하지 않습니다.");
    }

    /// 게시글 호출
    Post post = isPresentPost(id);
    if (null == post) {
      return ResponseDto.fail("NOT_FOUND", "존재하지 않는 게시글 id 입니다.");
    }
    if (post.validateMember(member)) {
      return ResponseDto.fail("BAD_REQUEST", "작성자만 수정할 수 있습니다.");
    }

    /// 기존 이미지 삭제
    List<Img> findImgList = imgRepository.findByPost_Id(post.getId());
    List<String> imgList = new ArrayList<>();
    for (Img img : findImgList) {
      imgList.add(img.getImgUrl());
    }
    for (String imgUrl : imgList) {
      awsS3UploadService.deleteFile(AwsS3UploadService.getFileNameFromURL(imgUrl));
    }
    imgRepository.deleteByPost_Id(post.getId());

    postBlankCheck(imgPaths);

    List<String> newImgList = new ArrayList<>();
    for (String imgUrl : imgPaths) {
      Img img = new Img(imgUrl, post);
      imgRepository.save(img);
      newImgList.add(img.getImgUrl());
    }

    post.setTitle(requestDto.getTitle());
    post.setContent(requestDto.getContent());
    post.setPrice(requestDto.getPrice());

    return ResponseDto.success(
            PostResponseDto.builder()
                    .id(post.getId())
                    .title(post.getTitle())
                    .content(post.getContent())
                    .imgList(newImgList)
                    .price(post.getPrice())
                    .likes(post.getLikes())
                    .view(post.getView())
                    .nickname(post.getMember().getNickname())
                    .createdAt(post.getCreatedAt())
                    .modifiedAt(post.getModifiedAt())
                    .build()
    );
  }

  // 게시글 삭제
  @Transactional
  public ResponseDto<?> deletePost(Long id, HttpServletRequest request) {
    if (null == request.getHeader("RefreshToken")) {
      return ResponseDto.fail("MEMBER_NOT_FOUND",
          "로그인이 필요합니다.");
    }

    if (null == request.getHeader("Authorization")) {
      return ResponseDto.fail("MEMBER_NOT_FOUND",
          "로그인이 필요합니다.");
    }

    Member member = validateMember(request);
    if (null == member) {
      return ResponseDto.fail("INVALID_TOKEN", "Token이 유효하지 않습니다.");
    }

    Post post = isPresentPost(id);
    if (null == post) {
      return ResponseDto.fail("NOT_FOUND", "존재하지 않는 게시글 id 입니다.");
    }

    if (post.validateMember(member)) {
      return ResponseDto.fail("BAD_REQUEST", "작성자만 삭제할 수 있습니다.");
    }

    postRepository.delete(post);

    List<Img> findImgList = imgRepository.findByPost_Id(post.getId());
    List<String> imgList = new ArrayList<>();
    for (Img img : findImgList) {
      imgList.add(img.getImgUrl());
    }

    for (String imgUrl : imgList) {
      awsS3UploadService.deleteFile(AwsS3UploadService.getFileNameFromURL(imgUrl));
    }

    return ResponseDto.success("delete success");
  }

  //게시글 검색
  @Transactional
  public ResponseDto<?> searchPost(String keyword) {
    List<Post> posts = postRepository.findByTitleContaining(keyword);
    List<PostResponseAllDto> postResponseAllDtoList = new ArrayList<>();

    if(keyword == null)
      return ResponseDto.fail("KEYWORD_NOT_FOUND",
              "검색 결과가 존재하지 않습니다.");

    if (posts.isEmpty())
      return ResponseDto.fail("KEYWORD_NOT_FOUND",
            "검색 결과가 존재하지 않습니다.");

    for (Post post : posts) {
      postResponseAllDtoList.add(this.convertEntityToDto(post));
    }

    if (postResponseAllDtoList == null) {
      return ResponseDto.fail("KEYWORD_NOT_FOUND",
              "검색 결과가 존재하지 않습니다.");
    } else {
      return ResponseDto.success(postResponseAllDtoList);
    }
  }
private PostResponseAllDto convertEntityToDto(Post post) {
  return PostResponseAllDto.builder()
          .id(post.getId())
          .title(post.getTitle())
          .price(post.getPrice())
          .imgUrl(imgRepository.findByPost_Id(post.getId()).get(0).getImgUrl())
          .likes(post.getLikes())
          .view(post.getView())
          .nickname(post.getMember().getNickname())
          .createdAt(post.getCreatedAt())
          .modifiedAt(post.getModifiedAt())
          .build();
}


  // URL 에서 파일이름(key) 추출
  public static String getFileNameFromURL(String url) {
    return url.substring(url.lastIndexOf('/') + 1, url.length());
  }

  @Transactional(readOnly = true)
  public Post isPresentPost(Long id) {
    Optional<Post> optionalPost = postRepository.findById(id);
    return optionalPost.orElse(null);
  }

  @Transactional
  public Member validateMember(HttpServletRequest request) {
    if (!tokenProvider.validateToken(request.getHeader("RefreshToken"))) {
      return null;
    }
    return tokenProvider.getMemberFromAuthentication();
  }
}
