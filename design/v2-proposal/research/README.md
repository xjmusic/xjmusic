# Research maps

Grounded maps written while turning the proposal into specifications. Each one reads a part of this repository, or of the outside world, and records what is actually there, with file and line references against version 1.9.6 or source links. The specifications in the [roadmap](../roadmap/) cite them, and where a map shows the proposal was wrong about the code, the specification follows the code and says so.

| Map | Covers |
|---|---|
| [engine-api-lifecycle.md](engine-api-lifecycle.md) | `Engine`, `WorkManager`, `CraftWork`, `DubWork`, timing settings, the rewrite path, schedule events, threading |
| [engine-fabricator-foundation.md](engine-fabricator-foundation.md) | What `MacroMainCraft` writes, segment types, intensity and delta, every direct main-program read, randomness sites |
| [engine-craft-stages-choice.md](engine-craft-stages-choice.md) | Beat, detail, transition and background craft; `MarbleBag`; meme grammar; picks; `NotePicker`; diagnostics hooks |
| [engine-content-config.md](engine-content-config.md) | What the engine loads, the JSON payload, entities, enums, the config dialect, unknown-field handling |
| [engine-store-build-tests.md](engine-store-build-tests.md) | Segment store, messages, CMake, the example player, CI and the test matrix |
| [gap-port-regressions-vs-java.md](gap-port-regressions-vs-java.md) | Which 1.9.6 C++ defects are regressions from the Java original and which are inherited behavior |
| [gap-chord-note-grammar.md](gap-chord-note-grammar.md) | Canonical chord, key and note spellings the foundation record and the Chord cue depend on |
| [java-mixer-ship-audio.md](java-mixer-ship-audio.md) | The Java mixer, dub, audio cache and ship stage that `xj-dsp` and `xj-content` replace |
| [java-project-editors-legacy.md](java-project-editors-legacy.md) | Project files, serialization, every editor operation (the Make-mode parity list), legacy cloud code |
| [repo-meta-issues-demos.md](repo-meta-issues-demos.md) | Labels, milestones, open issues and their V2 disposition, the demos, the Unreal plugin, the docs site |
| [external-stack-research.md](external-stack-research.md) | Versions, licenses and gotchas for every crate and SDK the proposal names, and the GitHub APIs used here |
| [gap-link-beatclock-api.md](gap-link-beatclock-api.md) | Ableton Link semantics behind the `BeatClock` trait and the session epoch |
| [gap-midir-device-identity.md](gap-midir-device-identity.md) | What `midir` exposes for controller identity, per platform |
| [gap-output-encoders-loudness.md](gap-output-encoders-loudness.md) | WAV, Vorbis and Opus writers, loudness measurement, and the standards the lint rule can cite |

One further map, on the demo audio files themselves, informed the Commons seeding tasks. It discusses sample provenance in detail and is kept as a private company record rather than published here.
