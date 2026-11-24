package com.aureva.transcribe.controller;

import com.aureva.transcribe.repository.AudioTranscriptionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/history")
public class HistoryController {

    private final AudioTranscriptionRepository audioTranscriptionRepository;

    public HistoryController(AudioTranscriptionRepository audioTranscriptionRepository) {
        this.audioTranscriptionRepository = audioTranscriptionRepository;
    }

    @GetMapping
    public List<HistoryResponse> getHistory() {
        return audioTranscriptionRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(item -> new HistoryResponse(
                        item.getId(),
                        item.getFileName(),
                        item.getLanguage(),
                        item.getTranscript(),
                        item.getCreatedAt()
                ))
                .toList();
    }

    // Удаление одной записи
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHistoryRecord(@PathVariable Long id) {

        if (!audioTranscriptionRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        audioTranscriptionRepository.deleteById(id);

        return ResponseEntity.noContent().build();
    }

    // Удаление выбранных записей
    @DeleteMapping("/selected")
    public ResponseEntity<Void> deleteSelectedRecords(
            @RequestBody List<Long> ids
    ) {
        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        boolean allExist = ids.stream()
                .allMatch(audioTranscriptionRepository::existsById);

        if (!allExist) {
            return ResponseEntity.notFound().build();
        }

        audioTranscriptionRepository.deleteAllByIdInBatch(ids);

        return ResponseEntity.noContent().build();
    }

    // Полная очистка истории
    @DeleteMapping
    public ResponseEntity<Void> deleteAllHistory() {

        audioTranscriptionRepository.deleteAllInBatch();

        return ResponseEntity.noContent().build();
    }

    public record HistoryResponse(
            Long id,
            String fileName,
            String language,
            String transcript,
            LocalDateTime createdAt
    ) {
    }
}
