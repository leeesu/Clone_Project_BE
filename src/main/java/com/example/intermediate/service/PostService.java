package com.example.intermediate.service;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.intermediate.controller.request.PostRequestDto;
import com.example.intermediate.controller.response.PostResponseAllDto;
import com.example.intermediate.controller.response.PostResponseDto;
import com.example.intermediate.controller.response.ResponseDto;
import com.example.intermediate.domain.Member;
import com.example.intermediate.domain.Post;
import com.example.intermediate.external.AwsS3UploadService;
import com.example.intermediate.external.UploadService;
import com.example.intermediate.jwt.TokenProvider;
import com.example.intermediate.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;


@Service
@RequiredArgsConstructor
public class PostService {

  private final PostRepository postRepository;
  private final TokenProvider tokenProvider;
  private final static Logger LOG = Logger.getGlobal();
  private final UploadService s3Service;

  private final AwsS3UploadService awsS3UploadService;

  // 게시글 작성
  @Transactional
  public ResponseDto<?> createPost(PostRequestDto requestDto1,
                                   PostRequestDto requestDto2,
                                   HttpServletRequest request,
                                   MultipartFile file) {

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

    String fileName = createFileName(file.getOriginalFilename());  // 파일 이름을 유니크한 이름으로 재지정. 같은 이름의 파일을 업로드 하면 overwrite 됨
    ObjectMetadata objectMetadata = new ObjectMetadata();
    objectMetadata.setContentType(file.getContentType());
    objectMetadata.setContentLength(file.getSize());
    try (InputStream inputStream = file.getInputStream()) {
      s3Service.uploadFile(inputStream, objectMetadata, fileName);
    } catch (IOException e) {
      throw new IllegalArgumentException(String.format("파일 변환 중 에러가 발생하였습니다 (%s)", file.getOriginalFilename()));
    }
     ResponseDto.success(s3Service.getFileUrl(fileName));

    Post post = Post.builder()
        .title(requestDto1.getTitle())
        .content(requestDto2.getContent())
        .imgUrl(s3Service.getFileUrl(fileName))
        .likes(0)
        .member(member)
        .build();

    postRepository.save(post);

    return ResponseDto.success(
        PostResponseDto.builder()
            .id(post.getId())
            .title(post.getTitle())
            .content(post.getContent())
            .imgUrl(post.getImgUrl())
            .likes(post.getLikes())
            .nickname(post.getMember().getNickname())
            .createdAt(post.getCreatedAt())
            .modifiedAt(post.getModifiedAt())
            .build()
    );
  }

  // 게시글 단건 조회
  @Transactional(readOnly = true)
  public ResponseDto<?> getPost(Long id) {
    Post post = isPresentPost(id);
    if (null == post) {
      return ResponseDto.fail("NOT_FOUND", "존재하지 않는 게시글 id 입니다.");
    }

    return ResponseDto.success(
        PostResponseDto.builder()
            .id(post.getId())
            .title(post.getTitle())
            .content(post.getContent())
            .imgUrl(post.getImgUrl())
            .likes(post.getLikes())
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
      postResponseAllDto.add(
              PostResponseAllDto.builder()
                      .id(post.getId())
                      .title(post.getTitle())
                      .imgUrl(post.getImgUrl())
                      .likes(post.getLikes())
                      .nickname(post.getMember().getNickname())
                      .createdAt(post.getCreatedAt())
                      .modifiedAt(post.getModifiedAt())
                      .build()
      );
    }
    return ResponseDto.success(postResponseAllDto);
    // original code
//    return ResponseDto.success(postRepository.findAllByOrderByModifiedAtDesc());
  }

  // 게시글 수정
  @Transactional
  public ResponseDto<?> updatePost(Long id,
                                   PostRequestDto requestDto1,
                                   PostRequestDto requestDto2,
                                   MultipartFile file,
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

    /// 게시글 수정
    String fileName = createFileName(file.getOriginalFilename());  // 파일 이름을 유니크한 이름으로 재지정. 같은 이름의 파일을 업로드 하면 overwrite 됨
    ObjectMetadata objectMetadata = new ObjectMetadata();
    objectMetadata.setContentType(file.getContentType());
    objectMetadata.setContentLength(file.getSize());
    try (InputStream inputStream = file.getInputStream()) {
      s3Service.uploadFile(inputStream, objectMetadata, fileName);
    } catch (IOException e) {
      throw new IllegalArgumentException(String.format("파일 변환 중 에러가 발생하였습니다 (%s)", file.getOriginalFilename()));
    }
    ResponseDto.success(s3Service.getFileUrl(fileName));

    awsS3UploadService.deleteFile(getFileNameFromURL(post.getImgUrl()));  // 기존 파일 삭제

    post.setTitle(requestDto1.getTitle());
    post.setContent(requestDto2.getContent());
    post.setImgUrl(s3Service.getFileUrl(fileName));

    return ResponseDto.success(
            PostResponseDto.builder()
                    .id(post.getId())
                    .title(post.getTitle())
                    .content(post.getContent())
                    .imgUrl(post.getImgUrl())
                    .likes(post.getLikes())
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

    awsS3UploadService.deleteFile(getFileNameFromURL(post.getImgUrl()));

    return ResponseDto.success("delete success");
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


  private String createFileName(String originalFileName) {
    return UUID.randomUUID().toString().concat(getFileExtension(originalFileName));
  }

  private String getFileExtension(String fileName) {
    try {
      return fileName.substring(fileName.lastIndexOf("."));
    } catch (StringIndexOutOfBoundsException e) {
      throw new IllegalArgumentException(String.format("잘못된 형식의 파일 (%s) 입니다", fileName));
    }
  }

}
