# 11 · Resolutions: a second pass over the open questions

The first pass ([10-risks-and-open-questions.md](10-risks-and-open-questions.md)) listed fourteen questions and said who should decide each. This pass does the homework: it looks up what can be looked up, measures what can be measured against the engine as built, and turns each question into a recommendation with the evidence beside it. Where a decision still belongs to the company or to an experiment, it says so, but the experiment is now narrower and the decision has a default.

Each section has the same shape: **what was found**, **options**, **recommendation**, **what remains**, and **what changes in the proposal**. Sources are linked inline; everything about the engine itself was checked against the code and, where it says so, measured.

A summary table is at the end.

---

## Q1 · UI toolkit

### What was found

- **Tauri 2** is stable and documents its floor precisely: macOS 10.15 or later, Windows 7 or later with WebView2 (preinstalled from Windows 10 1803), and webkit2gtk 4.1 on Linux ([Tauri prerequisites](https://v2.tauri.app/start/prerequisites/)). The core runs in-process in Rust; the web view only draws.
- **iced** describes itself as "currently experimental software," renders through wgpu with a tiny-skia software fallback, is MIT, and does not mention accessibility ([iced README](https://github.com/iced-rs/iced)). It is nonetheless the toolkit behind the most complete open-source Rust DAW found, Generic DAW, which hosts CLAP plugins under a custom iced interface ([awesome-rust-audio](https://github.com/kfrncs/awesome-rust-audio)).
- **GPUI** (the toolkit behind the Zed editor) is Apache-2.0 and usable standalone, but its own crate page warns it is pre-1.0 with frequent breaking changes, and forum reports call its documentation weak ([docs.rs gpui](https://docs.rs/crate/gpui-pre/0.3.0), [Rust users forum](https://users.rust-lang.org/t/which-gui-framwork-should-i-choose/137581)). Community forks exist (open-gpui, gpui-ce) precisely because of that churn.
- **egui** is the most downloaded Rust GUI crate by a wide margin and the one most audio tools reach for (SoundCraft, several plugin GUIs), but it is immediate-mode and its visual ceiling is the concern the first pass already named ([2026 survey](https://www.pistack.xyz/posts/2026-08-25-rust-gui-frameworks-egui-iced-slint-comparison/)).
- **Slint** is GPLv3, royalty-free, or commercial, with a strong embedded story and a visual live preview ([rustlang.com.br survey](https://rustlang.com.br/blog/rust-gui-2026/)). Its license model is a poor fit for the XJ license.
- A 2025 survey found that most Rust GUI libraries are not production-ready ([boringcactus survey](https://www.boringcactus.com/2025/04/13/2025-survey-of-rust-gui-libraries.html)). No source compares toolkits on audio-application criteria; that part of the spike cannot be replaced by reading.

### Options

| Option | For | Against |
|---|---|---|
| A: Tauri 2 shell, Svelte front-end, Canvas/WebGPU surface | Highest visual ceiling; fastest iteration; largest contributor pool; stable floor | Web-view feel; webkit2gtk on Linux is the weakest of the three views and WebGPU is not reliably available there |
| B1: iced | Pure Rust; wgpu rendering; proven in a Rust DAW | Self-described experimental; no accessibility story; smaller pool |
| B2: GPUI | Fast, GPU-native, Apache-2.0 | Pre-1.0 breaking changes and thin docs; a small team cannot absorb that churn |
| B3: egui | Most used; easiest to start | Immediate-mode redraw model and visual ceiling make it wrong for the instrument surface |
| Slint | Live preview; embedded | License model |

### Recommendation

Keep the Phase 0 spike, but narrow it to **A versus B1 (iced)**. Drop GPUI and egui from the spike on the evidence above. Drop Slint on license.

Make one decision now that the spike does not need to make: **the performance surface is a canvas-rendered component in either case**, driven by the station's snapshot stream and drawn with Canvas2D or WebGPU (A) or wgpu (B1). The toolkit decision is therefore about application chrome and editors, not about the instrument. That lowers the stakes and makes the surface portable between hosts.

Tie-breaker remains A. Two conditions are added to the criteria: the surface must have a Canvas2D fallback because WebGPU in webkit2gtk cannot be assumed, and the spike must be run on Linux as well as macOS and Windows, because the web view is the component most likely to differ by platform.

### What remains

The two-week spike, scored on the criteria in [05-workstation.md](05-workstation.md). It is now a two-candidate comparison with a declared default.

### Changes to the proposal

[05-workstation.md](05-workstation.md): candidate list narrowed; canvas-surface decision recorded; Linux added to the spike.

---

## Q2 · Time stretch and pitch shift

### What was found

- **Signalsmith Stretch** is MIT, C++11, sounds best "for more modest changes (between 0.75x and 1.5x)" of stretch, handles several octaves of pitch shift, has formant control, reports its latency explicitly, and offers a split-computation mode for stricter real-time budgets. It has a Rust wrapper on crates.io and a WASM/AudioWorklet build on NPM that tracks the C++ algorithm ([Signalsmith Stretch README](https://github.com/Signalsmith-Audio/signalsmith-stretch)). The Rust wrapper, `signalsmith-stretch`, is MIT, at version 0.1.3, built with bindgen and cc ([docs.rs](https://docs.rs/signalsmith-stretch)).
- **Bungee** is MPL-2.0, C++, a phase-vocoder design with continuously variable tempo and pitch, intended for real-time and offline use ([Bungee README](https://github.com/bungee-audio-stretch/bungee)).
- **Rubber Band** is GPLv2+ or a paid commercial license, and the vendor states that App Store distribution requires the commercial license ([Rubber Band license page](https://breakfastquay.com/rubberband/license.html)).
- A pure-Rust alternative, `timestretch`, is MIT but uses a different, less proven algorithm ([docs.rs](https://docs.rs/timestretch)).

### What XJ actually needs

Less than a DAW does. The two uses in the proposal are:

1. **Loop tempo matching.** A loop recorded at 120 bpm played in a session at 126 bpm is a 5% stretch. A Commons pack tagged 118–126 bpm never needs more than about 7%. This is far inside the 0.75–1.5× sweet spot.
2. **Gap filling by pitch shift.** `NotePicker` prefers an exact-tone sample and falls back to a nearby one shifted by a few semitones. A range of ±4 semitones covers the gaps a reasonably multisampled instrument has.

Both ratios are known *before* the audio plays: the foundation record for a segment arrives seconds ahead, and the pick names the sample and the shift. So the stretch does not have to run on the audio thread at all.

### Recommendation

- **Default to Signalsmith Stretch** through its Rust wrapper. MIT, formant control, a WASM build that keeps the browser-audition idea alive, and a sweet spot that matches the use.
- **Compare against Bungee** in the Phase 0 bake-off as the one alternative. Exclude Rubber Band unless someone buys the commercial license.
- **Render stretched and shifted variants ahead of time on the I/O thread**, cached by (content hash, stretch ratio, semitones), and hand the audio thread plain buffers. This removes stretch quality-versus-latency trade-offs from the real-time path entirely and makes the DSP voice model simpler than the first pass described: a voice plays a buffer; it does not stretch.
- Bake-off material, fixed now so the spike is not an argument about taste: four drum loops (transient preservation), four sustained pads (phase coherence), four bass samples (low-frequency smearing), four vocal-like samples (formants), each at 0.93×, 1.07× stretch and ±2, ±4 semitones shift. Three listeners, blind.

### What remains

The listening session. The architecture no longer depends on its outcome.

### Changes to the proposal

[05-workstation.md](05-workstation.md): voices no longer carry stretch or shift; `xj-content` renders variants into the cache. [04-engine.md](04-engine.md): the schedule event keeps `pitch_shift_semitones` and `stretch_ratio` as the engine's *request*; the station resolves them to a cached variant.

---

## Q3 · Ableton Link licensing

### What was found

- Link's README states it exactly: "Ableton Link is dual licensed under GPLv2+ and a proprietary license. If you would like to incorporate Link into a proprietary software application, please contact link-devs@ableton.com" ([Ableton/link](https://github.com/Ableton/link)).
- Whether the proprietary license costs money is not stated anywhere official. A 2016 interview had Ableton say developers may integrate the SDK "free of charge," and forum reports agree, but these are secondary and old ([CDM](https://cdm.link/2016/09/ableton-opening-link-everyone-starting-today/), [KVR thread](https://www.kvraudio.com/forum/viewtopic.php?p=6560450)).
- The Rust wrapper `rusty_link` (0.4.9) wraps Ableton's own C wrapper `abl_link`, so it inherits the same dual license ([docs.rs rusty_link](https://docs.rs/crate/rusty_link/latest)).
- Ableton's list of Link-enabled products includes the Akai Force, MPC Live, MPC Live II, MPC One and MPC X, and also monome norns, Torso T-1 and Denon Prime 4 ([Link products](https://www.ableton.com/en/link/products/)). Akai shipped Link in MPC firmware 2.0.5 in 2017, "the first time ever on a standalone hardware unit" ([Akai press release](https://www.akaipro.com/press/akai-professional-introduces-major-2-5-firmware-update-for-its-standalone-mpc-live-and-mpc-x-models.html)).

The XJ license is not GPL-compatible (it carries its own patent and trademark terms), so shipping Link under the GPL option is not available.

### Options

| Option | Notes |
|---|---|
| Obtain the proprietary license | One email. Terms unknown until asked; historically free. |
| Isolate Link in a separate GPL-licensed process (`xj-link-bridge`) talking to the station over a local socket | Common pattern; legally gray; adds a hop of jitter to the one thing that must not jitter |
| Own clock sync, no Link | Loses the audience's gear |

### Recommendation

- **Send the email in Phase 0, week one.** It is the cheapest action in the whole plan and unblocks the best feature in the session design.
- **Design the clock as a trait** (`BeatClock`) with two implementations behind feature flags: `link` (via `rusty_link`) and `session` (a conductor-hosted clock using NTP-style offset estimation over the session connection). The `session` clock is needed regardless, for venues that run no Link peers, and it is what the sessions soak tests run on in CI.
- **Add MIDI clock out** to the `session` clock so that non-Link hardware can follow an XJ station. This is cheap (`midir` already in the stack) and covers most older gear.
- Do not build the bridge-process workaround. If Ableton declines, the fallback is `session` plus MIDI clock, and the audience's MPCs follow MIDI clock from a station instead of Link.

### What remains

Ableton's answer. Everything else is decided.

### Changes to the proposal

[07-session-protocol.md](07-session-protocol.md): clock section describes the trait and both implementations. [09-roadmap.md](09-roadmap.md): the email becomes a Phase 0 task with a date.

---

## Q4 · The base taxonomy

### What was found

The first pass sketched a base taxonomy (ENERGY, MOOD, SPACE, TIME) and said the real one should come from the people who author packs. Before asking them, it is worth looking at what the four existing demo projects actually do. Their JSON was pulled from `xjmusic/xjmusic-demos` and every meme on every program, instrument and sequence binding was counted (685 files, 403 programs, 220 instruments, 3,000 audios, 62 distinct memes).

**Every demo defines its own exclusive palette, and no two overlap:**

| Demo | Template taxonomy | Memes actually used on content |
|---|---|---|
| Ambient Flow | `SEASON[WINTER, SPRING, SUMMER, FALL]` | SAVORY, SPICY, SWEET, SOUR on programs and instruments; ORANGES, LEMONS, PLUMS, PEACHES on bindings; the SEASON memes appear on **no** content |
| Deep House | `ELEMENT[EARTH, WIND, FIRE, WATER]` | The four elements everywhere (about 50 uses each); plus BREAK on 253 bindings; SMALL, LARGE, BIG on instruments; WIDEOPEN, OPEN, STRAIGHT, ROOTSY, 3NOTE, 4NOTE on programs |
| Hero's Journey | `ACTION[EXPLORATION, DIALOG, COMBAT]` and `HEROSJOURNEY[CALLTOACTION, EMBARK, ATONEMENT, VICTORY]` | Exactly those, plus 95BPM, 115BPM, 135BPM on nearly every instrument |
| Lofi Hip Hop | `CITY[ABERDEEN, LUXOR, NAGOYA, BOULDER]` | The cities; 75BPM and 85BPM on most instruments and programs; WIDEOPEN, STRAIGHT, CUSTOM on programs |

Two things follow. First, **the palettes are artistic and idiosyncratic on purpose**: seasons, flavors, fruit, cities, elements, story beats. Nobody authored these to be interoperable, and a base taxonomy that tried to replace them with ENERGY and MOOD would be fighting the way artists actually think. Second, **underneath the palettes there are shared technical conventions that nobody wrote down**:

| Convention | Form | Seen in | Engine rule it relies on |
|---|---|---|---|
| Recorded tempo | `75BPM`, `85BPM`, `95BPM`, `115BPM`, `135BPM` | Hero's Journey, Lofi, Deep House | Numeric memes with the same suffix and different numbers are mutually exclusive (`ParseNumeric`), so an 85BPM loop is never chosen into a 75BPM segment |
| Voicing density | `3NOTE`, `4NOTE` | Deep House | Same numeric rule |
| Instrument size or role | `SMALL`, `LARGE`, `BIG` | Deep House | Plain memes; parallel to the template's `eventNamesSmall/Medium/Large` |
| Harmonic character | `WIDEOPEN`, `OPEN`, `STRAIGHT`, `ROOTSY` and their anti-memes `!WIDEOPEN`, `!STRAIGHT`, `!ROOTSY` | Deep House, Lofi | Anti-memes exclude (`ParseAnti`) |
| Section marker | `BREAK` on sequence bindings | Deep House (253 times) | Plain meme used structurally |
| Force or forbid | `SOLID!` (strong: always favor), `$BUSY` (unique: only one), `!WUBBY` (anti) | Ambient Flow, Deep House | `ParseStrong`, `ParseUnique`, `ParseAnti` |

Also found: the Ambient Flow template declares a SEASON taxonomy that its content never uses. Declarations drift from content when nothing checks them.

### Recommendation

Replace the first pass's invented categories with a two-part answer that matches the evidence.

1. **`xj-base@1` codifies the technical conventions, not the moods.** It is a short document, not a list of feelings:
   - Tempo: `NNNBPM` numeric memes on instruments and bindings mean "recorded at this tempo." With pre-rendered stretch variants (Q2) they stop being a hard constraint, but they stay the declaration the stretch logic reads.
   - Density: `NNOTE` for voicing density.
   - Size: `SMALL`, `MEDIUM`, `LARGE` on instruments, aligned with the template's event-name sizes.
   - Structure: `BREAK`, `INTRO`, `OUTRO` on sequence bindings.
   - Character: a small open list (`OPEN`, `WIDEOPEN`, `STRAIGHT`, `ROOTSY`) with the rule that packs may add to it and must document additions in their README.
   - The meme grammar as the engine implements it: `!X` excludes, `X!` always favors, `$X` allows only one, a leading number makes a meme exclusive with others sharing its suffix.
2. **Palettes stay per pack and are declared, not standardized.** `pack.toml` gains a `[taxonomy]` table listing the pack's exclusive categories, in the engine's own `CATEGORY[A, B, C]` syntax. A show's template taxonomy is the union of its packs' declared categories, which the engine already supports (Hero's Journey runs two categories today). A Deep House pack and a Lofi pack in one show simply give the performer two palette rows, ELEMENT and CITY, and nothing conflicts. The lint catches the one real failure, the same word in two packs' categories meaning different things.
3. **Lint closes the drift.** `xj-content lint` fails a pack whose declared category memes appear on no content, or whose content uses a palette-like meme that no declared category contains. The Ambient Flow demo would fail today, which is the point.

### What remains

The authors of the first packs review the convention list and the character memes. The shape of the answer, conventions plus declared palettes, is decided.

### Changes to the proposal

[06-commons.md](06-commons.md): the "A shared taxonomy" section is rewritten; `pack.toml` gains `[taxonomy]`; lint gains two checks.

---

## Q5 · Equity for content contributions

### What was found

The README's rule is: one hour of labor equals one point, hours reported against specific pull requests, contract signed when the PR merges. The rule is about *evidence* (a PR, a log) as much as about hours.

### Options

| Option | For | Against |
|---|---|---|
| Same rule, same evidence | Simple; already understood | Content hours are harder to audit than code hours |
| Points per accepted artifact (per instrument, per program) | Auditable | Rewards volume over quality; hard to price |
| No equity for content; pay or revenue-share per pack instead | Clean separation | Splits the community into two classes |

### Recommendation

**Same rule, same evidence, same gate.** Make Commons publishing a pull request into a `xjmusic/commons` repository that holds the index (see Q7). A pack contribution is then literally a PR: it carries the hours log, it is reviewed, it passes CI (lint, signature, render), and it merges. Reviewing hours count for the maintainers who listen. The quality bar in [06-commons.md](06-commons.md) is the audit: a pack that does not pass does not merge, and a pack that merges was worth the hours.

Two guardrails: hours are capped per pack at review time if the reviewers judge the log implausible (the same discretion the code rule already implies), and the starter pack bundled with the workstation is commissioned, not crowdsourced, so that first-launch quality is not left to chance.

### What remains

The company's yes or no. This is a business decision; the proposal only shows that the mechanism costs nothing new.

### Changes to the proposal

[06-commons.md](06-commons.md): publishing is a PR to the index repository.

---

## Q6 · Default license for Commons packs

### What was found

- Under CC BY 4.0, "Share" explicitly includes public performance, so playing a pack's samples at a show is a licensed act that triggers attribution ([CC BY 4.0 legal code](https://creativecommons.org/licenses/by/4.0/legalcode), section 1).
- Attribution may be satisfied "in any reasonable manner based on the medium, means, and context in which You Share the Licensed Material" (section 3(a)(2)). A set list on a venue page or a credits file alongside a recording is reasonable; a spoken credit between songs is not required.
- What attribution must include, when the licensor supplies it: creator identification (pseudonyms allowed), a copyright notice, a reference to the license, and a link where practicable (section 3(a)(1)(A)).
- Freesound, the largest sample commons, settled on CC0 and CC BY after retiring its Sampling+ licenses, and its community treats CC0 as "never worry about credit" and CC BY as "always credit" ([Creative Commons on Freesound](https://creativecommons.org/2011/09/12/celebrating-freesound-2-0-retiring-sampling-licenses/)).

### Options

| License | Performer must credit | Game must credit | Share-alike | Commercial OK |
|---|---|---|---|---|
| CC0 | No | No | No | Yes |
| CC BY 4.0 | Yes, reasonably | Yes | No | Yes |
| CC BY-SA 4.0 | Yes | Yes | **Yes**: a soundtrack becomes share-alike | Yes |
| CC BY-NC | Yes | Yes | No | **No** |

Share-alike would contaminate game soundtracks; non-commercial would forbid a paid show. Both are out.

### Recommendation

- **CC BY 4.0 as the Commons default.** It keeps authors visible, which is what makes people contribute, and its attribution rule is written for exactly the "reasonable manner" a live set or a game credits screen can provide.
- **CC0 for the starter pack**, and permitted for any pack. The first thing a new user makes should carry no obligations.
- **Make attribution automatic.** The workstation writes a `CREDITS.md` into every capture, bounce, build and exported pack, listing each pack's authors, license and link from its manifest. The venue can paste it on a wall or a web page. This turns the one real cost of CC BY into a non-cost.
- A short **contributor declaration** on publish: the submitter owns or has cleared every sample in the pack. The registry records who declared.

### What remains

Legal review of the declaration text and of how the pack license interacts with the XJ software license's "you own the rights to the input audio" clause. The recommendation is compatible with it by design.

### Changes to the proposal

[06-commons.md](06-commons.md): default license set; `CREDITS.md` generation added; declaration added.

---

## Q7 · Registry hosting

### What was found

- GitHub Pages: sites may be no larger than 1 GB, a soft bandwidth limit of 100 GB per month, and it is not intended as a general hosting service ([GitHub Pages limits](https://docs.github.com/en/pages/getting-started-with-github-pages/github-pages-limits)). Fine for an index measured in megabytes; wrong for packs measured in hundreds of megabytes.
- Cloudflare R2: 10 GB-month free, then $0.015 per GB-month, 1 million Class A and 10 million Class B operations free per month, and **no egress fee** ([R2 pricing](https://developers.cloudflare.com/r2/pricing/)). S3 charges about $0.09 per GB of egress on the first 10 TB ([comparison](https://www.deployhq.com/blog/aws-s3-vs-cloudflare-r2-a-deep-dive-for-deployhq-users)).

For a registry, egress is the whole bill: a popular pack is downloaded far more often than it is stored.

### Recommendation

- **Index in a git repository, `xjmusic/commons`**, published as static JSON through GitHub Pages or raw content. Publishing is a pull request (which also resolves Q5). History is the audit log. Mirrors are a `git clone`.
- **Artifacts on Cloudflare R2** behind a custom domain, addressed by content hash. A CI workflow on the index repository verifies the signature and lint, uploads the artifact, and only then merges the index entry. At 100 GB of packs the storage bill is about $1.50 a month and the egress bill is zero.
- Because artifacts are hash-addressed, any mirror (a venue's NAS, a USB stick, another bucket) is interchangeable, and the workstation tries mirrors in order.

### What remains

Nothing that blocks. Someone creates the repository and the bucket in Phase 4.

### Changes to the proposal

[06-commons.md](06-commons.md): registry section rewritten around the repository plus bucket.

---

## Q8 · Unreal plugin and the C ABI

### What was found

The plugin's setup script clones this repository, checks out the tag `v1.9.6` inside `engine/`, and builds the `xjmusic` CMake target in Release. It builds the engine from source and uses the C++ API directly ([UnrealPlugin Setup.sh](https://raw.githubusercontent.com/xjmusic/UnrealPlugin/main/XJMusicPlugin/Setup.sh)).

### Recommendation

**The plugin stays on the C++ API in 2.0.** It bumps its tag to the 2.0 release and gains the cue, seed and diagnostics features through their C++ signatures, which exist anyway because the C ABI is implemented on top of them. The C ABI's customers are the Rust workstation, Unity, Godot, Python and the WebAssembly build. The plugin revisits the question only if it wants to adopt `xj-dsp` for rendering, which would come through `xj-dsp`'s own C ABI.

One engine-side consequence: the C++ API must remain a first-class, documented surface in 2.0, not a private implementation detail of the C ABI. [04-engine.md](04-engine.md) already says so; this confirms why.

### What remains

Nothing.

---

## Q9 · The HLS ship path

### What was found

The Java ship stage chunks PCM into HLS media segments following Apple's authoring spec, as the legacy streaming platform required. In V2 the DSP has a WAV capture sink, so an HLS sink is the same bytes with a segmenter and an encoder in front.

### Recommendation

**Archive it with the Java workstation; do not carry it into the 2.0 core.** Record the design as a one-page note so a future `xj-ship-hls` crate can be written in a week or two if a use case appears (a venue streaming its show, a web radio built on a station). The one open technical point for that future crate is the encoder: HLS audio means AAC in practice, and the usual AAC encoders carry licensing conditions, so an Opus-in-fMP4 or MP3 path may be preferable; decide when the crate is written, not now.

### What remains

Nothing in 2.0.

---

## Q10 · Minimum platforms

### What was found

- Tauri 2 floors: macOS 10.15, Windows with WebView2 (preinstalled from Windows 10 1803), webkit2gtk 4.1 on Linux ([Tauri prerequisites](https://v2.tauri.app/start/prerequisites/)). webkit2gtk 4.1 ships in Ubuntu 22.04 and Debian 12 and later.
- Rust's Apple Silicon target requires macOS 11 or later; the Intel target runs on older systems but Apple's notarization and signing toolchain favors recent SDKs.
- Steinberg relicensed ASIO in October 2025 as dual GPLv3 or proprietary; the proprietary agreement is free of charge but must be countersigned by Steinberg before a proprietary application ships with it, and developers report that countersigning can take weeks ([Heise](https://heise.de/-10963451), [Steinberg forum](https://forums.steinberg.net/t/where-do-i-submit-the-proprietary-asio-sdk-license-agreement/1035889)). cpal's `asio` feature downloads the SDK at build time ([asio-sys](https://docs.rs/crate/asio-sys/latest)).

### Recommendation

| Platform | Minimum | Architecture | Audio backend |
|---|---|---|---|
| macOS | 12 (Monterey) | Universal binary: arm64 and x86_64 | CoreAudio |
| Windows | 10 21H2 and 11 | x64 (ARM64 later if trivial) | WASAPI shared by default, WASAPI exclusive as an option; ASIO behind a feature flag, enabled in official builds only after Steinberg countersigns the agreement |
| Linux | glibc 2.35 or later (Ubuntu 22.04, Debian 12, Fedora 36 and newer) | x86_64 | ALSA direct, JACK, and PipeWire through its JACK or ALSA shims |

No 32-bit builds. No mobile in 2.0.

Submit the ASIO agreement in Phase 0 alongside the Link email; both are paperwork with long tails.

### What remains

Confirming Windows on ARM once there is a machine to test on.

---

## Q11 · Telemetry

### Recommendation

- **No usage telemetry in 2.0.** Musicians are rightly suspicious of it, and the proposal's determinism work provides a better bug report than any counter: a capture file reproduces the chain.
- **Opt-in crash reporting**, off by default, with a visible toggle and a plain description of what is sent, through the `sentry` Rust SDK pointed at a self-hosted GlitchTip or the Sentry free tier.
- **A local diagnostics bundle** the user can export from the Help menu: logs, engine messages, the current show lock, the last capture, system info. This is what bug reports should attach, and it needs no network.

### What remains

The company's confirmation. The recommendation is the conservative default.

---

## Q12 · Name

### Recommendation

Keep one product name: **XJ music workstation**, version 2. Inside it, the modes are Play, Make and Ship. The headless runtime is **XJ station** (already `xj-station` in the architecture). A group of stations is a **session**. The shared library is **the Commons**.

Do not launch a second brand for the performance mode in 2.0. A name is cheap to add later and expensive to retract, and the game-audio audience should see continuity. If the show in Phase 7 wants a name on the poster, that is the moment to choose one.

### What remains

Marketing's call. The proposal only asks that the code and documents use the names above consistently, which they now do.

---

## Q13 · Cue request policy in a session

### Recommendation

**Default: accept all, from everyone, with the sender shown.** Bands work by listening, not by arbitration. A conductor who wants control flips a per-station trust toggle, and an "ask" mode exists for shows with a designated music director. The default favors the duet case the vision describes, where either performer steers.

One rule the engine enforces regardless of policy: two foundation cues that land at the same boundary are applied in arrival order, and the second one wins. The UI shows the collision. That is enough; adding priority rules invites the arguments it claims to prevent.

### What remains

Performers' feedback after the Phase 5 duet, which may move the default.

---

## Q14 · Different `barBeats` across stations

### What was found

`barBeats` is not a template or session property. It is a field of `ProgramConfig`, so it is set per program, and a single station can already switch between a 4-beat main program and a 3-beat one at a macro change. The engine reads the *main program's* `barBeats` for sub-section arithmetic in `Craft.cpp` and `TransitionCraft.cpp` ([04-engine.md](04-engine.md), follow-mode caveat table). Beat and detail programs have their own `barBeats`; the engine does not currently check that they agree with the main program's.

### Recommendation

**Handle it by candidate filtering, not by refusing the session.** The foundation record already carries the conductor's `barBeats` per segment. In follow mode (and, as a free improvement, in internal mode too), a beat, detail, transition or background program whose `barBeats` differs from the segment's is excluded from the marble bag with a diagnostic ("excluded: 3-beat program against a 4-beat segment"). A follower with no matching material for a segment plays nothing for that layer rather than something that drifts, and the UI says why.

So the answer to "should follow mode permit stations with different `barBeats`" is: stations never had a `barBeats`; segments do; and every station already has to cope with it. This closes the question without adding a reject reason to the `hello` handshake.

### What remains

Nothing.

### Changes to the proposal

[04-engine.md](04-engine.md): candidate filtering by `barBeats` added to follow mode. [07-session-protocol.md](07-session-protocol.md): the `barBeats` non-goal is removed.

---

## A measurement that changes a risk: craft timing

The first pass left "how long does a segment craft take" to a Phase 0 spike and listed slow craft as what would force a redesign of the commit horizon (R3, and the "what would change the proposal's mind" list). It did not need to wait. The engine builds in a few minutes with CMake and the Visual Studio toolchain, and a 60-line harness that drives `Engine::RunCycle` at a 100 ms tick faster than real time gives the answer directly.

### Method

- Engine 1.9.6 from this repository, built with CMake and MSVC 19.44 in Release (`/O2`), single-threaded, on an Intel Core i9-14900HX with 64 GB under Windows 11. This is a fast machine; see the caveat below.
- A harness constructs an `Engine`, starts the project's first template, then calls `RunCycle` every 100 ms of simulated chain time for 600 simulated seconds and times each call. A cycle is classified as a craft cycle if the segment count grew during it, a rewrite cycle if a macro override was applied just before it, and idle otherwise.
- Projects: the engine's own test project, and the four demo projects from `xjmusic/xjmusic-demos` (JSON only; the engine schedules audio by reference and does not need the files to craft).

### Results

Default horizons (craft-ahead 20 s, dub-ahead 10 s), no overrides:

| Project | Content | Load | Craft cycles in 600 s | Craft mean | Craft p95 | Craft max | Idle p99 |
|---|---|---|---|---|---|---|---|
| Engine test project | 8 programs, 3 instruments | 19 ms | 609 | 0.22 ms | 0.49 ms | 0.70 ms | 0.05 ms |
| Ambient Flow | 68 files | 28 ms | 9 | 7.5 ms | 14.9 ms | 15.4 ms | 0.07 ms |
| Deep House | 388 files | 108 ms | 92 | 4.6 ms | 16.0 ms | 24.2 ms | 0.07 ms |
| Hero's Journey | 98 files | 22 ms | 46 | 0.85 ms | 2.2 ms | 2.6 ms | 0.02 ms |
| Lofi Hip Hop | 122 files | 32 ms | 104 | 1.9 ms | 5.1 ms | 6.9 ms | 0.05 ms |

Play-mode horizons (craft-ahead 8 s, dub-ahead 2 s) with a macro override every 20 s, on the largest project:

| Project | Cycle kind | n | Mean | p95 | p99 | Max |
|---|---|---|---|---|---|---|
| Deep House | Craft, no override | 119 | 2.4 ms | 7.9 ms | 9.8 ms | 18.3 ms |
| Deep House | Override and rewrite of the future | 29 | 19.5 ms | 23.3 ms | 24.5 ms | 27.9 ms |
| Deep House | Idle | 5,852 | 0.02 ms | 0.05 ms | 0.05 ms | 0.20 ms |

Segment lengths across the demos' 289 main sequences: most are 16 or 32 beats (234 of 289), at 120 bpm in two thirds of cases, so a typical segment is 8 to 16 seconds and a bar is 2 seconds. Ambient Flow is the outlier, with main programs at 30 and 15 bpm and sequences up to 512 seconds long.

### What it means

- **Craft is not the constraint.** The worst cycle observed, a full rewrite of the planned future on the largest demo, took 28 ms. One beat at 124 bpm is 484 ms. A one-beat commit horizon in Play mode has an order of magnitude of headroom on this machine, and a 2019 laptop at a third of the speed still has several times headroom. The "craft on another thread before Play mode" contingency in [10-risks-and-open-questions.md](10-risks-and-open-questions.md) is retired; the control thread can keep crafting inline.
- **Idle cycles cost nothing** (tens of microseconds), so a 20 Hz tick is free and the station could tick faster if cue latency ever demanded it.
- **Rewrite cost scales with content, not with horizon.** Rewrites with an 8 s horizon cost about the same as crafts with a 20 s horizon on the same project, so shrinking horizons for Play mode has no craft-side penalty.
- **Load is fast enough for the first-run target.** The largest demo loads in about 110 ms and starts a template in about 60 ms, which leaves the 10-second first-sound budget almost entirely to audio decoding and the UI.
- **Ambient content changes the cue story.** With segments measured in minutes, "next segment" is not a usable boundary for an ambient show; the bar and beat boundaries and the Advance cue are. That is already how the cue model is designed; the data confirms the design needs all its boundaries.

### Caveats

One fast machine, one compiler, single-threaded, JSON content with no audio I/O. Phase 0 should repeat the measurement on a 2019 laptop and on the largest Commons pack once one exists, and should add the per-stage breakdown (macro/main, beat, detail, transition, background) that the engine's new timing messages will provide. The harness is about sixty lines and should be checked in with the engine tests so the numbers are reproduced by CI rather than by hand.

---

## Summary

| # | Question | Resolution | Still open? |
|---|---|---|---|
| Q1 | UI toolkit | Spike narrowed to Tauri 2 vs iced; surface is canvas-rendered either way; Tauri is the tie-breaker; Linux added to the spike | Spike result |
| Q2 | Stretch and shift | Signalsmith Stretch (MIT) by default, Bungee as the comparison; render variants ahead of time, never on the audio thread | Listening session |
| Q3 | Link license | Email Ableton in week one; `BeatClock` trait with `link` and `session` implementations; MIDI clock out as fallback for gear | Ableton's answer |
| Q4 | Base taxonomy | `xj-base@1` codifies the technical conventions the demos already use (tempo, density, size, structure, grammar); palettes are declared per pack and unioned per show; lint catches drift | Pack authors' review of the convention list |
| Q5 | Content equity | Same rule, same evidence; publishing is a PR to the index repository | Company yes/no |
| Q6 | Pack license | CC BY 4.0 default, CC0 for the starter pack, automatic `CREDITS.md`, contributor declaration | Legal review of wording |
| Q7 | Registry hosting | Index in a git repository; artifacts on Cloudflare R2 (zero egress), hash-addressed, mirrorable | None |
| Q8 | Unreal and the C ABI | Plugin stays on the C++ API; C ABI serves everyone else | None |
| Q9 | HLS | Archived with the Java workstation; one-page design note kept | None |
| Q10 | Platforms | macOS 12+ universal, Windows 10 21H2+ x64, Linux glibc 2.35+ x86_64; ASIO gated on Steinberg's countersignature | Windows on ARM |
| Q11 | Telemetry | None for usage; opt-in crash reports; local diagnostics bundle | Company confirmation |
| Q12 | Name | One product name; modes are Play, Make, Ship; runtime is XJ station | Marketing |
| Q13 | Cue requests | Accept all by default, sender shown, trust toggles, "ask" mode available | Performer feedback |
| Q14 | `barBeats` | Per-segment filtering of candidates, not a session rule | None |

Four are closed outright (Q7, Q8, Q9, Q14). Four need only a yes from the company or counsel (Q5, Q6, Q11, Q12). Three wait on a third party (Ableton for Q3, Steinberg and a Windows-on-ARM machine for Q10, the first pack authors for Q4). Three wait on a small experiment that now has a declared default (the toolkit spike for Q1, the listening session for Q2, performer feedback for Q13). None is open-ended any more.
