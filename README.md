| Production                                                                                                                                                                     | Development                                                                                                                                                                   |
|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [![Production CI](https://github.com/xjmusic/services/actions/workflows/services-prod.yml/badge.svg)](https://github.com/xjmusic/services/actions/workflows/services-prod.yml) | [![Development CI](https://github.com/xjmusic/services/actions/workflows/services-dev.yml/badge.svg)](https://github.com/xjmusic/services/actions/workflows/services-dev.yml) |

|                  | GCP                                                                                                                                                                                 |
|------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| _Base Image_     | [![Base Image (GCP)](https://github.com/xjmusic/services/actions/workflows/services-base.yaml/badge.svg)](https://github.com/xjmusic/services/actions/workflows/services-base.yaml) |

# XJ Music Backend (Java) Services

*Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.*

## Deployment

CI builds & deploys the `prod` branch to Production, and `dev` to Development.

# XJ service deployment on AWS and GCP

## Terraform

We use Terraform to deploy the infrastructure on AWS and GCP. The Terraform code is in the `terraform` folder.

### Amazon Web Service (AWS)

We use AWS to host static content buckets (S3) and the content distribution network (CloudFront).

Install the AWS CLI according to [this tutorial](https://docs.aws.amazon.com/cli/latest/userguide/install-cliv2.html).

### Google Cloud Platform (GCP)

We use GCP to host the database (Cloud SQL) and services (Cloud Run).

Install the GCP CLI according to [this tutorial](https://cloud.google.com/sdk/docs/install).

### Google Cloud Run Services

Our preferred deployment method for all services is Google Cloud Run (serverless) services. For example, to tail the logs of the hub:

```shell
gcloud beta run services logs tail xj-dev-lab-hub --region us-west1 --project xj-vpc-host-prod
```

### Terraform CLI

Install the Terraform CLI according to [this tutorial](https://learn.hashicorp.com/tutorials/terraform/install-cli).

## Terraform Infrastructure

### Frontend Infrastructure

To plan changes to the AWS Terraform configuration (Frontend infra), run:

```shell
cd terraform/aws
terraform init
terraform plan
```

### Backend Infrastructure

Because we use the `gcloud` CLI to deploy updates to Google Cloud Run services, the Terraform state for these services 
will be out of sync with the actual state of the services. To fix this, we need to import the existing services into the
Terraform state. To do this, run:

```shell
cd terraform/gcp
terraform refresh
````

To plan changes to the GCP Terraform configuration (Backend infra), run:

```shell
cd terraform/gcp
terraform init
terraform plan
```

To get an output value, such as the secret id for postgres username and password:

```shell
cd terraform/gcp
terraform output -raw lab_postgres_username
terraform output -raw lab_postgres_password
```

### Connecting to Cloud SQL Postgres

To obtain credentials for the Lab Postgres database, run:

```shell
gcloud secrets versions access latest --secret=xj-lab-postgres-username
gcloud secrets versions access latest --secret=xj-lab-postgres-password
```

To proxy a local port to the Google Cloud SQL (Postgres) instance:

```shell
cloud-sql-proxy postgres-instance-4bda6363 --user=$lab_postgres_username --pass=$lab_postgres_password --database=xj_dev
```

Use the `cloud-sql-proxy` utility to proxy a database connection to the Cloud SQL instance:

```shell
cloud-sql-proxy xj-vpc-host-prod:us-west1:xj-lab-postgres
```

And here's all the steps to connect to the locally proxied database:

```shell
PGUSERNAME=$(gcloud secrets versions access latest --secret=xj-lab-postgres-username)
export PGPASSWORD=$(gcloud secrets versions access latest --secret=xj-lab-postgres-password)
psql --host=127.0.0.1 --port=5432 --user=${PGUSERNAME} xj_dev
```

Note, when importing a SQL dump into a Cloud SQL instance (e.g. [delete-and-insert-all-records.sql](.backup/delete-and-insert-all-records.sql)) be sure to use the proper user (there's an advanced user dropdown)

## Art

See the **/art** folder. By Accessing the contents of that folder, you agree to these terms:

> Please only read these files on your machine and then delete. Please do Not email that file anywhere, or upload it to
> any other servers. These files are confidential property of XJ Music Inc.

See specifically, the [XJ music User Guide](https://docs.google.com/document/d/1Jp1DT7jJ3Xn1pR5495Yh4TeStTGanvuCF1du0uEUy1A/)


### Architecture

Here's the general architecture of the XJ Music platform backend services. [(Download PDF)](art/XJ
LabStreamingSegmentsArchitecture.pdf)

![XJ Lab Streaming Segments Architecture](.art/XJLabStreamingSegmentsArchitecture.svg)

![Mixer Design v6](.art/MixerDesignV6_XJ_Music.jpg)

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
* `TODO` comments are used only in working branches. Upon completion of branch shipWork, any remaining `TODO` should be a
  new tracker issue.

## Dependencies

* Java 16
* Gradle (6+ via SDKMAN!)

## Service Ports

Services expose pose 8080

## Chain Work

This term refers (in the **xj** universe) to a layer of shipWork performed on the Segments (sequentially, by their offset)
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

## Shipping final audio & JSON to Amazon S3

Note that after an audio file is uploaded, it can be played back (on a GNU/Linux system) like:

    curl https://s3.amazonaws.com/xj-dev-audio/62536d52-8600-4941-ac04-a72106079610-instrument-5-audio.wav | aplay

Here are the public-facing Amazon CloudFront-backed URLs for audio files, and their respective Amazon S3 backing:

* [https://audio.xj.io](https://audio.xj.io) is the production URL, backed
  by [https://xj-prod-audio.s3.amazonaws.com](https://xj-prod-audio.s3.amazonaws.com)
* [https://audio.stage.xj.io](https://audio.stage.xj.io) is the staging URL, backed
  by [https://xj-stage-audio.s3.amazonaws.com](https://xj-stage-audio.s3.amazonaws.com)
* [https://audio.dev.xj.io](https://audio.dev.xj.io) is the development URL, backed
  by [https://xj-dev-audio.s3.amazonaws.com](https://xj-dev-audio.s3.amazonaws.com)

**NOTE** that our CloudFront configuration defines two tiers of caching behavior:

* Files containing NO hyphen will have a short TTL, e.g. **coolambience.json** (this is the chain manifest that is
  updated frequently)
* Files containing a hyphen will have a long TTL, e.g. **coolambience-000012598722847.ogg** (this is a segment file that
  never changes)

## Amazon S3

Therefore, it is helpful to be able to sync the audio files from production into the dev environment.

     aws sync s3://xj-prod-audio/ s3://xj-dev-audio/

Note that in order to use that command, the source bucket (xj-prod-audio) must grant `s3:ListBucket` and `s3:GetObject`
permission, and the target bucket (xj-dev-audio) must grant `s3:ListBucket` and `s3:PutObject` to the IAM user your AWS
CLI is authenticated as.

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

    Copyright (c) 1999-${today.year}, XJ Music Inc. (https://xj.io) All Rights Reserved.

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

# MP4 Encoding

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

# Base Docker Image for Nexus

Base Container required to ship Nexus as a container via jib
