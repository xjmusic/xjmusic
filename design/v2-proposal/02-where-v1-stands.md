# 02 · Where V1 stands

An honest inventory of what exists at version 1.9.6, what is strong enough to build on, and where the gaps are against the vision in [01-vision.md](01-vision.md). Everything here was checked against the code in this repository; where a statement is an inference rather than a fact read from source, it says so.

## Inventory

| Component | Where | Language | Size | Build and ship |
|---|---|---|---|---|
| Engine | [`engine/`](../../engine/) | C++17 | ~20,000 lines across `src/` and `include/` | CMake 3.24; vendored deps (nlohmann-json 3.11.3, yaml-cpp 0.8.0, googletest 1.15.0, SDL 2.30.5, FTXUI 5.0.0); CI on Ubuntu, macOS 13 and 14, Windows; static library attached to GitHub releases on tag; Doxygen to engine-docs.xjmusic.com |
| Workstation | [`workstation/`](../../workstation/) | Java 17 | ~47,000 lines in 294 files | Gradle 8, JavaFX 20, Spring Boot 3.1, javacpp for ffmpeg; jpackage to DMG (signed), DEB, EXE |
| Unreal plugin | separate repo `xjmusic/UnrealPlugin` | C++ | — | Consumes the engine's C++ API |
| Demo content | separate repo `xjmusic/xjmusic-demos` | JSON + audio | — | Cloned by the workstation on request |
| Docs | separate repo `xjmusic/xjmusic-docs` | Markdown | — | docs.xjmusic.com |

Both the engine and the workstation are at 1.9.6, and the release workflow checks that the two version numbers agree with the tag.

## The engine, as it is

**Lifecycle.** An `XJ::Engine` is constructed from a path to a `.xj` project file plus a control mode and four timing parameters (craft-ahead, dub-ahead, deadline, persistence window). `start(template)` clears the segment store, loads the project and template content, and begins work. The host calls `RunCycle(atChainMicros)` one to three times per second and receives a list of `AudioScheduleEvent`s, each a Create, Update or Delete of an `ActiveAudio` that names a pick, an instrument, an audio file, a start and stop time in chain microseconds, a from-volume and a to-volume, and a release time. `finish()` stops.

