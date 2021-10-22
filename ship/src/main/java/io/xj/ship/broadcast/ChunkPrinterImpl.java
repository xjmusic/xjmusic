// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.ship.broadcast;

import com.google.api.client.util.Lists;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.lib.app.Environment;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.mixer.AudioStreamWriter;
import io.xj.lib.mixer.OutputEncoder;
import io.xj.lib.util.CSV;
import io.xj.lib.util.Values;
import io.xj.nexus.persistence.Segments;
import io.xj.ship.ShipException;
import io.xj.ship.source.SegmentAudio;
import io.xj.ship.source.SegmentAudioManager;
import io.xj.ship.source.SegmentAudioState;
import org.mp4parser.Container;
import org.mp4parser.muxer.FileDataSourceImpl;
import org.mp4parser.muxer.Movie;
import org.mp4parser.muxer.tracks.AACTrackImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.xj.lib.util.Files.getFileSize;
import static io.xj.lib.util.Text.formatMultiline;
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
  private static final Logger LOG = LoggerFactory.getLogger(ChunkPrinterImpl.class);
  private static final int AUDIO_CHANNELS = 2;
  private static final float QUALITY = 100;
  private final Chunk chunk;
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

  // PCM data
  private double[][] output;

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

    bitrate = env.getShipBitrateHigh();
    String key = chunk.getKey(bitrate);

    aacFilePath = String.format("%s%s.aac", env.getTempFilePathPrefix(), key);
    m4sFileName = String.format("%s.m4s", key);
    m4sFilePath = String.format("%s%s-1.m4s", env.getTempFilePathPrefix(), key);
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
  public double[][] getOutputPcmData() {
    return output;
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

    if (!isSourceAudioReady()) return;

    var audios = getAllIntersectingAudios();

    // fail if any of the source audios are not ready
    var notReady = audios.stream()
      .filter(audio -> !SegmentAudioState.Ready.equals(audio.getState()))
      .map(SegmentAudio::getId)
      .collect(Collectors.toList());
    if (!notReady.isEmpty())
      throw new ShipException(String.format("Segment%s[%s] %s not actually ready!",
        1 < notReady.size() ? "s" : "",
        CSV.from(notReady),
        1 < notReady.size() ? "are" : "is"));

    // use any segment to determine audio metadata
    // NOTE: INCONSISTENCY AMONG SOURCE AUDIO RATES WILL RESULT IN A MALFORMED OUTPUT
    var ref = audios.stream().findAny()
      .orElseThrow(() -> new ShipException("No Segment Audio found!"))
      .getAudioFormat();
    int rate = (int) ref.getSampleRate();
    int channels = ref.getChannels();
    output = new double[rate * shipChunkSeconds][AUDIO_CHANNELS];

    // get the buffer from each audio and lay it into the output buffer
    LOG.debug("will mix source audio to buffer");
    chunkManager.put(chunk.setState(ChunkState.Mixing));
    for (var audio : audios) {
      var initialSourceFrame = audio.getFrame(chunk.getFromInstant());
      for (int f = 0; f < rate * shipChunkSeconds; f++)
        for (var c = 0; c < channels; c++)
          output[f][c] += read(initialSourceFrame + f, c, audio);
    }

    LOG.debug("will write mixing buffer to WAV");
    var audioFormat = computeAudioFormat(audios);
    try {
      var writer = new AudioStreamWriter(output, QUALITY);
      writer.writeToFile(getWavFilePath(), audioFormat, OutputEncoder.WAV, output.length);
      LOG.debug("did write {}", getWavFilePath());
    } catch (Exception e) {
      LOG.error("Failed to write audio", e);
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
      constructM4S_mp4parser();
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

    LOG.debug("will ship .mp4 initialization segment if necessary");
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
    execute("to construct initial MP4", List.of(
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
    execute("to encode AAC", List.of(
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
   Check the M4S output

   @throws IOException on failure
   */
  private void constructM4S_mp4parser() throws IOException {
    Files.deleteIfExists(Path.of(m4sFilePath));
    AACTrackImpl aacTrack = new AACTrackImpl(new FileDataSourceImpl(aacFilePath));
    Movie movie = new Movie();
    movie.addTrack(aacTrack);
    Container mp4file = new ChunkFragmentM4sBuilder(
      chunk.getTemplateConfig().getOutputFrameRate(),
      chunk.getLengthSeconds(),
      chunk.getSequenceNumber(),
      chunk.getTemplateConfig().getMixerDspBufferSize()
    ).build(movie);
    FileChannel fc = new FileOutputStream(m4sFilePath).getChannel();
    mp4file.writeContainer(fc);
    fc.close();
  }

  /*
   Check the M4S output

   @throws IOException on failure
   *
  private void constructM4S_mp4box() throws IOException, InterruptedException {
    Files.deleteIfExists(Path.of(m4sFilePath));

    // we provide the number before the one we want it to generate next
    String adjSeqNum = String.valueOf(chunk.getSequenceNumber() - 1);

    execute("to construct MP4", List.of(
      "MP4Box",
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
   */

  /**
   Whether all the source segments for this chunk are ready

   @return true if all segments are ready
   */
  private boolean isSourceAudioReady() {
    List<String> ready = Lists.newArrayList();
    List<String> notReady = Lists.newArrayList();
    var audios = getAllIntersectingAudios();

    if (audios.isEmpty()) {
      LOG.warn("waiting on segments");
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

  /**
   Compute the audio format from the given set of audios

   @param audios from which to compute format
   @return audio format
   */
  private AudioFormat computeAudioFormat(Collection<SegmentAudio> audios) {
    return audios.stream().findAny().orElseThrow().getAudioFormat();
  }

  /**
   Add one frame of a segment's source audio buffer onto the output buffer

   @param sourceFrame from which to get source
   @param audio       source
   */
  private double read(int sourceFrame, int channel, SegmentAudio audio) {
    if (sourceFrame >= 0 && sourceFrame < audio.getPcmData().size())
      return audio.getPcmData().get(sourceFrame)[channel];
    else
      return 0;
  }

  /**
   Execute the given command

   @param cmdParts command parts to join (space-separated) and execute
   @throws IOException          on failure
   @throws InterruptedException on failure
   */
  private void execute(String descriptiveInfinitive, List<String> cmdParts) throws IOException, InterruptedException {
    String cmd = String.join(" ", cmdParts);
    var proc = Runtime.getRuntime().exec(cmd);
    String line;
    List<String> output = Lists.newArrayList();
    BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
    while ((line = stdError.readLine()) != null) output.add(line);
    if (0 != proc.waitFor()) {
      throw new IOException(String.format("Failed %s: %s\n\n%s", descriptiveInfinitive, cmd, formatMultiline(output.toArray())));
    }
  }
}
