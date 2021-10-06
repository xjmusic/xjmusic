package io.xj.ship.persistence;

import com.jcraft.jogg.Packet;
import com.jcraft.jogg.Page;
import com.jcraft.jogg.StreamState;
import com.jcraft.jogg.SyncState;
import com.jcraft.jorbis.Block;
import com.jcraft.jorbis.Comment;
import com.jcraft.jorbis.DspState;
import com.jcraft.jorbis.Info;
import io.xj.api.Segment;
import io.xj.lib.util.Values;
import io.xj.ship.ShipException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import static io.xj.lib.util.Values.MICROS_IN_A_SECOND;

/**
 * An HTTP Live Streaming Media Segment
 * <p>
 * SEE: https://en.m.wikipedia.org/wiki/HTTP_Live_Streaming
 * <p>
 * SEE: https://developer.apple.com/documentation/http_live_streaming/hls_authoring_specification_for_apple_devices
 * <p>
 * Ship broadcast via HTTP Live Streaming #179453189
 */
public class SegmentAudio {
  private static final Logger LOG = LoggerFactory.getLogger(SegmentAudio.class);

  private final Segment segment;

  private SegmentAudioState state;
  // We need a buffer, its size, a count to know how many bytes we have read and an index to keep track of where we are.
  // This is standard networking stuff used with read().

  byte[] buffer = null;
  int bufferSize = 2048;
  int count = 0;
  int index = 0;
  // Fields for the converted buffer.
  // This is a buffer that is modified in regard to the number of audio channels.
  // Naturally, it will also need a size.

  byte[] convertedBuffer;
  int convertedBufferSize;
  // A three-dimensional an array with PCM information.

  private float[][][] pcmBuffer;

  // The index for the PCM information.
  private int[] pcmIndex;

  // Here are the four required JOgg objects...
  private final Packet oggPacket = new Packet();

  private final Page oggPage = new Page();
  private final StreamState oggStreamState = new StreamState();
  private final SyncState oggSyncState = new SyncState();
  // ... followed by the four required OGG/Vorbis objects.
  private final DspState vorbisDspState = new DspState();

  private final Block vorbisBlock = new Block(vorbisDspState);
  private final Comment vorbisComment = new Comment();

  private final Info vorbisInfo = new Info();
  private InputStream inputStream;
  private final String shipKey;

  public SegmentAudio(String shipKey, Segment segment) {
    this.shipKey = shipKey;
    this.segment = segment;
    state = SegmentAudioState.Pending;
  }

  public static SegmentAudio from(Segment segment, String shipKey) {
    return new SegmentAudio(shipKey, segment);
  }

  /**
   * Get metadata about the original OGG/Vorbis audio
   *
   * @return info
   */
  public Info getInfo() {
    return vorbisInfo;
  }

  /**
   * Get the underlying PCM audio buffer
   *
   * @return PCM audio data
   */
  public float[][][] getPcmBuffer() {
    return pcmBuffer;
  }

  /**
   * Whether this segment intersects the specified ship key, from instant, and to instant
   *
   * @param shipKey to test for intersection
   * @param from    to test for intersection
   * @param to      to test for intersection
   * @return true if the ship key matches, fromInstant is before the segment end, and toInstant is after the segment beginning
   */
  public boolean intersects(String shipKey, Instant from, Instant to) {
    if (!Objects.equals(shipKey, this.shipKey)) return false;
    return from.isBefore(Instant.parse(segment.getEndAt())) && to.isAfter(Instant.parse(segment.getBeginAt()));
  }

  /**
   * Load OGG/Vorbis audio data into PCM buffer
   *
   * @param inputStream from which to load
   */
  public void loadOggVorbis(InputStream inputStream) {
    try {
      this.inputStream = inputStream;

      initializeOggReader();

      // If we can read the header, we try to initialize the sound system.
      // If we could initialize the sound system, we try to read the body.
      if (readHeader()) {
        if (initializeSound()) {
          readBody();
        }
      }

      // Afterwards, we clean up.
      cleanUp();
      state = SegmentAudioState.Ready;
    } catch (Exception e) {
      state = SegmentAudioState.Failed;
    }
  }

