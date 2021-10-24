/*
 * Created in 2021 by Charney Kaye, California
 * Original mp4parser work copyright 2012 Sebastian Annies, Hamburg
 *
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an AS IS BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.xj.ship.broadcast;

import org.mp4parser.*;
import org.mp4parser.boxes.iso14496.part12.*;
import org.mp4parser.boxes.iso23001.part7.CencSampleAuxiliaryDataFormat;
import org.mp4parser.boxes.iso23001.part7.SampleEncryptionBox;
import org.mp4parser.boxes.iso23001.part7.TrackEncryptionBox;
import org.mp4parser.boxes.sampleentry.SampleEntry;
import org.mp4parser.boxes.samplegrouping.GroupEntry;
import org.mp4parser.boxes.samplegrouping.SampleGroupDescriptionBox;
import org.mp4parser.boxes.samplegrouping.SampleToGroupBox;
import org.mp4parser.muxer.Movie;
import org.mp4parser.muxer.Sample;
import org.mp4parser.muxer.Track;
import org.mp4parser.muxer.builder.Mp4Builder;
import org.mp4parser.muxer.tracks.encryption.CencEncryptedTrack;
import org.mp4parser.tools.IsoTypeWriter;
import org.mp4parser.tools.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.*;

import static org.mp4parser.tools.CastUtils.l2i;

/**
 Creates a fragmented MP4 file.
 */
public record ChunkFragmentM4sBuilder(int sequenceNumber) implements Mp4Builder {
  private static final Logger LOG = LoggerFactory.getLogger(ChunkFragmentM4sBuilder.class);

  private List<Box> createMoofMdat(final Movie movie) {
    List<Box> moofsMdats = new ArrayList<>();
    HashMap<Track, Double> track2currentTime = new HashMap<>();

    for (Track track : movie.getTracks()) {
      track2currentTime.put(track, 0.0);

    }

    Track earliestTrack = null;
    double earliestTime = Double.MAX_VALUE;
    for (Map.Entry<Track, Double> trackEntry : track2currentTime.entrySet()) {
      if (trackEntry.getValue() < earliestTime) {
        earliestTime = trackEntry.getValue();
        earliestTrack = trackEntry.getKey();
      }
    }
    assert earliestTrack != null;

    long[] startSamples = new long[]{1};
    long startSample = startSamples[0];
    long endSample = earliestTrack.getSamples().size() + 1;

    long[] times = earliestTrack.getSampleDurations();
    long timescale = earliestTrack.getTrackMetaData().getTimescale();
    for (long i = startSample; i < endSample; i++) {
      earliestTime += (double) times[l2i(i - 1)] / timescale;
    }
    createFragment(moofsMdats, earliestTrack, earliestTrack.getSamples().size(), sequenceNumber);

    return moofsMdats;
  }

  private void createFragment(List<Box> moofsMdats, Track track, long endSample, int sequence) {
    moofsMdats.add(createMoof(endSample, track, sequence));
    moofsMdats.add(createMdat(endSample, track));
  }

  /**
   {@inheritDoc}
   */
  public Container build(Movie movie) {
    LOG.debug("Creating movie " + movie);
    BasicContainer isoFile = new BasicContainer();


    isoFile.addBox(createSegmentTypeBox());

    for (Box box : createMoofMdat(movie)) {
      isoFile.addBox(box);
    }
    isoFile.addBox(createMfra(movie, isoFile));

    return isoFile;
  }

  private SegmentTypeBox createSegmentTypeBox() {
    return new SegmentTypeBox("msdh", 0, List.of(
      "msdh", "msix"
    ));
  }

  private Box createMdat(final long endSample, final Track track) {

    class Mdat implements Box {
      long size_ = -1;

      public long getSize() {
        if (size_ != -1) return size_;
        long size = 8; // I don't expect 2gig fragments
        for (Sample sample : getSamples(endSample, track)) {
          size += sample.getSize();
        }
        size_ = size;
        return size;
      }

      public String getType() {
        return "mdat";
      }

      public void getBox(WritableByteChannel writableByteChannel) throws IOException {
        ByteBuffer header = ByteBuffer.allocate(8);
        IsoTypeWriter.writeUInt32(header, l2i(getSize()));
        header.put(IsoFile.fourCCtoBytes(getType()));
        ((Buffer) header).rewind();
        writableByteChannel.write(header);

        List<Sample> samples = getSamples(endSample, track);
        for (Sample sample : samples) {
          sample.writeTo(writableByteChannel);
        }


      }


    }

    return new Mdat();
  }

