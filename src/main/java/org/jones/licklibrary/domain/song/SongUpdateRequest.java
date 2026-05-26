package org.jones.licklibrary.domain.song;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "song_update_request")
public class SongUpdateRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "song_id", nullable = false)
    private UUID songId;

    @Column(name = "submitter_user_id", nullable = false)
    private Long submitterUserId;

    @Column(name = "request_type", nullable = false)
    private String requestType;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String payload;

    @Column(nullable = false)
    private String status = "PENDING";

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public UUID getId() { return id; }
    public UUID getSongId() { return songId; }
    public void setSongId(UUID songId) { this.songId = songId; }
    public Long getSubmitterUserId() { return submitterUserId; }
    public void setSubmitterUserId(Long submitterUserId) { this.submitterUserId = submitterUserId; }
    public String getRequestType() { return requestType; }
    public void setRequestType(String requestType) { this.requestType = requestType; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
