# XJ music V2 proposal

*A technical proposal for the next major version of XJ music: a Rust performance workstation built on the C++ engine, a shared content commons, and a venue protocol that keeps many stations in the same tempo and the same chord changes.*

Status: **Proposal / request for comment.** Nothing in this folder is scheduled. The purpose is to answer one question end to end, in enough detail that the answer can be argued with:

> Is the original XJ vision, a room full of performance stations that share a library, share a clock, share a harmonic context, and lift the performer above the sequencer, actually buildable on the patented system as it stands today? If so, what has to change, in what order, and what is genuinely hard?

## The short answer

**Yes, it is buildable, and most of it is work rather than research.** The engine already contains the mathematical core the vision needs: a chain of segments, each crafted from macro and main programs that fix key, tempo, chords and memes, with beat, detail, transition and background layers arranged against that foundation. Everything the venue vision adds sits *around* that core rather than inside it:

| Vision element | What exists in V1 | What V2 adds |
|---|---|---|
| Many stations in the same tempo and chord changes | One autonomous chain per engine; tempo and chords chosen by macro/main craft | A **Session**: one station conducts macro/main, publishes a harmonic timeline a few bars ahead; follower engines craft their own layers against it. Beat phase via Ableton Link. See [07-session-protocol.md](07-session-protocol.md). |
| A common library every artist at the show can draw on | Per-project libraries; demos repo; `.xj` JSON content format | The **Commons**: versioned, content-addressed packs with a license and a registry. The shared MPC hard drive, rebuilt. See [06-commons.md](06-commons.md). |
| The performer lifted above the sequencer | Three live controls: macro override, taxonomy meme override, intensity | A layered **performer input model** with quantized cues, harmonic cues, layer claims, and sample-accurate gestures, mapped from MIDI/OSC/controllers. See [08-performer-input.md](08-performer-input.md). |
| A workstation young musicians want to open | Java 17 / JavaFX / Spring Boot content editor with a fabrication tab | A **Rust** workstation whose first screen is already making music, with the C++ engine embedded through a stable C ABI and a new Rust DSP layer. See [05-workstation.md](05-workstation.md). |

Three things are genuinely hard, and the proposal is honest about them in [10-risks-and-open-questions.md](10-risks-and-open-questions.md):

1. **Latency versus foresight.** The engine plans ahead (craft 20 s, dub 10 s by default). A performer wants the room to react now. The resolution is architectural, not a tuning knob: foundation changes are quantized cues that the engine honors at the next bar or segment, while immediate gestures act on the DSP layer, which knows nothing about segments. The second pass measured the engine as built: the worst case observed, a full rewrite of the planned future on the largest demo project, took 28 ms, so the engine's own cost is not what limits responsiveness ([11-resolutions.md](11-resolutions.md)). The "Three scales, three destinations" section of [08-performer-input.md](08-performer-input.md) spells this out.
2. **Musical quality of a shared commons.** No amount of engineering makes a library sound good. The Commons needs a curatorial process and a quality bar before it needs a registry.
3. **The workstation has to be excellent, not merely ported.** The toolkit decision (native GPU UI versus web-view shell) is deliberately left as a Phase 0 spike with explicit criteria rather than decided on taste.

A second pass over the fourteen open questions ([11-resolutions.md](11-resolutions.md)) closes four outright, reduces four to a yes-or-no from the company or counsel, and leaves the rest waiting on a third party or on a small experiment that now has a declared default. It also measures the engine's craft cost against the real demo content instead of deferring that to a spike.

Multi-station synchronization, the part that sounds most exotic, is the *least* risky part. Ableton Link solved beat-phase sync on a LAN years ago, and the harmonic timeline is a small, slow, bar-aligned message stream that the engine's existing segment model maps onto cleanly.

## Reading order

| # | Document | What it covers |
|---|---|---|
| 1 | [01-vision.md](01-vision.md) | The venue vision restated precisely, design principles, non-goals |
| 2 | [02-where-v1-stands.md](02-where-v1-stands.md) | Grounded assessment of the current engine and workstation, and the gaps |
| 3 | [03-architecture.md](03-architecture.md) | V2 system architecture: crates, processes, boundaries, data flow |
| 4 | [04-engine.md](04-engine.md) | Changes to the C++ engine: C ABI, follow mode, determinism, cue semantics |
| 5 | [05-workstation.md](05-workstation.md) | The Rust workstation: product shape, stack, DSP, first-run experience |
| 6 | [06-commons.md](06-commons.md) | The shared content library: pack format, registry, licensing, curation |
| 7 | [07-session-protocol.md](07-session-protocol.md) | Multi-station sessions: clock, harmonic timeline, roles, protocol |
| 8 | [08-performer-input.md](08-performer-input.md) | How performer gestures become musical decisions and mix changes |
| 9 | [09-roadmap.md](09-roadmap.md) | Phases, exit criteria, sizing, and what we keep from V1 during transition |
| 10 | [10-risks-and-open-questions.md](10-risks-and-open-questions.md) | Risks, alternatives considered, decisions deferred to spikes |
| 11 | [11-resolutions.md](11-resolutions.md) | Second pass: each open question researched, measured where possible, and resolved to a recommendation |
| — | [glossary.md](glossary.md) | Terms used across the proposal, including new ones |

## How to engage with this proposal

This is a PR so it can be reviewed line by line. Comment on the specific claim you disagree with. Where a section proposes a decision, it names the alternatives considered and the criteria, so the fastest way to change the proposal is to attack the criteria.

The intent is that once the shape is agreed, each phase in [09-roadmap.md](09-roadmap.md) decomposes into ordinary GitHub issues. Until then this folder is the single place the plan lives.

## Provenance

The vision comes from the two-MPC rig behind [*XJ Ontic: Live in Boulder* (2002)](https://outrightmental.com/projects/2002/xj-ontic-live-in-boulder-album/): two samplers sharing one hard drive, one holding the base layer while the other disrupts it, played live with no safety net. The engine comes from [U.S. Patent 10,446,126](https://patents.google.com/patent/US10446126B1/) and the code in [`engine/`](../../engine/) and [`workstation/`](../../workstation/) at version 1.9.6. This proposal was drafted with Claude as a research and writing partner, against the code as it stands in this repository.
