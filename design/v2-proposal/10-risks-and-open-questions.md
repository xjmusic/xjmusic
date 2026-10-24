# 10 · Risks, open questions, alternatives considered

Everything in this proposal that could be wrong, and the decisions it deliberately leaves to someone else or to an experiment.

## Risks

| # | Risk | Likelihood | Impact | Mitigation |
|---|---|---|---|---|
| R1 | **The rewrite trap.** A new workstation in a new language becomes a multi-year hole with nothing shipping. | Medium | High | The engine is not rewritten. Every phase ends with a demonstration. The Java workstation stays usable throughout. Phase 2 ships a CLI that already makes music before any UI exists. |
| R2 | **Wrong UI toolkit.** The toolkit cannot deliver an instrument-grade surface, or cannot deliver it on all three platforms. | Medium | High | Phase 0 spike with scored criteria. The surface is written against a view-model from the station core, so re-hosting it is a UI rewrite, not a product rewrite. |
| R3 | **Foresight versus latency disappoints performers.** Cues that land "in 2 bars" feel sluggish, however honest the UI is about it. | Medium | High | The commit horizon is tunable to one beat solo. Gestures are immediate. Phase 3 usability sessions with musicians are an exit criterion, not an afterthought. If it still disappoints, the next lever is shrinking segments in Play mode, which is content, not engine work. |
| R4 | **The Commons is mediocre.** A registry fills with packs nobody wants to play. | High | High | The quality bar and human review are not optional. Seed from the demos, which are known to sound good. A small base taxonomy. Accept a small Commons over a large bad one. |
| R5 | **Ableton Link licensing.** GPLv2 is incompatible with distributing a binary under the XJ license; Ableton's proprietary license terms are unknown until asked. | Medium | Medium | Ask Ableton in Phase 0. The session protocol depends on a shared beat timeline, not on Link. Fallback: an NTP-style clock sync over the session connection plus MIDI clock out for external gear. Link remains the recommendation because of what it unlocks with the audience's own gear. |
| R6 | **Time stretch and pitch shift sound bad** on the material that matters, or the best library has an incompatible license. | Low–Medium | Medium | Phase 0 bake-off with blind listening. Signalsmith Stretch is MIT. Content can avoid both by multisampling and tempo-matching, as V1 does today, so the feature is additive. |
| R7 | **Audio I/O platform pain.** ASIO SDK licensing on Windows, exclusive-mode quirks, macOS aggregate devices, Linux JACK versus PipeWire. | Medium | Medium | `cpal` covers the common paths. ASIO is a feature flag. Defaults are shared-mode and conservative buffers; experts can tune. |
| R8 | **Unsafe at the FFI boundary.** Memory errors where Rust meets C++. | Medium | Medium | The C ABI is narrow and flat. Fuzz it. Run the C tests under AddressSanitizer in CI. The safe wrapper is the only thing that calls it. |
| R9 | **Determinism work regresses craft.** Threading a seeded generator through four files touches the heart of the engine. | Low | High | The craft test matrix runs on every commit. Determinism tests are added before the generator is swapped. The swap is one PR with no other change in it. |
| R10 | **Team size.** The plan assumes two engineers for a year or more; the project is contributor-funded by equity. | High | High | Phases are sized so that a single engineer still ships Phase 0, 1 and 2 in a useful order. Issues are decomposed finely enough for occasional contributors. The engine work is attractive to contributors precisely because it is well-tested and bounded. |
| R11 | **Legal questions around performance and the Commons.** Does a paid live performance count as "audio output" under the software license (it should, by the license's own text)? What does the patent license mean for a venue running many stations? | Medium | Medium | Legal review before Phase 5 ships. The proposal assumes performances are output and therefore free, and that the venue's stations are the venue's problem only if XJ software is embedded in a commercial product, which it is not. |
| R12 | **Scope creep into a DAW or a synth.** Every musician who touches it will ask for audio tracks and plugins. | High | Medium | Non-goals in [01-vision.md](01-vision.md). CLAP hosting is a candidate for 2.x and the DSP command set leaves a place for it. Audio tracks are not. |
| R13 | **Platform split in development.** Development happens on Windows; most of the target audience's laptops are Macs. | Medium | Medium | CI on all three from Phase 0. Signing and notarization set up in Phase 3, not at the end. At least one maintainer on each platform. |
| R14 | **Two codebases during transition.** Users editing in Java and performing in Rust hit format edge cases. | Medium | Low | Same files, stable ordering, round-trip tests between the two writers in CI until the Java workstation is archived. |
| R15 | **Sessions work in the lab and fail in the room.** Venue networks are hostile. | Medium | High | Wired recommended and said loudly in the UI. Simulated-network tests. The three-station soak. The MPC-in-the-room test is an exit criterion. Every failure mode has a defined hold-and-rejoin behavior, and the UI shows what happened. |

## Open questions

Decisions this proposal does not make. Each names who decides and when.

| # | Question | Decided by | When |
|---|---|---|---|
| Q1 | UI toolkit: Candidate A (Tauri shell) or B (native Rust GPU)? | Phase 0 spike, scored against the criteria in [05-workstation.md](05-workstation.md) | End of Phase 0 |
| Q2 | Time stretch and pitch shift library | Phase 0 bake-off | End of Phase 0 |
| Q3 | Ableton Link license path, or the fallback clock | Ableton's answer; company | Phase 0 |
| Q4 | The contents of the base taxonomy | The authors of the first packs | Phase 0, revised in Phase 4 |
| Q5 | Does content contribution earn equity on the same terms as code? | Company | Before Phase 4 opens the registry |
| Q6 | Default pack license: CC BY 4.0, CC0, or something else? | Company with legal review | Phase 4 |
| Q7 | Registry hosting: GitHub Pages plus releases, or object storage with a CDN? | Maintainers | Phase 4 |
| Q8 | Does the Unreal plugin move to the C ABI in 2.0 or stay on the C++ API? | Unreal plugin maintainers | Phase 1 |
| Q9 | Does the HLS ship path survive into 2.x at all, or is it archived with the cloud code? | Company | Phase 2 |
| Q10 | Minimum supported OS versions and CPU architectures (Apple Silicon only? Intel Macs? Windows on ARM?) | Maintainers, from download telemetry if any exists | Phase 3 |
| Q11 | Telemetry: none, opt-in crash reports only, or opt-in usage? | Company | Phase 3 |
| Q12 | Name. Is the V2 workstation still "XJ music workstation," or does Play mode deserve a name of its own on stage? | Company | Whenever |
| Q13 | Cue request policy defaults in a session: accept all, or ask? | Performers, after Phase 5's duet | Phase 5 |
| Q14 | Should Follow mode permit stations with different `barBeats`? (Proposal says no in 2.0.) | Engine maintainers | Phase 1 |

## Alternatives considered and not taken

**Rewrite the engine in Rust.** Tempting for a single-language codebase. Rejected: the C++ engine is the patented, tested core with 119 test files and a game-engine consumer. Rewriting it would spend the project's scarcest resource on the part that already works, and would risk silent musical regressions that no test catches. If a Rust port is ever wanted, the C ABI and the determinism tests built here are what would make it safe.

**Keep Java and add the features.** Rejected: G1, G2 and G9 in [02-where-v1-stands.md](02-where-v1-stands.md) are not features. The workstation would still carry a second engine, a JVM startup, ffmpeg via javacpp, and a look that reads as a form. The real-time audio path in Java is workable but is the wrong place to invest for an instrument.

**Electron instead of Tauri for Candidate A.** Rejected: the core is Rust either way, and Tauri's process model keeps it in-process with a smaller footprint.

**Deterministic lockstep instead of a conductor.** Discussed and rejected for 2.0 in [07-session-protocol.md](07-session-protocol.md).

**Max/MSP or Pure Data as the performance front-end.** Rejected as the product, because the audience should not need a patching environment. Kept as an integration: the OSC input and the published session protocol make it possible for anyone who wants it.

**XJ as a plugin inside a DAW** (CLAP or VST3). Not rejected, deferred. The station core could be hosted as a plugin whose transport follows the DAW. It is attractive for game composers who live in a DAW and would be a 2.x feature once the DSP command set is stable. It does not serve the venue vision, which is why it is not in 2.0.

**A cloud session service instead of LAN.** Rejected for 2.0: the venue is a room, rooms have cables, and the timing budget on a LAN is comfortable. An internet mode would need a different latency model and authentication and is a research item.

**Pitch shifting in the engine.** Rejected: the engine chooses, the DSP renders. Keeping audio processing out of the engine is what lets Unreal and a browser build use the same engine with their own audio stacks.

## What would change the proposal's mind

- If the Phase 0 craft-timing spike shows segment craft routinely above 200 ms on target hardware, the commit-horizon design needs a craft-on-another-thread model before Play mode, and Phase 1 grows.
- If no stretch library passes the bake-off, G7 stands, the Commons quality bar gets stricter about multisampling, and Phase 4 gets harder.
- If both UI spikes miss the 60 fps criterion, the surface is scoped down before any other decision is made.
- If Ableton declines a license and the fallback clock cannot lock an MPC, the "audience's own gear" story in [07-session-protocol.md](07-session-protocol.md) is dropped and sessions sync only XJ stations.
