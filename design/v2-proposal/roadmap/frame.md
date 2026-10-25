# Architect's frame for the V2 work breakdown

This is the fixed frame every decomposition must fit. It is derived from design/v2-proposal/ (all chapters) and the repository as it stands at 1.9.6. The decomposition candidates may argue with granularity and ordering inside the frame; they may not drop a phase, invent a goal the proposal rejects (DAW, synth, streaming, internet sessions, craft redesign), or change the architecture (C++ engine stays; Rust body; C ABI; one content model).

## Governing rules (from 01-vision and 09-roadmap)

1. Keep the brain, rebuild the body. Craft algorithms are not redesigned; they gain inputs and outputs only.
2. Plan ahead, react now, never confuse the two: cues (engine, boundary-quantized) vs gestures (DSP, immediate).
3. One content model; V2 fields are additive and optional; engine 2.0 reads every 1.9.x project.
4. The network is a mode; a station never requires a session.
5. Legible machine: preview + diagnostics before any control is shown.
6. Determinism where it buys something (seeded chain; replayable captures).
7. The game-audio product never regresses: Build for engine from the first Rust release; Unreal plugin builds against every engine tag.
8. Something usable ships at the end of every phase; every exit criterion is a demonstration.
9. The craft test matrix is the acceptance test for the engine; nothing merges that fails it.
10. Decisions have dates: Phase 0 turns open questions into decision records.

## Phases (each a GitHub milestone)

| Phase | Name | Exit demonstration (from 09-roadmap) | Size (EM) |
|---|---|---|---|
| 0 | Spikes and decisions | Every spike row has a recorded decision; the C ABI spike seeds xj-engine-sys | 1.5–2.5 |
| 1 | Engine 2.0 | Follow-mode test (two engines, one conducting, one following, identical foundations per span) green on three platforms; engine 2.0.0 tagged; Unreal plugin builds unchanged | 3–4 |
| 2 | Station core | `xj-station play` on a V1 demo with a MIDI controller sounds at least as good as the Java workstation, runs 8 h without dropout, `replay` reproduces a capture byte for byte | 4–5 |
| 3 | Workstation 2.0, Play mode | First launch to sound < 10 s on a 2019 laptop; a musician new to XJ performs 20 min with a pad controller after the tour; five usability sessions filed as issues | 5–7 |
| 4 | Commons | Fresh install fetches a show, writes a lockfile, two machines load byte-identical content; one external contributor publishes a pack that passes lint | 2–3 + content |
| 5 | Sessions | Two workstations and one MPC on Link play a 30-min duet with two handoffs and no dropout, captured on both stations and published | 3–4 |
| 6 | Make mode parity | Every Java editing operation has a Rust equivalent; Java workstation archived with a pointer | 5–8 |
| 7 | The show | A real night, three or more stations, outside artists, a recording that stands beside Live in Boulder | not engineering |

Phase dependencies (09-roadmap): P0→P1, P0→P2, P1→P2, P2→P3, P2→P4, P1→P5, P2→P5, P3→P5, P3→P6, P4→P7, P5→P7. Phase 1 and the non-engine parts of Phase 2 run in parallel once the C ABI spike is done; Phase 4 alongside Phase 3; Phase 6 may start once Phase 3's shell exists.

Decision gates: after P0 (toolkit, stretch library, Link path, go/no-go); after P2 (does the station core sound good enough to build a product on); after P3 (do musicians want to play it); after P5 (did the duet work in a real room → schedule P7).

## Areas (labels)

engine (C++), c-abi, rust-engine (xj-engine-sys/xj-engine), content (xj-content), dsp (xj-dsp), transport (xj-transport), control (xj-control), station (xj-station), session (xj-session), workstation-ui, commons (registry/packs/taxonomy/content), ci-release, docs, legal-paperwork, decision, unreal, show.

## Candidate epic skeleton (to be argued with, not ignored)

Phase 0: Link license request; ASIO agreement; C ABI link spike; craft timing harness in-tree + 2019-laptop measurement; UI toolkit spike (Tauri 2 vs iced, same slice, three OS, scored); stretch/shift bake-off (fixed material, blind); Link lock spike (rusty_link vs MPC + Live); xj-base@1 taxonomy draft reviewed by first pack authors; company decisions (Q5 equity, Q6 license, Q11 telemetry, Q12 name) recorded; decision-record convention (design/v2-proposal/decisions/); go/no-go gate.

