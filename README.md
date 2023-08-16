[![Continuous Integration](https://github.com/xjmusic/workstation/actions/workflows/main_ci.yml/badge.svg)](https://github.com/xjmusic/workstation/actions/workflows/main_ci.yml)
[![Build Distribution](https://github.com/xjmusic/workstation/actions/workflows/tag_dist.yml/badge.svg)](https://github.com/xjmusic/workstation/actions/workflows/tag_dist.yml)

# XJ music workstation

XJ music pioneers the evolution of background audio with our innovative music engine enabling artists to compose new possibilities for streams, games, and spaces.

*Copyright (c) XJ Music Inc. All Rights Reserved.*

[U.S. Patent 10,446,126](https://patents.google.com/patent/US10446126B1/)

[xjmusic.com](https://xjmusic.com)

## Private Gradle (Maven) Package Repository

In order to access the private package we publish to [GitHub Packages](https://github.com/xjmusic/hub/packages/1917788) at  you must authenticate with GitHub. To do so, you
will need to create a personal access token with the `read:packages` scope. See the [GitHub documentation](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry#authenticating-to-github-packages)

Once you have created a personal access token, you can authenticate with the GitHub Packages registry by adding the
token to a **gradle-local.properties** file you create in the root of this project.

NOTE: the **gradle-local.properties** file is ***NOT*** checked in to Git because it contains a secret.

Your **gradle-local.properties** file should look like this:

```properties
repo.username=my-username
repo.token=ghp_hgjkag8879731hjJKJYKJ78798KJHsf78221
```

## Art

See the **art/** folder. By Accessing the contents of that folder, you agree to these terms:

> Please only read these files on your machine and then delete. Please do Not email that file anywhere, or upload it to
> any other servers. These files are confidential property of XJ Music Inc.

See specifically,
the [XJ music User Guide](https://docs.google.com/document/d/1Jp1DT7jJ3Xn1pR5495Yh4TeStTGanvuCF1du0uEUy1A/)

### Architecture

Here's the general architecture of the XJ Music platform backend
workstation. [(Download PDF)](art/XJLabStreamingSegmentsArchitecture.pdf)

![XJ Lab Streaming Segments Architecture](art/XJLabStreamingSegmentsArchitecture.svg)

![Mixer Design v6](art/MixerDesignV6_XJ_Music.jpg)

## Axioms

* Any network connection can and will fail.
* There are no launches, pertaining instead only to the spanning of time, and the availability of said platform and its
  components.
* The platform does not implement passwords; it relies on OAuth.
* The platform does not send or receive email; it relies on vendors for all external communications.

## Workflow

* Describe features as the desire of a person to take an action for a particular reason, e.g. "Artist wants Sequence and
  Pattern to be named according to musical norms, in order to make the most sense of XJ as a musical instrument."
* Describe bugs as expectation versus actual, e.g. "Artist expects to be able to list Audios after deleting an Audio
  from an Instrument," then:
  - DESCRIBE LIKE THIS: "I clicked the button labeled 'Turn;' I expected the Earth to turn; actually, it stood still."
  - NOT LIKE THIS: "I click the button and nothing happened."
* Commits reference issues by id #.
* Time is tracked against issues by id #.
* Branches are named according to `git-flow` including issue id #, e.g.:
  - Features are `feature/123-do-new-thing`
  - Bug Fixes are `bugfix/4567-should-do-this`
  - Hot Fixes are `hotfix/890-should-do-that`
* `TODO` comments are used only in working branches. Upon completion of branch shipWork, any remaining `TODO` should be
  a new tracker issue.

## Dependencies

* Java 18
* Gradle 8

## Chain Work

This term refers (in the **xj** universe) to a layer of shipWork performed on the Segments (sequentially, by their
offset)
in a Chain.

## Getting Started

To compile the Java server-side applications and package them for deployment:

    gradle clean assemble

## Integration testing

Run all tests with Gradle

    gradle test

## Cleanup

Clean all build targets:

    gradle clean

## Maven

To clean and build all artifacts:

    gradle clean compileJava

To clean, build, test and assemble artifacts for shipment:

    gradle clean test assemble

## Google Authentication

Login to the app using Google authentication. The redirect URL for local development
is http://xj.io/auth/google/callback

## Library

**Mixer** is a Java implementation of the Go project [go-mix](https://github.com/go-mix/mix).

**Music** is a Java implementation of the Go project [go-music-theory](https://github.com/go-music-theory/music-theory).

A **Note** is used to represent the relative duration and pitch of a sound.

The **Key** of a piece is a group of pitches, or scale upon which a music composition is created in classical, Western
art, and Western pop music.

A **Chord** is any harmonic set of three or more notes that is heard as if sounding simultaneously.

A **Scale** is any set of musical notes ordered by fundamental frequency or pitch.

## Telemetry

Contained in the `lib/telemetry` module.

## Apps

### nexus

Fabricates a musical audio composite from source sequences and instrument-audio. Built with Java Spring Boot.

### ship

Mixes and ships finished audio data to delivery. Built with Java Spring Boot.

### hub

Central structured data and business logic. Built with Java Spring Boot.

Depends on `lib` components

Connects to:

* S3 Filesystem

## Healthcheck Endpoint

**GET /healthz**

## Intro to Google OAuth2

https://developers.google.com/+/web/samples/java

## IntelliJ IDEA

Here's the official XJ Music Inc copyright Velocity template:

    Copyright (c) 1999-${today.year}, XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

## Troubleshooting the build

If you see an error having to do with destroying the build artifacts:

```text
> Task :hub:clean FAILED

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':hub:clean'.
> java.io.IOException: Unable to delete directory '/home/charney/xj/alpha-platform/hub/build'
    Failed to delete some children. This might happen because a process has files open or has its working directory set in the target directory.
    - /home/charney/xj/alpha-platform/hub/build/distributions/hub-1.0.tar
    - /home/charney/xj/alpha-platform/hub/build/distributions
```

It may be necessary to change all the permissions so your user owns the working tree:

```bash
sudo chown -R $(id -u):$(id -g) .
```

## FFmpeg

Instead of using ffmpeg on the command line, we use a bundled library https://github.com/bytedeco/javacv

### Convert all WAV files in folder to MP3

```shell
for n in *.wav; do ffmpeg -i "$n" -f mp3 "${n%.*}.mp3"; done
```

### MP4 Fragment (reference)

Sequence offset 0 - generated by ffmpeg

[](ship/src/test/resources/chunk_reference_outputs/test5-128k-151304042-ffmpeg.yaml)

### Generated MP4 Fragment

```
SegmentTypeBox:
  majorBrand: msdh
  minorVersion: 0
  compatibleBrand: msdh
  compatibleBrand: msix

SegmentIndexBox:
  entries:
    - Entry:
        referenceType: 0
        referencedSize: 64690
        subsegmentDuration: 480000
        startsWithSap: 1
        sapType: 0
        sapDeltaTime: 0
  referenceId: 1
  timeScale: 48000
  earliestPresentationTime: 0
  firstOffset: 0
  reserved: 0

MovieFragmentBox:
  MovieFragmentHeaderBox:
    sequenceNumber: 1
  
  TrackFragmentBox:
    TrackFragmentHeaderBox:
      trackId: 1
      baseDataOffset: -1
      sampleDescriptionIndex: 0
      defaultSampleDuration: 1024
      defaultSampleSize: 111
      defaultSampleFlags: 
        - SampleFlags:
            reserved: 0
            isLeading: 0
            depOn: 2
            isDepOn: 0
            hasRedundancy: 0
            padValue: 0
            isDiffSample: false
            degradPrio: 0     
      durationIsEmpty: false
      defaultBaseIsMoof: true
    
    TrackFragmentBaseMediaDecodeTimeBox:
      baseMediaDecodeTime: 0
    
    TrackRunBox:
      sampleCount: 470
      dataOffset: 3868
      dataOffsetPresent: true
      sampleSizePresent: true
      sampleDurationPresent: true
      sampleFlagsPresentPresent: false
      sampleCompositionTimeOffsetPresent: false
      firstSampleFlags: null
    
  

org.mp4parser.boxes.iso14496.part12.MediaDataBox@720c8f80
```

# Docker

For example, an artist/engineer runs the Nexus on their local machine using the Docker image:

```shell
docker run gcr.io/xj-vpc-host-prod/nexus:latest \
  -e AUDIO_BASE_URL=https://audio.xj.io/ \
  -e ENVIRONMENT=production \
  -e SHIP_BASE_URL=https://ship.xj.io/ \
  -e SHIP_KEY=bump_chill \
  -e SHIP_MODE=wav \
  -e SHIP_WAV_PATH=/tmp/bump_chill.wav \
  -e SHIP_WAV_SECONDS=300 \
  -e YARD_LOCAL_MODE_ENABLED=true
```

## Base Docker Image for Nexus

Base Container required to ship Nexus as a container via jib
