package com.maltan.community.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts", indexes = {
    @Index(name = "idx_posts_user_id", columnList = "user_id"),
    @Index(name = "idx_posts_category", columnList = "category"),
    @Index(name = "idx_posts_region", columnList = "region_si, region_gu, region_dong"),
    @Index(name = "idx_posts_recruitment", columnList = "is_recruitment, recruitment_deadline"),
    @Index(name = "idx_posts_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(nullable = false, length = 50)
    private String category;  // 자유, 질문, 정보, 모임, 봉사, 운동, 취미
    
    // 지역 정보
    @Column(name = "region_si", nullable = false, length = 50)
    private String regionSi;
    
    @Column(name = "region_gu", length = 50)
    private String regionGu;
    
    @Column(name = "region_dong", length = 50)
    private String regionDong;
    
    // 모집 정보
    @Column(name = "is_recruitment")
    @Builder.Default
    private Boolean isRecruitment = false;
    
    @Column(name = "recruitment_max")
    private Integer recruitmentMax;
    
    @Column(name = "recruitment_current")
    @Builder.Default
    private Integer recruitmentCurrent = 0;
    
    @Column(name = "recruitment_deadline")
    private LocalDateTime recruitmentDeadline;
    
    @Column(name = "event_date")
    private LocalDateTime eventDate;
    
    @Column(name = "event_location", length = 200)
    private String eventLocation;
    
    // 위치 정보 (Google Maps)
    @Column(name = "latitude")
    private Double latitude;
    
    @Column(name = "longitude")
    private Double longitude;
    
    @Column(name = "address", length = 500)
    private String address;
    
    // 통계
    @Column(name = "view_count")
    @Builder.Default
    private Integer viewCount = 0;
    
    @Column(name = "like_count")
    @Builder.Default
    private Integer likeCount = 0;
    
    @Column(name = "dislike_count")
    @Builder.Default
    private Integer dislikeCount = 0;
    
    @Column(name = "comment_count")
    @Builder.Default
    private Integer commentCount = 0;
    
    // 상태
    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean isDeleted = false;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private PostStatus status = PostStatus.ACTIVE;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 연관 관계
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PostImage> images = new ArrayList<>();
    
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();
    
    // 비즈니스 메서드
    public void incrementViewCount() {
        this.viewCount++;
    }
    
    public void incrementLikeCount() {
        this.likeCount++;
    }
    
    public void decrementLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }
    
    public void incrementDislikeCount() {
        this.dislikeCount++;
    }
    
    public void decrementDislikeCount() {
        if (this.dislikeCount > 0) {
            this.dislikeCount--;
        }
    }
    
    public void incrementCommentCount() {
        this.commentCount++;
    }
    
    public void decrementCommentCount() {
        if (this.commentCount > 0) {
            this.commentCount--;
        }
    }
    
    public void incrementCurrentCount() {
        if (this.recruitmentCurrent < this.recruitmentMax) {
            this.recruitmentCurrent++;
        }
    }
    
    public void decrementCurrentCount() {
        if (this.recruitmentCurrent > 0) {
            this.recruitmentCurrent--;
        }
    }
    
    public boolean isFull() {
        return this.isRecruitment && this.recruitmentCurrent >= this.recruitmentMax;
    }
    
    public boolean isExpired() {
        return this.isRecruitment && 
               this.recruitmentDeadline != null && 
               LocalDateTime.now().isAfter(this.recruitmentDeadline);
    }
    
    public void softDelete() {
        this.isDeleted = true;
        this.status = PostStatus.DELETED;
    }
    
    public void close() {
        this.status = PostStatus.CLOSED;
    }
    
    public void addImage(PostImage image) {
        this.images.add(image);
        image.setPost(this);
    }
    
    public void removeImage(PostImage image) {
        this.images.remove(image);
        image.setPost(null);
    }
}