Phase 1: seeded randomness (tests first, one PR swap); C ABI (header, lifecycle, cycle, control, snapshot JSON, introspection; xjmusic_c target + install; C-compiler header test; ASan; fuzz); cues with boundaries + commit horizon + earliest_landing + existing overrides as wrappers; TemplateConfig commitHorizonBeats/followHoldMaxBars; tempo policy; foundation record JSON + conduct-mode publication + follow-mode ExternalFoundationCraft + Fabricator answering the direct main-program reads + barBeats candidate filtering + hold/rejoin + foundation-cue rejection in follow mode; preview; choice diagnostics behind flag; craft timing messages; WorkState/messages via ABI; additive content fields + NotePicker shifted candidates + schedule event fields; two-engines-one-process follow test; determinism/cue/hold/preview tests; Doxygen + docs; engine 2.0.0 release + ABI 1.0.0; Unreal plugin verification; CI matrix modernization.

Phase 2: Rust workspace + CI (fmt, clippy, nextest, three OS, cargo-deny); xj-engine-sys (bindgen, build.rs→cmake, static link) + xj-engine safe wrapper; xj-content (V1 read/write round-trip vs Java writer, symphonia decode, rubato resample, blake3 cache, variant rendering, Build for engine); xj-transport (four time bases, affine chain↔device estimate, BeatClock trait, internal clock, Link behind feature flag, event→command conversion, late-command counter); xj-dsp (voice pool, ten buses + master, envelopes/release, master dynamics port, true-peak limiter, insert FX, SPSC command ring, meters, device/WAV/null sinks, offline render); xj-control (midir/rosc/keyboard/gilrs, TOML profiles, learn mode, cue/gesture routing, moves, LED feedback, influence); xj-station (tick loop, cue scheduling, snapshots, CLI play/render/capture/replay, settings, logging, diagnostics bundle); capture format + replay determinism; CI offline render with audio statistics + scheduled 8-h soak; exit A/B listening + gate.

Phase 3: shell in chosen toolkit; station integration + snapshot stream; performance surface canvas (scenes, palettes, intensity, cue boundary, harmony strip with commit-horizon line, layer strips with meters, advance/hold, capture) with preview on hover; timeline + why panel; mapping UI + learn + LED; starter pack bundled + first-run tour + time-to-sound; Ship mode (Build for engine, bounce, capture export, CREDITS.md); packaging/signing/notarization/auto-update; opt-in crash reporting + diagnostics bundle; accessibility/keyboard; usability program; Workstation 2.0.0.

Phase 4: pack format + manifest + hashes + provenance; xj-content CLI (pack/lint/verify/publish/fetch/lock/render); lint rules; registry (xjmusic/commons index repo, static JSON, PR publishing CI, R2 artifacts, mirrors, ed25519 signing); show manifest + lockfile + resolver + offline mirror/USB; xj-base@1 published; demos as packs + previews + starter pack (commissioned); license texts + contributor declaration + CREDITS.md; Export pack + license display in workstation; exit demo; Workstation 2.1.

Phase 5: protocol spec (JSON Schema, versioned, published); xj-session (mDNS, join code, QUIC/TCP framing, hello/welcome/reject); foundation publish/consume + retract + epoch + horizon handshake + effective horizon; claims; cue requests + policy; handoff; failure handling; `session` BeatClock + MIDI clock out; session UI; headless web status page; simulated-network tests, three-station soak, handoff soak; legal review (R11); real-gear duet; Workstation 2.2.

Phase 6: editor infrastructure (undo/redo, CRUD, library browser); instrument+audio editor; template editor; program editor bind/chord/event; Java↔Rust round-trip tests (start in Phase 2, finish here); migration guide; Java maintenance mode → archive; HLS design note.

Phase 7: show production plan, venue network kit, artist onboarding, recording and release.

## Issue conventions

- Epic = parent issue (type Feature, label epic + area + v2), one per capability per phase; tasks = sub-issues (type Task).
- Every task: 1–10 engineer-days; size XS(≤1d) S(2–3d) M(4–7d) L(8–12d); anything larger splits.
- Every task has: context, scope in/out, design (concrete: names, structs, functions, files), acceptance criteria (testable), test plan, dependencies (blocked by), risks, references (proposal sections, code file:line, external sources).
- Blocked-by is formal (GitHub issue dependencies) and acyclic; the waterfall is the topological order; milestones carry phase due dates.
