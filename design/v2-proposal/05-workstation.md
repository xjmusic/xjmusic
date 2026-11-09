# 05 · The workstation

The Rust desktop application. This document covers what it is for, how it is shaped, what it is built from, and the design of the real-time DSP layer that gives the engine a voice.

## What it is

Three things in one window, in this order of priority:

1. **A musical instrument.** You open it and it is already making music. You touch a control and the room changes. You see what the machine is doing and what it will do next.
2. **A content editor.** Programs, instruments, templates. Everything the Java workstation edits today, rebuilt progressively.
3. **A build tool for game audio.** The same content ships to Unreal and other engines, exactly as it does today, because that is the existing product and it must not regress.

The Java workstation inverted this order. The inversion is the single biggest product change in V2.

## Modes

| Mode | Purpose | First screen |
|---|---|---|
| **Play** | Perform. Performance surface, harmonic strip, layer strips, timeline, transport, session panel. | Yes, on first launch, with a starter pack already playing |
| **Make** | Edit content. Library, program, instrument, template editors. | — |
| **Ship** | Capture a performance, bounce audio, build for a game engine, export a pack. | — |

Modes share one project and one running engine. Switching to Make while playing does not stop the music; edits to content take effect at the next segment boundary, which is already how the engine reloads.

## The performance surface