  private void createTfhd(Track track, TrackFragmentBox parent) {
    TrackFragmentHeaderBox tfhd = new TrackFragmentHeaderBox();
    SampleFlags sf = new SampleFlags();

    tfhd.setDefaultSampleFlags(sf);
    tfhd.setBaseDataOffset(-1);
    tfhd.setSampleDescriptionIndex(track.getSampleEntries().indexOf(track.getSamples().get(l2i(1)).getSampleEntry()) + 1);
    tfhd.setTrackId(track.getTrackMetaData().getTrackId());
    tfhd.setDefaultBaseIsMoof(true);
    parent.addBox(tfhd);
  }

  private void createMfhd(int sequenceNumber, MovieFragmentBox parent) {
    MovieFragmentHeaderBox mfhd = new MovieFragmentHeaderBox();
    mfhd.setSequenceNumber(sequenceNumber);
    parent.addBox(mfhd);
  }

  private void createTraf(long endSample, Track track, MovieFragmentBox parent) {
    TrackFragmentBox traf = new TrackFragmentBox();
    parent.addBox(traf);
    createTfhd(track, traf);
    createTfdt(traf);
    createTrun(endSample, track, traf);

    if (track instanceof CencEncryptedTrack) {
      createSaiz(endSample, (CencEncryptedTrack) track, traf);
      createSenc(endSample, (CencEncryptedTrack) track, traf);
      createSaio(traf, parent);
    }


    Map<String, List<GroupEntry>> groupEntryFamilies = new HashMap<>();
    for (Map.Entry<GroupEntry, long[]> sg : track.getSampleGroups().entrySet()) {
      String type = sg.getKey().getType();
      List<GroupEntry> groupEntries = groupEntryFamilies.computeIfAbsent(type, k -> new ArrayList<>());
      groupEntries.add(sg.getKey());
    }


    for (Map.Entry<String, List<GroupEntry>> sg : groupEntryFamilies.entrySet()) {
      SampleGroupDescriptionBox sgpd = new SampleGroupDescriptionBox();
      String type = sg.getKey();
      sgpd.setGroupEntries(sg.getValue());
      sgpd.setGroupingType(type);
      SampleToGroupBox sbgp = new SampleToGroupBox();
      sbgp.setGroupingType(type);
      SampleToGroupBox.Entry last = null;
      for (int i = l2i((long) 1 - 1); i < l2i(endSample - 1); i++) {
        int index = 0;
        for (int j = 0; j < sg.getValue().size(); j++) {
          GroupEntry groupEntry = sg.getValue().get(j);
          long[] sampleNums = track.getSampleGroups().get(groupEntry);
          if (Arrays.binarySearch(sampleNums, i) >= 0) {
            index = j + 0x10001;
          }
        }
        if (last == null || last.getGroupDescriptionIndex() != index) {
          last = new SampleToGroupBox.Entry(1, index);
          sbgp.getEntries().add(last);
        } else {
          last.setSampleCount(last.getSampleCount() + 1);
        }
      }
      traf.addBox(sgpd);
      traf.addBox(sbgp);
    }


  }

  private void createSenc(long endSample, CencEncryptedTrack track, TrackFragmentBox parent) {
    SampleEncryptionBox senc = new SampleEncryptionBox();
    senc.setSubSampleEncryption(track.hasSubSampleEncryption());
    senc.setEntries(track.getSampleEncryptionEntries().subList(l2i((long) 1 - 1), l2i(endSample - 1)));
    parent.addBox(senc);
  }

  private void createSaio(TrackFragmentBox parent, MovieFragmentBox moof) {

    SampleAuxiliaryInformationOffsetsBox saio = new SampleAuxiliaryInformationOffsetsBox();
    parent.addBox(saio);
    assert parent.getBoxes(TrackRunBox.class).size() == 1 : "Don't know how to deal with multiple Track Run Boxes when encrypting";
    saio.setAuxInfoType("cenc");
    saio.setFlags(1);
    long offset = 0;
    offset += 8; // traf header till 1st child box
    for (Box box : parent.getBoxes()) {
      if (box instanceof SampleEncryptionBox) {
        offset += ((SampleEncryptionBox) box).getOffsetToFirstIV();
        break;
      } else {
        offset += box.getSize();
      }
    }
    offset += 16; // traf header till 1st child box
    for (Box box : moof.getBoxes()) {
      if (box == parent) {
        break;
      } else {
        offset += box.getSize();
      }

    }

    saio.setOffsets(new long[]{offset});

  }

