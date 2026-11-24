package com.aureva.transcribe.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audio_transcriptions")
public class AudioTranscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false, length = 20)
    private String language;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String transcript;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected AudioTranscription() {
    }

    public AudioTranscription(String fileName, String language, String transcript) {
        this.fileName = fileName;
        this.language = language;
        this.transcript = transcript;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public String getLanguage() {
        return language;
    }

    public String getTranscript() {
        return transcript;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}