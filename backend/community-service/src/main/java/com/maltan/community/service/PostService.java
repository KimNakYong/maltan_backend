package com.maltan.community.service;

import com.maltan.community.client.UserServiceClient;
import com.maltan.community.dto.PostDto;
import com.maltan.community.dto.request.CreatePostRequest;
import com.maltan.community.dto.request.UpdatePostRequest;
import com.maltan.community.dto.response.PostListResponse;
import com.maltan.community.exception.PostNotFoundException;
import com.maltan.community.exception.UnauthorizedException;
import com.maltan.community.model.*;
import com.maltan.community.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {
    
    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;
    private final PostVoteRepository postVoteRepository;
    private final RecruitmentParticipantRepository participantRepository;
    private final UserServiceClient userServiceClient;
    
    /**
     * 게시글 목록 조회
     */
    @Transactional(readOnly = true)
    public PostListResponse getPosts(
            String category,
            String regionSi,
            String regionGu,
            String regionDong,
            Boolean isRecruitment,
            String search,
            Pageable pageable,
            Long currentUserId
    ) {
        Page<Post> postsPage = postRepository.findByFilters(
            category, regionSi, regionGu, regionDong, isRecruitment, search, pageable
        );
        
        List<PostDto> postDtos = postsPage.getContent().stream()
            .map(post -> convertToDto(post, currentUserId))
            .collect(Collectors.toList());
        
        return PostListResponse.builder()
            .content(postDtos)
            .totalElements(postsPage.getTotalElements())
            .totalPages(postsPage.getTotalPages())
            .currentPage(postsPage.getNumber())
            .pageSize(postsPage.getSize())
            .hasNext(postsPage.hasNext())
            .hasPrevious(postsPage.hasPrevious())
            .build();
    }
    
    /**
     * 게시글 상세 조회
     */
    @Transactional
    public PostDto getPost(Long postId, Long currentUserId) {
        Post post = postRepository.findByIdAndIsDeletedFalse(postId)
            .orElseThrow(() -> new PostNotFoundException(postId));
        
        // 조회수 증가
        post.incrementViewCount();
        
        return convertToDto(post, currentUserId);
    }
    
    /**
     * 게시글 작성
     */
    @Transactional
    public PostDto createPost(CreatePostRequest request, Long userId) {
        Post post = Post.builder()
            .userId(userId)
            .title(request.getTitle())
            .content(request.getContent())
            .category(request.getCategory())
            .regionSi(request.getRegionSi())
            .regionGu(request.getRegionGu())
            .regionDong(request.getRegionDong())
            .isRecruitment(request.getIsRecruitment() != null ? request.getIsRecruitment() : false)
            .recruitmentMax(request.getRecruitmentMax())
            .recruitmentDeadline(request.getRecruitmentDeadline())
            .eventDate(request.getEventDate())
            .eventLocation(request.getEventLocation())
            .latitude(request.getLatitude())
            .longitude(request.getLongitude())
            .address(request.getAddress())
            .build();
        
        Post savedPost = postRepository.save(post);
        
        // 이미지 저장
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            int order = 0;
            for (String imageUrl : request.getImages()) {
                PostImage postImage = PostImage.builder()
                    .post(savedPost)
                    .imageUrl(imageUrl)
                    .imageOrder(order++)
                    .build();
                savedPost.addImage(postImage);
            }
        }
        
        log.info("게시글 작성 완료: postId={}, userId={}", savedPost.getId(), userId);
        
        return convertToDto(savedPost, userId);
    }
    
    /**
     * 게시글 수정
     */
    @Transactional
    public PostDto updatePost(Long postId, UpdatePostRequest request, Long userId) {
        Post post = postRepository.findByIdAndIsDeletedFalse(postId)
            .orElseThrow(() -> new PostNotFoundException(postId));
        
        // 작성자 확인
        if (!post.getUserId().equals(userId)) {
            throw new UnauthorizedException("게시글을 수정할 권한이 없습니다.");
        }
        
        // 게시글 정보 업데이트
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setCategory(request.getCategory());
        
        // 모집 정보 업데이트 (모집 게시글인 경우)
        if (post.getIsRecruitment()) {
            post.setRecruitmentDeadline(request.getRecruitmentDeadline());
            post.setEventDate(request.getEventDate());
            post.setEventLocation(request.getEventLocation());
        }
        
        // 기존 이미지 삭제
        post.getImages().clear();
        
        // 새 이미지 추가
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            int order = 0;
            for (String imageUrl : request.getImages()) {
                PostImage postImage = PostImage.builder()
                    .post(post)
                    .imageUrl(imageUrl)
                    .imageOrder(order++)
                    .build();
                post.addImage(postImage);
            }
        }
        
        log.info("게시글 수정 완료: postId={}, userId={}", postId, userId);
        
        return convertToDto(post, userId);
    }
    
    /**
     * 게시글 삭제
     */
    @Transactional
    public void deletePost(Long postId, Long userId) {
        Post post = postRepository.findByIdAndIsDeletedFalse(postId)
            .orElseThrow(() -> new PostNotFoundException(postId));
        
        // 작성자 확인
        if (!post.getUserId().equals(userId)) {
            throw new UnauthorizedException("게시글을 삭제할 권한이 없습니다.");
        }
        
        // Soft Delete
        post.softDelete();
        
        log.info("게시글 삭제 완료: postId={}, userId={}", postId, userId);
    }
    
    /**
     * Entity를 DTO로 변환
     */
    private PostDto convertToDto(Post post, Long currentUserId) {
        // 이미지 URL 목록
        List<String> imageUrls = post.getImages().stream()
            .map(PostImage::getImageUrl)
            .collect(Collectors.toList());
        
        // 사용자의 투표 상태 확인
        Boolean isLiked = false;
        Boolean isDisliked = false;
        if (currentUserId != null) {
            postVoteRepository.findByPostIdAndUserId(post.getId(), currentUserId)
                .ifPresent(vote -> {
                    if (vote.getVoteType() == VoteType.LIKE) {
                        // isLiked를 true로 설정하려면 변수를 final이 아닌 다른 방식으로 처리해야 함
                    } else if (vote.getVoteType() == VoteType.DISLIKE) {
                        // isDisliked를 true로 설정
                    }
                });
        }
        
        // 모집 참여 여부 확인
        Boolean isJoined = false;
        if (post.getIsRecruitment() && currentUserId != null) {
            isJoined = participantRepository.existsByPostIdAndUserIdAndStatus(
                post.getId(), currentUserId, ParticipantStatus.JOINED
            );
        }
        
        // User Service에서 사용자 이름 조회
        String userName = userServiceClient.getUserName(post.getUserId());
        
        return PostDto.builder()
            .id(post.getId())
            .userId(post.getUserId())
            .userName(userName)
            .title(post.getTitle())
            .content(post.getContent())
            .category(post.getCategory())
            .regionSi(post.getRegionSi())
            .regionGu(post.getRegionGu())
            .regionDong(post.getRegionDong())
            .isRecruitment(post.getIsRecruitment())
            .recruitmentMax(post.getRecruitmentMax())
            .recruitmentCurrent(post.getRecruitmentCurrent())
            .recruitmentDeadline(post.getRecruitmentDeadline())
            .eventDate(post.getEventDate())
            .eventLocation(post.getEventLocation())
            .latitude(post.getLatitude())
            .longitude(post.getLongitude())
            .address(post.getAddress())
            .viewCount(post.getViewCount())
            .likeCount(post.getLikeCount())
            .dislikeCount(post.getDislikeCount())
            .commentCount(post.getCommentCount())
            .status(post.getStatus())
            .createdAt(post.getCreatedAt())
            .updatedAt(post.getUpdatedAt())
            .images(imageUrls)
            .isPinned(post.getIsPinned())
            .pinnedUntil(post.getPinnedUntil())
            .isLiked(isLiked)
            .isDisliked(isDisliked)
            .isJoined(isJoined)
            .build();
    }
}

