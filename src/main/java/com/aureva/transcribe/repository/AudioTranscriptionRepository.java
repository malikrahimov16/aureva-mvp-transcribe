package com.aureva.transcribe.repository;

import com.aureva.transcribe.domain.model.AudioTranscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AudioTranscriptionRepository
        extends JpaRepository<AudioTranscription, Long> {

    List<AudioTranscription> findAllByOrderByCreatedAtDesc();
}