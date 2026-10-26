# Grounded map: repository meta, open issues, demos, docs, Unreal plugin

Scope: everything around the code that a V2 spec author will cite when designing labels and milestones, cross-linking issues, seeding the Commons from the demo projects, and keeping the Unreal plugin working. All facts below were read from `/Users/nck/xj/xjmusic` at commit `a94000b9a` (main, 2026-10-24), from the GitHub API via `gh` on 2026-10-25, and from blob-less sparse clones / raw fetches of the three sibling repositories. Nothing is inferred without saying so.

Working files produced alongside this map (all under `(research workspace) `): `open-issues.json` (raw `gh issue list` output, 44 issues), `demos-src/` (sparse clone, JSON only), `demos-all-files.txt` (full tree listing of the demos repo, 3,687 paths), `analyze-out.txt` and `taxo-out.txt` (demo content analysis output), `unreal/` (fetched plugin sources).

---

## 1. Repository meta files

| File | Lines / size | What it establishes |
|---|---|---|
| `README.md` | 60 lines | Product framing ("Adaptive music engine ... for video games"); links to workstation, engine, demos repo, UnrealPlugin, docs sites; the **$250K budget threshold**; the **equity formula**; badges for 4 workflows plus a `license-free_for_indies` badge and an `Unreal 5.4.4+` badge |
| `CONTRIBUTING.md` | ~190 lines | Godot-derived contributor guide: MRP in bug reports (exclude `render/`, keep `project.xj`); propose features with `enhance` or `feature` label; one-topic PRs; `git pull --rebase`; commit title < 72 chars imperative ("Add", "Fix", optional area prefix "Core:"); `Fixes #1234` in body not title; unit tests expected |
| `LICENSE.txt` | 2 documents | Software license + workstation EULA (details below) |
| `CODE_OF_CONDUCT.md` | 128 lines | Contributor Covenant 2.x; enforcement contact `hi@xjmusic.com` (line 63) |
| `SECURITY.md` | 5 lines | Vulnerabilities to `hi@xjmusic.com`, not the tracker |
| `CONFIDENTIALITY.md` | 3 lines | "By accessing the contents of this folder" ... "confidential property of XJ Music Inc." Sits at repo root in a public repo; a leftover from the monorepo consolidation (#367, closed 2024-05-04) |
| `.github/ISSUE_TEMPLATE/bug_report.yml` | 68 lines | The only issue form (details below) |
| `.github/nothing_left_todo.sh` | 25 lines | Fails CI if `\btodo\b` (case-insensitive) appears anywhere in `<component>/src`, `/include`, `/test`; takes exactly `workstation` or `engine` |
| `.github/workflows/` | 6 files | `engine_ci.yaml`, `engine_pr.yaml`, `engine_distro.yml`, `workstation_ci.yml`, `workstation_pr.yml`, `workstation_distro.yml` |
| `design/` | 17 entries | Patent PDF, instrument/program/segment model PDFs v0.5, mixer design v6 JPG, legacy streaming architecture, `XJ-Legacy-Mobile-App-Live-Streaming-Model.md`, and `v2-proposal/` |

### 1.1 License terms (LICENSE.txt, verbatim structure)

The file is two documents concatenated. Spec authors must distinguish them because the Commons licensing section (06-commons.md) says "the software keeps the current XJ music license" and that license has specific redistribution obligations.

**Document A: "Software License for the XJ music system" (© 2019-2024 XJ Music Inc.)**

| Section | Term |
|---|---|
| 1 Introduction | Covers both workstation and playback engine as implementations of **US Patent 10,446,126**. "A commercial license for using the XJ music system to generate musical audio compositions in commercially distributed software (e.g., video games) and patent licenses ... may be obtained through XJ Music Inc. ... under separate terms." |
| 2 Copyright license | Output audio: "free and clear rights ... provided that you own the rights to the input audio and music used", personal and commercial. "A commercial software license is only required if you intend to embed the XJ Music software itself into another piece of commercial software." Redistribution in source and binary with or without modification permitted **without copyright fees** provided: retain full license text; binary redistributions must make complete source of the system **and your modifications** available free of charge (a copyleft-like clause); "XJ music" is a registered trademark, may not be used to endorse derived products; no charging license fees for the software; modified versions must carry prominent change notices and be renamed "Third-Party Modified Version of XJ music". |
| 3 No patent license | "NO EXPRESS OR IMPLIED LICENSES TO ANY PATENT CLAIMS ... ARE GRANTED." "You may use this XJ music system software or modifications thereto only for purposes authorized by appropriate patent licenses." |
| 4 Disclaimer | Standard AS IS |
| 5 Contact | XJ Music Inc, 7190 W Sunset Blvd #72, Los Angeles CA 90046; `licensing@xjmusic.com`; `www.xjmusic.com/license` |

**The $250K budget threshold does not appear in LICENSE.txt.** It appears only in `README.md` ("completely free for projects with a budget under $250K") and in the docs site home page (`xjmusic-docs/content/_index.md`). GitHub reports the license as `NOASSERTION` (not an SPDX identifier) for all three repos (xjmusic, UnrealPlugin, xjmusic-demos).

**Document B: "End-User License Agreement (EULA) for the XJ music workstation"**: non-exclusive non-transferable; single computer; **no reverse engineering, no derivative works** (3.2), which is in tension with Document A's redistribution-with-modification grant for the same workstation; XJ Music Inc may terminate at any time (7.2); California jurisdiction; contact `legal@xjmusic.com`, phone 212-960-8811.

The identical LICENSE.txt is committed to `xjmusic-demos` (verified via `git show HEAD:LICENSE.txt`) and to `UnrealPlugin`. **There is no content-specific license anywhere in the demos repo**: the demo audio is covered only by the software license text, which does not address sample redistribution rights at all. This is the single most important fact for the Commons seeding spec (see Implications).

### 1.2 Bug report form (`bug_report.yml`)

Fields, in order, with `required`:

| # | Label | Type | Required | Notes |
|---|---|---|---|---|
| 1 | (preamble) | markdown | — | "one issue for one bug"; search open and closed first |
| 2 | Tested versions | textarea | yes | Asks for workstation/engine version "+ Git commit hash"; version "can be seen in the About modal"; placeholder cites 1.7.25 [abc251928] |
| 3 | System information | input | yes | Placeholder: "Windows 10 - XJ music workstation v1.7.23 - Intel Core i7..."; mentions GPU/"rendering backend (GLES2, GLES3, Vulkan)", copied from Godot and irrelevant to XJ |
| 4 | Issue description | textarea | yes | |
| 5 | Steps to reproduce | textarea | yes | |
| 6 | Minimal reproduction project (MRP) | textarea | no | "not include the `render` folder ... (but keep `project.xj`)"; 10 MB zip limit |

There is no feature-request form, no `config.yml` (blank issues are allowed), no PR template, no CODEOWNERS, no Dependabot config, no `FUNDING.yml`. Open issues #424, #472, #476 were filed with this form (their bodies carry the four section headers).

### 1.3 CI and release gating (facts a milestone/versioning spec needs)

| Workflow | Trigger | Matrix | What it does |
|---|---|---|---|
| `engine_ci.yaml` | push to `main`, paths `engine/**` | ubuntu-latest, macos-13, macos-14, windows-latest | `nothing_left_todo.sh engine` → cmake build → `ctest --verbose` (Windows: `build/test/Debug/xjmusic_test.exe`) → Doxygen `make doc` → deploy `engine/build/doc/html` to GitHub Pages (engine-docs.xjmusic.com) |
| `engine_pr.yaml` | pull_request, paths `engine/**` | same 4 | build + test only |
| `engine_distro.yml` | push tags `*` | same 4 | `verify_version_number` reads `workstation/gradle.properties` `version=` and `engine/CMakeLists.txt` ` VERSION `; **tag must equal `v$VERSION` and workstation version must equal engine version** (lines 48-77); then Release build and upload `xjmusic-v<ver>-{macos-intel,macos-silicon,windows}.lib.zip` and `-linux.lib.tar.gz` to the GitHub release **and to an S3 bucket** (`secrets.AWS_S3_BUCKET`). Note line 157: Windows copies `src/Debug/xjmusic.lib` after a `-DCMAKE_BUILD_TYPE=Release` reconfigure (multi-config generator ignores that flag), so the shipped Windows `.lib` is plausibly a Debug build; stated as an inference from the script, not verified by downloading. |
| `workstation_ci.yml` | push to `main`, paths `workstation/**` | same 4 | `nothing_left_todo.sh workstation` → JDK 17 → `./gradlew build test` |
| `workstation_pr.yml` | pull_request, paths `workstation/**` | | same |
| `workstation_distro.yml` | push tags `*` | | version check as above; `:jpackage`; macOS DMG signed (Apple cert into keychain) and **notarized** (`xcrun notarytool submit ... --wait`); artifacts `xjmusic-workstation-v<ver>-installer-{macOS-Intel,macOS-Silicon}.dmg.zip`, `-Windows.exe.zip`, Linux DEB; uploaded to release and S3 |

Version sources today: `engine/CMakeLists.txt:6` `VERSION 1.9.6`; `workstation/gradle.properties:4` `version=1.9.6`. **The release gate requires these to be identical**, which directly conflicts with 09-roadmap.md's version plan (engine 2.0.0 while the Java workstation stays 1.9.x). Any V2 release spec must change `engine_distro.yml` lines 70-77 first.

Secrets referenced across workflows: `REPO_TOKEN`, `REPO_USERNAME`, `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_REGION`, `AWS_S3_BUCKET`, Apple signing/notarization secrets, `GITHUB_TOKEN`. All actions pinned loosely (`actions/checkout@v2`, `svenstaro/upload-release-action@v2`).

Release history (`gh release list`): v1.9.6 (2024-09-18, latest), v1.9.5 "Floating Point Locale US", v1.9.4 "Workstation CLI opens Project", v1.9.3, v1.9.2 "Workstation JSON", v1.9.1 "Engine Updates", v1.8.42 "Build → Engine", v1.7.41, v1.7.40 "Open Source" (2024-06-09), v1.7.39 "Monorepo" (2024-05-03). No release since September 2024; the only commit activity since is README edits and PR #479 (the V2 proposal, merged 2026-10-24).

---

## 2. GitHub tracker state

### 2.1 Repository

`gh api repos/xjmusic/xjmusic`: created 2023-08-03; default branch `main`; 97 stars, 3 forks; `has_projects: true`, `has_discussions: true` (1 discussion: #383 "Welcome to xjmusic Discussions!", Announcements), `has_wiki: false`; topics `adaptive-music, game-development, music, music-engine, video-game-music`; homepage `https://xjmusic.com`. **44 open issues, 163 closed** (207 total). 0 open PRs. Last merged PRs: #479 (V2 proposal), #478/#477 README edits, #474 (2024-09-18).

**Projects (v2):** `gh project list --owner xjmusic` failed: `your authentication token is missing required scopes [read:project]`. The user's request names a new project "v2.0 Collaborative Musical Instrument" in `github.com/xjmusic/xjmusic/projects`; whether any project exists today could not be read. Any spec that creates the project needs `gh auth refresh -s read:project,project` first.

### 2.2 Labels (12, `gh label list`)

| Label | Color | Description | Used on open issues |
|---|---|---|---|
| `bug` | d73a4a | Something isn't working | 3 (#424, #472, #476) |
| `enhance` | 0c7913 | Improvement | 30 |
| `feature` | 1aff29 | Major new feature | 9 |
| `chore` | 0075ca | Needs to be done to support other work | 1 (#437) |
| `help wanted` | 7057ff | Extra attention is needed | 1 (#307) |
| `docs` | 0075ca | Improvements or additions to documentation | 0 |
| `comms` | c0c0c0 | Marketing & communications | 0 |
| `good first issue` | 7057ff | Good for newcomers | 0 |
| `duplicate`, `invalid`, `question`, `wontfix` | GitHub defaults | | 0 |

There is **no `v2` label, no phase labels, no area/component labels** (engine vs workstation vs unreal), no priority labels. 09-roadmap.md says "each bullet becomes one or more issues labeled `v2` and the phase"; none of those labels exist yet. `enhance` (not `enhancement`) is the house spelling; CONTRIBUTING.md references it.

### 2.3 Milestones (11, `gh api .../milestones?state=all`)

| # | Title | State | Open / closed | Description |
|---|---|---|---|---|
| 4 | Beta | closed | 0 / 59 | |
| 7 | v1.1 | closed | 0 / 19 | |
| 8 | v1.2 | closed | 0 / 11 | |
| 10 | v1.3 | closed | 0 / 5 | |
| 9 | v1.4 | closed | 0 / 2 | |
| 6 | v1.5 | closed | 0 / 1 | |
| 5 | v1.6 | closed | 0 / 6 | |
| 3 | v1.7 | closed | 0 / 11 | |
| 1 | v1.8 | closed | 0 / 25 | |
| 2 | v1.9 | **open** | 2 / 25 | "Game Integration" |
| 11 | 1.10 | **open** | 41 / 0 | "Advanced integration" (created 2024-08-03) |

Naming is inconsistent (`v1.9` with prefix, `1.10` without). No due dates on any milestone. `1.10` is a parking lot: 41 open, 0 closed, nothing touched since 2024-09-10. All milestones were created in a single import on 2024-04-13/14 (from the former `xjmusic/workstation` repo; issue bodies still link `github.com/xjmusic/workstation/files/...` and `.../assets/...`).

### 2.4 Open issues: authorship and age

44 open: 42 by `charneykaye`, 1 by `ptrptrstasiv` (#471), 1 by `JamalWhitaker` (#359). Created: 35 in 2024-04 (the Trello/PRD import), 1 in 2024-06, 5 in 2024-07, 1 in 2024-08, 2 in 2024-09. Only three have comments (#308: 1, #309: 2, #312: 1). Several carry checklists that reference closed child issues (#313 "Program Editor" is closed and checked in #312 and #317).

---

## 3. Open issue disposition against V2

Legend for "V2 disposition": **subsumed** (V2 element replaces or absorbs it; close with a pointer when the V2 issue is filed), **stays V1** (a Java-workstation or 1.9.x maintenance item, per 09-roadmap principle 3 the Java workstation is bug-fix-only after Phase 1), **carry** (still valid in V2 with a different implementation; file as a V2 issue), **unrelated** (process/infra).

| # | Title | Label | V2 disposition | V2 element / area |
|---|---|---|---|---|
| 303 | Verified software download on Windows | enhance | carry | Phase 3 packaging and signing (05-workstation; `workstation_distro.yml` already notarizes macOS, not Windows) |
| 305 | Live Pause/Play behavior | feature | subsumed | `xj-transport` time model; Play mode transport controls (03, 05) |
| 306 | Error reporting system for problems with the content in a template | enhance | subsumed | `xj-content lint` + engine diagnostics behind flags (06 quality bar, 04) |
| 307 | UX/UI design R&D | help wanted | subsumed | Phase 0 UI toolkit spike + Phase 3 usability sessions (09, 11 Q1) |
| 308 | Show colors for instrument types | feature | carry | Performance surface layer strips (05/08); color-per-type becomes a workstation theme concern |
| 309 | Intro tour on first open | feature | subsumed | Phase 3 "Starter pack bundled; first-run tour" (09); the issue's "clone the demo right away" becomes "starter pack is already there" |
| 310 | Workstation uses C++ engine library via JavaCPP bindings | enhance | subsumed (replaced) | `xj-engine-sys` C ABI + Rust workstation (03, 04). The goal (one engine) is met by a different route; the JavaCPP approach is abandoned |
| 311 | Audio files prepared as Vorbis and streamed off disk | enhance | subsumed | `xj-content` `symphonia` decode + content-hash cache (06 "What it replaces"); depends-on #310 in its body |
| 312 | Program Editor can drag to re-order Voices & Tracks | enhance | stays V1 → Phase 6 | Make mode parity, program editor (09 Phase 6) |
| 315 | Undo feature to undo N steps of editing | feature | stays V1 → Phase 6 | Make mode editors |
| 316 | Entering memes populated from list of already used memes | enhance | stays V1 → Phase 6 | Editors; also informs `xj-base` taxonomy UI (11 Q4) |
| 317 | Program Editor Events multi-select and edit | enhance | stays V1 → Phase 6 | Program editor event mode |
| 318 | Logs can be copied to clipboard | enhance | stays V1 | Trivial in a new shell; not a V2 design item |
| 319 | File tree style content browser | enhance | stays V1 → Phase 6 | Make mode content browser |
| 320 | Timeline status shows red when dub is behind ship | enhance | subsumed | Craft/dub timing as segment messages + "why" panel (04, 05) |
| 321 | Program Editor grid visible behind Events in Detail Programs | enhance | stays V1 → Phase 6 | Program editor |
| 322 | Content Reports | enhance | subsumed | `xj-content lint` / pack report (06) |
| 323 | Program/Instrument/Template Config has visual editor | feature | carry → Phase 6 | Config editors; the issue proposes reusing the engine's bare HOCON parser, which V2's C ABI could expose |
| 324 | Content Report: Macro/Main Program length | enhance | subsumed | `xj-content lint` (06) and the `analyze_demos.py` main-sequence-length analysis in `design/v2-proposal/tools/` |
| 325 | Segment Preview view any segment from timeline as multitrack | feature | subsumed | Preview and choice diagnostics behind flags (04); timeline "why" panel (05) |
| 326 | Open .xj files automatically | enhance | carry → Phase 3 | Packaging: file association for `.xj` and the new `.xjpack` (06) |
| 327 | Splash preloader | enhance | unrelated / stays V1 | JavaFX-specific; V2 target "first launch to sound < 10 s" (09) supersedes |
| 328 | Template meme taxonomy includes custom colors | feature | carry | Palette rows on the performance surface (05/08); pack `[taxonomy]` could carry colors (06) |
| 329 | Instrument audio batch processing | feature | carry → Phase 6 | Instrument and audio editor ("first, because packs need it", 09 Phase 6); `xj-content pack` tooling (06) |
| 330 | Quantization of instrument audio and detail programs | enhance | carry → Engine | Engine content fields; relates to 08 quantized cues but is a craft-time alignment, not a performer cue |
| 331 | Mixer bus/compressor enhancements | enhance | subsumed | `xj-dsp` buses, master dynamics ported from Java mixer (03, 05); body says "This will be moved over to the C++ Nexus project" |
| 332 | Chord-Mode Instrument can follow detail program rhythm | enhance | carry → Engine | Craft feature; unaddressed by the proposal |
| 336 | Scaffold content to improve music creation workflow | enhance | stays V1 → Phase 6 | Editors |
| 337 | Manual taxonomy mode: add taxonomies implied by number memes | enhance | subsumed | `xj-base@1` codifies `NNNBPM` numeric memes (11 Q4); palette UI (08) |
| 338 | Right-clickable menus use default context menu | enhance | stays V1 | JavaFX-specific |
| 339 | Instrument modes locked to available modes for given type | enhance | carry → Phase 6 + lint | Editor validation; `xj-content lint` rule |
| 340 | Program/Instrument/Audio list attributes can be edited in place | feature | stays V1 → Phase 6 | Editors |
| 341 | Manual control taxonomy disables impossible choices | enhance | subsumed | Performance surface palettes with content-aware availability (08); depends on #337 |
| 343 | Timeline shows intensity of elements from manual override | enhance | subsumed | Performance surface intensity + layer strips; "why" panel (05, 08) |
| 359 | Workstation responds to system display scale setting | enhance | subsumed | New toolkit (Tauri/iced both DPI-aware); Phase 0 spike criterion |
| 400 | Entities all use integer ids, not UUID | enhance | **conflicts** | 06 says "Entity ids remain UUIDs" and adds `originPack`/`originVersion`; this issue proposes integer ids in the build export. Must be explicitly closed or reconciled |
| 415 | Workstation should preprocess special accidentals ♯ → # and ♭ → b | enhance | carry | `xj-content` read path normalization; relates to #377 (closed) |
| 423 | Workstation & Unreal Plugin can build & utilize OGG audio files | enhance | subsumed (partly) | `symphonia` decode in `xj-content`; **Unreal half stays open** (UnrealPlugin#1 unchecked item); Build-for-engine container choice (06) |
| 424 | Workstation should not lose audio files when content is renamed | bug (v1.9) | stays V1; moot in V2 | Content-addressed audio (BLAKE3 `contentHash`, 06) eliminates the folder-rename class of bug |
| 437 | Engine complies with C++ standards | chore | carry → Phase 1 | Engine 2.0 code standards; interacts with "C++ API must remain a first-class documented surface" (11 Q8) |
| 440 | C++ music engine ships javacpp bindings | enhance | subsumed (replaced) | C ABI (04); JavaCPP abandoned with #310 |
| 471 | Adjustable fade out function for instrument release time | enhance (no milestone) | subsumed | `xj-dsp` voice envelopes (05); today `releaseMillis` is a linear/sine fade in both Java mixer and Unreal `Envelope` |
| 472 | Engine example FTXUI menu collapses after releasing the mouse | bug | stays V1 / unrelated | Upstream FTXUI bug (PR 909); `engine/example/` is replaced by `xj-station` CLI in Phase 2 |
| 476 | Workstation preview should not skip time when taxonomy changes | bug (v1.9) | stays V1; root cause relevant to V2 | A regression in the Java engine's override/rewrite path; V2's quantized cues with `commit_horizon` (04, 08) are the structural fix; the follow-mode/determinism tests should cover it |

Counts: subsumed 19, carry 11, stays V1 (mostly Phase 6 parity) 12, unrelated 2. Not one open issue mentions MIDI, OSC, sync, Link, sessions, networking, packs, or licensing: the entire Phase 2 control stack, Phase 4 Commons, and Phase 5 Sessions have **zero existing issues** to cross-link.

Closed issues worth citing as precedent (from the 163): #333 "Open source code available on GitHub" (2024-05-14), #367 "Consolidate platform into a single repository", #380 "Software License" (2024-07-13), #372-#381/#401 the C++ port series ("Hub models in C++", "Content Store and JSON deserialization in C++", "Engine Fabricator in C++", "Engine Craft in C++", "Engine work in C++"), #421 "Workstation has Build functionality to prepare assets for Engine", #433 "Engine distro CI ships static library to release on GitHub", #446 "Engine vendors all dependencies", #449 "Engine cycle returns incremental audio schedule", #450 "InstrumentAudio requires known length of audio", #452/#454/#456 pretty, dash-separated, stably ordered JSON project files, #463 "Workstation opens a file provided in the first command-line argument" (used by the Unreal toolbar button).

---

## 4. The Unreal plugin (`xjmusic/UnrealPlugin`)

Repo meta: default branch `main`, last push 2025-01-08, 6 open issues, license NOASSERTION (same LICENSE.txt). Releases: `v1.0.0-1.9.5` (2024-09-21), `v1.0.1-1.9.5`, `v1.0.1-1.9.6`, `1.0.2-1.9.6` "Showcase Hero's Journey" (2024-09-27, latest). **Release tag convention is `<plugin>-<engine>`**, which is the only place in the whole org where plugin and engine versions are decoupled.

### 4.1 How it builds against the engine

`XJMusicPlugin/Setup.sh` (15 lines) and `Setup.bat`:

```
TAG="v1.9.6"            # line 1
git clone https://github.com/xjmusic/xjmusic.git xj/      # line 5
cd xj/engine && git checkout tags/$TAG                    # lines 7-9
mkdir -p build && cd build && cmake .. && cmake --build . --config Release --target xjmusic   # 11-15
```

So the plugin **vendors nothing**: it clones the whole monorepo into `XJMusicPlugin/xj/` and builds the static library from source at a pinned tag. `XJMusicPlugin.Build.cs` then hard-codes:

- `PublicIncludePaths`: `../../xj/engine/include/` and `../../xj/engine/build/_deps/json-src/include` (nlohmann JSON pulled by the engine's CMake FetchContent) — lines 36-37
- `PublicAdditionalLibraries`: `../../xj/engine/build/src/Release/xjmusic.lib` — line 39. **This is a Windows multi-config path.** On macOS/Linux the engine CMake emits `build/src/libxjmusic.a`, so Build.cs as committed cannot link on those platforms, even though `distribution.yml` runs `Setup.sh` on ubuntu-latest and macos-13 and ships `xjmusic-plugin-<tag>-{linux.tar.gz,macOs.zip}`. The macOS/Linux plugin zips therefore contain an engine build tree but a Build.cs that points at a file that does not exist there (inference from the file paths; not verified by opening a release archive).
- `CppStandard = Cpp17`; UE modules: Core, Projects, InputCore, CoreUObject, Engine, Slate, SlateCore, AudioMixer, AudioExtensions.

`.uplugin`: `VersionName "1.0"`, Category "Audio", two modules `XJMusicPlugin` (Runtime) and `XJMusicPluginEditor` (Editor), `CanContainContent: false`, `DocsURL https://docs.xjmusic.com/`, `SupportURL "mailti:hi@xjmusic.com"` (typo). README requires CMake ≥ 3.24 and UE 5.4+; the example project is `Examples/Showcase/XJTestWorld.uproject` with maps `IntensityDemo`, `RadioDemo`, `TaxonomyDemo`, `TopDownMap`, and 109 cooked audio `.uasset`s under `Content/XJ/vgm/` named by InstrumentAudio UUID plus `vgm.uasset` (the imported Hero's Journey build).

### 4.2 How it consumes the engine (C++ API surface actually used)

`Private/Engine/XjMainEngine.cpp` is the only file that touches `XJ::`:

| Line | Call | Note |
|---|---|---|
| 7-9 | `#include <xjmusic/work/WorkManager.h>`, `<xjmusic/fabricator/SegmentUtils.h>`, `<xjmusic/Engine.h>` | |
| 10-21 | `MakeUnique<Engine>(path, Fabricator::ControlMode::Taxonomy, craftAhead=60, dubAhead=30, deadline=nullopt, persistence=nullopt)` | **Plugin runs craft-ahead 60 s / dub-ahead 30 s**, not the engine defaults (20/10) cited in 02-where-v1-stands.md; same as the Java workstation's `fabrication.defaultCraftAheadSeconds=60` |
| 29-50 | `getSettings()`, `getProjectContent()->getTemplates()`, `start(FirstTemplate->id)` | Always starts the **first** template in the `std::set` (pointer order); no template selection |
| 59 | `doOverrideMemes(std::set<std::string>)` | from `DefaultTaxonomies` settings |
| 66 | `RunCycle(ChainMicros)` → `std::vector<AudioScheduleEvent>` | reads `Event.type`, `schedule.getId()`, `getAudio()->waveformKey/name`, `getStartAtChainMicros()`, `getStopAtChainMicros()`, `getReleaseMillis()`, `getFromVolume()`, `getToVolume()` |
| 120, 142-152 | `doOverrideMemes`, `getAllMacroPrograms()`, `doOverrideMacro(const Program*)` | macro matched by **name string** |
| 160 | `setIntensityOverride(float)` | |
| 173 | `getTemplateContent()->getMemeTaxonomy().getCategories()` → `MemeCategory::hasMemes/getMemes` | |
| 213-219 | `WorkSettings` fields `craftAheadMicros`, `dubAheadMicros`, `deadlineMicros`, `persistenceWindowSeconds` | |
| 231-261, 299-334 | `getSegmentStore()->readChoice(id, Program::Main)`, `readAllSegmentChoiceArrangementPicks`, `readAllSegments`, `readAllSegmentMemes`, `readAllSegmentChoices`; `Segment` fields `id, delta, beginAtChainMicros, type, total, durationMicros, intensity, tempo, key`; `SegmentChoice` fields `programId, programType, instrumentId, instrumentType`; `Pick` fields `startAtSegmentMicros, lengthMicros, instrumentAudioId`; `InstrumentAudio` `transientSeconds, lengthSeconds`; `program->config.barBeats` | debug chain view |

Everything else is Unreal-side:

- **Threading**: `FXjRunnable` (`XjManager.h:28`) runs the engine on its own `FRunnable` thread at `RunCycleFrequency = 9` Hz, consuming a `TQueue<XjCommand>` of `TaxonomyChange | MacrosChange | IntensityChange`. The engine is never touched from the game thread.
- **Audio**: `XjMixer.cpp:114-115` hard-codes `SampleRate = 48000; NumChannels = 2`; mixes int16 `USoundWave` sample data with a per-voice sine fade (`Envelope`, `XjMixer.h:14-60`) over `ReleaseTime`, linear from/to volume, then `ApplyLogarithmicCompression` on the summed buffer (line 196). Output is a `USoundWaveProcedural` subclass `UXjOutput` producing Float PCM, exposed as a Sound Cue node `UXjOutputSoundNode` ("XJ Player", `IsPlayWhenSilent`, `INDEFINITELY_LOOPING_DURATION`). No buses, no per-type compression; UnrealPlugin#4 asks for a MetaSounds node.
- **Content path**: the Editor module's "XJ Build" toolbar (`XJMusicPluginEditor.cpp:99-169`) opens a directory picker, expects `<project>/build/`, finds `*.wav` recursively (line 132) and imports them via `AssetToolsModule.ImportAssetTasks`; `UXjProject` (`XjProject.h`) stores the build folder's files as `TArray<FFileData>` byte blobs inside a `.uasset` and `RestoreDirectory` writes them back to a runtime temp dir at game start (`XjManager.cpp:31` passes `<RuntimeProjectDir>/<ProjectName>.xj` to `Engine::Setup`). `UXjAudioLoader::RetrieveProjectsContent` (`XjAudioLoader.cpp:177`) maps `AssetName + ".wav"` → soft object path, i.e. **the plugin can only resolve WAV waveformKeys**; OGG support is the unchecked item in UnrealPlugin#1 and xjmusic#423.
- **Settings**: `UXJMusicDefaultSettings` (config `XJMusicConfig`): `FXjSettings { LaunchProject (soft path to UXjProject), bDefaultOutput=true, bShowDebugTimeline=true, DefaultTaxonomies }` and `PathToXjMusicWorkstation` (used by `FPlatformProcess::CreateProc` to launch the workstation, line 96; relies on xjmusic#463). `ASettingsActor` lets a level override each field.
- **Blueprint API** (`XjMusicInstanceSubsystem.h:30-44`): `DoOverrideTaxonomy(FString)`, `DoOverrideMacro(FString)`, `DoOverrideIntensity(float)`, `GetAllTaxonomyMemes()`, `GetAllMacros()`. Taxonomy string is whitespace-split into a meme set.

### 4.3 Unreal plugin issues

| # | State | Title | V2 relevance |
|---|---|---|---|
| 1 | open | Unreal Engine Plugin (epic checklist, mostly done) | Unchecked: ".uplugin at repo root", "Examples folder" restructure; OGG |
| 2 | closed | UE5 appears to hang while building XJ project | |
| 3 | open | Ability for game to start/stop music | Maps to transport semantics in 04/07 (engine starts work immediately in `Engine::start`) |
| 4 | open | XJ mixer output functions as a node inside MetaSounds | Candidate for `xj-dsp` C ABI (11 Q8 "revisits only if it wants to adopt xj-dsp") |
| 5 | open | Build & ship complete plugin package in CI (4 platforms incl. Mac Silicon) | `distribution.yml` exists but builds on macos-13 (Intel) only and has the Build.cs path problem above |
| 6 | open | YouTube video demonstrating the plugin (docs) | |

The Engine's build-folder contract the plugin depends on: `engine/src/Engine.cpp:163-172` resolves `<folder containing .xj>/build/` and throws `"Build folder does not exist. Please build the project before playing it."`; `Engine.h:119` `getBuildPath()`. The Build step writes `build/<InstrumentAudio.id>.<container>` (`ProjectManagerImpl.java:425-427`) and `build/<name>.json`, with defaults from `workstation/src/main/resources/application.properties`: `build.defaultOutputChannels=2`, `build.defaultOutputContainer=WAV`, `build.defaultOutputFrameRate=48000`, `build.defaultOutputSampleBits=16`. The Unreal mixer's hard-coded 48000/2 matches these defaults and nothing checks that they agree.

---

## 5. The demo content (`xjmusic/xjmusic-demos`)

Sparse clone restricted to `*.json` and `*.xj` at `(research workspace) demos-src` (HEAD `8b9a021c`, 2024-07-29, "Heroes journey main programs all have BPM meme (#9)"). Full tree listing (`git ls-tree -r`) has **3,687 paths**: 2,995 `.wav`, 676 `.json`, 4 `.xj`, 4 `.jpg`, 5 `.md`, LICENSE.txt, `.gitattributes` (`*.wav filter=lfs`: **all audio is Git LFS**), `.gitignore` (ignores `build/` and `render/`; none committed). Repo-level files mirror the main repo (CODE_OF_CONDUCT, CONFIDENTIALITY, CONTRIBUTING, SECURITY, LICENSE). README shows a 2×2 image grid of the four demos.

### 5.1 The four projects

| Folder | `.xj` project name | Template name | `shipKey` | `platformVersion` | Libraries | Programs (Macro/Main/Beat/Detail) | Instruments | Audios in JSON | WAV files in repo |
|---|---|---|---|---|---|---|---|---|---|
| `Ambient-Flow-Demo` | Space | Flow | `space_flow` | 1.9.2 | 11 | 43 (12/13/4/14) | 13 | 927 | 927 |
| `Deep-House-Demo` | Bump | Bump: Deep House | `bump_deep` | 1.9.2 | 16 | 281 (12/103/8/158) | 90 | 1,432 | 1,429 |
| `Heros-Journey-Demo` | Hero's Journey | Video Game Demo | `vgm` | 1.9.2 | 10 | 21 (9/12/0/0) | 66 | 98 | 98 |
| `Lofi-Hip-Hop-Demo` | Slaps | Slaps: Lofi | `slaps_lofi` | 1.9.2 | 12 | 58 (4/24/11/19) | 51 | 543 | 541 |
| **Total** | | | | | **49** | **403** | **220** | **3,000** | **2,995** |

(The 11 Q4 numbers "685 files, 403 programs, 220 instruments, 3,000 audios" reconcile: 676 json + 4 xj + 5 md = 685.) Deep House has 3 and Lofi 2 more `instrumentAudios` than WAV files: either shared waveformKeys or dangling references; not resolved here. Hero's Journey has **no Beat or Detail programs**: it is entirely Macro/Main plus Loop/Chord-mode instruments, which is why it is the Unreal showcase and the smallest download (98 files).

`.xj` file shape: `{"demo": true, "project": {id, name, updatedAt, platformVersion, isDeleted}}` only (confirmed by `ProjectManagerImpl.java:1420` comment ".xj file is only the Project and whether it's a demo"). Every entity file is a `HubContent`-shaped JSON with a top-level `demo: true` and flat arrays per entity type.

### 5.2 Instrument types, modes and audio metadata

| Project | Instrument types | Modes | Audio tempo (count) | Top `loopBeats` | `tones` style | `lengthSeconds` min/max/mean |
|---|---|---|---|---|---|---|
| Ambient Flow | Sticky 4, Stripe 3, Stab 3, Pad 3 | Event 10, Chord 3 | 30 (707), 15 (195), 60 (24), 8 (1) | 16, 90, 12, 4 | pitched (`A#1`…) | 8.0 / 118.8 / 59.2 s |
| Deep House | Percussion 23, Drum 25, Bass 16, Stripe 4, Stab 6, Pad 6, Sticky 4, Transition 6 | Loop 23, Event 67 | 120 (1,342), 115 (46), 130 (32), 121 (12) | 1, 32, 4, 0 | mostly `X` (unpitched) | 0.02 / 29.7 / 7.0 s |
| Hero's Journey | Transition 12, Percussion 12, Hook 11, Stripe 10, Pad 12, Stab 4, Bass 5 | Event 29, Loop 22, Chord 15 | 135 (36), 95 (35), 115 (27) | 32, 16, 64, 128 | `X` | 3.8 / 80.8 / 22.6 s |
| Lofi | Pad 7, Hook 4, Transition 8, Stripe 4, Bass 1, Drum 9, Percussion 18 | Chord 7, Loop 22, Event 22 | 120 (220), 75 (172), 85 (151) | 16, 8, 1, 4 | chord symbols (`Ab-7`, `Absus2/C`) | 0.09 / 25.6 / 9.4 s |

`InstrumentAudio` fields present in every file: `event, id, instrumentId, intensity, lengthSeconds, loopBeats, name, tempo, tones, transientSeconds, volume, waveformKey`. **No sample rate, bit depth, channel count or content hash is stored anywhere in the JSON**; the only format metadata is the `.wav` extension (3,000 of 3,000 waveformKeys end in `.wav`; 0 contain a `/`). Sample rates of the source WAVs could not be read (blob-less clone); the Build step resamples everything to 48 kHz / 16-bit / stereo WAV by default. `waveformKey` is a bare filename like `Molasses-04-As1-As1.wav` or `Earth-Large-Bass-A1-A1.wav` (pattern `<instrument>-<audio name>-<tones>.wav`, produced by `LocalFileUtils.computeWaveformKey`; see closed #314 "Project cleanup renames all audio waveform according to notes/tones"). Instrument `config` keys in use: `isMultiphonic, isOneShot, isOneShotCutoffEnabled, isTonal, oneShotObserveLengthOfEvents` (all), `isAudioSelectionPersistent, releaseMillis` (Hero's Journey, Lofi), `attackMillis` (4 in Ambient), `oneShotCutoffs` (20 in Deep House).

### 5.3 Template configuration (verbatim blocks)

All four templates set the full key list (`choiceMuteProbability, deltaArcBeatLayersIncoming, deltaArcBeatLayersToPrioritize, deltaArcDetailLayersIncoming, deltaArcEnabled, detailLayerOrder, dubMasterVolume, eventNamesLarge/Medium/Small, instrumentTypesForAudioLengthFinalization, instrumentTypesForInversionSeeking, intensityAutoCrescendoEnabled/Maximum/Minimum, intensityLayers, intensityThreshold, mainProgramLengthMaxDelta, memeTaxonomy, mixerCompressAheadSeconds, mixerCompressDecaySeconds, mixerCompressRatioMax, mixerCompressRatioMin, mixerCompressToAmplitude, mixerDspBufferSize, mixerHighpassThresholdHz, mixerLowpassThresholdHz, mixerNormalizationBoostThreshold, mixerNormalizationCeiling, stickyBunEnabled`). **None sets an output sample rate or encoding**; output format is a workstation setting, not a template property, in 1.9.x.

| Template | `memeTaxonomy` | `intensityLayers` (non-default) | `choiceMuteProbability` (non-zero) | `deltaArcEnabled` | `mainProgramLengthMaxDelta` | `mixerCompressToAmplitude` |
|---|---|---|---|---|---|---|
| Flow | `[{"memes":["WINTER","SPRING","SUMMER","FALL"],"name":"SEASON"}]` | Background 3, Hook 3, Pad 3, Percussion 3, Transition 3, Stab/Sticky/Stripe 2 | none | false | 280 | 0.8 |
| Bump: Deep House | `[{"memes":["EARTH","WIND","FIRE","WATER"],"name":"ELEMENT"}]` | all 1 | Hook/Pad/Stab/Sticky/Stripe 0.1 | **true** | 240 | 0.5 |
| Video Game Demo | `[{"memes":["EXPLORATION","DIALOG","COMBAT"],"name":"ACTION"},{"memes":["CALLTOACTION","EMBARK","ATONEMENT","VICTORY"],"name":"HEROSJOURNEY"}]` | Background/Percussion/Transition 3, others 2, Drum 1 | none | false | 280 | 1.0 |
| Slaps: Lofi | `[{"memes":["ABERDEEN","LUXOR","NAGOYA","BOULDER"],"name":"CITY"}]` | Background 3, Transition 3, rest 1 | Bass .1, Drum .2, Hook .2, Pad .3, Percussion .2, Stab .3, Sticky .3, Stripe .4 | false | 280 | 1.0 |

`intensityThreshold` is 0.5 for every type in every template. `eventNamesLarge/Medium/Small` differ: Flow and Bump use `["LARGE","BIG"] / ["MEDIUM","REGULAR"] / ["SMALL","LITTLE"]`; VGM and Slaps add `"HIGH","PRIMARY"` / `"MIDDLE","SECONDARY"` / `"LOW"`. `detailLayerOrder` is `["Bass","Stripe","Pad","Sticky","Stab"]` except VGM `["Pad","Stripe","Bass","Sticky","Stab"]`.

### 5.4 Memes actually used (distinct names)

| Project | programMemes | instrumentMemes | programSequenceBindingMemes |
|---|---|---|---|
| Ambient Flow | AMIN7, CMAJ, LOOSE, SAVORY, SOLID!, SOUR, SPARSE, SPICY, SWEET (9) | $BUSY, $SPICY, LOOSE, ORANGES, SAVORY, SOUR, SPARSE, SPICY, SWEET (9) | LEMONS, ORANGES, PEACHES, PLUMS, SAVORY, SOUR, SPICY, SWEET (8) |
| Deep House | !ROOTSY, !ROOTSY2, !STRAIGHT, !WUBBY, $BUSY, 3NOTE, 4NOTE, 95BPM, CALLTOACTION, EARTH/Earth, EXPLORATION, FIRE/Fire, FMINHARD, LARGE, LONGBASS, NEW, OPEN, ROOTSY, ROOTSY2, SMALL, STRAIGHT, WATER/Water, WIDEOPEN, WIND/Wind (28) | $PELICANS, $TOMFILL, BIG, EARTH, FIRE, LARGE, LONGBASS, SMALL, WATER, WIND, WUBBY (11) | BREAK/Break, EARTH/Earth, Fire, Large, WIND/Water/Wind (9) |
| Hero's Journey | 115BPM, 135BPM, 95BPM, ATONEMENT, CALLTOACTION, COMBAT, DIALOG, EMBARK, EXPLORATION, VICTORY (10) | same 10 | same minus EMBARK, VICTORY (8) |
| Lofi | !CUSTOM, !STRAIGHT, !WIDEOPEN, 75BPM, 85BPM, ABERDEEN, BEEF, BOULDER, CHICKEN, CUSTOM, LUXOR, NAGOYA, OPEN, PORK, STRAIGHT, WIDEOPEN (16) | 75BPM, 85BPM, ABERDEEN, BOULDER, CHICKEN, LUXOR, NAGOYA (7) | 75BPM, 85BPM (2) |

Three facts the lint spec needs: (a) Ambient Flow's SEASON palette appears on **zero** content (drift, as 11 Q4 notes); (b) Deep House mixes **case variants** (`EARTH`/`Earth`, `BREAK`/`Break`, `WIND`/`Wind`), so meme comparison semantics (the engine upper-cases? not verified here) matter to any hash or lint; (c) Deep House carries Hero's Journey vocabulary (`CALLTOACTION`, `EXPLORATION`, `95BPM`) on programs, evidence of cross-project copy-paste that a cross-pack "same word, different meaning" lint would flag.

### 5.5 How the workstation actually gets the demos (correction to the proposal)

02-where-v1-stands.md says demo content is "Cloned by the workstation on request" from the git repository, and 06 says V2 replaces "Cloning the demos repository from inside the workstation." **The workstation does not clone git.** The code path is:

1. `ProjectCreationModalController` toggle buttons carry `id="vgm"`, `id="bump_deep"` (and by the same pattern `slaps_lofi`, `space_flow`; the first two verified at `project-creation-modal.fxml` lines 90 and 105); `getId()` is passed as `templateKey` (line 192).
2. `ProjectManagerImpl.createProjectFromDemoTemplate` (lines 155-245) sets `demoBaseUrl` (default `"https://audio.xj.io/"`, line 102, also `application.properties:8 demo.baseUrl`) and calls `HubClientFactoryImpl.loadApiV1` (line 52): `GET {baseUrl}{templateKey}.json` → deserialized as `HubContent`, `content.setDemo(true)`.
3. For each audio: `GET {baseUrl}{waveformKey}` with a HEAD size check, retry `demo.audioDownloadRetries=3`, then the waveformKey is **rewritten** to the local `computeWaveformKey` form and saved.

So the live demo source of truth is the **`audio.xj.io` CDN** (a hub-era export: one flat JSON per template keyed by shipKey plus flat audio files), and the GitHub `xjmusic-demos` repo is a parallel, LFS-backed copy with `platformVersion 1.9.2` whose WAV filenames already carry the rewritten keys. Nothing in the repo or workstation ties the two together or verifies they match. The Hero's Journey `.xj` and README exist in both; the Unreal showcase was imported from a workstation Build of the CDN copy (its `.uasset`s are UUID-named, which is the Build output format).

---

## 6. The docs site (`xjmusic/xjmusic-docs`)

Hugo site, theme `hugo-theme-relearn` (vendored under `themes/`), `baseURL = "https://docs.xjmusic.com/"`, Google Analytics `G-GW9VCFXV42`, cookie-consent JS in `static/js/`. Deploys on push to `main` via `.github/workflows/deploy.yaml`: `hugo --minify` → `aws s3 cp --recursive ./public/ s3://docs.xjmusic.com/` → CloudFront invalidation. `pr.yaml` builds only. Last commit 2024-06-06 ("Add Embedded YouTube Video to Demo Project Tutorial (#14)"). README is three lines.

Content tree (section → pages):

| Section (`content/`) | Weight | Pages |
|---|---|---|
| `what-is-xj-music/` | | 1 |
| `how-it-works/` | 2 | 1 (System / Workstation / Engine; engine described as "a shared library ... in any language that can interface with C++") |
| `installation/` | | linux, mac-intel, mac-silicon, windows |
| `getting-started/` | | content-browser, fabrication-tab, instrument-editor, menu-bar (names the demos "Bump, Slaps, Space, and The Hero's Journey"; embeds YouTube `z5i8ZD8AyWE`), program-editor |
| `making-xj-music/` | 5 | chords; fabrication (delta-arcs, fabrication-settings); instruments (craft-phases×types×modes matrix PNG, behavior, configuration, menu, modes, properties, types); libraries; memes ("pick a marble from the bag"); programs (configurations, events, types); templates (binding, configuration, ship-keys, dubbing-of-output-audio) |
| `glossary/` | | 1 |
| `references/` | 8 | 1 (the patent citation only) |
| `authors/`, `tags/` | | |

**Absent from the docs site**: any engine integration guide (the `how-it-works` page links the source folder), any Unreal plugin page (the plugin's `.uplugin` `DocsURL` points at the site root), any page on the Build step or `build/` folder layout, any content-format or JSON schema reference, any licensing page beyond the home-page paragraph. The C++ class reference lives separately at engine-docs.xjmusic.com (Doxygen, deployed by `engine_ci.yaml` to GitHub Pages).

---

## 7. Implications for V2 specs

Concrete pitfalls, seams and facts, grouped by the spec that needs them.

**Labels, milestones, project (tracker design spec)**

1. Only 12 labels exist and none is `v2`, a phase, or an area. A V2 tracker spec must create at minimum: `v2`; phase labels (or use milestones for phases, which 09 prescribes: "each phase becomes a GitHub milestone"); area labels distinguishing `engine`, `workstation-rust`, `workstation-java`, `unreal`, `commons`, `session`, `docs`, `ci`. Keep `enhance` (house spelling) rather than introducing `enhancement`.
2. Milestone naming is already inconsistent (`v1.9` vs `1.10`). Pick one convention for V2 (`v2.0 Phase 0 · Spikes`, …) and decide what to do with `1.10` (41 open, 0 closed, untouched since 2024-09-10): either retitle it as the Java-workstation maintenance bucket or close it and re-milestone the 12 "stays V1 → Phase 6" items.
3. `read:project` scope is missing from the current `gh` token; creating or populating the "v2.0 Collaborative Musical Instrument" project requires `gh auth refresh -s read:project,project`. GitHub Projects v2 "blocked by" relationships are issue-level (`gh issue edit --add-blocked-by` is not a thing; use the Projects v2 `blocked_by`/dependency feature via GraphQL `addSubIssue`/issue dependencies, or the "Blocked by" tracked-by syntax in bodies). The spec should name the mechanism explicitly.
4. Issue #400 (integer ids) **contradicts** 06-commons.md ("Entity ids remain UUIDs") and must be closed with rationale, not left open alongside V2 issues. Issue #437 (C++ standards) and 11 Q8 ("C++ API must remain a first-class, documented surface") should be linked. #310/#440 (JavaCPP) are superseded and should close referencing the C ABI issue.
5. Zero existing issues cover MIDI/OSC, Link, sessions, packs, licensing, determinism, or the C ABI. Every Phase 2/4/5 issue is new; there is nothing to cross-link except the proposal documents and (for #331 → `xj-dsp`, #311/#423 → `symphonia`, #306/#322/#324 → lint, #309 → first-run, #305 → transport) the disposition table above.

**Release and versioning spec**

6. `engine_distro.yml` lines 70-77 hard-fail any tag where `workstation/gradle.properties` version ≠ `engine/CMakeLists.txt` VERSION, and both must equal the tag. 09's plan (engine 2.0.0, Java workstation 1.9.x, Rust workstation 2.0/2.1/2.2, C ABI 1.0.0) is impossible under the current workflow. The spec must define per-component tags (e.g. `engine-v2.0.0`, `workstation-v2.0.0`) and rewrite both distro workflows' `verify_version_number` jobs.
7. The Unreal plugin already uses `<plugin>-<engine>` tags (`1.0.2-1.9.6`). The spec should either adopt that pattern org-wide or define the mapping.
8. `nothing_left_todo.sh` accepts only `workstation` or `engine`; new Rust crates need their own gate (or `cargo clippy -D warnings`) and the script extended.
9. Windows `.lib` upload copies `src/Debug/xjmusic.lib` after a Release reconfigure (line 157); a C ABI spec that ships prebuilt static libraries for `build.rs` must fix the generator config (`--config Release`) and should publish checksums.

**Unreal continuity spec**

10. The plugin links a **Windows-only** path (`build/src/Release/xjmusic.lib`, Build.cs:39) and clones the entire monorepo at a pinned tag. 09 principle 2 ("the Unreal plugin builds against every engine tag") and 11 Q8 ("stays on the C++ API") imply the continuity spec must: bump `TAG` in `Setup.sh`/`Setup.bat`; keep every C++ symbol in the table in §4.2 source-compatible (`Engine` ctor with 6 args incl. `std::optional<int>` seconds, `RunCycle`, `AudioScheduleEvent` accessors, `SegmentStore::read*`, `MemeTaxonomy::getCategories`, `WorkSettings` fields, `Program::config.barBeats`); and either fix Build.cs for `libxjmusic.a` on macOS/Linux or declare the plugin Windows-only.
11. The plugin resolves audio by `AssetName + ".wav"` and hard-codes 48000 Hz / 2 channels. Any change to Build-for-engine output (OGG per #423, hashes per 06, sample rate) breaks it unless the plugin's `XjAudioLoader`/`XjMixer` change in step. The spec should pin "Build for engine" output to WAV 48k/16/2 named `<InstrumentAudio.id>.wav` plus `<name>.json` as the compatibility contract, and add OGG as an opt-in.
12. The plugin runs craft-ahead 60 s / dub-ahead 30 s (not 20/10) and always starts the first template; `Engine::start` begins work immediately (UnrealPlugin#3 wants start/stop). V2 transport/cue semantics (04) must leave this blocking behavior available for game hosts.
13. There is no engine-integration documentation on docs.xjmusic.com at all; the plugin's `DocsURL` points at the site root. A V2 docs spec should add an "Integrate" section (engine C++ API, C ABI, Unreal, Build contract).

**Commons seeding spec**

14. **The demo audio has no separate content license.** `xjmusic-demos/LICENSE.txt` is the software license plus EULA; it does not address redistributing the sample audio itself. Sample provenance must be inventoried and confirmed before any demo is relicensed as a Commons pack (tracked by the Phase 0 rights inventory and the Phase 4 rights-clearance decision; the per-file inventory is a private company record).
15. Two sources of truth exist for the demos: `audio.xj.io/{shipKey}.json` + flat audio (what the workstation fetches today, keys `vgm`, `bump_deep`, `slaps_lofi`, `space_flow`) and the LFS git repo (`platformVersion 1.9.2`). The seeding spec should name the git repo as canonical, run `xj-content pack` against it, and schedule the retirement of `demo.baseUrl` / `HubClientFactoryImpl.loadApiV1` (which also removes the last live use of the hub client, gap G11).
16. All 3,000 audios are WAV via LFS, 2,995 files; Deep House and Lofi have 3 and 2 more JSON audios than files. Pack lint must report dangling `waveformKey`s rather than fail silently; the Build step today merely logs "File not found" and continues (`ProjectManagerImpl.java:432`).
17. No sample rate, bit depth, channel count or hash exists in the JSON; `lengthSeconds`/`transientSeconds` are the only audio facts. `contentHash` (06) and any format fields are additive; `xj-content pack` must read the WAV headers to populate them.
18. Palette drift is real: Flow's SEASON has zero content usage; Deep House mixes `EARTH`/`Earth`; Deep House reuses Hero's Journey vocabulary. The three lint rules in 06 ("declared category unused", "palette-like meme undeclared", "same word two meanings") each have a live positive case in the demos, which makes the demos good lint fixtures and means **at least one demo fails lint as-is** (Ambient Flow). The spec should say which demos are expected to need edits before they can be the first packs.
19. Hero's Journey is the natural **starter pack / Unreal fixture** candidate: 98 audios, Macro/Main only, two palette rows, already the Unreal showcase. Deep House is the stress fixture (281 programs, 1,432 audios, `deltaArcEnabled=true`, the only template with delta arcs on).
20. Template configs carry mixer parameters (`mixerCompress*`, `mixerHighpass/LowpassThresholdHz`, `mixerNormalization*`, `dubMasterVolume`, `mixerDspBufferSize`) that belong to the Java `MixerImpl` and are parsed by the engine's `TemplateConfig.cpp` for parity. `xj-dsp` must decide whether these remain template properties (so packs can ship them) or move to a show/session setting; today they vary per demo (`mixerCompressToAmplitude` 0.5–1.0).

**Workstation / first-run spec**

21. #309's checklist is the most detailed existing statement of first-run UX (welcome modal, "don't show again", demo clone → tour, "Tip N of 9", skip) and should be cited by the Phase 3 first-run tour issue, replacing "clone a demo" with "starter pack already bundled".
22. The bug form asks for "Git commit hash" and the About modal; the Rust workstation needs an About/version surface and the form's Godot GPU boilerplate should be replaced (a `docs`/`ci` chore issue).

**Process**

23. CONTRIBUTING.md's commit style (imperative title < 72 chars, `Fixes #N` in body, optional `Area:` prefix) and the equity rule (1 hour = 1 point, hours logged per PR) are the only written process; 11 Q5 extends the same gate to content PRs in a future `xjmusic/commons` repo. `CONFIDENTIALITY.md` at the root of a public repo (and of the demos repo) should be removed or rewritten as part of the Commons licensing clean-up; it is confusing next to a redistribution license.
24. Discussions are enabled but unused (1 welcome post); the wiki is off. If the V2 plan wants an RFC venue other than PR review of `design/v2-proposal/`, Discussions is available and empty.
