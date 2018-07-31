// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.mixer.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xiph.libogg.ogg_packet;
import org.xiph.libogg.ogg_page;
import org.xiph.libogg.ogg_stream_state;
import org.xiph.libvorbis.vorbis_block;
import org.xiph.libvorbis.vorbis_comment;
import org.xiph.libvorbis.vorbis_dsp_state;
import org.xiph.libvorbis.vorbis_info;
import org.xiph.libvorbis.vorbisenc;

import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Random;

/**
 [#159449508] Ops wants to ship Ogg/Vorbis format, in order avoid licensing issues, and leverage more mature open-source audio
 */
public class VorbisEncoder {
  private static final Logger log = LoggerFactory.getLogger(VorbisEncoder.class);
  private static final int VORBIS_ANALYSIS_BLOCK_FRAMES = 1024;
  private final Random generator;
  private final vorbis_dsp_state dspState;
  private final vorbis_info vorbisInfo;
  private final vorbis_block block;
  private final ogg_stream_state oggStreamState;
  private final ogg_packet header;
  private final ogg_packet header_comm;
  private final ogg_packet header_code;
  private final ogg_page page;
  private final ogg_packet packet;
  private final double[][] stream;

  /**
   Instantiate new Vorbis Encoder

   @param stream    to encode
   @param frameRate at which to encode audio
   */
  public VorbisEncoder(double[][] stream, int frameRate, float quality) {
    this.stream = stream;
    generator = new SecureRandom();  // need to randomize seed

    // structures that store all the vorbis bitstream settings
    vorbisInfo = new vorbis_info();
    vorbisenc encoder = new vorbisenc();
    try {
      encoder.vorbis_encode_init_vbr(vorbisInfo, stream[0].length, frameRate, quality);
    } catch (Exception e) {
      log.error("Failed to initialize Vorbis encoding", e);
    }

    // central working state for the packet->PCM decoder
    dspState = new vorbis_dsp_state();
    if (!dspState.vorbis_analysis_init(vorbisInfo)) {
      log.error("Failed to Initialize vorbis_dsp_state");
    }

    // local working space for packet->PCM decode
    block = new vorbis_block(dspState);

    // take physical pages, weld into a logical stream of packets
    oggStreamState = new ogg_stream_state(generator.nextInt(256));

    // structures for building OGG packets
    header = new ogg_packet();
    header_comm = new ogg_packet();
    header_code = new ogg_packet();

    // structures that stores all the user comments
    vorbis_comment comment = new vorbis_comment();
    comment.vorbis_comment_add_tag("COPYRIGHT", "XJ Music Inc.");
    dspState.vorbis_analysis_headerout(comment, header, header_comm, header_code);

    oggStreamState.ogg_stream_packetin(header); // automatically placed in its own page
    oggStreamState.ogg_stream_packetin(header_comm);
    oggStreamState.ogg_stream_packetin(header_code);

    // one Ogg bitstream page.  VorbisEncoder packets are inside
    page = new ogg_page();
    // one raw packet of data for decode
    packet = new ogg_packet();

  }

  /**
   Encode OGG_VORBIS VorbisEncoder@param stream      input channels of floating point samples

   @param output to write OGG Vorbis data
   */
  public void encode(FileOutputStream output) throws IOException {
    while (oggStreamState.ogg_stream_flush(page)) {
      output.write(page.header, 0, page.header_len);
      output.write(page.body, 0, page.body_len);
    }
    log.debug("Wrote Header");

    int atFrame = 0;
    int totalFrames = stream.length;
    Boolean endOfStream = false;
    while (!endOfStream) {

      /*
      From: https://xiph.org/vorbis/doc/libvorbis/vorbis_analysis_buffer.html
       *
      The Vorbis encoder expects the caller to write audio data as non-interleaved floating point samples into its internal buffers.
      The general procedure is to call this function with the number of samples you have available.
      The encoder will arrange for that much internal storage and return an array of buffer pointers, one for each channel of audio.
      The caller must then write the audio samples into those buffers, as float values,
      and finally call vorbis_analysis_wrote() to tell the encoder the data is available for analysis.
      */
      if (atFrame < totalFrames) {
        float[][] buffer = dspState.vorbis_analysis_buffer(VORBIS_ANALYSIS_BLOCK_FRAMES);

        // copy a block of samples into the analysis buffer
        int bufferChannels = buffer.length;
        int wroteBufferLength = 0;
        for (int frame = 0; frame < VORBIS_ANALYSIS_BLOCK_FRAMES; frame++) {
          if (atFrame < stream.length) {
            for (int channel = 0; channel < bufferChannels; channel++) {
              buffer[channel][dspState.pcm_current + frame] = (float) stream[atFrame][channel];
            }
            wroteBufferLength++;
          }
          atFrame++;
        }

        // tell the library how much we actually submitted
        dspState.vorbis_analysis_wrote(wroteBufferLength);
      } else {
        dspState.vorbis_analysis_wrote(0);
      }

      // vorbis does some data pre-analysis, then divvies up blocks for more involved
      // (potentially parallel) processing.  Get a single block for encoding now
      while (block.vorbis_analysis_blockout(dspState)) {

        // analysis, assume we want to use bitrate management

        block.vorbis_analysis(null);
        block.vorbis_bitrate_addblock();

        while (dspState.vorbis_bitrate_flushpacket(packet)) {

          // weld the packet into the bitstream
          oggStreamState.ogg_stream_packetin(packet);

          // write out pages (if any)
          while (!endOfStream) {

            if (!oggStreamState.ogg_stream_pageout(page)) {
              break;
            }

            output.write(page.header, 0, page.header_len);
            output.write(page.body, 0, page.body_len);

            // this could be set above, but for illustrative purposes, I do
            // it here (to show that vorbis does know where the stream ends)
            if (page.ogg_page_eos() > 0)
              endOfStream = true;
          }
        }
      }

    }

    output.close();

    log.debug("Wrote {} frames", atFrame);


  }

}
