# External stack research: crates, SDKs, licenses, and the GitHub API

**Scope.** Every third-party crate, SDK and service the V2 proposal names (`design/v2-proposal/05-workstation.md` lines 76–92 stack table; `03-architecture.md` lines 24–25, 54, 62, 70, 101–102; `06-commons.md` lines 27, 60, 103; `07-session-protocol.md` lines 37–56; `08-performer-input.md` lines 64–67; `11-resolutions.md` Q1, Q2, Q3, Q10, Q11), plus the GitHub REST/GraphQL surface the roadmap-to-issues step will use. Checked against crates.io, GitHub, docs.rs, docs.github.com and vendor pages on **2026-10-25**. Where a fact could not be verified it is marked as such.

**Baseline.** Rust stable is **1.99.1** (2026-10-16; 1.99.0 on 2026-10-01) ([rust-lang/rust releases](https://api.github.com/repos/rust-lang/rust/releases)). The highest MSRV in the stack is **1.93** (iced master) followed by 1.91 (cargo-nextest, build only), 1.90 (tauri, tauri-plugin-updater), 1.88 (sentry, iced 0.14), 1.87 (rubato, wgpu), 1.86 (triple_buffer), 1.85 (cpal, symphonia, tokio, toml, ed25519-dalek). A workspace MSRV of 1.90 is safe for the Tauri path; the iced path needs 1.93 if it tracks master.

---

## 1. Audio I/O and DSP crates

| Crate | Version (date) | License | Maintenance | Spec-relevant gotcha |
|---|---|---|---|---|
| `cpal` | 0.18.2 (2026-08-16) | Apache-2.0 | Active; 0.17.0 Dec 2025, 0.18.0 Jun 2026, 0.18.2 Aug 2026 | **No WASAPI exclusive mode.** Streams no longer auto-start in 0.18 (`play()` required). Hot-plug notification reverted in 0.18.2. |
| `asio-sys` | 0.4.0 (2026-08-16) | Apache-2.0 | Released with cpal | Downloads Steinberg SDK at build time unless `CPAL_ASIO_DIR` set; needs LLVM (`LIBCLANG_PATH`). |
| `symphonia` | 0.6.1 (2026-08-13) | MPL-2.0 | Active; 0.6.0 (2026-05-15) was a breaking rewrite | **MP3, AAC, MP4/M4A, AIFF, ALAC are not default features.** MSRV 1.85, edition 2024. |
| `rubato` | 5.0.1 (2026-10-01) | MIT OR Apache-2.0 | Very active: 2.0 Apr, 3.0 May, 4.0 Jul, 5.0 Aug 2026 | Four major versions in six months; API tied to `audioadapter` majors. MSRV 1.87. |
| `rtrb` | 0.4.0 (2026-08-17); 0.3.5 (2026-08-18) backport | MIT OR Apache-2.0 | Active | Versions < 0.3.5/0.4.0 have a double-free in `ReadChunk::commit()`; pin ≥ 0.4.0. |
| `arc-swap` | 1.9.3 (2026-10-18) | MIT OR Apache-2.0 | Active | 1.9 added stronger memory orderings to fix bugs; docs say this "probably introduces some performance degradation". |
| `triple_buffer` | 9.0.0 (2026-02-22) | **MPL-2.0** | Steady, ~1 release/yr | MSRV 1.86; accessor renames in 8.1/9.0. |

Sources: [crates.io cpal](https://crates.io/api/v1/crates/cpal), [cpal CHANGELOG](https://github.com/RustAudio/cpal/blob/master/CHANGELOG.md), [cpal README](https://github.com/RustAudio/cpal/blob/master/README.md), [asio-sys](https://docs.rs/crate/asio-sys/latest), [crates.io symphonia 0.6.1](https://crates.io/api/v1/crates/symphonia/0.6.1), [Symphonia releases](https://api.github.com/repos/pdeljanov/Symphonia/releases), [rubato on docs.rs](https://docs.rs/crate/rubato/latest), [rtrb releases](https://api.github.com/repos/mgeier/rtrb/releases), [arc-swap docs](https://docs.rs/arc-swap/latest/arc_swap/), [triple-buffer CHANGELOG](https://raw.githubusercontent.com/HadrienG2/triple-buffer/master/CHANGELOG.md).

### 1.1 cpal in detail

- **Hosts.** Windows: WASAPI default, `asio` and `jack` optional. Linux/BSD: ALSA default, `jack`, `pipewire`, `pulse` optional; **0.18.0 added native PipeWire and PulseAudio hosts** and the default host priority is PipeWire → PulseAudio → ALSA. macOS: CoreAudio, `jack` optional ([README](https://github.com/RustAudio/cpal/blob/master/README.md)). The proposal's Linux row in `11-resolutions.md` Q10 ("ALSA direct, JACK, and PipeWire through its JACK or ALSA shims") predates the native PipeWire host.
- **0.18.0 breaking changes** (2026-06-06): unified `Error`/`ErrorKind`; streams no longer start on creation; `build_*_stream()` takes `StreamConfig` by value; `DeviceId` fields private; `StreamInstant` mirrors `std::time::Instant`; Emscripten host removed; **default config prefers 48 kHz, then 44.1 kHz**; new `ErrorKind::{DeviceChanged, DeviceBusy, RealtimeDenied}`; `realtime` feature promotes the callback thread (Linux needs `CAP_SYS_NICE`, root, an `rtprio` limit, or the `realtime-dbus` feature); timestamps account for hardware latency on CoreAudio/ASIO/WASAPI/JACK ([CHANGELOG](https://github.com/RustAudio/cpal/blob/master/CHANGELOG.md)).
- **0.18.2** (2026-08-16): `ErrorKind::Xrun` on AAudio/CoreAudio/PipeWire/WASAPI capture; **the WASAPI `DeviceChanged`-on-default-change behaviour was reverted**; timestamps monotonic across device changes; JACK `DeviceId` no longer embeds the PID.
- **WASAPI exclusive mode is not available.** PR [#1368 "feat(wasapi): add opt-in exclusive output support"](https://api.github.com/search/issues?q=repo:RustAudio/cpal+exclusive+mode+wasapi) (opened 2026-09-13) is open and labelled "postponed"; issue [#1220 "v0.19 design goals"](https://github.com/RustAudio/cpal/issues/1220) lists "Exclusive mode on CoreAudio, PipeWire & WASAPI" under a future extension-traits mechanism with "Target date: when it's done". `11-resolutions.md` Q10 promises "WASAPI exclusive as an option"; that promise cannot be kept with cpal 0.18.
- **Known limitations from the README:** with PipeWire/PulseAudio running, ALSA `default` is held exclusively (opens fail with `DeviceBusy`); `BufferSize::Default` on ALSA can be very large; PipeWire compatibility layers can glitch on sample-rate mismatch; duplex needs one device with both directions.
- **ASIO build chain:** `asio-sys` 0.4.0 depends on `bindgen ^0.72` and `cc ^1.2`; the build script checks `CPAL_ASIO_DIR`, then a cached `asio_sdk` in temp, then downloads from Steinberg; expects Visual Studio (`vcvarsall.bat`); MinGW cross-compile possible. The crate note says users "must comply with Steinberg's licensing terms" ([asio-sys](https://docs.rs/crate/asio-sys/latest)). An official build that enables `asio` therefore vendors or downloads the SDK on CI, which is itself a licensing act (section 7).

### 1.2 symphonia in detail

- Default features of 0.6.1: `opt-simd, all-meta, adpcm, flac, mkv, ogg, pcm, vorbis, wav` ([crates.io 0.6.1](https://crates.io/api/v1/crates/symphonia/0.6.1)). The proposal's "WAV, FLAC, MP3, OGG, AAC" list needs `mp3` (or `mpa`), `aac`, and `isomp4` enabled explicitly; `aiff`, `alac`, `caf` likewise.
- The rel-0.5 README rated WAV/FLAC/MP3/PCM/Vorbis "Excellent", AAC-LC/ALAC/OGG/MP4 "Great", **HE-AAC, Opus and WavPack not implemented**, and gapless only where both demuxer and decoder support it (no gapless for MP4 or AAC) ([README](https://github.com/pdeljanov/Symphonia/blob/master/README.md)). Nothing in the 0.6 notes claims Opus or HE-AAC were added.
- 0.6.0 (2026-05-15) redesigned audio buffer, metadata and timing types with a migration guide; 0.6.1 added cloneable/sliceable buffers and Opus channel info for ISO BMFF ([releases](https://api.github.com/repos/pdeljanov/Symphonia/releases)). MSRV policy: raising MSRV is a breaking change.
- MPL-2.0 is file-level copyleft: a proprietary binary may link it unmodified; modifications to Symphonia's own files must be published. `triple_buffer` and Bungee are also MPL-2.0.

### 1.3 rubato in detail

Types in 5.x: `Fft` (synchronous, fixed ratio, `fft_resampler` feature), `Async::new_sinc` / `Async::new_poly` (variable ratio), `Slip` (ppm-level drift correction). `Resampler::process_into_buffer()` is documented allocation-free; `process_all()` allocates; the `log` feature must stay off in real-time code. 4.0.0 changed `process` to `Option<&Indexing>`, moved ratio changes to `Adjustable`/`Resizable`, made error enums `#[non_exhaustive]`; 5.0.0 bumped `audioadapter` to 5.0 ([docs.rs](https://docs.rs/crate/rubato/latest)). A spec that pins "rubato" should pin `5.x` and `audioadapter 5.x` together.

### 1.4 Lock-free primitives

- `rtrb` 0.4.0's one breaking change restores cross-thread synchronisation of `is_abandoned()`; 0.3.5 was published a day later as a backport of the `commit()` panic-safety fix ([releases](https://api.github.com/repos/mgeier/rtrb/releases)).
- `arc-swap` is "optimized for read-mostly scenarios"; the `Cache` helper trades delayed reclamation for faster reads. The docs do not call `load()` wait-free; a spec should say "lock-free snapshot read" and test the audio-thread path ([docs](https://docs.rs/arc-swap/latest/arc_swap/)).
- `triple_buffer` 9.0.0: `Input::input_buffer_publisher()` RAII API since 8.1.0; `input_buffer()`/`output_buffer()` are read-only since 9.0.0; MSRV 1.86 ([CHANGELOG](https://raw.githubusercontent.com/HadrienG2/triple-buffer/master/CHANGELOG.md)).

---

## 2. Time stretch and pitch shift

### 2.1 Signalsmith Stretch (upstream)

- **No tagged releases** ("There aren't any releases here") ([releases page](https://github.com/Signalsmith-Audio/signalsmith-stretch/releases)). The version lives in the header: `static constexpr size_t version[3] = {1, 3, 2};` → **1.3.2** ([signalsmith-stretch.h](https://raw.githubusercontent.com/Signalsmith-Audio/signalsmith-stretch/main/signalsmith-stretch.h)).
- Last commits: 2026-10-22 "Bump Linear version", 2026-09-25 "Bump Linear version (0.6.4 for bugfixes)", 2026-01-24 ([commits](https://api.github.com/repos/Signalsmith-Audio/signalsmith-stretch/commits)).
- **MIT**, "Copyright (c) 2022 Geraint Luff / Signalsmith Audio Ltd." ([LICENSE.txt](https://raw.githubusercontent.com/Signalsmith-Audio/signalsmith-stretch/main/LICENSE.txt)). C++11. Depends on Signalsmith Linear for FFTs; PFFFT, Accelerate and IPP are optional faster backends. Time stretch "sounds best for modest changes (roughly 0.75x to 1.5x)"; multi-octave pitch shift; `setFormantFactor()`; latency via `inputLatency()`/`outputLatency()`; WASM/AudioWorklet build on NPM; Python binding on PyPI ([README](https://github.com/Signalsmith-Audio/signalsmith-stretch)).
- API (header): `configure(nChannels, blockSamples, intervalSamples, splitComputation)`, `presetDefault(nChannels, sampleRate)` (block 0.12 s, interval 0.03 s), `setTransposeFactor(multiplier, tonalityLimit)`, `process(inputs, inputSamples, outputs, outputSamples)`, `seek`, `flush`. `process()` is not strictly allocation-free: `tmpProcessBuffer.resize()` and `peaks.emplace_back()` can reallocate past the capacity reserved in `configure()`. Consistent with the proposal's decision to render ahead of time, never on the audio thread.

### 2.2 Rust bindings

| Crate | Version (date) | Binding | Pinned upstream | Notes |
|---|---|---|---|---|
| `signalsmith-stretch` | 0.1.3 (2025-09-18) | `bindgen 0.70` + `cc`, C++14, `src/wrapper.cpp` | Submodule at `c598726` = upstream commit of **2025-06-14** | 16 months behind upstream (pre-1.3.2 Linear bumps). README is four lines. 139k downloads. |
| `ssstretch` | 0.1.0 (2026-10-07) | `cxx` / `cxx-build` | not stated | Brand new, single `Stretch` struct documented, maintainer bmisiak. |

Sources: [crates.io signalsmith-stretch](https://crates.io/api/v1/crates/signalsmith-stretch), [Cargo.toml](https://raw.githubusercontent.com/colinmarc/signalsmith-stretch-rs/main/Cargo.toml), [build.rs](https://raw.githubusercontent.com/colinmarc/signalsmith-stretch-rs/main/build.rs), [submodule entry](https://api.github.com/repos/colinmarc/signalsmith-stretch-rs/contents/signalsmith-stretch), [upstream commit c598726](https://api.github.com/repos/Signalsmith-Audio/signalsmith-stretch/commits/c5987267e6ad42ff9b620b2802c4600472e85818), [ssstretch](https://docs.rs/ssstretch/latest/ssstretch/).

### 2.3 Bungee

- **MPL-2.0**, "Modern C++", Eigen + PFFFT, CMake; every commit to `main` is auto-tagged (latest tags v2.4.35, v2.4.30, v2.4.24; release page dates "08 Oct", "29 Aug", "03 May" without years). API: `Bungee::Stretcher<Basic>`, `Request{pitch, speed, position}`, `specifyGrain/analyseGrain/synthesiseGrain`, `Bungee::Stream` FIFO wrapper. Forward-only playback in both modes. **Bungee Pro** is a commercial variant with SIMD and Web Audio builds ([README](https://github.com/bungee-audio-stretch/bungee), [tags](https://api.github.com/repos/bungee-audio-stretch/bungee/tags)).
- **Rust binding is dead.** `bungee-rs` 0.1.1 (2025-09-28) is the only non-yanked version; 0.2.0 (2026-02-21) is yanked; the repo is marked Deprecated and was **archived 2026-03-03**, citing lack of MSVC support upstream, and points users to `signalsmith-stretch-rs` ([crates.io](https://crates.io/api/v1/crates/bungee-rs), [repo](https://github.com/emuell/bungee-rs)). A Phase 0 bake-off against Bungee means writing a binding or running the C++ CLI offline.

---

## 3. Performer input

| Crate | Version (date) | License | Gotcha |
|---|---|---|---|
| `midir` | 0.11.1 (2026-10-10) | MIT | **No hot-plug or disconnect callback on any backend.** |
| `rosc` | 0.11.4 (2025-03-23) | MIT/Apache-2.0 | 19 months since last release; encoder/decoder only, no transport. |
| `gilrs` | 0.11.2 (2026-05-30) | Apache-2.0/MIT | Hot-plug supported; WGI on Windows needs a focused window; evdev needs udev permissions. |

- **midir**: backends ALSA, WinMM, CoreMIDI, WinRT (`winrt` feature), JACK (`jack` feature), Web MIDI, Android (0.11.0). Virtual ports "except on Windows"; full SysEx. 0.11.0 (2026-04-18) broke `find_port_by_id(&str)`; 0.11.1 fixed WinMM `port_name()` on disconnected ports and a CoreMIDI truncated-packet read ([CHANGELOG](https://raw.githubusercontent.com/Boddlnagg/midir/master/CHANGELOG.md), [README](https://github.com/Boddlnagg/midir/blob/master/README.md)). `MidiOutput`/`MidiOutputPort` exist (the `id()`/`find_port_by_id()` additions in 0.10.1 are on both port types), so MIDI clock out is feasible. **Hot-plug**: issue [#201 "midir has no portable hotplug/disconnect callback"](https://api.github.com/search/issues?q=repo:Boddlnagg/midir+hotplug) (2026-08-26, open), #86 "Best practice to allow hot-plugging?" (2021, open: callers poll `ports()`), #78 (macOS: devices plugged after startup did not appear even in a new `MidiInput`), #191 (ALSA disconnect is silent). `05-workstation.md` line 86 and `08-performer-input.md` line 64 say "hot-plug"; the spec must own a polling/rescan strategy.
- **gilrs**: SDL_GameControllerDB mappings plus `SDL_GAMECONTROLLERCONFIG`; `Gilrs::next_event()` polling; rumble on Linux/Windows only ([docs.rs](https://docs.rs/gilrs/latest/gilrs/)).

---

## 4. Clock: Ableton Link and rusty_link

### 4.1 Ableton Link upstream

| Release | Date | Notes |
|---|---|---|
| Link 3.1.5 | 2025-12-03 | ESP32 performance, logging |
| Link 4.0.0 beta 1 | 2026-02-10 | **Requires C++17**; introduces **Link Audio** (`LinkAudio.hpp`, LinkAudioHut) |
| Link 4.0.0 beta 2 | 2026-03-06 | Discovery across subnets |
| Link 4.0.0 beta 3 | 2026-04-01 | Link Audio added to the **abl_link C API** |
| Link 4.0 | 2026-05-04 | "Add support for Link Audio" |
| Link 4.1 | 2026-09-23 | asio standalone 1.38.2; **minimum CMake 3.18** |
| Link 4.1.1 | 2026-10-19 | Buffer resend fix |

Source: [Ableton/link releases](https://api.github.com/repos/Ableton/link/releases), [tags](https://api.github.com/repos/Ableton/link/tags).

License text ([LICENSE.md](https://raw.githubusercontent.com/Ableton/link/master/LICENSE.md)): "Copyright 2016, Ableton AG, Berlin. All rights reserved. This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any later version." followed by the standard warranty disclaimer and the note that proprietary incorporation requires contacting link-devs@ableton.com. README: "Ableton Link is dual licensed under GPLv2+ and a proprietary license." Build requirements table: MSVC 17 2022 (ASIO SDK 2.3 optional for examples), Xcode 16.2, Clang 13 / GCC 10; non-CMake builds define `LINK_PLATFORM_{MACOSX,LINUX,WINDOWS}=1` and on Windows `INCL_EXTRA_HTON_FUNCTIONS=1` and `_WIN32_WINNT`; Link-enabled apps should comply with `TEST-PLAN.md` ([README](https://github.com/Ableton/link/blob/master/README.md)). No fee information is published anywhere official; the proposal's Q3 assessment stands.

### 4.2 rusty_link

- 0.4.9 (2026-04-02), **GPL-2.0-or-later**, "Rust bindings for Ableton Link through the official C Wrapper (abl_link)"; author describes it as a hobby project ([crates.io](https://crates.io/api/v1/crates/rusty_link), [docs.rs](https://docs.rs/crate/rusty_link/latest)).
- Link is a git submodule pinned to commit `0fc58dc` (2025-12-15, "Use int64_t consistently for time"), i.e. **post-3.1.5 master, pre-4.0**; CHANGELOG 0.4.8 says "moved to Ableton Link 3.1.5". Last repo commit 2026-01-07 ([contents](https://api.github.com/repos/anzbert/rusty_link/contents/), [commit](https://api.github.com/repos/Ableton/link/commits/0fc58dc11977a1531990ea2cdbcac033be8433c5), [CHANGELOG](https://github.com/anzbert/rusty_link/blob/master/CHANGELOG.md), [commits](https://api.github.com/repos/anzbert/rusty_link/commits)). **rusty_link does not expose Link 4 / Link Audio**, and nothing suggests it will soon.
- Build: CMake ≥ 3.14 and bindgen (libclang) at build time; thread-safe except session-state capture/commit; `HostTimeFilter` is a Rust port for the audio callback; tested on macOS M1, Windows 11, Ubuntu 22 on Pi 4 (the sound example has "cpal/ALSA issues?!") ([README](https://github.com/anzbert/rusty_link)).

---

## 5. Networking, serialization, build

| Crate | Version (date) | License | MSRV | Gotcha |
|---|---|---|---|---|
| `quinn` | 0.11.12 (2026-09-14) | MIT OR Apache-2.0 | 1.80 (README) | Needs rustls + `ring` or `aws-lc-rs`; LAN peers need self-signed certs (`rcgen`) and custom verification / trust-on-first-use; `runtime-tokio` default, `runtime-smol`/`async-std` features. |
| `mdns-sd` | 0.21.6 (2026-10-19) | Apache-2.0 OR MIT | 1.71 | Own daemon thread + `flume` channels; no unicast responses (RFC 6762 §5.4); self-described beta; default packet 1452 bytes; tested against Avahi, dns-sd, Bonjour. |
| `tokio` | 1.54.1 (2026-10-21) | MIT | 1.85 | 1.54.0 yanked (2026-10-19). |
| `serde` | 1.0.229 (2026-07-18) | MIT OR Apache-2.0 | 1.56 | `serde_core` split; `serde_json` 1.0.154 (2026-10-11) features `preserve_order`, `float_roundtrip`, `arbitrary_precision`, `raw_value`. |
| `toml` | 1.1.8+spec-1.1.0 (2026-10-09) | MIT OR Apache-2.0 | 1.85 | 1.0.0 on 2026-02-11; TOML spec 1.1 (since 0.9.10); `Time::second`/`nanosecond` became `Option`. |
| `bindgen` | 0.73.2 (2026-09-08) | BSD-3-Clause | 1.71 | Requires libclang on every build machine (`LIBCLANG_PATH`). |
| `cmake` | 0.1.58 (2026-03-26) | MIT OR Apache-2.0 | — | Stable 0.1.x line. |
| `cc` | 1.10.0 (2026-10-23) | MIT OR Apache-2.0 | 1.65 | Five minor releases in October 2026 (1.6.0 → 1.10.0); `parallel`, `jobserver`, `compile-commands` features. |

Sources: [quinn](https://crates.io/api/v1/crates/quinn), [quinn README](https://github.com/quinn-rs/quinn/blob/main/README.md), [mdns-sd](https://crates.io/api/v1/crates/mdns-sd), [mdns-sd README](https://github.com/keepsimple1/mdns-sd/blob/main/README.md), [tokio](https://crates.io/api/v1/crates/tokio), [serde](https://crates.io/api/v1/crates/serde), [serde_json](https://crates.io/api/v1/crates/serde_json), [toml](https://crates.io/api/v1/crates/toml), [toml CHANGELOG](https://raw.githubusercontent.com/toml-rs/toml/main/crates/toml/CHANGELOG.md), [bindgen](https://crates.io/api/v1/crates/bindgen), [cmake](https://crates.io/api/v1/crates/cmake), [cc](https://crates.io/api/v1/crates/cc).

---

## 6. Content integrity and signing

| Crate | Version (date) | License | Notes |
|---|---|---|---|
| `blake3` | 1.8.7 (2026-08-20) | CC0-1.0 OR Apache-2.0 OR Apache-2.0 WITH LLVM-exception | Features `mmap`, `rayon`, `zeroize`, `pure`, `digest`; no MSRV declared. |
| `ed25519-dalek` | 3.0.0 (2026-07-06) | BSD-3-Clause | Edition 2024, MSRV 1.85; `std` feature removed; `rand_core` 0.10, `signature` 3.0, `sha2` 0.11, `getrandom` 0.4. |
| `minisign` | 0.10.0 (2026-09-25) | MIT | Pure Rust; deps `scrypt 0.11`, `ct-codecs`, `getrandom 0.4`, `rpassword` — **does not use ed25519-dalek**. API: `KeyPair::generate_encrypted_keypair`, `SecretKeyBox`/`PublicKeyBox`, `sign` → `SignatureBox`, `verify`; trusted + untrusted comments. |
| `minisign-verify` | 0.3.0 (2026-09-25) | MIT | Zero-dependency verifier; 18M downloads. |

Sources: [blake3](https://crates.io/api/v1/crates/blake3), [ed25519-dalek](https://crates.io/api/v1/crates/ed25519-dalek), [ed25519-dalek CHANGELOG](https://raw.githubusercontent.com/dalek-cryptography/curve25519-dalek/main/ed25519-dalek/CHANGELOG.md), [minisign](https://crates.io/api/v1/crates/minisign), [minisign Cargo.toml](https://raw.githubusercontent.com/jedisct1/rust-minisign/master/Cargo.toml), [minisign README](https://github.com/jedisct1/rust-minisign/blob/master/README.md), [minisign-verify](https://crates.io/api/v1/crates/minisign-verify).

Tauri's CLI pins `minisign = "0.9"` with the comment "Careful with updating minisign, often broke our updater signatures" ([tauri-cli Cargo.toml](https://raw.githubusercontent.com/tauri-apps/tauri/dev/crates/tauri-cli/Cargo.toml)). If pack signing (`06-commons.md` line 103, "ed25519, `minisign` style") and the app updater share a key format, the two version lines must be kept compatible.

---

## 7. Steinberg ASIO SDK licensing, late 2026

- Steinberg announced dual licensing on 2025-10-15 (press release; heise coverage 2025-10-29): the ASIO SDK is available under "an open-source variant of the GPLv3 license, in addition to the previous proprietary licensing model"; VST 3.8 moved to MIT ([heise](https://heise.de/-10963451), [headlinerhub](https://www.headlinerhub.com/steinberg-extends-its-asio-licensing-model-to-include-both-proprietary-and-open-source-options.html)).
- Current SDK is **2.3.4**; a third-party import dated 2026-07-22 shows the package ships "Steinberg ASIO Licensing Agreement.pdf", "Steinberg ASIO Usage Guidelines.pdf" and a README whose Dual Licensing section reads: "Developers can choose between two options: Use of the proprietary ASIO SDK license, for use cases that benefit from closed-source licensing. Adopt the General Public License (GPL) Version 3 for full open-source integration. Both options grant access to the same SDK." FAQ: existing products may stay proprietary; switching to GPLv3 requires source disclosure; the ASIO logo under GPLv3 is allowed only with trademark compliance; "ASIO" may not appear in company or product names ([codeberg mirror](https://codeberg.org/rubiefawn/asiosdk)). MSYS2 packages `mingw-w64-asiosdk` 2.3.4 as GPL-3.0-only ([packages.msys2.org](https://packages.msys2.org/base/mingw-w64-asiosdk)).
- **Proprietary path in practice.** Steinberg's pointer is https://www.steinberg.net/developers/prorietary-sdk/ (sic; page content did not load for this research). Forum threads show developers emailing the signed agreement to reception@steinberg.de (cc info@steinberg.de) and waiting **5–9 weeks with no acknowledgement** (threads of 2026-08-13, 2026-09-05, 2026-10-14); Steinberg's forum staff reply only with a link to an "ASIO SDK Proprietary License - Frequently Asked Questions" thread; an individual without a registered business asked on 2026-10-05 whether they may sign and received no substantive answer ([no response after 5 weeks](https://forums.steinberg.net/t/proprietary-asio-sdk-license-agreement-no-response-after-5-weeks-odemi-daw/1043608), [where to submit](https://forums.steinberg.net/t/where-do-i-submit-the-proprietary-asio-sdk-license-agreement/1035889), [individual developer](https://forums.steinberg.net/t/proprietary-asio-licence-for-an-individual-developer-before-business-registration/1042871)). The proposal's "weeks" estimate is optimistic; plan for a quarter.

---

## 8. UI toolkits and rendering

### 8.1 Tauri 2

- **Current stable: `tauri` 2.12.5 (2026-10-21)**, Apache-2.0 OR MIT, **MSRV 1.90**; `tauri-bundler` 2.10.4, `tauri-cli` 2.12.4, `tauri-macos-sign` 2.4.3 released the same day ([crates.io](https://crates.io/api/v1/crates/tauri), [releases](https://api.github.com/repos/tauri-apps/tauri/releases)).
- **Tauri 3.0.0-alpha.1 (2026-09-15) through alpha.5 (2026-10-14)** exist on crates.io. Their release notes contain only internal refactors (removing an `Arc`/`Mutex`, deprecating `InvokeMessage::state`) and dependency bumps; no public 3.0 roadmap or migration guide was found ([alpha.1](https://api.github.com/repos/tauri-apps/tauri/releases/tags/tauri-v3.0.0-alpha.1), [alpha.5](https://api.github.com/repos/tauri-apps/tauri/releases/tags/tauri-v3.0.0-alpha.5)). The updater docs already say `createUpdaterArtifacts: "v1Compatible"` "will be removed in v3".
- **Bundler targets** (distribute docs): Linux .deb, RPM, AppImage, Flatpak, Snap, AUR; macOS .app, DMG, App Store; Windows installer (NSIS .exe and MSI) and Microsoft Store. Signing guides per platform; macOS notarization required outside the App Store; CrabNebula Cloud is the first-party hosted update/distribution service ([distribute](https://v2.tauri.app/distribute/)).
- **Updater plugin** ([docs](https://v2.tauri.app/plugin/updater/)): signing "cannot be disabled"; keys from `tauri signer generate -w ~/.tauri/myapp.key`; env `TAURI_SIGNING_PRIVATE_KEY` (+`_PASSWORD`), `.env` files not read; config `plugins.updater.pubkey` (content, not path), `plugins.updater.endpoints` (TLS enforced), `plugins.updater.windows.installMode` = `passive` | `basicUi` | `quiet`; endpoint variables `{{current_version}}`, `{{target}}`, `{{arch}}`; static manifest `{version, notes?, pub_date?, platforms: {"<os>-<arch>": {url, signature}}}`; dynamic server returns 204 for no update. **Updatable formats:** Linux AppImage only (not .deb/.rpm), macOS `.app.tar.gz`, Windows NSIS and MSI. Windows installs exit the app; downgrades blocked by default; plugin requires Rust 1.90; losing the private key strands existing installs. Signatures are minisign-format (CLI depends on `minisign 0.9`).
- **Security advisories 2026:** CVE-2026-95624 (`allowDowngrades` was accepted from frontend JS; moved to plugin config) and CVE-2026-95625 (manifest protected only by TLS, enabling replay of an older signed build); both fixed in **tauri-plugin-updater 2.12.0 (2026-09-20)**, which adds a signed-version option ([Tauri updater 2.12.0 notes](https://v2.tauri.app/release/updater/v2.12.0/), [CVE-2026-95625](https://security-tracker.debian.org/tracker/CVE-2026-95625), [CVE-2026-95624](https://app.opencve.io/cve/CVE-2026-95624)).
- **WebGPU inside the three webviews** (relevant to `05-workstation.md` line 99 "WebGPU or Canvas surface"): WKWebView has WebGPU on by default from macOS 26 / Safari 26 ([web.dev](https://web.dev/blog/webgpu-supported-major-browsers?hl=en), [Apple forum](https://developer.apple.com/forums/thread/770862)); WebView2 tracks Edge stable (WebGPU since Chromium 113), earlier builds needed `--enable-unsafe-webgpu` via `additionalBrowserArgs` ([tauri#6381](https://github.com/tauri-apps/tauri/issues/6381)); **WebKitGTK has no WebGPU**: a 2023 maintainer reply said "not supported and nobody is working on it", and no 2.48–2.52 release note mentions it ([webkit-gtk list](https://lists.webkit.org/pipermail/webkit-gtk/2023-October/003947.html), [2.48.0](https://lists.webkit.org/pipermail/webkit-gtk/2025-March/004043.html), [2.51.1](https://webkitgtk.org/2025/10/24/webkitgtk2.51.1-released.html)). The macOS 10.15 floor also predates WebGPU in WKWebView. Candidate A's surface must be Canvas2D or WebGL2 to run everywhere.

### 8.2 iced

- **0.14.0 (2025-12-07)**, MIT, MSRV 1.88; headline: reactive rendering, time-travel debugging, Animation API, headless/end-to-end testing, IME, hot reloading, `comet` debugger, `table`/`grid`/`sensor`/`float` widgets, wgpu 27 ([crates.io](https://crates.io/api/v1/crates/iced), [release](https://api.github.com/repos/iced-rs/iced/releases)). Previous release 0.13.1 was September 2024: a 15-month gap.
- **master is 0.15.0-dev, MSRV 1.93, wgpu 30, winit pinned to a git fork (`iced-rs/winit` rev 4811646…)**, no `accesskit` dependency ([Cargo.toml](https://raw.githubusercontent.com/iced-rs/iced/master/Cargo.toml)). README still says "Iced is currently experimental software" ([README](https://github.com/iced-rs/iced/blob/master/README.md)).
- **Accessibility:** issue #552 "Implement accessibility support" open since 2019; PR #1849 (AccessKit) stale since 2024; the pop-os/iced fork used by COSMIC carries AccessKit; no maintainer reply to 2025–2026 pings ([#552](https://github.com/iced-rs/iced/issues/552)). The proposal's "no accessibility story yet" remains accurate.

### 8.3 wgpu

**30.0.1 (2026-08-22)**, MIT OR Apache-2.0, MSRV 1.87. 30.0.0 (2026-07-01) renamed `dispatch` → `dispatch_workgroups`, `TextureUsages::TRANSIENT` → `TRANSIENT_ATTACHMENT`, moved `SurfaceTexture::present()` to `Queue::present`, made vertex buffer slots `Option<VertexBufferLayout>`, and requires explicit `@interpolate(flat)` for integer shader I/O; mesh shaders on Metal/DX12 ([crates.io](https://crates.io/api/v1/crates/wgpu), [releases](https://api.github.com/repos/gfx-rs/wgpu/releases)). Major versions land roughly quarterly (29.0.1 Mar 2026, 30.0.0 Jul 2026); any custom wgpu surface in iced must follow iced's pinned major.

### 8.4 Rust GUI landscape since mid-2026 (Tauri-vs-iced context)

| Toolkit | Version (date) | License | Change since the proposal |
|---|---|---|---|
| `egui` | 0.37.0 (2026-10-15) | MIT OR Apache-2.0 | AccessKit shipped ("enabled by default in eframe", Windows + macOS) ([README](https://github.com/emilk/egui/blob/main/README.md)); still immediate-mode. |
| `slint` | 1.18.1 (2026-09-21) | GPL-3.0-only OR LicenseRef-Slint-Royalty-free-2.0 OR LicenseRef-Slint-Software-3.0 | Royalty-free license covers proprietary desktop apps (excludes embedded) ([crates.io](https://crates.io/api/v1/crates/slint), [pistack 2026-08-25](https://www.pistack.xyz/posts/2026-08-25-rust-gui-frameworks-egui-iced-slint-comparison/)). `11-resolutions.md` line 30 "Drop Slint on license" is weaker than stated. |
| `dioxus` | 0.7.10 stable; 0.8.0-alpha.3 (2026-10-16) | MIT OR Apache-2.0 | `native` feature (non-webview renderer) in 0.8 alphas; webview path praised for free accessibility/IME ([crates.io](https://crates.io/api/v1/crates/dioxus), [wrenlearnsrust 2026-03-11](https://wrenlearnsrust.com/posts/2026-03-11-rust-gui-landscape-2026.html)). |
| `gpui` | 0.2.2 (2025-10-22) | Apache-2.0 | On crates.io, `publish = true`; no release in 12 months; Windows "in progress but not production-ready" ([crates.io](https://crates.io/api/v1/crates/gpui), [gpui Cargo.toml](https://raw.githubusercontent.com/zed-industries/zed/main/crates/gpui/Cargo.toml), [gpui-starter 2026-06-05](https://gpui-starter.freeoxide.com/blog/rust-gui-frameworks-comparison-2026/)). |
| `xilem` | 0.4.0 (2025-10-29) | Apache-2.0 | Still "not ready for production" per 2026 commentary. |

Net: nothing published since mid-2026 overturns the A-vs-B1 narrowing, but iced's toolchain demands (1.93, winit fork) and its unanswered accessibility issue have grown, egui has the accessibility story iced lacks, and Slint's royalty-free tier is a legitimate proprietary-desktop option if the spike criteria ever reopen.

---

## 9. Observability, testing, release packaging

| Tool | Version (date) | License | Notes |
|---|---|---|---|
| `sentry` | 0.49.3 (2026-09-21) | MIT | MSRV 1.88. Default features now include `logs`, `metrics`, `release-health`. 0.49.0 made `ClientOptions` `#[non_exhaustive]` (builder setters only). **0.49.3 added `minidump` feature**: native crash capture in a separate process, sent as attachments, Linux/macOS/Windows. No offline queue/persistence ([crates.io](https://crates.io/api/v1/crates/sentry), [CHANGELOG](https://raw.githubusercontent.com/getsentry/sentry-rust/master/CHANGELOG.md)). |
| `cargo-nextest` | 0.9.149 (2026-10-20) | Apache-2.0 OR MIT | `rust_version` 1.91 to build from source; prebuilt binaries avoid that ([crates.io](https://crates.io/api/v1/crates/cargo-nextest)). |
| `cargo-dist` / `dist` | crates.io 0.32.0 (2026-05-22); GitHub v0.33.1 (2026-10-21) | MIT OR Apache-2.0 | Community-maintained since July 2025 (Misty De Meo "granted permission to resume development"; improvements merged from the `astral-sh/cargo-dist` fork); canonical repo remains `axodotdev/cargo-dist`; v0.33.0 added Azure Artifact Signing for Windows x86_64; generates GitHub Actions release.yml, shell/PowerShell/Homebrew/npm installers; **crates.io lags GitHub releases** ([crates.io](https://crates.io/api/v1/crates/cargo-dist), [releases](https://api.github.com/repos/axodotdev/cargo-dist/releases), [blog](https://blog.mistys-internet.website/post/2025/07/20/dist-0-dot-28-dot-1-is-out)). Aimed at CLI binaries; no macOS notarization or Linux desktop bundles documented. |
| `cargo-packager` | 0.11.8 (2025-11-27) | Apache-2.0 OR MIT | CrabNebula's standalone bundler (NSIS/MSI/DMG/AppImage/deb); **11 months without a release** ([crates.io](https://crates.io/api/v1/crates/cargo-packager)). |

For the Tauri candidate, `tauri-bundler` + updater is the packaging story; for the iced candidate, nothing in the Rust ecosystem is as complete, and `cargo-packager`'s dormancy is a risk the spike should weigh.

---

## 10. GitHub API facts for the roadmap-to-issues step

### 10.1 REST API versions and rate limits

- Supported versions: **`2026-03-10`** (latest, released 2026-03-10) and `2022-11-28` (supported until 2028-03-10). **Omitting `X-GitHub-Api-Version` defaults to `2022-11-28`**, not latest ([api-versions](https://docs.github.com/en/rest/about-the-rest-api/api-versions)). Breaking in 2026-03-10: singular `assignee` removed on issue create/update (use `assignees`); `/contents` returns `type: "submodule"`; workflow dispatch returns 200 with run details; several deprecated fields dropped ([breaking changes](https://docs.github.com/en/rest/about-the-rest-api/breaking-changes?apiVersion=2026-03-10)).
- Primary: 5,000 req/hr per user (PAT/OAuth); GitHub App installations 5,000 scaling to 12,500; `GITHUB_TOKEN` 1,000/hr/repo. GraphQL: 5,000 points/hr per user; `first`/`last` 1–100; ≤ 500,000 nodes per call.
- **Secondary:** ≤ 100 concurrent requests; ≤ 900 REST points/min (GET = 1, POST/PATCH/PUT/DELETE = 5 → at most 180 writes/min); ≤ 2,000 GraphQL points/min; ≤ 90 s CPU per 60 s; **≤ 80 content-generating requests/min and ≤ 500/hr** (issues, comments, dependency links, project item adds all count; web UI actions count too). On 403/429 honour `retry-after`, else `x-ratelimit-reset`, else wait ≥ 1 min with exponential backoff ([REST rate limits](https://docs.github.com/en/rest/using-the-rest-api/rate-limits-for-the-rest-api), [GraphQL limits](https://docs.github.com/en/graphql/overview/rate-limits-and-node-limits-for-the-graphql-api)).

### 10.2 Issue dependencies (blocked by / blocking)

REST ([issue-dependencies](https://docs.github.com/en/rest/issues/issue-dependencies)):

| Method | Path | Body |
|---|---|---|
| GET | `/repos/{owner}/{repo}/issues/{issue_number}/dependencies/blocked_by` | — (`per_page` ≤ 100) |
| POST | `/repos/{owner}/{repo}/issues/{issue_number}/dependencies/blocked_by` | `{"issue_id": <numeric issue id, not number>}` → 201 |
| DELETE | `/repos/{owner}/{repo}/issues/{issue_number}/dependencies/blocked_by/{issue_id}` | — → 200 |
| GET | `/repos/{owner}/{repo}/issues/{issue_number}/dependencies/blocking` | — |

There is **no POST for `blocking`**: a "blocks" edge is always written from the blocked issue's side. Headers: `Accept: application/vnd.github+json`, `X-GitHub-Api-Version: 2026-03-10`; no preview header. Issue objects carry `issue_dependencies_summary {blocked_by, blocking, total_blocked_by, total_blocking}`. POST warns of secondary rate limiting and 422 "spammed". GraphQL: `addBlockedBy(input: {issueId, blockingIssueId})`, `removeBlockedBy`, `Issue.blockedBy` / `Issue.blocking` connections, `issueDependenciesSummary` ([GraphQL issues](https://docs.github.com/en/graphql/reference/issues)). Requires triage permission; available on Free/Pro/Team/Enterprise Cloud; `gh issue create --blocked-by/--blocking` and `gh issue edit --add-blocked-by` exist ([creating issue dependencies](https://docs.github.com/en/enterprise-cloud@latest/issues/tracking-your-work-with-issues/using-issues/creating-issue-dependencies)). **No documented maximum number of dependencies per issue** was found.

### 10.3 Sub-issues

REST ([sub-issues](https://docs.github.com/en/rest/issues/sub-issues)): `GET .../issues/{n}/parent`; `GET .../issues/{n}/sub_issues`; `POST .../issues/{n}/sub_issues` body `{"sub_issue_id": <id>, "replace_parent": bool?}` (sub-issue "must belong to the same repository owner as the parent") → 201; `DELETE .../issues/{n}/sub_issue` body `{"sub_issue_id"}`; `PATCH .../issues/{n}/sub_issues/priority` body `{"sub_issue_id", "after_id" | "before_id"}`. Limits (UI docs): **up to 100 sub-issues per parent, up to eight levels** ([adding sub-issues](https://docs.github.com/en/issues/tracking-your-work-with-issues/using-issues/adding-sub-issues)). Issue objects carry `parent_issue_url` and `sub_issues_summary {total, completed, percent_completed}`. GraphQL: `addSubIssue(input: {issueId, subIssueId | subIssueUrl, replaceParent})`, `removeSubIssue`, `reprioritizeSubIssue(issueId, subIssueId, afterId | beforeId)`.

### 10.4 Creating issues with a type and a parent

`POST /repos/{owner}/{repo}/issues` body: `title` (required), `body`, `milestone`, `labels[]`, `assignees[]`, **`type` (string: issue type name, or null)**, **`parent_issue_id` (integer)**, `issue_field_values[{field_id, value}]`. Labels/assignees/milestone/type are **silently dropped** without push access. `PATCH` accepts `type` as null/string/object ([create an issue](https://docs.github.com/en/rest/issues/issues?apiVersion=2022-11-28#create-an-issue)). GraphQL `createIssue` takes `issueTypeId`, `parentIssueId`, `projectV2Ids`, `milestoneId`, `labelIds`, `assigneeIds`; `updateIssue` takes `issueTypeId` or `issueType: {issueTypeId, rationale ≤ 280, suggest}` (mutually exclusive; `issueType` added 2026-07-14). **Issue types are configured per organization** (Settings → Planning → Issue types), defaults Task/Bug/Feature, **max 25 types**; the docs describe no equivalent for user-owned repositories ([managing issue types](https://docs.github.com/en/issues/tracking-your-work-with-issues/configuring-issues/managing-issue-types-in-an-organization)). `xjmusic/xjmusic` is organization-owned, so types are usable.

### 10.5 Projects v2 via GraphQL

From the Projects reference ([GraphQL projects](https://docs.github.com/en/graphql/reference/projects)) and the API guide ([using the API to manage projects](https://docs.github.com/en/issues/planning-and-tracking-with-projects/automating-your-project/using-the-api-to-manage-projects)):

- `createProjectV2(input: {ownerId: ID!, title: String!, repositoryId?, teamId?})` → `projectV2 { id }`.
- `createProjectV2Field(input: {projectId!, name!, dataType: ProjectV2CustomFieldType!, singleSelectOptions?, multiSelectOptions?, iterationConfiguration?})`. `ProjectV2CustomFieldType` = `TEXT | NUMBER | DATE | SINGLE_SELECT | MULTI_SELECT | ITERATION`. `ProjectV2SingleSelectFieldOptionInput` = `{name: String!, color: ProjectV2SingleSelectFieldOptionColor!, description: String!}` (**description is required**; colors `BLUE GRAY GREEN ORANGE PINK PURPLE RED YELLOW`). `ProjectV2IterationFieldConfigurationInput` = `{startDate: Date!, duration: Int!, iterations: [{title!, startDate!, duration!, id?}]!}`.
- `addProjectV2ItemById(input: {projectId!, contentId!})` → `item { id }` (idempotent: returns the existing item id). `addProjectV2DraftIssue(projectId, title, body)`.
- `updateProjectV2ItemFieldValue(input: {projectId!, itemId!, fieldId!, value: ProjectV2FieldValue!})`, `ProjectV2FieldValue` = one of `{text | number: Float | date: Date | singleSelectOptionId | iterationId | multiSelectOptionIds}`. Cannot set Assignees/Labels/Milestone/Repository.
- `updateProjectV2(input: {projectId!, title?, public?, closed?, readme?, shortDescription?})`.
- **Views are creatable via API as of 2026-07-28** (GraphQL changelog): `createProjectV2View(input: {projectId!, name!, layout: ProjectV2ViewLayout!, configuration: {visibleFieldIds}})`, `updateProjectV2View(viewId!, name?, layout?, filter?, configuration?)`, `deleteProjectV2View(viewId!)`; `ProjectV2ViewLayout` = `TABLE_LAYOUT | BOARD_LAYOUT | ROADMAP_LAYOUT` ([changelog](https://docs.github.com/en/graphql/overview/changelog)). The task's "believed no" is **out of date**. GraphQL cannot yet set sort or group; REST can (below).

### 10.6 Projects v2 via REST (2026-03-10)

Endpoint groups: projects, fields, items, drafts, views ([REST projects](https://docs.github.com/en/rest/projects?apiVersion=2026-03-10)).

- **Projects: GET only** (`/orgs/{org}/projectsV2`, `/orgs/{org}/projectsV2/{project_number}`, user equivalents). **No REST endpoint creates a project**; use `createProjectV2` ([projects](https://docs.github.com/en/rest/projects/projects?apiVersion=2026-03-10)).
- **Fields:** `POST /orgs/{org}/projectsV2/{project_number}/fields` body `{name, data_type: text|number|date|single_select|iteration, single_select_options: [{name, color, description}], iteration_configuration: {start_date, duration, iterations: [{title, start_date, duration}]}}` (docs are internally inconsistent about which nested fields are required) ([fields](https://docs.github.com/en/rest/projects/fields?apiVersion=2026-03-10)).
- **Items:** `POST .../items` body `{type: "Issue" | "PullRequest", id}` or `{type, owner, repo, number}`; `PATCH .../items/{item_id}` body `{fields: [{id, value}]}` where value is text/number/date literal, single-select option id or iteration id, `null` clears; `GET .../items?q=<filter>&fields=…` and `GET .../views/{view_number}/items`; `DELETE .../items/{item_id}` ([items](https://docs.github.com/en/rest/projects/items?apiVersion=2026-03-10)).
- **Views:** `POST /orgs/{org}/projectsV2/{project_number}/views` body `{name!, layout!: table|board|roadmap, filter?, visible_fields?: [field_id] (not for roadmap), sort_by?: [[field_id, "asc"|"desc"]], group_by?: [field_id], vertical_group_by?: [field_id]}` → 201. Title, Reviewers, Linked PRs, Sub-issues progress, Tracked by, Tracks cannot be group fields ([views](https://docs.github.com/en/rest/projects/views?apiVersion=2026-03-10)). No GET/PATCH/DELETE for views in REST; use GraphQL for those.
- **Item limit: "A project can contain a maximum of 50,000 items across both active views and the archive page"** ([archiving items](https://docs.github.com/en/issues/planning-and-tracking-with-projects/managing-items-in-your-project/archiving-items-from-your-project)). The 1,200 figure was the pre-2024 limit and survives only in GHES ≤ 3.13 docs.

---

## 11. Implications for V2 specs

**Audio I/O**
1. `11-resolutions.md` Q10 promises "WASAPI exclusive as an option". cpal 0.18 cannot do it; PR #1368 is postponed and #1220 defers it to an unscheduled v0.19. Specs must either drop the promise, scope a cpal fork/upstream contribution, or write a direct WASAPI path for exclusive mode.
2. cpal 0.18 streams do not start until `play()`; the default config prefers 48 kHz; `DeviceChanged` hot-plug notification was reverted in 0.18.2. The transport spec needs its own device-loss detection (stream error → rescan).
3. Native PipeWire and PulseAudio hosts exist since 0.18.0 and PipeWire is the default Linux host; rewrite the Linux backend row accordingly. ALSA `default` fails with `DeviceBusy` under PipeWire.
4. Enabling the `asio` feature pulls `asio-sys`, which downloads Steinberg's SDK during the build and needs libclang; CI for official Windows builds therefore needs the SDK and a license posture even before shipping.

**Decode, resample, stretch**
5. Symphonia's defaults exclude MP3, AAC, MP4/M4A, AIFF, ALAC; the `xj-content` spec must list features explicitly, state that HE-AAC/Opus/WavPack are unsupported, and note no gapless for AAC/MP4. MPL-2.0 obligations apply to Symphonia, `triple_buffer` and Bungee.
6. rubato shipped four majors in 2026; pin `rubato 5` + `audioadapter 5` and name `process_into_buffer` as the only call allowed near real time.
7. Signalsmith Stretch has no releases (version in header: 1.3.2; MIT, Signalsmith Audio Ltd.). The `signalsmith-stretch` crate pins a June 2025 upstream commit; the spec should vendor upstream directly (C++11, `cc`) or accept the stale pin. `process()` can allocate, which confirms "never on the audio thread".
8. The Bungee Rust binding is archived (2026-03-03); a bake-off needs the C++ CLI or a fresh binding. Bungee is MPL-2.0 and forward-only.

**Control and clock**
9. midir has no hot-plug or disconnect notification on any platform (issue #201, 2026-08-26); `05-workstation.md` line 86 and `08-performer-input.md` line 64 overstate it. The input spec must define a rescan interval and reconnect behaviour; macOS may require recreating `MidiInput`.
10. rosc has not been released since March 2025; it is a codec only, so the OSC transport (UDP socket, bundles, timetags) is the spec's responsibility.
11. Ableton Link 4.x (May 2026) adds Link Audio, requires C++17 and, from 4.1, CMake 3.18. `rusty_link` 0.4.9 pins a December 2025 pre-4.0 commit, is GPL-2.0-or-later, and is a one-person hobby project last touched 2026-01-07. The `BeatClock` spec should name the exact Link tag, plan for owning or forking the binding, and record that the proprietary license must cover the binding as well as Link.
12. quinn needs certificates; a LAN session protocol must specify self-signed `rcgen` certs with trust-on-first-use or join-code-bound verification. mdns-sd runs its own thread, lacks unicast responses, and self-describes as beta; the discovery spec should allow a manual IP fallback.

**Commons and signing**
13. `minisign` 0.10 does not depend on `ed25519-dalek`; pick one of (a) `minisign` for packs with the minisign key/signature text format, or (b) `ed25519-dalek` 3 with a custom envelope. Tauri's updater pins `minisign 0.9` and warns that bumps have broken signatures; keep pack signing and app-update signing on separately pinned versions.
14. `ed25519-dalek` 3.0.0 (edition 2024, `rand_core` 0.10, `signature` 3) is new; any dependent crate still on dalek 2 will cause duplicate-crypto-stack builds.

**UI, packaging, telemetry**
15. Tauri: stable is 2.12.5 (MSRV 1.90); 3.0 alphas exist but carry no public plan. Updater signatures are minisign; only AppImage, `.app.tar.gz`, NSIS and MSI are updatable (not .deb/.rpm); CVE-2026-95624/95625 mean the spec must require updater ≥ 2.12.0 and the signed-version option. WebGPU is absent in WebKitGTK and only present in WKWebView from macOS 26, so Candidate A's surface must be Canvas2D/WebGL2.
16. iced: 0.14 (Dec 2025) after a 15-month gap; master needs Rust 1.93 and a winit fork; accessibility issue #552 unanswered; egui and Dioxus have accessibility, Slint's royalty-free tier covers proprietary desktop. Record these in the Phase 0 spike criteria rather than relitigating the narrowing.
17. wgpu majors land about quarterly (30.0.0 July 2026 with renames); any custom surface must track iced's pinned major.
18. sentry 0.49.3 added native crash minidumps and enables `logs`/`metrics` by default; the "no usage telemetry" stance in Q11 requires disabling those default features explicitly.
19. cargo-dist is community-maintained (axodotdev repo, v0.33.1 Oct 2026, crates.io lagging at 0.32.0) and targets CLI binaries; cargo-packager has been dormant since Nov 2025. The iced candidate has no first-class desktop bundler; the Tauri candidate has `tauri-bundler`.
20. Steinberg's proprietary ASIO agreement is submitted by email to reception@steinberg.de and developers report 5–9 weeks with no acknowledgement; GPLv3 is the alternative only for a GPLv3 product. The SDK 2.3.4 trademark rules forbid "ASIO" in product names.

**Roadmap-to-issues tooling**
21. Send `X-GitHub-Api-Version: 2026-03-10` explicitly; the default is still 2022-11-28, and 2026-03-10 removed singular `assignee`.
22. Blocked-by edges are written only from the blocked side: `POST .../issues/{n}/dependencies/blocked_by {"issue_id": <id>}` with the blocker's numeric `id` (not its number). There is no `blocking` POST. No per-issue dependency cap is documented.
23. Sub-issues: ≤ 100 per parent, ≤ 8 levels, same repository owner; `parent_issue_id` on create avoids a second call. Issue types are org-level (≤ 25) and set by name via `type` on REST create.
24. A project cannot be created over REST; use `createProjectV2(ownerId, title)`. Fields, items, item field values and **views** can be created over REST or GraphQL; REST views accept `sort_by`, `group_by`, `vertical_group_by`, which GraphQL lacks. `singleSelectOptions[].description` is required in GraphQL. Item ceiling is 50,000.
25. Budget writes against the secondary limits: ≤ 80 content-creating requests/min and ≤ 500/hr, ≤ 180 REST writes/min by points. A roadmap of N issues plus D dependency edges plus N project adds plus N field updates is roughly 3N + D content requests; at 500/hr, 100 issues with 150 edges takes about an hour of paced writes, and the script must honour `retry-after`.
