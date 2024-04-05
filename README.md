[![Continuous Integration](https://github.com/xjmusic/workstation/actions/workflows/main_ci.yml/badge.svg)](https://github.com/xjmusic/workstation/actions/workflows/main_ci.yml)
[![Build Distribution](https://github.com/xjmusic/workstation/actions/workflows/tag_dist.yml/badge.svg)](https://github.com/xjmusic/workstation/actions/workflows/tag_dist.yml)

# XJ music workstation

XJ music pioneers the evolution of background audio with our innovative music engine enabling artists to compose new
possibilities for streams, games, and spaces.

![Opening Demo Flagship Masthead - XJ music workstation](art/xjmusic-workstation-screenshot-demo.png)

![Live Fabrication Timeline - XJ music workstation](art/xjmusic-workstation-screenshot-fabrication.png)

*Copyright (c) XJ Music Inc. All Rights Reserved.*

[U.S. Patent 10,446,126](https://patents.google.com/patent/US10446126B1/)

[xjmusic.com](https://xjmusic.com)

## Setup

### Dependencies

* Java 17
* Gradle 8


### Running the application

This project is built with Gradle. To run the XJ music workstation, run:

```shell
./gradlew :gui:bootRun
```

You should then see the JavaFX GUI open the main window.

Click the Project menu on the top left-hand side and choose Clone to bring up a selection of four demo projects. Click OK to begin cloning the chosen project onto your machine. Once done, the main window will display all of the libraries included in your select project in the Content tab. Click the Fabrication tab in the top right-hand corner to switch to the Fabrication tab, there you can click Start to begin playing the project. 

Click here for a video walkthrough! https://youtu.be/9utq6PVemsk

## Architecture

The command above (`gradle :gui:bootRun`) invokes the `bootRun` task in the `gui` subproject. The `bootRun` task is a
Gradle task provided by the Spring Boot Gradle plugin. It runs the application in the current JVM.

All the business logic for the application is contained in the `nexus` subproject. The `gui` and `service` sub-projects
provide two different ways of running the business logic, either as a GUI application or as a service.

The `nexus` subproject business logic primarily comprises these packages:

* the `io.xj.nexus.craft` package is the most esoteric. It contains all the logic about fabricating music basic on the
  input content.
* the `io.xj.nexus.dub` package is an audio mixer-- it consumes the output of the craft package above and uses the
  musical choices as an edit decision list to read the source audio files, use ffmpeg via javacpp to do audio
  resampling, add up the audio files, and send the output back as bytes
* the `io.xj.nexus.ship` package consumes the output of the dub package above and sends it either to local system
  output, file output, or HLS stream e.g. youtube output

We recommend starting by ignoring the craft package (very esoteric) and focusing on the dub package (lots of
straightforward algorithms for mixing audio)

## Art

See the **art/** folder. By Accessing the contents of that folder, you agree to these terms:

> Please only read these files on your machine and then delete. Please do Not email that file anywhere, or upload it to
> any other servers. These files are confidential property of XJ Music Inc.

See specifically,
the [XJ music User Guide](https://docs.google.com/document/d/1Jp1DT7jJ3Xn1pR5495Yh4TeStTGanvuCF1du0uEUy1A/)

## Workflow Standards

### Axioms

* Any network connection can and will fail.
* There are no launches, pertaining instead only to the spanning of time, and the availability of said platform and its
  components.
* The platform does not implement passwords; it relies on OAuth.
* The platform does not send or receive email; it relies on vendors for all external communications.

### Writing

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