  public SegmentAudioState getState() {
    return state;
  }

  public void setState(SegmentAudioState state) {
    this.state = state;
  }

  public UUID getId() {
    return segment.getId();
  }

  public Segment getSegment() {
    return segment;
  }

  /**
   * Initializes OGG/Vorbis Reader.
   * <p>
   * First, we initialize the <code>SyncState</code>
   * object. After that, we prepare the <code>SyncState</code> buffer.
   * <p>
   * Then we "initialize" our buffer, taking the data in <code>SyncState</code>.
   */
  private void initializeOggReader() {
    LOG.debug("Initializing OGG/Vorbis reader.");

    // Initialize SyncState
    oggSyncState.init();

    // Prepare the SyncState internal buffer
    oggSyncState.buffer(bufferSize);

    // Fill the buffer with the data from SyncState's internal buffer.
    // Note how the size of this new buffer is different from bufferSize.
    buffer = oggSyncState.data;

    LOG.debug("Done initializing OGG/Vorbis reader.");
  }

  /**
   * This method reads the header of the stream, which consists of three
   * packets.
   *
   * @return true if the header was successfully read, false otherwise
   */
  private boolean readHeader() {
    LOG.debug("Starting to read the header.");

    // Variable used in loops below.
    // While we need more data, we will continue to read from the InputStream.
    boolean needMoreData = true;

    // We will read the first three packets of the header.
    // We start off by defining packet = 1 and increment that value whenever we have successfully read another packet.
    int packet = 1;


    // While we need more data (which we do until we have read the three
    // header packets), this loop reads from the stream and has a big
    // <code>switch</code> statement which does what it's supposed to do in
    // regard to the current packet.
    while (needMoreData) {
      // Read from the InputStream.
      try {
        count = inputStream.read(buffer, index, bufferSize);
      } catch (IOException e) {
        LOG.error("Could not read from the input stream.", e);
      }

      // We let SyncState know how many bytes we read.
      oggSyncState.wrote(count);

      // We want to read the first three packets.
      // For the first packet, we need to initialize the StreamState object and a couple of other things.
      // For packet two and three, the procedure is the same: we take out a page, and then we take out the packet.
      switch (packet) {
        // The first packet.
        case 1: {
          // We take out a page.
          switch (oggSyncState.pageout(oggPage)) {
            // If there is a hole in the data, we must exit.
            case -1 -> {
              LOG.error("There is a hole in the first packet data.");
              return false;
            }


            // If we need more data, we break to get it.
            case 0 -> {
            }


            // We got where we wanted. We have successfully read the first packet, and we will now initialize and reset
            // StreamState, and initialize the Info and Comment objects. Afterwards we will check that the page doesn't
            // contain any errors, that the packet doesn't contain any errors and that it's Vorbis data.
            case 1 -> {
              // Initializes and resets StreamState.
              oggStreamState.init(oggPage.serialno());
              oggStreamState.reset();

              // Initializes the Info and Comment objects.
              vorbisInfo.init();
              vorbisComment.init();

              // Check the page (serial number and stuff).
              if (oggStreamState.pagein(oggPage) == -1) {
                LOG.error("We got an error while reading the first header page.");
                return false;
              }

              // Try to extract a packet. All other return values than "1" indicates there's something wrong.
              if (oggStreamState.packetout(oggPacket) != 1) {
                LOG.error("We got an error while reading the first header packet.");
                return false;
              }

              // We give the packet to the Info object, so that it can extract the Comment-related information, among
              // other things. If this fails, it's not Vorbis data.
              if (vorbisInfo.synthesis_headerin(vorbisComment,
                oggPacket) < 0) {
                LOG.error("We got an error while interpreting the first packet. Apparently, it's not Vorbis data.");
                return false;
              }

              // We're done here, let's increment "packet".
              packet++;
            }
          }

          // Note how we are NOT breaking here if we have proceeded to the second packet. We don't want to read from the
          // input stream again if it's not necessary.
          if (packet == 1) break;
        }

        // The code for the second and third packets follow.
        case 2:
        case 3: {
          // Try to get a new page again.
          switch (oggSyncState.pageout(oggPage)) {
            // If there is a hole in the data, we must exit.
            case -1 -> {
              LOG.error("There is a hole in the second or third packet data.");
              return false;
            }


            // If we need more data, we break to get it.
            case 0 -> {
            }


            // Here is where we take the page, extract a packet and (if everything goes well) give the information to
            // the Info and Comment objects like we did above.
            case 1 -> {
              // Share the page with the StreamState object.
              oggStreamState.pagein(oggPage);

              // Just like the switch(...packetout...) lines above.
              switch (oggStreamState.packetout(oggPacket)) {
                // If there is a hole in the data, we must exit.
                case -1 -> {
                  LOG.error("There is a hole in the first packet data.");
                  return false;
                }


                // If we need more data, we break to get it.
                case 0 -> {
                }


                // We got a packet, let's process it.
                case 1 -> {
                  // Like above, we give the packet to the Info and Comment objects.
                  vorbisInfo.synthesis_headerin(
                    vorbisComment, oggPacket);

                  // Increment packet.
                  packet++;

                  if (packet == 4) {
                    // There is no fourth packet, so we will just end the loop here.
                    needMoreData = false;
                  }
                }
              }
            }
          }

          break;
        }
      }

      // We get the new index and an updated buffer.
      index = oggSyncState.buffer(bufferSize);
      buffer = oggSyncState.data;

      // If we need more data but can't get it, the stream doesn't contain enough information.
      if (count == 0 && needMoreData) {
        LOG.error("Not enough header data was supplied.");
        return false;
      }
    }

    LOG.debug("Finished reading the header.");

    return true;
  }

