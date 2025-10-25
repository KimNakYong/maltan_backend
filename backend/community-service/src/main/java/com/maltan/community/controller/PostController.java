package com.maltan.community.controller;

import com.maltan.community.dto.PostDto;
import com.maltan.community.dto.request.CreatePostRequest;
import com.maltan.community.dto.request.UpdatePostRequest;
import com.maltan.community.dto.response.PostListResponse;
import com.maltan.community.dto.response.PostResponse;
import com.maltan.community.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/community/posts")
@RequiredArgsConstructor
public class PostController {
    
    private final PostService postService;
    
    /**
     * 게시글 목록 조회
     */
    @GetMapping
    public ResponseEntity<PostListResponse> getPosts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String regionSi,
            @RequestParam(required = false) String regionGu,
            @RequestParam(required = false) String regionDong,
            @RequestParam(required = false) Boolean isRecruitment,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort,
            @RequestAttribute(value = "userId", required = false) Long userId
    ) {
        String[] sortParams = sort.split(",");
        Sort sortOrder = Sort.by(
            sortParams[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC,
            sortParams[0]
        );
        
        Pageable pageable = PageRequest.of(page, size, sortOrder);
        
        PostListResponse response = postService.getPosts(
            category, regionSi, regionGu, regionDong, isRecruitment, pageable, userId
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 게시글 상세 조회
     */
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> getPost(
            @PathVariable Long postId,
            @RequestAttribute(value = "userId", required = false) Long userId
    ) {
        PostDto post = postService.getPost(postId, userId);
        return ResponseEntity.ok(PostResponse.builder().post(post).build());
    }
    
    /**
     * 게시글 작성
     */
    @PostMapping
    public ResponseEntity<PostResponse> createPost(
            @Valid @RequestBody CreatePostRequest request,
            @RequestAttribute(value = "userId", required = false) Long userIdFromAttribute
    ) {
        // 임시: 인증 시스템 구현 전까지 요청 본문에서 userId 사용
        Long userId = userIdFromAttribute != null ? userIdFromAttribute : request.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("userId는 필수입니다.");
        }
        PostDto post = postService.createPost(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(PostResponse.builder().post(post).build());
    }
    
    /**
     * 게시글 수정
     */
    @PutMapping("/{postId}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody UpdatePostRequest request,
            @RequestAttribute("userId") Long userId
    ) {
        PostDto post = postService.updatePost(postId, request, userId);
        return ResponseEntity.ok(PostResponse.builder().post(post).build());
    }
    
    /**
     * 게시글 삭제
     */
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId,
            @RequestAttribute(value = "userId", required = false) Long userIdFromAttribute,
            @RequestParam(required = false) Long userId
    ) {
        // 임시: 인증 시스템 구현 전까지 쿼리 파라미터에서 userId 사용
        Long finalUserId = userIdFromAttribute != null ? userIdFromAttribute : userId;
        if (finalUserId == null) {
            throw new IllegalArgumentException("userId는 필수입니다.");
        }
        postService.deletePost(postId, finalUserId);
        return ResponseEntity.noContent().build();
    }
}

