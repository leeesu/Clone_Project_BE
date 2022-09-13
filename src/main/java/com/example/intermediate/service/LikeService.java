package com.example.intermediate.service;

import com.example.intermediate.controller.response.ResponseDto;
import com.example.intermediate.domain.Likes;
import com.example.intermediate.domain.Member;
import com.example.intermediate.domain.Post;
import com.example.intermediate.jwt.TokenProvider;
import com.example.intermediate.repository.LikeRepository;
import com.example.intermediate.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final PostRepository postRepository;
    private final LikeRepository likeRepository;

    private final TokenProvider tokenProvider;

    // 게시글 좋아요 기능
    @Transactional
    public ResponseDto<?> addPostLike(Long productId, HttpServletRequest request) {
        if (null == request.getHeader("RefreshToken")) {
            return ResponseDto.fail("MEMBER_NOT_FOUND",
                    "로그인이 필요합니다.");
        }

        if (null == request.getHeader("Authorization")) {
            return ResponseDto.fail("MEMBER_NOT_FOUND",
                    "로그인이 필요합니다.");
        }
        System.out.println("===============");
        Member member = validateMember(request);
        if (null == member) {
            return ResponseDto.fail("INVALID_TOKEN", "Token이 유효하지 않습니다.");
        }
        System.out.println("===============");
        Post post = isPresentPost(productId);
        if (null == post) {
            return ResponseDto.fail("NOT_FOUND", "존재하지 않는 게시글 id 입니다.");
        }

        Likes likes = isPresentLikes(post.getId(), member.getNickname());

        if (null == likes)
            likeRepository.save(
                    Likes.builder()
                            .postId(post.getId())
                            .nickname(member.getNickname())
                            .build()
            );
        else
            likeRepository.delete(likes);

        post.syncLikes(likeRepository.findAllByPostId(post.getId()).size());

        if (likes == null) {
            return ResponseDto.success("좋아요가 정상적으로 반영되었습니다.");
        } else {
            return ResponseDto.success("좋아요가 삭제되었습니다.");
        }

    }


    @Transactional(readOnly = true)
    public Post isPresentPost(Long postId) {
        Optional<Post> optionalPost = postRepository.findById(postId);
        return optionalPost.orElse(null);
    }


    public Likes isPresentLikes(Long postId, String nickname) {
        Optional<Likes> optionalLikes = likeRepository.findByPostIdAndNickname(postId,nickname);
        return optionalLikes.orElse(null);
    }


    @Transactional
    public Member validateMember(HttpServletRequest request) {
        if (!tokenProvider.validateToken(request.getHeader("RefreshToken"))) {
            return null;
        }
        return tokenProvider.getMemberFromAuthentication();
    }
}
