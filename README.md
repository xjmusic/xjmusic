# XJ Music™ platform backend services

*Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.*



## Art

See the **/art** folder. By Accessing the contents of that folder, you agree to these terms:

> Please only read these files on your machine and then delete. Please do Not email that file anywhere, or upload it to 
> any other servers. These files are confidential property of XJ Music Inc.


### Architecture

Here's the general architecture of the XJ Music platform backend services. [(Download PDF)](art/XJ
LabStreamingSegmentsArchitecture.pdf) 

![XJ Lab Streaming Segments Architecture](art/XJLabStreamingSegmentsArchitecture.svg)


## Axioms
  * Any network connection can and will fail.
  * There are no launches, pertaining instead only to the spanning of time, and the availability of said platform and 
    its components.
  * The platform does not implement passwords; it relies on OAuth.
  * The platform does not send or receive email; it relies on vendors for all external communications.
  
  
## Workflow
  * Features are described as the desire of a person to take an action for a particular reason, e.g. "Artist wants 
    Sequence and Pattern to be named according to musical norms, in order to make the most sense of XJ as a musical 
    instrument."
  * Bugs are described as expectation versus actual, e.g. "Artist expects to be able to list Audios after deleting an 
    Audio from an Instrument," then:
    - DESCRIBE LIKE THIS: "I clicked the button labeled 'Turn;' I expected the Earth to turn; actually, it stood still."
    - NOT LIKE THIS: "I click the button and nothing happened."
  * Commits reference issues by id #.
  * Time is tracked against issues by id #.
  * Branches are named according to `git-flow` including issue id #, e.g.:
    - Features are `feature/123-do-new-thing`
    - Bug Fixes are `bugfix/4567-should-do-this`
    - Hot Fixes are `hotfix/890-should-do-that`
  * `TODO` comments are used only in working branches. Upon completion of branch work, any remaining `TODO` should be a 
    new tracker issue.


## Dependencies
  * Java 11
  * Gradle (6+ via SDKMAN!)
  * FDK AAC native libraries (apt `libfdk-aac-dev`)


## Service Ports

Each service has a unique port assignment:

| Service       | Port          |
| ------------- |---------------|
| nexus         | 3002          |


## Chain Work

This term refers (in the **xj** universe) to a layer of work performed on the Segments (sequentially, by their offset) 
in a Chain.


## Getting Started

