package io.xj.nexus.audio_cache;

import javax.sound.sampled.AudioFormat;

public record CachedAudio(float[][] audio, AudioFormat format, String pathToAudioFile) {
}
