# XJ music 2.0: a room full of stations, one song

**Play adaptive music like an instrument. Share a library like a crew. Keep a whole room in the same tempo and the same chord changes.**

XJ music began with two samplers wired to one hard drive: one machine holding the groove, the other tearing it apart, both playing the same show. Version 2 builds that room for real. The patented engine that composes adaptive soundtracks for games becomes the heart of a live performance workstation.

**The mission:** lift the performer above the sequencer. The machine handles the lowest level of the music, so you get to do the part that matters: steer it, play it, and connect with the people in front of you.

**Where it stands:** the plan is complete and public, and engineering begins with Engine 2.0. Today's download is XJ music 1.9, and it is the best way to start making adaptive music now.

### For musicians

- **It plays the moment it opens.** No project to build first. The workstation opens in Play mode with a starter pack already making music.
- **You steer, it composes.** Cue where the music goes next and land changes on the beat, the bar or the section. Fire drops, sweeps, fills and stutters in the moment, from a pad controller, a keyboard or the screen.
- **You always know what it will do.** Every choice the engine makes is on screen, and every control shows what it will do before you touch it.
- **Bring your sound, share the stage.** Draw on the open Commons, a shared library of signed, licensed packs, or on your own material. Everything you play is chosen against the shared harmony and tempo, so it fits.
- **Two stations, one song.** Join a session on the venue network and every station follows the same tempo and chord changes, trading the lead back and forth at a section boundary with no dropout. Ableton Link keeps the beat with the rest of your gear.
- **Still made for games.** A game soundtrack and a live set are the same content, and the engine keeps preparing adaptive music for Unreal and other engines.

### For hackers

- **Engine 2.0.** The C++ craft engine stays the single source of musical truth, now behind a stable C ABI, with seeded randomness so a performance can be replayed and two stations compared.
- **A Rust workstation and station runtime** on that ABI: real-time DSP, Link, MIDI and OSC mapping, capture and replay.
- **The Commons.** Versioned, content-addressed, signed packs, a static registry with no server to run, and a lint that understands music.
- **Sessions.** An open protocol for a venue network, in which one station conducts a harmonic timeline published bars ahead of the beat and every other station crafts its own layers against it.
- **Designed in the open, down to the acceptance criteria.** Read [the V2 proposal](design/v2-proposal/README.md), follow [the roadmap](https://github.com/orgs/xjmusic/projects/7) from [its umbrella issue](https://github.com/xjmusic/xjmusic/issues/480), pick an issue, and build it with us. Every hour of merged work earns equity in XJ music, as [described below](#earning-equity-in-xj-music).

The 1.9 workstation stays the editor of record, in maintenance, until the new editors reach parity. Nothing you make today is left behind: both read and write the same project files.

---


[![Engine CI](https://github.com/xjmusic/xjmusic/actions/workflows/engine_ci.yaml/badge.svg)](https://github.com/xjmusic/xjmusic/actions/workflows/engine_ci.yaml)
[![Engine Distro](https://github.com/xjmusic/xjmusic/actions/workflows/engine_distro.yml/badge.svg)](https://github.com/xjmusic/xjmusic/actions/workflows/engine_distro.yml)<br/>
[![Workstation CI](https://github.com/xjmusic/xjmusic/actions/workflows/workstation_ci.yml/badge.svg)](https://github.com/xjmusic/xjmusic/actions/workflows/workstation_ci.yml)
[![Workstation Distro](https://github.com/xjmusic/xjmusic/actions/workflows/workstation_distro.yml/badge.svg)](https://github.com/xjmusic/xjmusic/actions/workflows/workstation_distro.yml)
<br/>
<a href="https://xjmusic.com/license"><img src="https://img.shields.io/badge/license-free_for_indies-4dc81f" alt="License" /></a>
<a href="https://discord.xj.io/">![Discord](https://img.shields.io/discord/1143999288440000522.svg?label=Discord&logo=discord&color=7289DA&labelColor=2C2F33)</a>
<a href="https://www.unrealengine.com/">![Unreal Engine](https://img.shields.io/badge/Unreal-5.4.4%2B-dea309)</a>


<a href="https://xjmusic.com">
  <img width="320" height="320" src="design/xj-music-logo.png" alt="XJ music logo" />
</a>


# Adaptive music engine

XJ music is the first end-to-end solution for adaptive music in video games built specifically and only for music, enabling composers to realize new possibilities.

XJ includes the [XJ music workstation](workstation/README.md), a DAW dedicated to preparing adaptive video game music, and the 
[XJ music engine](engine/README.md) to integrate the product within your game audio system.

Download and install the XJ music workstation on your computer to start creating adaptive music for video games. The latest version of the XJ music workstation can be downloaded from [xjmusic.com/download](https://xjmusic.com/download).

![XJ music workstation in action](workstation/design/xjmusic-workstation-screenshot-fabrication.png)


## Getting started

First, [Download the XJ music workstation](https://xjmusic.com/download) and follow the [installation instructions](https://docs.xjmusic.com/installation/).

Then, read the [getting started documentation](https://docs.xjmusic.com/getting-started/) which will show you how to create one of our Demo projects to get started checking out the workstation right away.

The source files for the demos are hosted [here](https://github.com/xjmusic/xjmusic-demos)

When you're ready to integrate XJ music into your game, check out the [Unreal Engine Plugin](https://github.com/xjmusic/UnrealPlugin/) or go directly for the [C++ playback engine](https://github.com/xjmusic/xjmusic/tree/main/engine/) and [its documentation](https://engine-docs.xjmusic.com/).


## Patented technology, open source codebase

The underlying technology is protected by [U.S. Patent 10,446,126](https://patents.google.com/patent/US10446126B1/)
You may use the XJ music engine completely free for projects with a budget under $250K.
For larger projects, contact licensing@xjmusic.com for a license.


## Community and contributing

XJ music is an ever-growing community of users and developers.

The best way to get in touch with the core engine developers is to join the
[XJ music Discord](https://discord.xj.io).

To get started contributing to the project, see the [contributing guide](CONTRIBUTING.md).
This document also includes guidelines for reporting bugs.

### Earning Equity in XJ music

All contributors to XJ music, including founder, director of engineering, and lead developer Charney Kaye, earn equity in the company based on the following formula: 1 hour of labor = 1 point of equity. Developers report all hours against specific Pull Requests. If you would like to contribute to the XJ music codebase and earn equity in the company, prepare your code in a Pull Request and [contact us](https://xjmusic.com/contact-us/) with a log of your hours corresponding to that work. If your pull request is accepted and merged into the main branch, we'll sign a contract guaranteeing your share of future revenue.


## Documentation

The official documentation is published at [docs.xjmusic.com](https://docs.xjmusic.com).
It is maintained by the XJ music community in its own [GitHub repository](https://github.com/xjmusic/xjmusic-docs).

C++ class documentation for the in-game runtime engine is available at [engine-docs.xjmusic.com](https://engine-docs.xjmusic.com).
