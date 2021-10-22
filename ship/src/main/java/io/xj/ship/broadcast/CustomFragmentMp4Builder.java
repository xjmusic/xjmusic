package io.xj.ship.broadcast;

import org.mp4parser.*;
import org.mp4parser.boxes.iso14496.part12.*;
import org.mp4parser.muxer.Movie;
import org.mp4parser.muxer.Sample;
import org.mp4parser.muxer.Track;
import org.mp4parser.muxer.builder.DefaultFragmenterImpl;
import org.mp4parser.muxer.builder.Fragmenter;
import org.mp4parser.muxer.builder.Mp4Builder;
import org.mp4parser.tools.IsoTypeWriter;
import org.mp4parser.tools.Offsets;
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
 Creates a plain MP4 file from a video. Plain as plain can be.
 */
public class CustomFragmentMp4Builder implements Mp4Builder {
    private static final Logger LOG = LoggerFactory.getLogger(CustomFragmentMp4Builder.class);
    private static final String BRAND_MSDH = "msdh";
    private static final String BRAND_MSIX = "msix";
    private final long subsegmentDuration;
    private final int sampleRate;
    private final long sequenceNumber;
    private final int lengthSeconds;
    private final long dspBufferSize;
    Map<Track, StaticChunkOffsetBox> chunkOffsetBoxes = new HashMap<>();
    Set<SampleAuxiliaryInformationOffsetsBox> sampleAuxiliaryInformationOffsetsBoxes = new HashSet<>();
    HashMap<Track, List<Sample>> track2Sample = new HashMap<>();
    HashMap<Track, long[]> track2SampleSizes = new HashMap<>();
    private Fragmenter fragmenter;

    public CustomFragmentMp4Builder(
            int sampleRate,
            int lengthSeconds,
            long sequenceNumber,
            long dspBufferSize
    ) {
        this.sampleRate = sampleRate;
        this.lengthSeconds = lengthSeconds;
        this.sequenceNumber = sequenceNumber;
        this.dspBufferSize = dspBufferSize;
        subsegmentDuration = (long) sampleRate * lengthSeconds;
    }

    private static long sum(int[] ls) {
        long rc = 0;
        for (long l : ls) {
            rc += l;
        }
        return rc;
    }

    /**
     {@inheritDoc}
     */
    public Container build(Movie movie) {
        if (fragmenter == null) {
            fragmenter = new DefaultFragmenterImpl(lengthSeconds);
        }
        LOG.debug("Creating movie {}", movie);
        for (Track track : movie.getTracks()) {
            // getting the samples may be a time-consuming activity
            List<Sample> samples = track.getSamples();
            putSamples(track, samples);
            long[] sizes = new long[samples.size()];
            for (int i = 0; i < sizes.length; i++) {
                Sample b = samples.get(i);
                sizes[i] = b.getSize();
            }
            track2SampleSizes.put(track, sizes);
        }

        BasicContainer isoFile = new BasicContainer();

        isoFile.addBox(createSegmentTypeBox());

        isoFile.addBox(createSegmentIndexBox());

        Map<Track, int[]> chunks = new HashMap<>();
        for (Track track : movie.getTracks()) {
            chunks.put(track, getChunkSizes(track));
        }
        ParsableBox moof = createMovieFragmentBox(movie);
        isoFile.addBox(moof);
        List<TrackRunBox.Entry> entries = ((TrackRunBox) Path.getPath(moof, "traf/trun")).getEntries();

        long contentSize = 0;
        for (TrackRunBox.Entry traf : entries)
            contentSize += traf.getSampleSize();
        LOG.debug("About to create mdat");
        InterleaveChunkMdat mdat = new InterleaveChunkMdat(movie, chunks, contentSize);

        long dataOffset = 16;
        for (Box lightBox : isoFile.getBoxes()) {
            dataOffset += lightBox.getSize();
        }
        isoFile.addBox(mdat);
        LOG.debug("mdat crated");

        /*
        dataOffset is where the first sample starts. In this special mdat the samples always start
        at offset 16 so that we can use the same offset for large boxes and small boxes
         */

        for (StaticChunkOffsetBox chunkOffsetBox : chunkOffsetBoxes.values()) {
            long[] offsets = chunkOffsetBox.getChunkOffsets();
            for (int i = 0; i < offsets.length; i++) {
                offsets[i] += dataOffset;
            }
        }
        for (SampleAuxiliaryInformationOffsetsBox saio : sampleAuxiliaryInformationOffsetsBoxes) {
            long offset = saio.getSize(); // the calculation is systematically wrong by 4, I don't want to debug why. Just a quick correction --san 14.May.13
            offset += 4 + 4 + 4 + 4 + 4 + 24;
            // size of all header we were missing otherwise (moov, trak, mdia, minf, stbl)
            offset = Offsets.find(isoFile, saio, offset);

            long[] saioOffsets = saio.getOffsets();
            for (int i = 0; i < saioOffsets.length; i++) {
                saioOffsets[i] = saioOffsets[i] + offset;

            }
            saio.setOffsets(saioOffsets);
        }

        return isoFile;
    }