  private void createSaiz(long endSample, CencEncryptedTrack track, TrackFragmentBox parent) {
    SampleEntry se = track.getSampleEntries().get(l2i(parent.getTrackFragmentHeaderBox().getSampleDescriptionIndex() - 1));

    TrackEncryptionBox tenc = Path.getPath((Container) se, "sinf[0]/schi[0]/tenc[0]");

    SampleAuxiliaryInformationSizesBox saiz = new SampleAuxiliaryInformationSizesBox();
    saiz.setAuxInfoType("cenc");
    saiz.setFlags(1);
    if (track.hasSubSampleEncryption()) {
      short[] sizes = new short[l2i(endSample - (long) 1)];
      List<CencSampleAuxiliaryDataFormat> auxs =
        track.getSampleEncryptionEntries().subList(l2i((long) 1 - 1), l2i(endSample - 1));
      for (int i = 0; i < sizes.length; i++) {
        sizes[i] = (short) auxs.get(i).getSize();
      }
      saiz.setSampleInfoSizes(sizes);
    } else {
      assert tenc != null;
      saiz.setDefaultSampleInfoSize(tenc.getDefaultIvSize());
      saiz.setSampleCount(l2i(endSample - (long) 1));
    }
    parent.addBox(saiz);
  }

  /**
   Gets all samples starting with <code>startSample</code> (one based -&gt; one is the first) and
   ending with <code>endSample</code> (exclusive).

   @param endSample high endpoint (exclusive) of the sample sequence
   @param track     source of the samples
   @return a <code>List&lt;Sample&gt;</code> of raw samples
   */
  private List<Sample> getSamples(long endSample, Track track) {
    // since startSample and endSample are one-based subtract 1 before addressing list elements
    return track.getSamples().subList(l2i(1) - 1, l2i(endSample) - 1);
  }

  /**
   Gets the sizes of a sequence of samples.

   @param endSample high endpoint (exclusive) of the sample sequence
   @param track     source of the samples
   @return the sample sizes in the given interval
   */
  private long[] getSampleSizes(long endSample, Track track) {
    List<Sample> samples = getSamples(endSample, track);

    long[] sampleSizes = new long[samples.size()];
    for (int i = 0; i < sampleSizes.length; i++) {
      sampleSizes[i] = samples.get(i).getSize();
    }
    return sampleSizes;
  }

  private void createTfdt(TrackFragmentBox parent) {
    TrackFragmentBaseMediaDecodeTimeBox tfdt = new TrackFragmentBaseMediaDecodeTimeBox();
    tfdt.setVersion(1);
    long startTime = 0;
    tfdt.setBaseMediaDecodeTime(startTime);
    parent.addBox(tfdt);
  }

  /**
   Creates one or more track run boxes for a given sequence.@param endSample      high endpoint (exclusive) of the sample sequence

   @param track  source of the samples
   @param parent the created box must be added to this box
   */
  private void createTrun(long endSample, Track track, TrackFragmentBox parent) {
    TrackRunBox trun = new TrackRunBox();
    trun.setVersion(1);
    long[] sampleSizes = getSampleSizes(endSample, track);

    trun.setSampleDurationPresent(true);
    trun.setSampleSizePresent(true);
    List<TrackRunBox.Entry> entries = new ArrayList<>(l2i(endSample - (long) 1));


    List<CompositionTimeToSample.Entry> compositionTimeEntries = track.getCompositionTimeEntries();
    int compositionTimeQueueIndex = 0;
    CompositionTimeToSample.Entry[] compositionTimeQueue =
      compositionTimeEntries != null && compositionTimeEntries.size() > 0 ?
        compositionTimeEntries.toArray(new CompositionTimeToSample.Entry[0]) : null;
    long compositionTimeEntriesLeft = compositionTimeQueue != null ? compositionTimeQueue[compositionTimeQueueIndex].getCount() : -1;


    trun.setSampleCompositionTimeOffsetPresent(compositionTimeEntriesLeft > 0);

    // fast-forward composition stuff

    boolean sampleFlagsRequired = (track.getSampleDependencies() != null && !track.getSampleDependencies().isEmpty() ||
      track.getSyncSamples() != null && track.getSyncSamples().length != 0);

    trun.setSampleFlagsPresent(sampleFlagsRequired);

    for (int i = 0; i < sampleSizes.length; i++) {
      TrackRunBox.Entry entry = new TrackRunBox.Entry();
      entry.setSampleSize(sampleSizes[i]);
      if (sampleFlagsRequired) {
        //if (false) {
        SampleFlags sflags = new SampleFlags();

        if (track.getSampleDependencies() != null && !track.getSampleDependencies().isEmpty()) {
          SampleDependencyTypeBox.Entry e = track.getSampleDependencies().get(i);
          sflags.setSampleDependsOn(e.getSampleDependsOn());
          sflags.setSampleIsDependedOn(e.getSampleIsDependedOn());
          sflags.setSampleHasRedundancy(e.getSampleHasRedundancy());
        }
        if (track.getSyncSamples() != null && track.getSyncSamples().length > 0) {
          // we have to mark non-sync samples!
          if (Arrays.binarySearch(track.getSyncSamples(), (long) 1 + i) >= 0) {
            sflags.setSampleIsDifferenceSample(false);
            sflags.setSampleDependsOn(2);
          } else {
            sflags.setSampleIsDifferenceSample(true);
            sflags.setSampleDependsOn(1);
          }
        }
        // i don't have sample degradation
        entry.setSampleFlags(sflags);

      }

      entry.setSampleDuration(track.getSampleDurations()[l2i((long) 1 + i - 1)]);

      if (compositionTimeQueue != null) {
        entry.setSampleCompositionTimeOffset(compositionTimeQueue[compositionTimeQueueIndex].getOffset());
        if (--compositionTimeEntriesLeft == 0 && (compositionTimeQueue.length - compositionTimeQueueIndex) > 1) {
          compositionTimeQueueIndex++;
          compositionTimeEntriesLeft = compositionTimeQueue[compositionTimeQueueIndex].getCount();
        }
      }
      entries.add(entry);
    }

    trun.setEntries(entries);

    parent.addBox(trun);
  }

