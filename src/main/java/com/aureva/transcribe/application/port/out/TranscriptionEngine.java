package com.aureva.transcribe.application.port.out;

import com.aureva.transcribe.domain.model.AudioInput;

public interface TranscriptionEngine {

    String getName();

    String transcribe(AudioInput audioInput, String language);
}