    private SegmentIndexBox createSegmentIndexBox() {
        SegmentIndexBox sidx = new SegmentIndexBox();
        sidx.setEntries(List.of(new SegmentIndexBox.Entry(
                0,
                64690, // TODO what is the meaning of "referenced size" and how do we compute it?
                subsegmentDuration,
                true,
                0,
                0
        )));
        sidx.setTimeScale(getTimescale());
        sidx.setReferenceId(1); // TODO set reference id from for real chunk values
        sidx.setEarliestPresentationTime(0); // TODO set earliest presentation time from for real chunk values
        sidx.setFirstOffset(0); // TODO set first offset from for real chunk values
        sidx.setReserved(0); // TODO set reserved from for real chunk values
        return sidx;
    }

    protected void putSamples(Track track, List<Sample> samples) {
        track2Sample.put(track, samples);
    }

    protected SegmentTypeBox createSegmentTypeBox() {
        return new SegmentTypeBox(BRAND_MSDH, 0, List.of(
                BRAND_MSDH, BRAND_MSIX
        ));
    }

    protected MovieFragmentBox createMovieFragmentBox(Movie movie) {
        MovieFragmentBox mfb = new MovieFragmentBox();
        MovieFragmentHeaderBox mfhb = new MovieFragmentHeaderBox();

        mfhb.setSequenceNumber(sequenceNumber);
        long movieTimeScale = getTimescale();
        long duration = 0;

        for (Track track : movie.getTracks()) {
            long tracksDuration;

            tracksDuration = (track.getDuration() * movieTimeScale / track.getTrackMetaData().getTimescale());

            if (tracksDuration > duration) {
                duration = tracksDuration;
            }
        }

        // find the next available trackId
        long nextTrackId = 0;
        for (Track track : movie.getTracks()) {
            nextTrackId = Math.max(nextTrackId, track.getTrackMetaData().getTrackId());
        }

        mfb.addBox(mfhb);
        for (Track track : movie.getTracks()) {
            mfb.addBox(createTrackFragmentBox(track));
        }

        return mfb;
    }

    protected TrackFragmentBox createTrackFragmentBox(Track track) {

        TrackFragmentBox tfb = new TrackFragmentBox();
        tfb.addBox(computeTrackFragmentHeaderBox(track));
        tfb.addBox(computeTrackFragmentBaseMediaDecodeTimeBox());
        tfb.addBox(computeTrackRunBox(track));

        LOG.debug("did create box for track_{}", track.getTrackMetaData().getTrackId());
        return tfb;
    }

    public long getTimescale() {
        return sampleRate;
    }

    private TrackFragmentHeaderBox computeTrackFragmentHeaderBox(Track track) {
        TrackFragmentHeaderBox tfhb = new TrackFragmentHeaderBox();
        tfhb.setTrackId(track.getTrackMetaData().getTrackId());
        tfhb.setBaseDataOffset(-1);
        tfhb.setSampleDescriptionIndex(0);
        tfhb.setDefaultSampleDuration(dspBufferSize);
        tfhb.setDefaultSampleSize(111);
        tfhb.setDurationIsEmpty(false);
        tfhb.setDefaultBaseIsMoof(true);
        tfhb.setDefaultSampleFlags(computeTrackFragmentHeaderBoxSampleFlags());
        return tfhb;
    }

    private SampleFlags computeTrackFragmentHeaderBoxSampleFlags() {
        SampleFlags sf = new SampleFlags();
        sf.setReserved(0);
        sf.setIsLeading((byte) 0);
        sf.setSampleDependsOn(2);
        sf.setSampleIsDependedOn(0);
        sf.setSampleHasRedundancy(0);
        sf.setSamplePaddingValue(0);
        sf.setSampleIsDifferenceSample(false);
        sf.setSampleDegradationPriority(0);
        return sf;
    }