  /**
   Creates a 'moof' box for a given sequence of samples.

   @param endSample      high endpoint (exclusive) of the sample sequence
   @param track          source of the samples
   @param sequenceNumber the fragment index of the requested list of samples
   @return the list of TrackRun boxes.
   */
  private ParsableBox createMoof(long endSample, Track track, int sequenceNumber) {
    MovieFragmentBox moof = new MovieFragmentBox();
    createMfhd(sequenceNumber, moof);
    createTraf(endSample, track, moof);

    TrackRunBox firstTrun = moof.getTrackRunBoxes().get(0);
    firstTrun.setDataOffset(1); // dummy to make size correct
    firstTrun.setDataOffset((int) (8 + moof.getSize())); // mdat header + moof size

    return moof;
  }

  /**
   Creates a 'tfra' - track fragment random access box for the given track with the isoFile.
   The tfra contains a map of random access points with time as key and offset within the isofile
   as value.

   @param track   the concerned track
   @param isoFile the track is contained in
   @return a track fragment random access box.
   */
  private Box createTfra(Track track, Container isoFile) {
    TrackFragmentRandomAccessBox tfra = new TrackFragmentRandomAccessBox();
    tfra.setVersion(1); // use long offsets and times
    List<TrackFragmentRandomAccessBox.Entry> offset2timeEntries = new LinkedList<>();

    long offset = 0;
    long duration = 0;

    for (Box box : isoFile.getBoxes()) {
      if (box instanceof MovieFragmentBox) {
        List<TrackFragmentBox> trafs = ((MovieFragmentBox) box).getBoxes(TrackFragmentBox.class);
        for (int i = 0; i < trafs.size(); i++) {
          TrackFragmentBox traf = trafs.get(i);

          if (traf.getTrackFragmentHeaderBox().getTrackId() == track.getTrackMetaData().getTrackId()) {

            // here we are at the offset required for the current entry.
            List<TrackRunBox> truns = traf.getBoxes(TrackRunBox.class);
            for (int j = 0; j < truns.size(); j++) {
              List<TrackFragmentRandomAccessBox.Entry> offset2timeEntriesThisTrun = new LinkedList<>();
              TrackRunBox trun = truns.get(j);
              for (int k = 0; k < trun.getEntries().size(); k++) {
                TrackRunBox.Entry trunEntry = trun.getEntries().get(k);
                offset2timeEntriesThisTrun.add(new TrackFragmentRandomAccessBox.Entry(
                  duration,
                  offset,
                  i + 1, j + 1, k + 1));
                duration += trunEntry.getSampleDuration();
              }
              if (offset2timeEntriesThisTrun.size() == trun.getEntries().size() && trun.getEntries().size() > 0) {
                // Oops. every sample seems to be random access sample
                // is this an audio track? I don't care.
                // I just use the first for trun sample for tfra random access
                offset2timeEntries.add(offset2timeEntriesThisTrun.get(0));
              } else {
                offset2timeEntries.addAll(offset2timeEntriesThisTrun);
              }
            }
          }
        }
      }


      offset += box.getSize();
    }
    tfra.setEntries(offset2timeEntries);
    tfra.setTrackId(track.getTrackMetaData().getTrackId());
    return tfra;
  }

  /**
   Creates a 'mfra' - movie fragment random access box for the given movie in the given
   isofile. Uses {@link #createTfra(Track, Container)}
   to generate the child boxes.

   @param movie   concerned movie
   @param isoFile concerned isofile
   @return a complete 'mfra' box
   */
  private ParsableBox createMfra(Movie movie, Container isoFile) {
    MovieFragmentRandomAccessBox mfra = new MovieFragmentRandomAccessBox();
    for (Track track : movie.getTracks()) {
      mfra.addBox(createTfra(track, isoFile));
    }

    MovieFragmentRandomAccessOffsetBox mfro = new MovieFragmentRandomAccessOffsetBox();
    mfra.addBox(mfro);
    mfro.setMfraSize(mfra.getSize());
    return mfra;
  }

}
