// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.ship.broadcast;

import com.google.api.client.util.Lists;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.lib.app.Environment;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.mixer.AudioSampleFormat;
import io.xj.lib.mixer.AudioStreamWriter;
import io.xj.lib.mixer.FormatException;
import io.xj.lib.mixer.OutputEncoder;
import io.xj.lib.util.CSV;
import io.xj.lib.util.Command;
import io.xj.lib.util.Values;
import io.xj.nexus.persistence.Segments;
import io.xj.ship.ShipException;
import io.xj.ship.source.SegmentAudio;
import io.xj.ship.source.SegmentAudioManager;
import io.xj.ship.source.SegmentAudioState;
import org.apache.commons.io.FileUtils;
import org.mp4parser.Container;
import org.mp4parser.muxer.FileDataSourceImpl;
import org.mp4parser.muxer.Movie;
import org.mp4parser.muxer.tracks.AACTrackImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static io.xj.lib.util.Files.getFileSize;
import static io.xj.lib.util.Values.MICROS_PER_SECOND;
import static io.xj.lib.util.Values.toEpochMicros;
import static org.joda.time.DateTimeConstants.MILLIS_PER_SECOND;

/**
 Ship broadcast via HTTP Live Streaming #179453189
 <p>
 Side lab experiments done in
 - https://github.com/charneykaye/encode-fmp4-demo
 referenced in the question I posted on Stack Overflow
 - https://stackoverflow.com/questions/69625970/java-mp4parser-to-create-a-single-m4s-fragment
 */
public class ChunkPrinterImpl implements ChunkPrinter {
  public static final int MAX_INT_LENGTH_ARRAY_SIZE = 2147483647;
  private static final Logger LOG = LoggerFactory.getLogger(ChunkPrinterImpl.class);
  private static final float QUALITY = 100;
  private static final int AUDIO_CHANNELS = 2;
  private static final int READ_BUFFER_BYTE_SIZE = 4096;
  private final AudioFormat outputFormat;
  private final Chunk chunk;
  private final ChunkFragmentConstructionMethod fragmentConstructionMethod;
  private final ChunkManager chunkManager;
  private final FileStoreProvider fileStoreProvider;
  private final SegmentAudioManager segmentAudioManager;
  private final String aacFilePath;
  private final String m4sFileName;
  private final String m4sFilePath;
  private final String mp4InitFileName;
  private final String mp4InitFilePath;
  private final String streamBucket;
  private final String tempPlaylistPath;
  private final String threadName;
  private final String wavFilePath;
  private final int bitrate;
  private final int shipChunkPrintTimeoutSeconds;
  private final int shipChunkSeconds;
  // used to support mp4box operations
  @SuppressWarnings({"unused", "FieldCanBeLocal"})
  private final String tempSegmentFilenameTemplate;

  /**
   Note: these buffers can't be constructed until after the sources are Put, ergo defining the total buffer length.
   */
  private double[][] output; // final output buffer like [frame][channel]

  @Inject
  public ChunkPrinterImpl(
    @Assisted("chunk") Chunk chunk,
    ChunkManager chunkManager,
    Environment env,
    FileStoreProvider fileStoreProvider,
    SegmentAudioManager segmentAudioManager
  ) {
    this.chunk = chunk;
    this.chunkManager = chunkManager;
    this.fileStoreProvider = fileStoreProvider;
    this.segmentAudioManager = segmentAudioManager;

    outputFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
      48000,
      32,
      2,
      8,
      48000,
      false);

    bitrate = env.getShipBitrateHigh();
    fragmentConstructionMethod = ChunkFragmentConstructionMethod.fromString(env.getShipFragmentConstructionMethod());
    String key = chunk.getKey(bitrate);

    aacFilePath = String.format("%s%s.aac", env.getTempFilePathPrefix(), key);
    m4sFileName = String.format("%s.m4s", key);
    m4sFilePath = switch (fragmentConstructionMethod) {
      case MANUAL -> String.format("%s%s.m4s", env.getTempFilePathPrefix(), key);
      case MP4BOX -> String.format("%s%s-1.m4s", env.getTempFilePathPrefix(), key);
    };