    /**
     Creates one or more track run boxes for a given sequence.

     @param track source of the samples
     @return track run box
     */
    private TrackRunBox computeTrackRunBox(Track track) {
        TrackRunBox trb = new TrackRunBox();
        trb.setVersion(1);
        long endSample = track.getSamples().size() - 1;
        long[] sampleSizes = getSampleSizes(track);

        trb.setSampleDurationPresent(true);
        trb.setSampleSizePresent(true);
        List<TrackRunBox.Entry> entries = new ArrayList<>(l2i(endSample));


        List<CompositionTimeToSample.Entry> compositionTimeEntries = track.getCompositionTimeEntries();
        int compositionTimeQueueIndex = 0;
        CompositionTimeToSample.Entry[] compositionTimeQueue =
                compositionTimeEntries != null && compositionTimeEntries.size() > 0 ?
                        compositionTimeEntries.toArray(new CompositionTimeToSample.Entry[0]) : null;
        long compositionTimeEntriesLeft = compositionTimeQueue != null ? compositionTimeQueue[compositionTimeQueueIndex].getCount() : -1;


        trb.setSampleCompositionTimeOffsetPresent(compositionTimeEntriesLeft > 0);

        // fast-forward composition stuff

        boolean sampleFlagsRequired = (track.getSampleDependencies() != null && !track.getSampleDependencies().isEmpty() ||
                track.getSyncSamples() != null && track.getSyncSamples().length != 0);

        trb.setSampleFlagsPresent(sampleFlagsRequired);

        for (int i = 0; i < sampleSizes.length; i++) {
            TrackRunBox.Entry entry = new TrackRunBox.Entry();
            entry.setSampleSize(sampleSizes[i]);
            if (sampleFlagsRequired) {
                //if (false) {
                SampleFlags sf = new SampleFlags();

                if (track.getSampleDependencies() != null && !track.getSampleDependencies().isEmpty()) {
                    SampleDependencyTypeBox.Entry e = track.getSampleDependencies().get(i);
                    sf.setSampleDependsOn(e.getSampleDependsOn());
                    sf.setSampleIsDependedOn(e.getSampleIsDependedOn());
                    sf.setSampleHasRedundancy(e.getSampleHasRedundancy());
                }
                if (track.getSyncSamples() != null && track.getSyncSamples().length > 0) {
                    // we have to mark non-sync samples!
                    if (Arrays.binarySearch(track.getSyncSamples(), i) >= 0) {
                        sf.setSampleIsDifferenceSample(false);
                        sf.setSampleDependsOn(2);
                    } else {
                        sf.setSampleIsDifferenceSample(true);
                        sf.setSampleDependsOn(1);
                    }
                }
                // i don't have sample degradation
                entry.setSampleFlags(sf);

            }

            entry.setSampleDuration(track.getSampleDurations()[l2i(i)]);

            if (compositionTimeQueue != null) {
                entry.setSampleCompositionTimeOffset(compositionTimeQueue[compositionTimeQueueIndex].getOffset());
                if (--compositionTimeEntriesLeft == 0 && (compositionTimeQueue.length - compositionTimeQueueIndex) > 1) {
                    compositionTimeQueueIndex++;
                    compositionTimeEntriesLeft = compositionTimeQueue[compositionTimeQueueIndex].getCount();
                }
            }
            entries.add(entry);
        }

        trb.setEntries(entries);
        return trb;
    }

    /**
     Gets the chunk sizes for the given track.

     @param track the track we are talking about
     @return the size of each chunk in number of samples
     */
    int[] getChunkSizes(Track track) {

        long[] referenceChunkStarts = fragmenter.sampleNumbers(track);
        int[] chunkSizes = new int[referenceChunkStarts.length];


        for (int i = 0; i < referenceChunkStarts.length; i++) {
            long start = referenceChunkStarts[i] - 1;
            long end;
            if (referenceChunkStarts.length == i + 1) {
                end = track.getSamples().size();
            } else {
                end = referenceChunkStarts[i + 1] - 1;
            }

            chunkSizes[i] = l2i(end - start);
        }
        assert CustomFragmentMp4Builder.this.track2Sample.get(track).size() == sum(chunkSizes) : "The number of samples and the sum of all chunk lengths must be equal";
        return chunkSizes;
    }