We use [Docker](https://www.docker.com/) and [Docker Compose](https://docs.docker.com/compose/) for local development.

There is an example configuration called **env.example.conf** in the root of the project. It is up to you, the 
developer, to obtain keys and fill in the values of your own environment variables, in a new file called **env.conf** 
which is never checked in to version control or released with the distribution. So, the use of environment variables is 
federated across development and production deployments, while all actual configurations are kept outside the scope of 
the code.

Once your **env.conf** file is configured, it's time to bring up the server:

    docker-compose up -d

In the above example, `-d` tells Docker to start the containers in the background (as Daemons).

Note that `localhost` simply points to the local loopback. Docker-compose maps maps local port 80 to the `hub01xj1` 
docker container port 80.

To compile the Java server-side applications and package them for deployment:

    gradle clean assemble

To build and deploy the platform during local development, we run this a lot:

    gradle assemble && docker restart hub01xj1 nexus01xj1

For a complete rebuild, including configurations and front-end, we could run:

    docker compose up -d --build

Tail the docker container logs for the `hub` app while it's running (/var/log in the container is mounted from local 
volume ./log):

    tail -f log/hub/*

Or tail container logs for the `nexus` app:

    tail -f log/nexus/*

Only between major platform configuration changes, it may be necessary to force Docker to rebuild the container 
using `--build`:

    docker-compose up -d --build    


## Additional commands

To only setup the workflow and check dependencies:

    bin/setup


## App Configuration

There is an example configuration called **env.example.conf** in the root of the project. It is up to you, the 
developer, to obtain keys and fill in the values of your own environment variables, in a new file called **env.conf** 
which is never checked in to version control or released with the distribution.  Developers modify their local 
**env.conf** file with private keys and ceonfiguration. The **env.conf** file is never committed to the repository, 
because it contains secrets. The **env.example.conf** file is kept up-to-date with all environment variables required 
for the developer to configure.


## Run local platform in Docker containers

Before running the docker container, be sure to package the latest Java build artifacts, with `make` or `bin/assemble`.

Bring up the `hub01xj1` docker container and its required resource containers:

    docker-compose up -d

The `-d` option above runs containers as background daemons, instead of seeing all their `stdin`. Use `docker-compose` 
or `docker` to manage containers from there.

To see running containers:

    docker ps --format

To attach to a container by `<name>`:

    docker attach <name>

To stop all containers:

    docker-compose down

To remove all containers:

    docker-compose rm

To bring up containers with a forced build:

    docker-compose up --build

To run just the `hub01xj1` container, attached via tty:

    docker-compose run xj

The configuration uses volumes such that the latest build artifacts are available without having to rebuild the docker 
container. The container runs as user `root` by default. Project folders are available inside the container as:

    /var/app/current/


## Compile server-side platform

Compile & Package the Java server-side application, e.g. as JAR files:

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

Login to the app using Google authentication. The redirect URL for local development is http://xj.io/auth/google/callback


## Shipping final audio & JSON to Amazon S3

Note that after an audio file is uploaded, it can be played back (on a GNU/Linux system) like:

    curl https://s3.amazonaws.com/xj-dev-audio/62536d52-8600-4941-ac04-a72106079610-instrument-5-audio.wav | aplay

Here are the public-facing Amazon CloudFront-backed URLs for audio files, and their respective Amazon S3 backing:

  * [https://audio.xj.io](https://audio.xj.io) is the production URL, backed by [https://xj-prod-audio.s3.amazonaws.com](https://xj-prod-audio.s3.amazonaws.com)
  * [https://audio.stage.xj.io](https://audio.stage.xj.io) is the staging URL, backed by [https://xj-stage-audio.s3.amazonaws.com](https://xj-stage-audio.s3.amazonaws.com)
  * [https://audio.dev.xj.io](https://audio.dev.xj.io) is the development URL, backed by [https://xj-dev-audio.s3.amazonaws.com](https://xj-dev-audio.s3.amazonaws.com)

**NOTE** that our CloudFront configuration defines two tiers of caching behavior:

  * Files containing NO hyphen will have a short TTL, e.g. **coolambience.json** (this is the chain manifest that is updated frequently)
  * Files containing a hyphen will have a long TTL, e.g. **coolambience-000012598722847.aac** (this is a segment file that never changes)

## Amazon S3

Therefore, it is helpful to be able to sync the audio files from production into the dev environment.

     aws sync s3://xj-prod-audio/ s3://xj-dev-audio/

Note that in order to use that command, the source bucket (xj-prod-audio) must grant `s3:ListBucket` and `s3:GetObject` permission, and the target bucket (xj-dev-audio) must grant `s3:ListBucket` and `s3:PutObject` to the IAM user your AWS CLI is authenticated as.

## Library

Contained in the **[lib](lib/)** folder, these shared modules are dependencies of the XJ Music™ platform backend services built with Java.

**Craft** fabricates a musical audio composite from source sequences and instrument-audio. Built with Java, Guice, Tomcat, Maven.

**Dub** mixes and ships finished audio data to delivery. Built with Java, Guice, Tomcat, Maven.

**Mixer** is a Java implementation of the Go project [https://github.com/go-mix/mix](go-mix).

**Music** is a Java implementation of the Go project [https://github.com/go-music-theory/music-theory](go-music-theory).

A **Note** is used to represent the relative duration and pitch of a sound.

The **Key** of a piece is a group of pitches, or scale upon which a music composition is created in classical, Western art, and Western pop music.

A **Chord** is any harmonic set of three or more notes that is heard as if sounding simultaneously.

A **Scale** is any set of musical notes ordered by fundamental frequency or pitch.


## Telemetry

Contained in the `lib/telemetry` module.

Requires this environment variable set in .env file:

    DD_API_KEY=ffe6de0162d7b2903a673a33139e6604


## Services

Contained in the [service](service/) folder.


### nexus

Central structured data and business logic. Built with Java.

Depends on `lib` components

Connects to:

  * S3 Filesystem



### pulse

This app exists solely to be run in AWS Lambda, and call the Hub /heartbeat endpoint once per minute.


## Healthcheck Endpoint

**GET /-/health**


## Intro to Google OAuth2

https://developers.google.com/+/web/samples/java


## Intro to Jersey and Grizzly2

See [Java SE 8: Creating a Basic REST Web Service using Grizzly, Jersey, and Maven](http://www.oracle.com/webfolder/technetwork/tutorials/obe/java/griz_jersey_intro/Grizzly-Jersey-Intro.html)


## Jersey

[Latest User Guide](https://jersey.java.net/documentation/latest/user-guide.html)


## IntelliJ IDEA

Here's the official XJ Music Inc copyright Velocity template:

    Copyright (c) 1999-${today.year}, XJ Music Inc. (https://xj.io) All Rights Reserved.


## Docker run as non-root user

 - Add the docker group if it doesn't already exist:

        sudo groupadd docker

 - Add the connected user "$USER" to the docker group. Change the user name to match your preferred user if you do not want to use your current user:

        sudo gpasswd -a $USER docker

 - log out/in to activate the changes to groups.

## OSX

On OSX, because we are unable to connect to the container from the host, we are using the following workarounds, which are built in to the cross-platform workflow:

  * Local port 80 (e.g. http://localhost) is mapped to Docker container `hub01xj1` port 80

Docker documentation: https://docs.docker.com/docker-for-mac/networking/#per-container-ip-addressing-is-not-possible
GitHub Open Issue: https://github.com/docker/for-mac/issues/155


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