    mp4InitFileName = String.format("%s-%s-IS.mp4", chunk.getShipKey(), Values.k(bitrate));
    mp4InitFilePath = String.format("%s%s-temp_init.mp4", env.getTempFilePathPrefix(), key);
    shipChunkPrintTimeoutSeconds = env.getShipChunkPrintTimeoutSeconds();
    shipChunkSeconds = env.getShipChunkSeconds();
    streamBucket = env.getStreamBucket();
    tempPlaylistPath = String.format("%s%s-temp.mpd", env.getTempFilePathPrefix(), key);
    tempSegmentFilenameTemplate = String.format("%s-%s-temp-%%d.m4s", chunk.getShipKey(), Values.k(bitrate));
    threadName = String.format("CHUNK:%s", chunk.getKey());
    wavFilePath = String.format("%s%s.wav", env.getTempFilePathPrefix(), chunk.getKey());
  }

  @Override
  public void print() {
    final Thread currentThread = Thread.currentThread();
    final String oldName = currentThread.getName();
    currentThread.setName(threadName);
    try {
      doWork();
    } catch (ShipException e) {
      LOG.error("Failed to print!", e);
    } finally {
      currentThread.setName(oldName);
    }
  }

  @Override
  public String getWavFilePath() {
    return wavFilePath;
  }

  @Override
  public String getM4sFilePath() {
    return m4sFilePath;
  }

  @Override
  public String getMp4InitFilePath() {
    return mp4InitFilePath;
  }

  /**
   Do the work inside a named thread
   */
  private void doWork() throws ShipException {
    switch (chunk.getState()) {
      case Pending:
        break;
      case Done:
        return;
      case Encoding, Mixing, Shipping:
        if (Instant.now().minusSeconds(shipChunkPrintTimeoutSeconds).isAfter(chunk.getUpdated()))
          chunkManager.put(chunk.reset());
        break;
    }

    var audios = getAllIntersectingAudios();
    if (!areAllReady(audios)) return;

    output = new double[chunk.getTemplateConfig().getOutputFrameRate() * shipChunkSeconds][AUDIO_CHANNELS];

    // get the buffer from each audio and lay it into the output buffer
    LOG.debug("will mix source audio to buffer");
    chunkManager.put(chunk.setState(ChunkState.Mixing));
    for (var audio : audios)
      applySource(audio);

    LOG.debug("will write mixing buffer to WAV");
    try {
      var writer = new AudioStreamWriter(output, QUALITY);
      writer.writeToFile(getWavFilePath(), outputFormat, OutputEncoder.WAV, output.length);
      LOG.debug("did write {}", getWavFilePath());
    } catch (Exception e) {
      LOG.error("Failed to mix and write audio", e);
      return;
    }

    LOG.debug("will encode AAC from WAV");
    chunkManager.put(chunk.setState(ChunkState.Encoding));
    try {
      encodeAAC_ffmpeg();
      LOG.info("did encode AAC audio at {} to {}", bitrate, aacFilePath);
    } catch (Exception e) {
      LOG.error("Failed to encode AAC audio at {} to {}", bitrate, aacFilePath, e);
      return;
    }

    LOG.debug("will construct .m4s fragment from AAC");
    chunkManager.put(chunk.setState(ChunkState.Encoding));
    try {
      switch (fragmentConstructionMethod) {
        case MANUAL -> constructM4S_manually();
        case MP4BOX -> constructM4S_mp4box();
      }
      LOG.info("did construct M4S at {} to {}", bitrate, m4sFilePath);
    } catch (Exception e) {
      LOG.error("Failed to construct M4S at {} to {}", bitrate, m4sFilePath, e);
      return;
    }

    LOG.debug("will ship .m4s fragment");
    chunkManager.put(chunk.setState(ChunkState.Shipping));
    try {
      fileStoreProvider.putS3ObjectFromTempFile(m4sFilePath, streamBucket, m4sFileName);
      LOG.info("did ship {} bytes of {} audio to s3://{}/{}", getFileSize(m4sFilePath), Values.k(bitrate), streamBucket, m4sFileName);
    } catch (Exception e) {
      LOG.error("Failed to ship audio", e);
      return;
    }

    LOG.debug("will construct and ship .mp4 initialization segment if necessary");
    if (!chunkManager.isInitialized(chunk.getShipKey()))
      try {
        constructInitialMP4_mp4box();
        fileStoreProvider.putS3ObjectFromTempFile(mp4InitFilePath, streamBucket, mp4InitFileName);
        chunkManager.didInitialize(chunk.getShipKey());
        LOG.info("did ship {} bytes of {} initializer to s3://{}/{}", getFileSize(mp4InitFilePath), Values.k(bitrate), streamBucket, mp4InitFileName);
      } catch (Exception e) {
        LOG.error("Failed to ship audio", e);
        return;
      }

    chunk.addStreamOutputKey(m4sFileName);
    chunkManager.put(chunk.setState(ChunkState.Done));
  }

  /**
   apply one source to the mixing buffer

   @param source to apply
   */
  private void applySource(SegmentAudio source) throws ShipException {
    int i; // frame iterator
    int b; // bytes iterator
    int tc; // target channel iterator
    int sf = 0; // current source frame
    int tf, otf = -1; // target output buffer frame, and cache the old value in order to skip non-advanced frames (upsampling), init at -1 to force initial frame
    int ptf; // put at frame iterator (in output)
    double v; // a single sample value, and the enveloped value

    // source preroll frames (segment waveform preroll expressed in terms of source audio frame count)
    var spf = (int) (source.getSegment().getWaveformPreroll() * source.getAudioFormat().getFrameRate());

    // actual microseconds-since-epoch to begin segment audio
    var sam = toEpochMicros(Instant.parse(source.getSegment().getBeginAt())) + source.getSegment().getWaveformPreroll() * MICROS_PER_SECOND;

    // actual microseconds-since-epoch to begin this output chunk
    var oam = toEpochMicros(chunk.getFromInstant());

    // calculate the starting frame (in output) to align with frame 0 of this source audio
    // compute which frame in output to align with 0 frame of source (this may be negative if source begins before output)
    var frf = (int) Math.floor(outputFormat.getSampleRate() * (sam - oam) / MICROS_PER_SECOND);

    // ratio of target frame rate to source frame rate
    // e.g. mixing from 96hz source to 48hz target = 0.5
    var fr = outputFormat.getFrameRate() / source.getFrameRate();

    try (
      var fileInputStream = FileUtils.openInputStream(new File(source.getAbsolutePath()));
      var bufferedInputStream = new BufferedInputStream(fileInputStream);
      var audioInputStream = AudioSystem.getAudioInputStream(bufferedInputStream)
    ) {
      var frameSize = source.getFrameSize();
      var channels = source.getChannels();
      var isStereo = 2 == channels;
      var sampleSize = frameSize / channels;
      var expectBytes = audioInputStream.available();

      if (MAX_INT_LENGTH_ARRAY_SIZE <= expectBytes)
        throw new ShipException("loading audio steams longer than 2,147,483,647 frames (max. value of signed 32-bit integer) is not supported");

      int expectFrames;
      if (expectBytes == frameSize) {
        // this is a bug where AudioInputStream returns bytes (instead of frames which it claims)
        expectFrames = expectBytes / source.getFrameSize();
      } else {
        expectFrames = source.getFrameSize();
      }

      if (AudioSystem.NOT_SPECIFIED == frameSize || AudioSystem.NOT_SPECIFIED == expectFrames)
        throw new ShipException("audio streams with unspecified frame size or length are unsupported");

      AudioSampleFormat sampleFormat = AudioSampleFormat.typeOfInput(source.getAudioFormat());

      int numBytesReadToBuffer;
      byte[] sampleBuffer = new byte[source.getSampleSizeInBits() / 8];
      byte[] readBuffer = new byte[READ_BUFFER_BYTE_SIZE];
      while (-1 != (numBytesReadToBuffer = audioInputStream.read(readBuffer))) {
        for (b = 0; b < numBytesReadToBuffer; b += frameSize) {
          tf = (int) Math.floor((sf - spf) * fr); // compute the target frame (converted from source rate to target rate)
          // FUTURE: skip frame if unnecessary (source rate higher than target rate)
          for (tc = 0; tc < outputFormat.getChannels(); tc++) {
            System.arraycopy(readBuffer, b + (isStereo ? tc : 0) * sampleSize, sampleBuffer, 0, sampleSize);
            v = AudioSampleFormat.fromBytes(sampleBuffer, sampleFormat);
            for (i = otf + 1; i <= tf; i++) {
              ptf = frf + i;
              if (ptf < 0 || ptf >= output.length) continue;
              output[ptf][tc] += v;
            }
          }
          otf = tf;
          sf++;
        }
      }

    } catch (UnsupportedAudioFileException | IOException | FormatException e) {
      throw new ShipException(String.format("Failed to apply Source[%s]", source.getShipKey()), e);
    }
  }


  /*
   run ffmpeg to create the initial segment for MPEG-DASH
   *
  private void constructInitialMP4_ffmpeg() throws IOException, InterruptedException {
    Files.deleteIfExists(Path.of(mp4InitFilePath));
    execute("to construct initial MP4", List.of(
      "ffmpeg",
      "-i", getWavFilePath(),
      "-f", "hls",
      "-ac", "2",
      "-c:a", "aac",
      "-b:a", Values.k(bitrate),
      "-minrate", Values.k(bitrate),
      "-maxrate", Values.k(bitrate),
      "-start_number", String.valueOf((int) chunk.getSequenceNumber()),
      "-hls_fmp4_init_filename", mp4InitFileName,
      "-hls_segment_filename", tempSegmentFilenameTemplate,
      "-hls_segment_type", "fmp4",
      "-hls_time", "11",
      tempPlaylistPath));
  }
   */

  /**
   run ffmpeg to create the initial segment for MPEG-DASH
   */
  private void constructInitialMP4_mp4box() throws IOException, InterruptedException {
    Files.deleteIfExists(Path.of(mp4InitFilePath));
    String adjSeqNum = String.valueOf(chunk.getSequenceNumber() - 1);
    Command.execute("to construct initial MP4", List.of(
      "MP4Box",
      "-single-traf",
      "-add", aacFilePath,
      "-dash", String.valueOf(chunk.getLengthSeconds() * MILLIS_PER_SECOND),
      "-frag", String.valueOf(chunk.getLengthSeconds() * MILLIS_PER_SECOND),
      "-idx", adjSeqNum,
      "-moof-sn", adjSeqNum,
      "-out", tempPlaylistPath,
      "-profile", "live",
      "-segment-name", String.format("%s-", chunk.getKey(bitrate)),
      "-v",
      "/tmp:period=%s", adjSeqNum));
  }


  /**
   run ffmpeg for this chunk printing
   */
  private void encodeAAC_ffmpeg() throws IOException, InterruptedException {
    Files.deleteIfExists(Path.of(aacFilePath));
    Command.execute("to encode AAC", List.of(
      "ffmpeg",
      "-i", getWavFilePath(),
      "-ac", "2",
      "-c:a", "aac",
      "-b:a", Values.k(bitrate),
      "-minrate", Values.k(bitrate),
      "-maxrate", Values.k(bitrate),
      aacFilePath));
  }

  /**
   Construct the M4s manually

   @throws IOException on failure
   */
  private void constructM4S_manually() throws IOException {
    Files.deleteIfExists(Path.of(m4sFilePath));
    AACTrackImpl aacTrack = new AACTrackImpl(new FileDataSourceImpl(aacFilePath));
    Movie movie = new Movie();
    movie.addTrack(aacTrack);
    Container mp4file = new ChunkFragmentM4sBuilder(chunk.getSequenceNumber()).build(movie);
    FileChannel fc = new FileOutputStream(m4sFilePath).getChannel();
    mp4file.writeContainer(fc);
    fc.close();
  }

  /**
   Construct the M4S output with MP4Box

   @throws IOException on failure
   */
  private void constructM4S_mp4box() throws IOException, InterruptedException {
    Files.deleteIfExists(Path.of(m4sFilePath));

    // we provide the number before the one we want it to generate next
    String adjSeqNum = String.valueOf(chunk.getSequenceNumber() - 1);

    Command.execute("to construct MP4", List.of(
      "MP4Box",
      "-profile", "live",
      "-add", aacFilePath,
      "-dash", String.valueOf(chunk.getLengthSeconds() * MILLIS_PER_SECOND),
      "-frag", String.valueOf(chunk.getLengthSeconds() * MILLIS_PER_SECOND),
      "-idx", adjSeqNum,
      "-moof-sn", adjSeqNum,
      "-out", tempPlaylistPath,
      "-segment-name", String.format("%s-", chunk.getKey(bitrate)),
      "-segment-ext", "m4s",
      "-single-traf",
      "-subsegs-per-sidx", "0",
      "-daisy-chain",
      "-single-segment",
      "-v",
      "/tmp:period=%s", adjSeqNum));
  }

  /**
   Whether all the source segments for this chunk are ready

   @return true if all segments are ready
   */
  private boolean areAllReady(Collection<SegmentAudio> audios) {
    List<String> ready = Lists.newArrayList();
    List<String> notReady = Lists.newArrayList();

    if (audios.isEmpty()) {
      LOG.debug("waiting on segments");
      return false;
    }

    for (var audio : audios)
      if (SegmentAudioState.Ready.equals(audio.getState()))
        ready.add(Segments.getIdentifier(audio.getSegment()));
      else
        notReady.add(Segments.getIdentifier(audio.getSegment()));

    if (notReady.isEmpty() && Objects.equals(ready.size(), audios.size())) {
      LOG.info("will mix audio from segments {}", CSV.from(ready));
      return true;
    }

    LOG.warn("waiting on audio from segments {}", CSV.from(notReady));
    return false;
  }

  /**
   Get all the segment audios intersecting with this chunk

   @return segment audios
   */
  private Collection<SegmentAudio> getAllIntersectingAudios() {
    return segmentAudioManager.getAllIntersecting(chunk.getShipKey(), chunk.getFromInstant(), chunk.getToInstant());
  }

}