    private long[] getSampleSizes(Track track) {
        List<Sample> samples = track.getSamples();

        long[] sampleSizes = new long[samples.size()];
        for (int i = 0; i < sampleSizes.length; i++) {
            sampleSizes[i] = samples.get(i).getSize();
        }
        return sampleSizes;
    }

    private TrackFragmentBaseMediaDecodeTimeBox computeTrackFragmentBaseMediaDecodeTimeBox() {
        var tfbmdt = new TrackFragmentBaseMediaDecodeTimeBox();
        tfbmdt.setBaseMediaDecodeTime(0);
        return tfbmdt;
    }

    /**
     Class for interleaved chunk mdat
     */
    private static class InterleaveChunkMdat implements Box {
        List<Track> tracks;
        List<List<Sample>> chunkList = new ArrayList<>();

        long contentSize;

        private InterleaveChunkMdat(Movie movie, Map<Track, int[]> chunks, long contentSize) {
            this.contentSize = contentSize;
            this.tracks = movie.getTracks();
            List<Track> tracks = new ArrayList<>(chunks.keySet());
            tracks.sort((o1, o2) -> l2i(o1.getTrackMetaData().getTrackId() - o2.getTrackMetaData().getTrackId()));
            Map<Track, Integer> trackToChunk = new HashMap<>();
            Map<Track, Integer> trackToSample = new HashMap<>();
            Map<Track, Double> trackToTime = new HashMap<>();
            for (Track track : tracks) {
                trackToChunk.put(track, 0);
                trackToSample.put(track, 0);
                trackToTime.put(track, 0.0);
            }

            while (true) {
                Track nextChunksTrack = null;
                for (Track track : tracks) {
                    if ((nextChunksTrack == null || trackToTime.get(track) < trackToTime.get(nextChunksTrack)) &&
                            // either first OR track's next chunk's start time is smaller than nextTrack's next chunks start time
                            // AND their need to be chunks left!
                            (trackToChunk.get(track) < chunks.get(track).length)) {
                        nextChunksTrack = track;
                    }
                }
                if (nextChunksTrack == null) {
                    break;
                }
                // found the next one

                int nextChunksIndex = trackToChunk.get(nextChunksTrack);
                int numberOfSampleInNextChunk = chunks.get(nextChunksTrack)[nextChunksIndex];
                int startSample = trackToSample.get(nextChunksTrack);
                double time = trackToTime.get(nextChunksTrack);
                for (int j = startSample; j < startSample + numberOfSampleInNextChunk; j++) {
                    time += (double) nextChunksTrack.getSampleDurations()[j] / nextChunksTrack.getTrackMetaData().getTimescale();
                }
                chunkList.add(nextChunksTrack.getSamples().subList(startSample, startSample + numberOfSampleInNextChunk));

                trackToChunk.put(nextChunksTrack, nextChunksIndex + 1);
                trackToSample.put(nextChunksTrack, startSample + numberOfSampleInNextChunk);
                trackToTime.put(nextChunksTrack, time);
            }
        }

        public String getType() {
            return "mdat";
        }

        public long getSize() {
            return 16 + contentSize;
        }

        private boolean isSmallBox(long contentSize) {
            return (contentSize + 8) < 4294967296L;
        }

        public void getBox(WritableByteChannel writableByteChannel) throws IOException {
            ByteBuffer bb = ByteBuffer.allocate(16);
            long size = getSize();
            if (isSmallBox(size)) {
                IsoTypeWriter.writeUInt32(bb, size);
            } else {
                IsoTypeWriter.writeUInt32(bb, 1);
            }
            bb.put(IsoFile.fourCCtoBytes("mdat"));
            if (isSmallBox(size)) {
                bb.put(new byte[8]);
            } else {
                IsoTypeWriter.writeUInt64(bb, size);
            }
            ((Buffer) bb).rewind();
            writableByteChannel.write(bb);
            long writtenBytes = 0;
            long writtenMegaBytes = 0;

            LOG.debug("About to write {}", contentSize);
            for (List<Sample> samples : chunkList) {
                for (Sample sample : samples) {
                    sample.writeTo(writableByteChannel);
                    writtenBytes += sample.getSize();
                    if (writtenBytes > 1024 * 1024) {
                        writtenBytes -= 1024 * 1024;
                        writtenMegaBytes++;
                        LOG.debug("Written {} MB", writtenMegaBytes);
                    }
                }
            }
        }
    }
}
