// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.ship.broadcast;

import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.mixer.AudioFileWriter;
import io.xj.lib.mixer.AudioFileWriterImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sound.sampled.AudioFormat;

@Service
public class BroadcastFactoryImpl implements BroadcastFactory {
  final PlaylistPublisher playlistPublisher;
  final FileStoreProvider fileStoreProvider;

  int bitrate;
  String streamBucket;
  String shipChunkContentType;
  int chunkTargetDuration;
  String tempFilePathPrefix;
  String shipMode;
  String shipFFmpegVerbosity;
  String shipChunkAudioEncoder;

  @Autowired
  public BroadcastFactoryImpl(
    PlaylistPublisher playlistPublisher,
    FileStoreProvider fileStoreProvider,
    @Value("${ship.bitrate}") int bitrate,
    @Value("${stream.bucket}") String streamBucket,
    @Value("${ship.chunk.content.type}") String shipChunkContentType,
    @Value("${chunk.duration.seconds}") int chunkTargetDuration,
    @Value("${temp.file.path.prefix}") String tempFilePathPrefix,
    @Value("${ship.mode}") String shipMode,
    @Value("${ship.mpeg.verbosity}") String shipFFmpegVerbosity,
    @Value("${ship.chunk.audio.encoder}") String shipChunkAudioEncoder
  ) {
    this.playlistPublisher = playlistPublisher;
    this.fileStoreProvider = fileStoreProvider;
    this.bitrate = bitrate;
    this.streamBucket = streamBucket;
    this.shipChunkContentType = shipChunkContentType;
    this.chunkTargetDuration = chunkTargetDuration;
    this.tempFilePathPrefix = tempFilePathPrefix;
    this.shipMode = shipMode;
    this.shipFFmpegVerbosity = shipFFmpegVerbosity;
    this.shipChunkAudioEncoder = shipChunkAudioEncoder;
  }

  @Override
  public StreamEncoder encoder(AudioFormat format, String shipKey) {
    return new StreamEncoderImpl(shipKey, format, fileStoreProvider, playlistPublisher,
      bitrate,
      streamBucket,
      shipChunkContentType,
      chunkTargetDuration,
      tempFilePathPrefix,
      shipFFmpegVerbosity,
      shipChunkAudioEncoder
    );
  }

  @Override
  public StreamPlayer player(AudioFormat format) {
    return new StreamPlayerImpl(format);
  }

  @Override
  public AudioFileWriter writer(AudioFormat format) {
    return new AudioFileWriterImpl(format);
  }
}
