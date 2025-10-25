package com.maltan.community.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comments", indexes = {
    @Index(name = "idx_comments_post_id", columnList = "post_id"),
    @Index(name = "idx_comments_user_id", columnList = "user_id"),
    @Index(name = "idx_comments_parent_id", columnList = "parent_comment_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;
    
    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comment> replies = new ArrayList<>();
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "like_count")
    @Builder.Default
    private Integer likeCount = 0;
    
    @Column(name = "dislike_count")
    @Builder.Default
    private Integer dislikeCount = 0;
    
    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean isDeleted = false;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 비즈니스 메서드
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
    
    public void softDelete() {
        this.isDeleted = true;
        this.content = "삭제된 댓글입니다.";
    }
    
    public boolean isReply() {
        return this.parentComment != null;
    }
}

