package com.maltan.community.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "recruitment_participants",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"post_id", "user_id"})
    },
    indexes = {
        @Index(name = "idx_recruitment_post_status", columnList = "post_id, status"),
        @Index(name = "idx_recruitment_user_id", columnList = "user_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecruitmentParticipant {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "post_id", nullable = false)
    private Long postId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private ParticipantStatus status = ParticipantStatus.JOINED;
    
    @CreationTimestamp
    @Column(name = "joined_at", updatable = false)
    private LocalDateTime joinedAt;
    
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    
    // 비즈니스 메서드
    public void cancel() {
        this.status = ParticipantStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
    }
    
    public void rejoin() {
        this.status = ParticipantStatus.JOINED;
        this.cancelledAt = null;
    }
    
    public boolean isJoined() {
        return this.status == ParticipantStatus.JOINED;
    }
}

