# 01 · The vision, stated precisely

## Where it comes from

In 2001 and 2002 the rig was two Akai MPC 2000 samplers linked to one hard drive. All samples and sequences were authored on one machine and saved to the shared drive, and either machine could load and save from it. Networking a sampler was unusual then. The point of it was not convenience. It was that two semi-independent players could work from one body of material: one machine sustaining a base layer, the other disrupting it, both playing the same show.

The vision that grew out of that rig, and that the XJ music project was started to realize, is this:

> A venue with several performance stations, each overlooking the stage. Every station is an XJ music station. Each artist arrives with material they have prepared, and every artist also has access to a common library of material that belongs to that show. Because machine intelligence handles the lowest level of the electronic music performance, the performer's role is elevated: they are there to connect with the audience and with the other performers. The system sources events and decision information from the performer and turns them into musical choices and, ultimately, into the mix. Two performers can play at the same time, trading back and forth, and both stations know they are in the same tempo and following the same chord changes. However different the material each introduces, they never leave the shared tempo and harmony.

The project as it stands today, an adaptive music engine and a content workstation for video games, has not arrived there. The engine is the right mathematical framework; the surrounding platform was never built.

## What the vision actually requires

Stated as requirements rather than imagery:

1. **A shared musical foundation across machines.** Tempo, meter, beat phase, key and chord changes are the same on every station at the same wall-clock moment, including through changes, and the shared state is known far enough ahead that each station can prepare material against it.
2. **Independent material on top of a shared foundation.** Each station chooses and plays its own rhythmic, melodic, textural and transitional material. Material is chosen *against* the shared harmony and tempo, so it always fits.
3. **A common library per show, plus personal libraries.** Material is portable between stations, addressable, versioned, licensed for the purpose, and good enough to be worth playing.
4. **Performer input at several time scales.** Some decisions are about the next few minutes (where the music is going), some about the next bar (when a change lands), some about this instant (a hit, a sweep, a drop). The system must accept all three and route them to the right layer.
5. **The performer sees what the machine is doing.** Lifting the performer above the sequencer only works if the machine's choices are legible: what it is playing, what it is about to play, and what it will do if you push a given control.
6. **Out of the box it makes music.** The workstation must sound good within seconds of first launch, with no project to build first.
7. **Everything stays usable alone.** A single station with no network is still the best way to prepare and perform adaptive music. The venue is a mode, not a prerequisite.

## Design principles for V2

These are the rules used throughout the proposal to decide between alternatives.

- **Keep the brain, rebuild the body.** The engine's craft logic (macro/main, beat, detail, transition, background, memes, chords, voicings, picks) is the patented, tested core. It stays in C++ and stays the single source of musical truth. Everything that touches an audio device, a screen, a controller or a network is new.
- **Plan ahead, react now, never confuse the two.** Anything that changes the foundation is a cue honored at a musical boundary. Anything that must happen this instant acts on audio already scheduled. The two paths are separate in the architecture, not just the UI.
- **One content model.** A game soundtrack and a live set are the same content: programs, instruments, templates. V2 does not fork the model. Performance features are additive.
- **The network is a mode.** A station never requires a session to run. Joining and leaving a session happens at a segment boundary with no dropout.
- **Legible machine.** Every choice the engine makes is observable in the UI in real time, and every control shows what it will do before you touch it.
- **Determinism where it buys something.** Seeded randomness so a performance can be replayed, a bug reproduced, or two stations compared. Not determinism for its own sake.
- **Ship the game-audio product all the way through.** The current users and revenue are game composers. V2 must remain, and become better at being, the workstation that prepares adaptive music for Unreal and other engines.

## Non-goals for V2

- **Not a general DAW.** No arbitrary audio tracks, no linear timeline editing of recorded takes. XJ fabricates; it does not record multitrack sessions. (Capturing a performance to a chain file and bouncing it to audio is in scope; editing that audio is not.)
- **Not a synthesizer.** V2 remains sample-based. Hosting CLAP or VST3 instruments is a candidate for a later minor version, discussed in [10-risks-and-open-questions.md](10-risks-and-open-questions.md), and the architecture leaves room for it.
- **Not a streaming service.** The 24/7 HLS streaming platform described in [XJ-Legacy-Mobile-App-Live-Streaming-Model.md](../XJ-Legacy-Mobile-App-Live-Streaming-Model.md) is a historical deployment. A V2 station can still ship HLS, because shipping is a sink on the DSP output, but the cloud fleet is not part of this proposal.
- **Not internet-scale sessions.** The Session protocol targets a venue LAN with bounded, measurable latency. Remote sessions over the public internet are a research item.
- **Not a rewrite of the craft algorithms.** They may gain inputs (external chords, seeds, layer claims) and outputs (richer diagnostics). Their musical behavior is not redesigned in V2.