The physical reference is a 4×4 pad grid plus faders, because that is what the audience the vision describes already owns, and because the MPC is where this started. The on-screen surface is designed so a controller maps onto it one-to-one, and so a laptop keyboard can drive it without a controller.

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ ● LINK 124.0  │ 3.2 │ SESSION "Boulder Night 3" · CONDUCTING · 3 stations  │
├─────────────────────────────┬───────────────────────────────────────────────┤
│  SCENES (macro programs)    │  HARMONY                                      │
│  ┌────┐┌────┐┌────┐┌────┐   │  now  Am ──── F ──── C ──── G  │ next: Dm  …  │
│  │EARTH││FIRE││WATR││WIND│  │  ▲ landing in 2 bars           cue chord ▾     │
│  └────┘└────┘└────┘└────┘   ├───────────────────────────────────────────────┤
│  PALETTES (taxonomy)        │  LAYERS                                       │
│  MOOD   [COOL] WARM  DARK   │  Drum ▮▮▮▮▮▮░░ ● claimed by you              │
│  CITY   DENVER [CHI]  PHL   │  Bass ▮▮▮▮░░░░ ○ claimed by Station 2        │
│  ENERGY LOW   [MID]  HIGH   │  Pad  ▮▮▮░░░░░ ●     Stab ▮░░░░░░░ ○ muted   │
├─────────────────────────────┤  Hook ░░░░░░░░ ·     Perc ▮▮▮▮▮░░░ ●        │
│  INTENSITY  ████████░░ 0.72 │  Bkg  ▮▮░░░░░░ ●     Trans ▮░░░░░░░ ●       │
│  CUE AT   beat  [bar]  seg  ├───────────────────────────────────────────────┤
│  ADVANCE ▶   HOLD ⟲         │  TIMELINE  ◀ past │ now │ future ▶   ⏺ CAPTURE│
└─────────────────────────────┴───────────────────────────────────────────────┘
```

Elements and what they do, each tied to a cue or gesture defined in [08-performer-input.md](08-performer-input.md):

- **Scenes.** One pad per macro program in the template. Press: Macro cue at the selected boundary. Hover or hold: preview ("Fire, A minor, 124, lands in 2 bars") from the engine's preview call.
- **Palettes.** One row per taxonomy category, one button per meme, exclusive within a row as the taxonomy demands. Press: Memes cue.
- **Intensity.** The one continuous control the engine already honors, now with a ramp time.
- **Cue boundary.** Beat, bar, segment, section. Every foundation cue lands at the selected boundary, and the harmony strip shows where that is.
- **Harmony strip.** Current and upcoming chords from the segment store, with the commit horizon drawn as a line. "Cue chord" opens a chord picker that sends a Chord cue.
- **Layers.** One strip per instrument type with meter, mute/solo (immediate gesture on the DSP bus plus a Layer Mute cue for future craft), and the session claim indicator.
- **Advance and Hold.** The two section-level cues.
- **Timeline.** Segments as blocks, past to future, with choices and the landing points of pending cues drawn where they will land.
- **Capture.** Starts recording the chain (segment store), the seed, the cue log and the bounced audio to a capture file. A capture replays deterministically.

The surface never shows a control without showing what it will do. That is the legibility principle from [01-vision.md](01-vision.md) made concrete, and it depends on the preview and diagnostics calls in [04-engine.md](04-engine.md).

## The timeline and the "why" panel

The timeline is the Java fabrication tab's segment view, extended: segments show their type, key, tempo, chords, memes and intensity; each choice shows its program or instrument; clicking a choice opens the "why" panel, which lists the candidates the `MarbleBag` considered, their weights, and the exclusions (meme stack, range, mute). This is the diagnostic output from [04-engine.md](04-engine.md) section 8, and it is how content authors learn what their memes and bindings actually do.

## Editors

Rebuilt in this order, chosen by what the Commons needs first:

1. **Instrument and audio editor.** Import audio, set tones, event, transient, length, loop beats, tempo, intensity and the new pitch shift and stretch allowances. Waveform view with transient and loop markers. This is where pack authors spend their time.
2. **Template editor.** Bindings, taxonomy, intensity layers, mixer settings, the new commit horizon and hold settings.
3. **Program editor**, all three modes: bind, chord, event. The largest editor and the one with the most V1 surface area, so it comes last, and until then the Java workstation remains the program editor of record ([09-roadmap.md](09-roadmap.md)).

Every editor edits the same V1 JSON files the Java workstation writes, so the two can be used on the same project during the transition.

## Technology stack

| Concern | Choice | Notes |
|---|---|---|
| Language | Rust, stable toolchain, workspace of crates as in [03-architecture.md](03-architecture.md) | No nightly features |
| Engine | C++ via the C ABI, built by `cmake` from `build.rs`, statically linked | One binary |
| Audio I/O | `cpal` | WASAPI, CoreAudio, ALSA and JACK out of the box; ASIO behind a feature flag: Steinberg's SDK has been dual GPLv3 or proprietary since October 2025, and official builds enable it only once the proprietary agreement is countersigned ([11-resolutions.md](11-resolutions.md), Q10) |
| Decoding | `symphonia` | WAV, FLAC, MP3, OGG, AAC; pure Rust; no ffmpeg |
| Resampling | `rubato` | Sinc-interpolated, pure Rust |
| Time stretch and pitch shift | Signalsmith Stretch (C++, MIT) via the `signalsmith-stretch` crate; Bungee (MPL-2.0) as the comparison | Rendered ahead of time into the cache, never on the audio thread; see [11-resolutions.md](11-resolutions.md), Q2 |
| MIDI | `midir` | Cross-platform; hot-plug |
| OSC | `rosc` | For controllers and for integrations such as lighting |
| Tempo sync | Ableton Link via `rusty_link` | Link is C++, GPLv2 or Ableton's proprietary license; license review required ([10-risks-and-open-questions.md](10-risks-and-open-questions.md)) |
| Network | `tokio`, `quinn` (QUIC) or plain TCP, `mdns-sd` for discovery | Session protocol in [07-session-protocol.md](07-session-protocol.md) |
| Serialization | `serde` with `serde_json` for V1 formats and the session protocol; `toml` for manifests and settings | |
| Lock-free queues | `rtrb` for commands, `arc-swap` for snapshots | |
| Settings | TOML in the platform config directory | |
| Packaging | Platform bundlers; signed on macOS and Windows; auto-update channel | |

### The UI toolkit decision

This is deliberately not decided here. It is the decision with the highest product impact and the least engineering certainty, so it gets a two-week spike in Phase 0 that builds the same slice (the scenes grid, the harmony strip with a live commit-horizon line, four layer strips with real meters at 60 fps, driven by a real running engine) in two candidates, on all three platforms, then decides on measured criteria.

**Candidate A: Tauri 2 shell with a Svelte front-end and a WebGPU or Canvas surface.** All real-time work is Rust regardless; the web view only draws. Highest visual ceiling (CSS, web fonts, shaders), fastest iteration, largest contributor pool. Risk: web-view feel, startup time, and the discipline needed to keep the UI from ever touching audio.

**Candidate B: `iced`**, a native Rust toolkit rendering through wgpu with a software fallback. Single language, smallest binary, fastest startup, no web view. Risk: it describes itself as experimental, has no accessibility story yet, and has a smaller contributor pool. GPUI and egui were considered and dropped in the second pass, GPUI for pre-1.0 churn and thin documentation, egui for its visual ceiling ([11-resolutions.md](11-resolutions.md), Q1).

**Decided without the spike:** the performance surface is a canvas-rendered component in either candidate (Canvas2D or WebGPU in A, wgpu in B), drawn from the station's snapshot stream. The toolkit decision is about chrome and editors, not the instrument.

Criteria, in priority order:

1. Sustained 60 fps with meters, animated chord strip and 200 timeline elements, on a 2019 laptop.
2. Visual ceiling: can a designer make it look like an instrument and not a form.
3. Time to first sound after launch: under 2 s.
4. Iteration speed: hot reload or equivalent.
5. Accessibility: keyboard navigation and screen-reader labels on every control.
6. Cross-platform parity with no per-platform special cases in the surface.
7. Contributor pool.
8. No separate code path on Linux, where Candidate A runs on webkit2gtk; the surface keeps a Canvas2D fallback because WebGPU cannot be assumed there.

Default if the spike is a tie: Candidate A, because criterion 2 is where V1 fell shortest, and because the architecture guarantees the real-time path is identical either way.

## DSP layer (`xj-dsp`)

The engine says what to play and when. `xj-dsp` plays it. It reproduces the Java mixer's design where that design was good and adds what the vision needs.

### Voices

A voice is one scheduled playback of one decoded audio buffer. It carries: start frame, stop frame, gain from and gain to (the `ActiveAudio` amplitude ramp), release time and bus. When the engine requests a pitch shift or a stretch, `xj-content` renders that variant into the cache ahead of time, keyed by content hash, ratio and semitones, and the voice simply plays the variant; nothing stretches on the audio thread. Voices come from a fixed pool (default 256) so the audio thread never allocates. A voice past its stop frame enters release and is reclaimed when silent.

### Buses

One bus per instrument type plus a master. V1's `MixerImpl` assigns a bus to each instrument type as it is first encountered; V2 allocates all ten up front so a bus is addressable by type before anything plays on it. Each bus has gain (from `dubMasterVolume`), mute, solo, a meter, and a small chain of insert effects for performance: a state-variable filter, a tempo-synced delay, a short reverb, and a saturator. Effects exist for the performer's immediate gestures, not for the engine, which never knows about them.

### Master

The Java design, ported: a look-ahead compressor whose ratio is re-targeted each cycle and interpolated every frame; logarithmic dynamic-range compression; high-pass and low-pass from `TemplateConfig`; no normalization, matching the V1 decision. Plus a true-peak safety limiter at the output, which V1 lacked.

### Command set

The contract between the control thread and the audio thread:

| Command | Fields | Source |
|---|---|---|
| `StartVoice` | id, buffer (the sample or a pre-rendered variant), start frame, stop frame, gain from, gain to, release, bus | Engine schedule event (Create) |
| `UpdateVoice` | id, stop frame, gain to | Engine schedule event (Update) |
| `StopVoice` | id, release | Engine schedule event (Delete) |
| `TriggerOneShot` | buffer, frame, gain, bus | Performer gesture |
| `SetBusGain`, `SetBusMute`, `SetBusSolo` | bus, value, ramp frames | Performer gesture, template |
| `SetFxParam` | bus, slot, parameter, value, ramp frames | Performer gesture |
| `SetMasterParams` | the compressor and filter fields | Template |
| `CaptureStart`, `CaptureStop` | path | Ship mode |

Every command carries an absolute device frame. Commands with a frame in the past are applied at the next buffer boundary and counted, because that count is the first thing to look at when something feels late.

### Sinks

Device output through `cpal`; WAV capture of the master and optionally each bus; a null sink for faster-than-real-time rendering in Ship mode and in tests; HLS as a later addition, reusing the chunking design from the Java ship stage.

### Latency budget

| Stage | Target |
|---|---|
| Device buffer | 256 frames at 48 kHz, about 5 ms; configurable |
| Gesture to audible | one device buffer, under 10 ms |
| Beat-quantized gesture | lands within one frame of the beat |
| Engine cue to audible | the chosen boundary, never sooner than the commit horizon (default one beat in Play mode) |
| Engine cycle | under 5 ms when nothing to craft; craft itself measured and reported |

### Offline render

Ship mode renders a template for N minutes faster than real time using the same graph against a null clock. This replaces the Java workstation's WAV export and is also how CI asserts on rendered audio.

## First run, concretely

1. Launch. The window opens in Play mode within 2 s.
2. The bundled starter pack (see [06-commons.md](06-commons.md)) is already a project. The engine starts on its default template. Sound within 10 s of launch, with a "what is playing" overlay naming the macro program, key and tempo.
3. A three-step tour: press a scene pad, drag intensity, press a palette button. Each shows its preview before it lands.
4. Plug in a controller and the mapping panel offers a default layout for it.
5. Make mode is one click away and opens on the instrument that is playing right now.

Everything the vision asks of the workstation is in those five steps. The rest of the application is in service of making step 2 sound better.

## Game-audio continuity

Ship mode keeps **Build for engine**: it writes `build/*.wav` and the project JSON exactly as V1 does, so the Unreal plugin and any other engine consumer keep working on day one of V2. It adds **Export pack** for the Commons and **Capture** for performances. Nothing a game composer does today becomes harder.

<!-- roadmap-issues:begin -->

## Implementation issues

*Generated by `roadmap/tools/chapter_links.py` from the tasks that cite this chapter. The roadmap is [12-work-breakdown.md](12-work-breakdown.md); the issues are the source of truth for status.*

| Section cited | Issues |
|---|---|
| Advance and Hold | [#728](https://github.com/xjmusic/xjmusic/issues/728) |
| Buses | [#664](https://github.com/xjmusic/xjmusic/issues/664), [#665](https://github.com/xjmusic/xjmusic/issues/665) |
| Candidate A | [#576](https://github.com/xjmusic/xjmusic/issues/576) |
| Candidate B | [#575](https://github.com/xjmusic/xjmusic/issues/575) |
| Capture | [#694](https://github.com/xjmusic/xjmusic/issues/694) |
| Command set | [#691](https://github.com/xjmusic/xjmusic/issues/691), [#662](https://github.com/xjmusic/xjmusic/issues/662), [#685](https://github.com/xjmusic/xjmusic/issues/685), [#832](https://github.com/xjmusic/xjmusic/issues/832) |
| Criteria | [#577](https://github.com/xjmusic/xjmusic/issues/577), [#725](https://github.com/xjmusic/xjmusic/issues/725) |
| Criteria 5 | [#743](https://github.com/xjmusic/xjmusic/issues/743) |
| Cue boundary | [#684](https://github.com/xjmusic/xjmusic/issues/684), [#734](https://github.com/xjmusic/xjmusic/issues/734), [#736](https://github.com/xjmusic/xjmusic/issues/736), [#845](https://github.com/xjmusic/xjmusic/issues/845) |
| Decided without the spike | [#725](https://github.com/xjmusic/xjmusic/issues/725) |
| Editors | [#485](https://github.com/xjmusic/xjmusic/issues/485), [#657](https://github.com/xjmusic/xjmusic/issues/657), [#654](https://github.com/xjmusic/xjmusic/issues/654), [#808](https://github.com/xjmusic/xjmusic/issues/808), [#809](https://github.com/xjmusic/xjmusic/issues/809), [#810](https://github.com/xjmusic/xjmusic/issues/810), [#812](https://github.com/xjmusic/xjmusic/issues/812), [#811](https://github.com/xjmusic/xjmusic/issues/811), [#822](https://github.com/xjmusic/xjmusic/issues/822) |
| Editors 1 | [#813](https://github.com/xjmusic/xjmusic/issues/813), [#814](https://github.com/xjmusic/xjmusic/issues/814), [#815](https://github.com/xjmusic/xjmusic/issues/815), [#816](https://github.com/xjmusic/xjmusic/issues/816), [#832](https://github.com/xjmusic/xjmusic/issues/832) |
| Editors 2 | [#823](https://github.com/xjmusic/xjmusic/issues/823), [#824](https://github.com/xjmusic/xjmusic/issues/824) |
| Editors 3 | [#817](https://github.com/xjmusic/xjmusic/issues/817) |
| Editors 3 bind | [#818](https://github.com/xjmusic/xjmusic/issues/818) |
| Editors 3 chord | [#819](https://github.com/xjmusic/xjmusic/issues/819) |
| Editors 3 event | [#820](https://github.com/xjmusic/xjmusic/issues/820) |
| First run | [#728](https://github.com/xjmusic/xjmusic/issues/728), [#712](https://github.com/xjmusic/xjmusic/issues/712), [#845](https://github.com/xjmusic/xjmusic/issues/845) |
| First run 2 | [#711](https://github.com/xjmusic/xjmusic/issues/711) |
| First run 3 | [#739](https://github.com/xjmusic/xjmusic/issues/739) |
| First run 4 | [#714](https://github.com/xjmusic/xjmusic/issues/714) |
| First run 5 | [#726](https://github.com/xjmusic/xjmusic/issues/726) |
| Game-audio continuity | [#644](https://github.com/xjmusic/xjmusic/issues/644), [#645](https://github.com/xjmusic/xjmusic/issues/645), [#656](https://github.com/xjmusic/xjmusic/issues/656), [#704](https://github.com/xjmusic/xjmusic/issues/704), [#727](https://github.com/xjmusic/xjmusic/issues/727), [#740](https://github.com/xjmusic/xjmusic/issues/740), [#844](https://github.com/xjmusic/xjmusic/issues/844) |
| Game-audio continuity Export pack | [#780](https://github.com/xjmusic/xjmusic/issues/780) |
| Harmony strip | [#735](https://github.com/xjmusic/xjmusic/issues/735) |
| Intensity | [#591](https://github.com/xjmusic/xjmusic/issues/591), [#736](https://github.com/xjmusic/xjmusic/issues/736) |
| Latency budget | [#559](https://github.com/xjmusic/xjmusic/issues/559), [#621](https://github.com/xjmusic/xjmusic/issues/621), [#671](https://github.com/xjmusic/xjmusic/issues/671), [#742](https://github.com/xjmusic/xjmusic/issues/742), [#844](https://github.com/xjmusic/xjmusic/issues/844) |
| Layers | [#732](https://github.com/xjmusic/xjmusic/issues/732), [#733](https://github.com/xjmusic/xjmusic/issues/733), [#802](https://github.com/xjmusic/xjmusic/issues/802) |
| Master | [#666](https://github.com/xjmusic/xjmusic/issues/666), [#667](https://github.com/xjmusic/xjmusic/issues/667) |
| Modes | [#614](https://github.com/xjmusic/xjmusic/issues/614), [#713](https://github.com/xjmusic/xjmusic/issues/713), [#724](https://github.com/xjmusic/xjmusic/issues/724), [#726](https://github.com/xjmusic/xjmusic/issues/726), [#741](https://github.com/xjmusic/xjmusic/issues/741) |
| Modes Ship | [#731](https://github.com/xjmusic/xjmusic/issues/731), [#740](https://github.com/xjmusic/xjmusic/issues/740) |
| Offline render | [#669](https://github.com/xjmusic/xjmusic/issues/669), [#670](https://github.com/xjmusic/xjmusic/issues/670), [#696](https://github.com/xjmusic/xjmusic/issues/696), [#727](https://github.com/xjmusic/xjmusic/issues/727) |
| Palettes | [#738](https://github.com/xjmusic/xjmusic/issues/738) |
| Scenes | [#734](https://github.com/xjmusic/xjmusic/issues/734), [#738](https://github.com/xjmusic/xjmusic/issues/738) |
| Sinks | [#668](https://github.com/xjmusic/xjmusic/issues/668), [#669](https://github.com/xjmusic/xjmusic/issues/669), [#647](https://github.com/xjmusic/xjmusic/issues/647) |
| Technology stack | [#484](https://github.com/xjmusic/xjmusic/issues/484), [#489](https://github.com/xjmusic/xjmusic/issues/489), [#567](https://github.com/xjmusic/xjmusic/issues/567), [#680](https://github.com/xjmusic/xjmusic/issues/680), [#719](https://github.com/xjmusic/xjmusic/issues/719), [#844](https://github.com/xjmusic/xjmusic/issues/844) |
| Technology stack Audio I/O | [#668](https://github.com/xjmusic/xjmusic/issues/668) |
| Technology stack Decoding | [#649](https://github.com/xjmusic/xjmusic/issues/649) |
| Technology stack MIDI | [#557](https://github.com/xjmusic/xjmusic/issues/557) |
| Technology stack Packaging | [#718](https://github.com/xjmusic/xjmusic/issues/718), [#721](https://github.com/xjmusic/xjmusic/issues/721) |
| Technology stack Resampling | [#650](https://github.com/xjmusic/xjmusic/issues/650) |
| Technology stack Settings | [#699](https://github.com/xjmusic/xjmusic/issues/699) |
| The UI toolkit decision | [#574](https://github.com/xjmusic/xjmusic/issues/574) |
| The performance surface | [#745](https://github.com/xjmusic/xjmusic/issues/745) |
| The performance surface header | [#803](https://github.com/xjmusic/xjmusic/issues/803) |
| The timeline and the why panel | [#604](https://github.com/xjmusic/xjmusic/issues/604), [#616](https://github.com/xjmusic/xjmusic/issues/616), [#744](https://github.com/xjmusic/xjmusic/issues/744) |
| Timeline | [#742](https://github.com/xjmusic/xjmusic/issues/742) |
| Voices | [#652](https://github.com/xjmusic/xjmusic/issues/652), [#663](https://github.com/xjmusic/xjmusic/issues/663), [#701](https://github.com/xjmusic/xjmusic/issues/701) |

<!-- roadmap-issues:end -->