  /**
   * This method starts the sound system. It starts with initializing the
   * <code>DspState</code> object, after which it sets up the
   * <code>Block</code> object. Last but not least, it opens a line to the
   * source data line.
   *
   * @return true if the sound system was successfully started, false
   * otherwise
   */
  private boolean initializeSound() throws ShipException {
    LOG.debug("Initializing the sound system.");

    // This buffer is used by the decoding method.
    convertedBufferSize = bufferSize * 2;
    convertedBuffer = new byte[convertedBufferSize];

    // Initializes the DSP synthesis.
    vorbisDspState.synthesis_init(vorbisInfo);

    // Make the Block object aware of the DSP.
    vorbisBlock.init(vorbisDspState);

    // Must be 2 channels
    if (2 != vorbisInfo.channels)
      throw new ShipException("Input audio must be stereo!");

    // We create the PCM variables.
    // The index is an array with the same length as the number of audio channels.
    pcmBuffer = new float[1][][];
    pcmIndex = new int[vorbisInfo.channels];

    LOG.debug("Done initializing the sound system.");

    return true;
  }

  /**
   * This method reads the entire stream body. Whenever it extracts a packet,
   * it will decode it by calling <code>decodeCurrentPacket()</code>.
   */
  private void readBody() {
    LOG.debug("Reading the body.");

    // Variable used in loops below, like in readHeader().
    // While we need more data, we will continue to read from the InputStream.
    boolean needMoreData = true;

    int totalBytes = 0;

    while (needMoreData) {
      switch (oggSyncState.pageout(oggPage)) {
        // If there is a hole in the data, we just proceed.
        case -1: {
          LOG.debug("There is a hole in the data. We proceed.");
        }

        // If we need more data, we break to get it.
        case 0: {
          break;
        }

        // If we have successfully checked out a page, we continue.
        case 1: {
          // Give the page to the StreamState object.
          oggStreamState.pagein(oggPage);

          // If granulepos() returns "0", we don't need more data.
          if (oggPage.granulepos() == 0) {
            needMoreData = false;
            break;
          }

          // Here is where we process the packets.
          processPackets:
          while (true) {
            switch (oggStreamState.packetout(oggPacket)) {
              // Is it a hole in the data?
              case -1: {
                LOG.debug("There is a hole in the data, we continue though.");
              }

              // If we need more data, we break to get it.
              case 0: {
                break processPackets;
              }

              // If we have the data we need, we decode the packet.
              case 1: {
                decodeCurrentPacket();
              }
            }
          }

          // If the page is the end-of-stream, we don't need more data.
          if (oggPage.eos() != 0) needMoreData = false;
        }
      }

      // If we need more data...
      if (needMoreData) {
        // We get the new index and an updated buffer.
        index = oggSyncState.buffer(bufferSize);
        buffer = oggSyncState.data;

        // Read from the InputStream.
        try {
          count = inputStream.read(buffer, index, bufferSize);
        } catch (Exception e) {
          LOG.error("Failed", e);
          return;
        }

        // We let SyncState know how many bytes we read.
        oggSyncState.wrote(count);

        // There's no more data in the stream.
        totalBytes += count;
        if (count < 0) needMoreData = false;
      }
    }
    LOG.info("decoded {} bytes of OGG/Vorbis audio", totalBytes);
  }

