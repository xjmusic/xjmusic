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

### Gradle (Maven) Package Repository

In order to access the `io.xj.models` package we publish
to [github.com/xjmusic/hub](https://github.com/xjmusic/hub/packages/1917815), you must authenticate with GitHub using an
account with read access to the **xjmusic/hub** repository. To do so, you
will need to create a personal access token with the `read:packages` scope. See
the [GitHub documentation](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry#authenticating-to-github-packages)

Once you have created a personal access token, you can authenticate with the GitHub Packages registry by adding the
token to a **gradle-local.properties** file you create in the root of this project.

NOTE: the **gradle-local.properties** file is ***NOT*** checked in to Git because it contains a secret. However, there is a [gradle-local.example.properties](gradle-local.example.properties) file that you can copy and use as a template.

Your **gradle-local.properties** file should look like this:

```properties
repo.username=my-username
repo.token=ghp_hgjkag8879731hjJKJYKJ78798KJHsf78221
```

### Running the application

This project is built with Gradle. To run the XJ music workstation, run:

```shell
./gradlew :gui:bootRun
```

You should then see the JavaFX GUI open the main window.

Press the hamburger menu next to the last button to open the fabrication configuration pane. In the field called "Input Template Key" you'll see the default value `slaps_lofi` but you can also try the
values `space_binaural` or `space_flow`

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