**Work.** `WorkManager` runs two cooperating cycles. `CraftWork` keeps the chain crafted ahead of now by `craftAheadMicros` (default 20 s) with a `deadlineMicros` (default 1 s) and discards segments older than the persistence window (default 3600 s). `DubWork` turns crafted segments within `dubAheadMicros` (default 10 s) into the set of active audios, applying the intensity override. An override of the macro program or the taxonomy memes causes a rewrite: future segments are deleted, the current segment is cut short at the next sub-section boundary after the point already dubbed (a multiple of bar beats times the main program's `cutoffMinimumBars`), and fabrication resumes with the override in force.

**Craft pipeline, per segment.** `MacroMainCraft` selects the macro program and main program (or continues them), and from them fixes the segment's type (Initial, Continue, NextMain, NextMacro), key, tempo, chords, chord voicings per instrument type, memes and intensity. `BeatCraft` arranges drum material. `DetailCraft` arranges bass, pads, stabs, sticky and stripe layers, hooks and percussion loops, honoring intensity layers and thresholds from the template configuration. `TransitionCraft` and `BackgroundCraft` add their instrument types. Every craft stage reads the foundation through the `Fabricator`, never directly from the macro or main program, which matters a great deal for V2.

**Choice machinery.** `MarbleBag` makes phased, weighted random choices ("always prefer a directly bound program or instrument when one exists"). `MemeStack` enforces the anti, numeric, unique and taxonomy-category axioms so no segment carries contradictory meaning. `NotePicker` and `RankedNote` choose one voicing note per event note within an optimal range, seeking inversions for the instrument types configured to do so. `SegmentRetrospective` lets Continue-type segments remember what the current main program has already done. `StickyBun` persists "notes in the margin" so a given event keeps the same random choices across segments.

**Content.** A project is a `.xj` JSON file next to `libraries/<Library>/<Program or Instrument>/<name>.json` and `templates/<Template>/<name>.json`, all in the `HubContent` payload shape (flat arrays of entities keyed by UUID). Audio is referenced by `waveformKey`. The workstation's Build step pre-renders every referenced audio file into `build/<uuid>.wav` at the target sample rate, bit depth and channel count, and the engine's hosts play those files.

**Audio.** The engine does not render audio. It emits a schedule, and the host plays files. The example terminal player in [`engine/example/`](../../engine/example/) loads each scheduled WAV with SDL2 and plays it at the scheduled time with the scheduled volume. `ActiveAudio` carries volumes only. There is no pitch shifting: the engine chooses the sample whose recorded `tones` match the required notes, which is the isometric selection the patent describes. There is no time stretching: pick lengths are computed in beats at the segment tempo, but no component stretches audio, so loop audio must be recorded at the tempo it will be played at.

**Control surface.** Three control modes (Auto, Macro, Taxonomy) and three live inputs: `doOverrideMacro`, `doOverrideMemes`, `setIntensityOverride`. Read access to the segment store (segments, choices, arrangements, picks, chords, voicings, memes, messages, metas) for display.

**Randomness.** `std::random_device` seeds a `std::mt19937` in `Fabricator`, `MarbleBag`, `StickyBun` and `ValueUtils`. Two runs of the same project never produce the same chain, and there is no way to ask for one that does.

**Tests.** 119 test files under [`engine/test/`](../../engine/test/). The craft tests form a matrix: each craft stage × each segment type (Initial, Continue, NextMain, NextMacro) × with and without a program voice × layered voices. These encode the musical expectations of the system and are the most valuable artifact in the repository after the craft code itself.

## The workstation, as it is

**A content editor first.** The main window is a content browser over libraries, programs, instruments and templates. The program editor has three modes: bind mode (which sequences are bound at which offsets), chord edit mode (chords and per-instrument-type voicings on a timeline) and event edit mode (voices, tracks, patterns and events). The instrument editor lists audios and opens a per-audio editor with a waveform view and the config fields. The template editor manages bindings and the HOCON configuration, and exports.

**A fabrication tab second.** A timeline of segments with their choices, plus controls for control mode, macro and meme override, and intensity. The controls are built in code rather than FXML. There is no controller mapping, no MIDI, no OSC and no network of any kind; a search of the workstation source for MIDI or OSC finds nothing.

**Its own engine.** The workstation does not use the C++ engine. It plays through a Java implementation of craft, fabricator and work under `io.xj.engine`, a second, parallel implementation of the same patented logic. On top of that sits a Java mixer (`MixerImpl`: one bus per instrument type, a look-ahead compressor that smooths its ratio every frame, logarithmic dynamic-range compression, high-pass and low-pass filters, and deliberately no normalization) and a ship stage that plays locally, writes WAV, or chunks HLS. Audio format conversion goes through ffmpeg via javacpp, a heavy native dependency bundled into the jar.

**Project management.** Create, open, clone a demo from GitHub, save (pretty, stably ordered JSON), Build (pre-render audio for the engine), Export.

**Cloud-era remnants.** A hub client, HTTP utilities, a JSON:API layer, user/auth/project-user entities and a telemetry package remain from the hub.xj.io platform described in [XJ-Legacy-Mobile-App-Live-Streaming-Model.md](../XJ-Legacy-Mobile-App-Live-Streaming-Model.md).

## What V1 got right

These are the things V2 must not lose.

1. **The content model.** Programs as partial compositions with sequences bound at offsets; instruments as groups of samples with musical metadata; memes as portable meaning; templates as the rules of a show; intensity as a continuous dimension that every layer responds to. It is expressive, stable, and already contains the ideas the venue vision needs.
2. **The foundation/layers seam.** Macro/main craft fixes the harmonic and rhythmic foundation; every other craft stage reads it through the `Fabricator`. This is exactly the seam along which a Session can replace a station's own foundation with a shared one.
3. **Override and rewrite.** The engine can already discard its planned future and re-plan from a new instruction, cutting the current segment short to do so. Live control in V2 is a matter of making this finer and quantized, not of inventing it.
4. **Portability.** The engine is C++17 with vendored dependencies, builds on three platforms in CI, and already runs inside a game engine.
5. **The test matrix.** The craft tests are the specification of the music. They survive into V2 unchanged.
6. **A plain, durable project format.** JSON files and audio files in folders. Diffable, backup-friendly, and simple to extend.

## Gaps against the vision

Numbered so later documents can refer to them.

| # | Gap | Evidence | Addressed in |
|---|---|---|---|
| G1 | **Two engines.** The workstation runs Java craft; everything else runs C++ craft. Divergence is a standing risk and every fix is done twice. | `io.xj.engine.craft` alongside `engine/src/craft` | [03](03-architecture.md) |
| G2 | **No real-time audio in the engine.** Every host reimplements playback; the Java mixer's bus and dynamics design is unavailable outside Java. | `ActiveAudio` carries volumes only; example app plays WAVs via SDL | [05](05-workstation.md) |
| G3 | **No external clock.** Tempo is a property of content. No beat phase is exported, no external tempo is accepted, nothing can synchronize two machines. | `Segment.tempo` set in `MacroMainCraft` | [04](04-engine.md), [07](07-session-protocol.md) |
| G4 | **No external harmony.** Key, chords and voicings derive only from the station's own main program choice. | `MacroMainCraft` | [04](04-engine.md), [07](07-session-protocol.md) |
| G5 | **Coarse live control.** Three overrides; a rewrite discards the entire future; cutoff is by a minimum bar count, not by a chosen boundary; no harmonic cue, no per-layer control, no instantaneous gestures. | `CraftWork::doFabricationRewrite`, `doCutoffLastSegment` | [04](04-engine.md), [08](08-performer-input.md) |
| G6 | **Nondeterminism.** Unseeded `random_device` in four places. A performance cannot be replayed, a bug cannot be reproduced from a seed, two stations cannot be compared. | `Fabricator.cpp`, `MarbleBag.cpp`, `StickyBun.cpp`, `ValueUtils.cpp` | [04](04-engine.md) |
| G7 | **No pitch shift, no time stretch.** Instruments must be deeply multisampled and loops recorded at the playing tempo. This raises the bar for content so high that a commons is impractical without it. | `ActiveAudio`, `MixerImpl.mix` | [05](05-workstation.md) |
| G8 | **Heavy audio toolchain.** ffmpeg via javacpp for conversion; a bake step per project before the engine can play. | `FFmpegUtils`, Build | [06](06-commons.md) |
| G9 | **Editor-first UI; the live surface is a monitor.** No controller input; no mapping; a look and feel that does not read as a musical instrument. | `io.xj.gui` | [05](05-workstation.md), [08](08-performer-input.md) |
| G10 | **No content distribution model.** Demos are cloned from a git repository. No pack format, no content hashes, no dependencies, no license metadata per pack. | `ProjectManager` demo cloning | [06](06-commons.md) |
| G11 | **Legacy cloud code.** Hub client, JSON:API, user and auth entities, telemetry. | `io.xj.engine.hub_client` et al. | [03](03-architecture.md) |
| G12 | **The machine is not yet legible enough.** The UI shows what was chosen, not why, and not what a control would do if pushed. | Fabrication timeline | [04](04-engine.md), [05](05-workstation.md) |

None of these gaps is in the craft logic. That is the single most important finding of this assessment: the thing the patent protects and the tests specify is sound, and the work is in everything around it.