  /**
   * Decodes the current packet and sends it to the audio output line.
   */
  private void decodeCurrentPacket() {
    int samples;

    // Check that the packet is an audio data packet etc.
    if (vorbisBlock.synthesis(oggPacket) == 0) {
      // Give the block to the DspState object.
      vorbisDspState.synthesis_blockin(vorbisBlock);
    }

    // We need to know how many samples to process.
    int range;

    // Get the PCM information and count the samples.
    // And while these samples are more than zero...
    while ((samples = vorbisDspState.synthesis_pcmout(pcmBuffer, pcmIndex)) > 0) {
      // We need to know for how many samples we are going to process.
      range = Math.min(samples, convertedBufferSize);

      // For each channel...
      for (int i = 0; i < vorbisInfo.channels; i++) {
        int sampleIndex = i * 2;

        // For every sample in our range...
        for (int j = 0; j < range; j++) {
          // Get the PCM value for the channel at the correct position.
          int value = (int) (pcmBuffer[0][i][pcmIndex[i] + j] * 32767);

          // We make sure our value doesn't exceed or falls below +-32767.
          if (value > 32767) {
            value = 32767;
          }
          if (value < -32768) {
            value = -32768;
          }

          // If the value is less than zero, we bitwise-or it with 32768 (which is 1000000000000000 = 10^15).
          if (value < 0) value = value | 32768;

          // Take our value and split it into two, one with the last byte and one with the first byte.
          convertedBuffer[sampleIndex] = (byte) (value);
          convertedBuffer[sampleIndex + 1] = (byte) (value >>> 8);

          // Move the sample index forward by two (since that's how many values we get at once) times the number of channels.
          sampleIndex += 2 * (vorbisInfo.channels);
        }
      }

      // Update the DspState object.
      vorbisDspState.synthesis_read(range);
    }
  }

  /**
   * A clean-up method, called when everything is finished.
   * Clears the OGG/Vorbis objects and closes the <code>InputStream</code>.
   */
  private void cleanUp() {
    LOG.debug("Cleaning up.");

    // Clear the necessary OGG/Vorbis objects.
    oggStreamState.clear();
    vorbisBlock.clear();
    vorbisDspState.clear();
    vorbisInfo.clear();
    oggSyncState.clear();

    LOG.debug("Done cleaning up.");
  }

  /**
   * Get the frame for a given instant
   *
   * @param of instant
   * @return source audio frame for the specified instant
   */
  public int getFrame(Instant of) {
    return (int)
      Math.floor(getInfo().rate *
        (Values.toEpochMicros(of) - Values.toEpochMicros(Instant.parse(segment.getBeginAt())))
        / MICROS_IN_A_SECOND);
  }

  /**
   * Get the total number of frames in the pcm buffer
   *
   * @return total number of frames
   */
  public int getTotalPcmFrames() {
    return pcmBuffer[0][0].length;
  }

  /**
   * Get the PCM data from the buffer
   *
   * @return PCM data
   */
  public float[][] getPcmData() {
    return pcmBuffer[0];
  }
}
