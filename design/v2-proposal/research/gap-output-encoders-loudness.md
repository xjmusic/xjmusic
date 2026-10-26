# Gap map: audio output encoders, muxing and loudness metering for V2

Scope: everything that *writes* or *measures* audio in the V2 proposal. Consumers: Capture and the Ship-mode bounce (`design/v2-proposal/05-workstation.md` "Sinks", "Offline render"; `design/v2-proposal/08-performer-input.md` "Capture and replay"), `xj-content render` and `preview/render.ogg` (`design/v2-proposal/06-commons.md` lines 28-29, 160), the lint rule "Audio is level-matched to a documented target" (06 line 125), and the master true-peak limiter (05 line 132). Decoding, resampling and time stretch are already mapped in `external-stack-research.md` (symphonia 0.6.1 §1.2, rubato, Signalsmith); the V1 Java mixer, ship stage and ffmpeg use are mapped in `java-mixer-ship-audio.md` (§3 mixer, §3.8 dead normalization, §3.9 16-bit wrap, §5 ship/HLS). This map does not repeat those; it cites them.

All crates.io facts below were read from the crates.io API on 2026-10-25 (`/api/v1/crates/<name>` and `/<ver>/dependencies`); repo activity from the GitHub API the same day. "Pure Rust" means no C compiled or linked.

## 1. What V1 does today with output level and format (grounding)

| Fact | Where |
|---|---|
| Per-voice amplitude is `intensityAmplitude × pick.amplitude × instrument.volume × audio.volume` | `workstation/src/main/java/io/xj/engine/mixer/ActiveAudio.java:40-41` |
| `InstrumentAudio.volume` is a float field in the content model (engine and Java) | `engine/include/xjmusic/content/InstrumentAudio.h:26,46`; `workstation/src/main/java/io/xj/model/pojos/InstrumentAudio.java:25,217-225` |
| `dubMasterVolume` per instrument type, all ten default to `1.0` | `engine/src/content/TemplateConfig.cpp:30-41`; `engine/include/xjmusic/content/TemplateConfig.h:38,83` |
| Mixer defaults: `mixerCompressToAmplitude = 1.0`, `mixerCompressRatioMax = 1.0`, `mixerCompressRatioMin = 0.3`, `mixerHighpassThresholdHz = 30`, `mixerLowpassThresholdHz = 15000`, `mixerNormalizationCeiling = 0.999` (dead) | `engine/src/content/TemplateConfig.cpp:85-94`; `workstation/src/main/java/io/xj/model/TemplateConfig.java:103-111` |
| No normalization (`// NO NORMALIZATION! See issue #275`), log dynamic range stage caps any in-range bus sum at ≤ 0.618 FS (about −4.2 dBFS), compressor is a unity stage under defaults, **no limiter, 16-bit conversion wraps above 1.0** | `java-mixer-ship-audio.md` §3.6–3.9 (`MixerImpl.java:137-138, 348-351`, `AudioSampleFormat.java:191-193`) |
| Build-for-engine output defaults: 2 channels, WAV, 48000 Hz, 16-bit | `workstation/src/main/resources/application.properties:2-5` |
| `AudioFileContainer { WAV, OGG, FLAC, MP3, M4A, AAC }` exists but only WAV is reachable (container and bits choices disabled in Settings) | `workstation/src/main/java/io/xj/gui/types/AudioFileContainer.java:3-10`; `java-mixer-ship-audio.md` line 276 |
| The only "normalization" in V1 is the waveform *display* scale: `instrumentAudioEditor.normalizeMaxValue=0.95` divided by the max absolute sample | `application.properties:22`; `InstrumentAudioEditorController.java:88-89, 492-501` (`computeDisplayVolumeRatio`) |
| No LUFS, loudness, true-peak or limiter code anywhere in `engine/`, `workstation/` or the Unreal plugin | `grep -rn -i "lufs\|loudness\|true.peak\|limiter"` returns only `ChordForm.normalize` and the dead `mixerNormalization*` keys |
| Open V1 issues already asking for compressed audio: #311 "Audio files prepared as Vorbis and streamed off disk", #423 "Workstation & Unreal Plugin can build & utilize OGG audio files" | `repo-meta-issues-demos.md` lines 139, 169 |

**How V1 content is actually levelled (measured from the four demo projects' JSON, `scratchpad/understand/demos-src`, 676 JSON files):**

- 3000 `InstrumentAudio` entries, every `waveformKey` is `.wav`.
- `audio.volume`: min 0.0, max 3.0, median 1.0; **2009 of 3000 (67 %) are exactly 1.0**; 208 sit near 0.5, 183 near 0.8, 92 near 1.5, 45 near 2.0. `instrument.volume` ranges 0.1–2.0 (mean 0.958). Authors balance with the `volume` fields, not by normalizing files.
- `lengthSeconds`: min 0.025 s, max 118.8 s, median 10.67 s. **39 audios are shorter than 0.4 s, 259 shorter than 1 s, 817 shorter than 3 s, 2183 are 3 s or longer.** Instrument modes: Event 128, Loop 67, Chord 25.
- The demo audio itself is not on this machine (no `.wav` under `/Users/nck/xj`, no `ffmpeg`/`sox` installed), so **no loudness of actual demo files was measured in this session**. The lint spec should make that measurement its first deliverable (see §6).

## 2. Crate facts: writing audio

### 2.1 WAV: `hound`

| | |
|---|---|
| Version | 3.5.1, published 2023-09-25 (crates.io `max_stable_version`). Repo `ruuda/hound` last commit 2026-08-09 ("Saturate on overflow in write_sample"), 40 open issues: active but unreleased work. |
| License | Apache-2.0 (readme notes a GPLv2 exception is possible). |
| Pure Rust / FFI | Pure Rust, zero runtime dependencies (`dependencies` list is empty; only `cpal 0.2.12` as dev-dep). |
| MSRV | 1.40 (changelog 3.5.1: "compatibility with Rust 1.40.0 through 1.72.1"). No `rust-version` key in Cargo.toml. |
| API | `WavSpec { channels: u16, sample_rate: u32, bits_per_sample: u16, sample_format: SampleFormat::{Int, Float} }` (`src/lib.rs:335-362`). `WavWriter::create(path, spec)` wraps `BufWriter<File>` (`src/write.rs:801-805`); `WavWriter::new(W: Write + Seek, spec)`; `new_with_spec_ex`; `write_sample<S: Sample>` per sample (`write.rs:660`); `get_i16_writer(num_samples)` → `SampleWriter16` bulk path with `write_sample_unchecked` (`write.rs:679-681, 896-963`); `flush`, `finalize` patch RIFF/data sizes via Seek (`write.rs:702-711`); `WavSpec::into_header_for_infinite_file()` for a streaming header without Seek (`lib.rs:522`). |
| Formats written | Integer PCM 8/16/24/32 and IEEE float 32 (readme table). Writes `PCMWAVEFORMAT` normally and **`WAVEFORMATEXTENSIBLE` when `channels > 2 || bits_per_sample > 16`** (`write.rs:375-377, 459`). No 64-bit float. |
| Size limit | Data chunk length is `u32` (`write.rs:209`, `self.len += written as u32` at 215; written at 290). **No RF64/W64.** 4 GiB = 3.1 h at 48 kHz stereo f32, 4.1 h at 24-bit, 6.2 h at 16-bit. An overflow wraps silently in release builds. No RF64 issue or PR exists in the repo (search of `ruuda/hound` issues for RF64/4GB returned only unrelated items). |
| Allocation | One `BufWriter` buffer at creation; `write_sample` allocates nothing; `SampleWriter16` allocates its buffer once and recycles it. Safe to call from a non-RT writer thread fed by the DSP queue; not intended for the audio callback (it does I/O). |

Alternative: `wavers` 1.5.1 (2024-12-29, MIT, pure Rust, optional `rubato` resampling feature) — fewer downloads (68 k vs 21.5 M), not evaluated further.

### 2.2 Ogg Vorbis encoding

**There is no maintained pure-Rust Vorbis encoder.** `lewton` 0.10.2 (2021-01-20), `pure_vorbis` (2016) and `symphonia-codec-vorbis` are decoders only; the `ogg` crate README states "There is no extra support for vorbis codec decoding or encoding in this crate." Every encoder on crates.io wraps libvorbis:

| Crate | Version / date | License | Binding | MSRV | Notes |
|---|---|---|---|---|---|
| `vorbis_rs` | 0.5.6, 2026-07-30 (repo last commit 2026-10-24) | BSD-3-Clause | FFI via `aotuv_lancer_vorbis_sys 0.1.6` + `ogg_next_sys 0.1.5`, both vendoring the C sources and compiling them with `cc` (no cmake); `links = "vorbis"` | 1.87.0 on crates.io; README on master says 1.94 and that bumping MSRV is not a breaking change | libvorbis with the aoTuV and Lancer patches; "may introduce slight numerical variations" vs reference libvorbis |
| `vorbis-encoder` | 0.1.4, 2016-10-17 | MIT | FFI via `vorbis-sys 0.1.1` (2021) | none | Abandoned; vorbis-rs README: depends on "an old version of the libvorbis C library with known security vulnerabilities" |

`vorbis_rs` API (docs.rs 0.5.6): `VorbisEncoderBuilder::new(sampling_frequency: NonZeroU32, channels: NonZeroU8, sink: W: Write)` (random serial, needs default feature `stream-serial-rng` → `getrandom`), or `new_with_serial(.., stream_serial: i32)`; `.bitrate_management_strategy(VorbisBitrateManagementStrategy)`; `.comment_tag(tag, value)` / `.comment_tags(..)` (none written by default); `.minimum_page_data_size(Option<u16>)`; `.build() -> VorbisEncoder<W>` writes the three header packets immediately. `VorbisEncoder::encode_audio_block<B: AsRef<[S]>, S: AsRef<[f32]>>(&mut self, planar_block)` — **planar f32, one slice per channel, Vorbis I channel order**; docs recommend blocks of about 1024 samples and warn that blocks ≥ 2^18 samples degrade badly. `finish(self) -> Result<W>` (also on drop). Strategies: `Vbr { target_bitrate: NonZeroU32 }` (bit/s, management engine off), `QualityVbr { target_quality: f32 }` in **[-0.2, 1.0]**, `Abr { average_bitrate }`, `ConstrainedAbr { maximum_bitrate }`. A `Default` impl exists; its variant was not verified. Decoder half: non-chained streams only, no seeking; `wasm32-unknown-unknown` unsupported (`wasm32-wasip1` or emscripten only). Allocation: libvorbis allocates internally per block; offline/worker-thread use only.

Nominal Vorbis bitrates at 44.1 kHz stereo (Wikipedia "Vorbis", libvorbis table): q0 64 kbit/s, q2 96, q3 112, q4 128, q5 160, q6 192, q8 256, q10 500. A 120 s preview at q4–q5 is 1.9–2.4 MB. Xiph has stated since February 2013 that Vorbis should be deprecated in favour of Opus (same source); libvorbis is nevertheless still maintained and aoTuV merges land in 1.3.4+.

### 2.3 Opus encoding and Ogg Opus muxing

| Crate | Version / date | License | Binding | MSRV | Notes |
|---|---|---|---|---|---|
| `opus` (SpaceManiac/opus-rs) | 0.4.0, 2026-08-23 | MIT/Apache-2.0 | FFI via **`opusic-sys ^0.7.3`** (Cargo.toml) | none declared; edition 2015 | Safe wrapper; `Encoder::new(sample_rate: u32, Channels, Application)`, `encode(&[i16], &mut [u8]) -> usize`, `encode_float(&[f32], &mut [u8])`, `set_bitrate(Bitrate)`, `set_vbr(bool)`, `get_lookahead() -> i32` |
| `opusic-sys` | 0.7.5, 2026-08-06 | BSD-3-Clause (same as libopus) | Bundles **libopus 1.6.1**, built with **cmake** under default feature `bundled`; `default-features = false` links a system libopus (`OPUS_LIB_DIR`, `OPUS_LIB_STATIC`) | 1.82.0 | Carries an `opus.patch` against upstream; `dred`, `osce`, `fixed-point` features |
| `audiopus` / `audiopus_sys` | 0.2.0 (0.3.0-rc.0), 2021-04-22 | ISC | FFI | none | **Stale 5½ years**; do not cite |
| `opus-rs` (restsend) | 0.1.39, 2026-10-20 (repo created 2026-02-28, 53 stars, pushed 2026-10-25) | BSD-3-Clause on crates.io; GitHub reports `NOASSERTION` | **Pure Rust** port of libopus 1.6, `no_std` + no-`alloc` option, "bit-faithful" PLC claims | none declared | Self-described "Production-ready"; eight months old, 39 releases in that time. `OpusEncoder::new(rate, channels, Application)`, `encode(&[f32], frame_size, &mut [u8])`. Worth a spike, not a dependency to cite as settled |
| `ogg-opus`/`ogg_opus` | 0.1.2, 2021-05-18 | MIT | — | — | Stale Ogg Opus encoder/decoder |
| `opus_mux` | 0.1.1, 2022-08-30 | MIT/Apache-2.0/Zlib | pure Rust | — | **Demux only** ("Extracts an encoded Opus stream from an Opus file per RFC 7845") |

libopus constraints the spec must state (opus-codec.org API docs, `opus_encoder_create`/`opus_encode`): input rate "must be one of 8000, 12000, 16000, 24000, or 48000" — **a 44.1 kHz render must be resampled (rubato) before Opus**; `frame_size` "at 48 kHz the permitted values are 120, 240, 480, 960, 1920, and 2880" (2.5–60 ms); `OPUS_APPLICATION_AUDIO` "gives best quality at a given bitrate for most non-voice signals like music"; bitrate is set in bit/s.

**Ogg Opus muxing has no maintained crate**; it must be written over `ogg` per RFC 7845:

- `ogg` 0.9.2 (2025-01-12), BSD-3-Clause, pure Rust, MSRV 1.61, dep `byteorder`; repo `RustAudio/ogg` last commit 2025-01-12, 15 open issues. `PacketWriter::new(W: Write)`; `write_packet<P: Into<Cow<[u8]>>>(pck, serial: u32, PacketWriteEndInfo::{NormalPacket, EndPage, EndStream}, absgp: u64)`; `into_inner()`. README: "the encoder part of the Crate isn't as well tested as the decoder part."
- RFC 7845 requirements (sections 3–5, 7, 9): ID header `OpusHead` (19 bytes for mapping family 0: magic, version = 1, channel count ≠ 0, **pre-skip u16 LE in 48 kHz samples**, input sample rate u32 LE (informational), **output gain i16 Q7.8 dB, "A muxer SHOULD set this field to zero"**, mapping family 0 for mono/stereo) **alone on page 0 with BOS**; `OpusTags` comment header (vendor string + `NAME=value` UTF-8, no framing bit) that "MUST finish the page on which it completes"; granule positions count 48 kHz samples cumulatively including pre-skip, header pages carry 0; pre-skip for a fresh encode = encoder lookahead (+ any extrapolated lead-in), the 3840-sample figure is only for cropping existing streams; end trimming via a smaller final granule position; EOS flag on the last page. RFC 7845 §9: MIME `audio/ogg; codecs=opus`, "The RECOMMENDED filename extension for Ogg Opus files is '.opus'". Optional **`R128_TRACK_GAIN`** tag (integer Q7.8 dB relative to −23 LUFS, at most one per stream) — a standardized place to carry measured loudness.

### 2.4 FLAC: `flacenc`

| | |
|---|---|
| Version | 0.5.1, 2025-12-18; repo `yotarok/flacenc-rs` last commit 2026-06-29, 41 stars, 14 open issues. Major version zero: "we increment the minor version when there's a breaking API change". |
| License | Apache-2.0. |
| Pure Rust | Yes. Runtime deps: `crc 3.2`, `heapless 0.8`, `md-5 0.10` (STREAMINFO MD5), `num-traits`, `rustversion`, `seq-macro`; optional `crossbeam-channel` (`par`), `log`, `serde`, `nom` (`decode`), `nalgebra` (`experimental`), `mimalloc`. Default features: `log`, `par`, `serde`. |
| MSRV | Policy, not a `rust-version` key: "the stable Rust version that was the latest as of one year prior to the date of the latest stable release"; the README example gives 1.83.0 as of October 2025. |
| Limits | `DEFAULT_BLOCK_SIZE 4096`, `MIN_BLOCK_SIZE 32`, `MAX_BLOCK_SIZE 32767`, `MIN_BITS_PER_SAMPLE 8`, **`MAX_BITS_PER_SAMPLE 24`**, `MAX_CHANNELS 8` (`src/constant.rs:32-60`). Input is interleaved `&[i32]` already scaled to the bit depth; **no float input**, so a float mix must be converted (and dithered or limited) to ≤ 24-bit before encoding. |
| API | `config::Encoder::default().into_verified()`; `source::MemSource::from_samples(&[i32], channels, bits, rate)` or a custom `source::Source`; `encode_with_fixed_block_size(&config, source, block_size) -> Stream`; `Stream` implements `BitRepr` → `bitsink::ByteSink`; frames can be written one at a time. `Encoder { block_size, multithread (default = cfg!(feature = "par")), workers: Option<NonZeroUsize> }` (`src/config.rs:85-109`). |
| Allocation | Heavy and whole-stream in memory by default (`Stream` holds every frame); multi-threaded via crossbeam when `par`. Offline use only. Decoding (`decode` feature) is "experimental"; verify with `symphonia`'s FLAC decoder instead. |

Alternatives: `flac-bound` 0.5.0 (2024-11-05, MIT) is FFI to libFLAC; `claxon` 0.4.3 (2020) is decode-only.

### 2.5 Summary table

| Need | Crate | Ver (date) | License | Pure Rust? | Build tools | MSRV | RT-safe? |
|---|---|---|---|---|---|---|---|
| WAV write | `hound` | 3.5.1 (2023-09) | Apache-2.0 | yes | none | 1.40 | writer thread only; 4 GiB cap |
| Vorbis encode | `vorbis_rs` | 0.5.6 (2026-07) | BSD-3 | no (libvorbis aoTuV via `cc`) | C compiler | 1.87 | no |
| Opus encode | `opus` → `opusic-sys` | 0.4.0 / 0.7.5 (2026-08) | MIT/Apache-2.0 / BSD-3 | no (libopus 1.6.1) | C compiler + **cmake** | 1.82 | no |
| Opus encode (pure) | `opus-rs` (restsend) | 0.1.39 (2026-10) | BSD-3 | yes | none | n/a | claims heap-free |
| Ogg mux | `ogg` | 0.9.2 (2025-01) | BSD-3 | yes | none | 1.61 | n/a |
| FLAC encode | `flacenc` | 0.5.1 (2025-12) | Apache-2.0 | yes | none | ~1.83 | no |
| Loudness / true peak | `ebur128` | 0.1.10 (2024-10) | MIT | yes | none | 1.60 | after construction, see §3 |

## 3. Crate facts: measuring audio

### 3.1 `ebur128` (port of libebur128)

| | |
|---|---|
| Version | 0.1.10, 2024-10-26; repo `sdroege/ebur128` last commit 2025-02-24, pushed 2025-12-13, 8 open issues. The C original `jiixyj/libebur128` last released 1.2.6 on 2021-02-14; the crate's optional `capi` exports an ABI-compatible `libebur128` 1.2.6. |
| License | MIT. |
| Pure Rust | Yes. Deps `bitflags 1.0`, `smallvec 1.0`, `dasp_sample 0.11`, `dasp_frame 0.11`; `cc` only for the optional `c-tests` feature. |
| MSRV | `rust-version = "1.60"`, edition 2018. |
| Standards | README: "Implements M, S and I modes (EBU Tech 3341)", "loudness range measurement (EBU Tech 3342)", "True peak scanning", "passes all tests defined in EBU Tech 3341 and 3342", "produces the same results as the C library". Gating per ITU-R BS.1770-4: 400 ms blocks, 100 ms hop (`samples_in_100ms * 4` for the first block, then `samples_in_100ms`; `src/ebur128.rs:332, 584-621`). |
| Modes | `Mode::M`, `S`, `I`, `LRA`, `SAMPLE_PEAK`, `TRUE_PEAK` (= `0b00110001`, implies `SAMPLE_PEAK` and `M`), `HISTOGRAM` (`ebur128.rs:60-73`). |
| API | `EbuR128::new(channels: u32, rate: u32, mode) -> Result` with `1..=64` channels and `16..=2 822 400` Hz (`ebur128.rs:258-259, 288-296`); `set_channel_map`, `change_parameters`, `set_max_window`, `set_max_history`, `reset`; `add_frames_{i16,i32,f32,f64}` interleaved and `add_frames_planar_*` (`ebur128.rs:668-703`); `loudness_global()` (integrated, needs `Mode::I`), `loudness_momentary()`, `loudness_shortterm()`, `loudness_window(ms)`, `loudness_range()`, `sample_peak(ch)`, `prev_sample_peak(ch)`, `true_peak(ch)`, `prev_true_peak(ch)`, `relative_threshold()`, `gating_block_count_and_energy()`; `loudness_global_multiple(&[&EbuR128])` for album/pack-level loudness (`ebur128.rs:764-987`). All loudness values are linear-amplitude-free `f64` LUFS; peaks are linear (convert with `20·log10`). |
| True peak | Doc comment: "custom polyphase FIR interpolator… oversample 4x for sample rates < 96000 Hz, 2x for sample rates < 192000 Hz" (`ebur128.rs:926-931`). Feature `precision-true-peak` raises precision at a stated significant performance cost unless `-C target-feature=+fma`. |
| Short input | `loudness_global()` returns `-f64::INFINITY` when no gating block passes the absolute (−70 LUFS) and relative (−10 LU) gates (`history.rs:374`); momentary/short-term return `-inf` before 400 ms / 3 s of input (`ebur128.rs:817, 832, 850`). **An audio file shorter than 400 ms produces no integrated or momentary value at all.** |
| Allocation | `new()` allocates the K-weighted ring buffer sized to the window (400 ms for `M`, 3 s for `S`), peak arrays, filter state, and the history. `add_frames` writes into those buffers and does not allocate, **except** that the block-energy `History::Queue` is a `VecDeque` with initial capacity `min(max, 5000)` and default `max = usize::MAX` (`ebur128.rs:302; history.rs:166-178`), so it grows by one `f64` per 100 ms indefinitely; with `Mode::HISTOGRAM` it is a fixed `Box<[u64; 1000]>` (`history.rs:54-58`) and nothing grows. For a per-bus meter on the audio thread, construct with `Mode::M | Mode::TRUE_PEAK | Mode::HISTOGRAM` (or `set_max_history`) and verify allocation-freedom with a counting allocator in CI; this map did not run that test. |

### 3.2 Others seen on crates.io

| Crate | Ver (date) | License | Notes |
|---|---|---|---|
| `bs1770` (ruuda) | 1.0.0 (2020-09-02) | Apache-2.0 | Pure Rust BS.1770-4 integrated loudness; single release six years ago; description does not mention true peak |
| `ebur128-stream` | 0.2.0 (2026-05-09) | MIT OR Apache-2.0 | "Streaming, zero-allocation EBU R128 loudness measurement in pure Rust", MSRV 1.85, `no_std`-style `alloc`/`std` features, optional `rubato`; one release, 2 k downloads: unproven |
| `broadcast-loudness` | 0.5.0 (2026-10-17) | MIT OR Apache-2.0 | Claims BS.1770-5; MSRV 1.95; 121 downloads |
| `truepeak` | 0.1.1 (2026-10-20) | MIT | "True-peak measurement and lookahead limiting for planar f32"; MSRV 1.96; 25 downloads, five days old |
| `oximedia-normalize` / `oximedia-metering` | 0.2.1 (2026-08-12) | n/a | Loudness normalization/metering suites, low adoption |

**No mature true-peak limiter crate exists.** A crates.io search for "true peak limiter" returns only crates created in the last three months. The 05 master limiter and the per-bus meters are in-house `xj-dsp` code; `ebur128` is the reference meter the lint and tests measure against, not a component of the limiter.

### 3.3 Standards and versions to cite

| Document | Version in force | What it fixes |
|---|---|---|
| ITU-R BS.1770 | **BS.1770-5 (11/2023)**; -4 (10/2015), -3 (08/2012), -2 (03/2011, introduced gating), -1, -0 superseded (itu.int/rec/R-REC-BS.1770) | K-weighting, gating (−70 LUFS absolute, −10 LU relative), true peak via 4× oversampling. `ebur128` documents itself against -4; what -5 changed was not verified in this session |
| EBU R 128 | **v5.0, 21 Nov 2023** (tech.ebu.ch/publications/r128) | Programme Loudness −23.0 LUFS; Maximum True Peak Level −1 dBTP; Loudness Range descriptor; references Tech 3341 (metering), 3342 (LRA), 3343 (production), 3344 (distribution) |
| EBU R 128 s1 (short-form) | v3, Aug 2020 (`tech.ebu.ch/docs/r/r128s1.pdf`, text extracted) | For adverts/promos: Programme Loudness −23.0 LUFS (QC tolerance ±0.2 LU); **Maximum Short-term Loudness shall not exceed −18 LUFS** (+0.2 LU tolerance); True Peak ≤ −1 dBTP (±0.3 dB); "The measure [Loudness Range] is not useful for short-form content… too few data points" |
| EBU R 128 s2 (streaming) | v3, Nov 2023 (`r128s2.pdf`, text extracted) | Stream at −23 LUFS unchanged; where a broadcaster adapts, "the interim value for the Distribution Loudness Level should be in the range of −20.0 to −16.0 LUFS"; true-peak limiting per Tech 3344 in the device |
| Splice Creators audio requirements (`creators.splice.com/audio`; page returns HTTP 401 to direct fetch, text as surfaced by search snippet) | current | "All audio content delivered to Splice must be in WAV format, normalized at a volume level of −1dB peak, with a minimum sample rate of 44.1kHz and a bit depth of 24-bit"; consistent sample rate within a pack; no digital clipping; micro-fades < 3 ms that do not alter the transient or loopability |
| Sony ASWG-R001 (game audio, 2013) | secondary source only (Audio Media International, "Mobile loudness: an adaptive approach") | −24 LUFS for home consoles (aligned with BS.1770-3 broadcast practice; Microsoft and Nintendo later aligned); −18 LUFS mobile target. Primary PDF not retrieved |
| Loopmasters, Ableton Packs, Native Instruments, Noiiz, Sounds.com | — | **No public technical submission specification found.** Loopmasters catalogue is uniformly "24-bit WAV" with key/BPM in file names; "becoming a Loopmasters label" goes through a distributor (Beatport label support). Nothing states a loudness or peak target |

Conclusion for the lint: **no industry standard for sample-pack loudness exists.** The only published commercial rule (Splice) is *peak* normalization to −1 dB. Loudness-based rules exist only for finished programmes (R 128) and games (ASWG). The spec must therefore *define* XJ's target and say so.

## 4. Decoding the preview in the workstation and the browser

- `symphonia` 0.6.1 default features: `opt-simd, all-meta, adpcm, flac, mkv, ogg, pcm, vorbis, wav` (crates.io features JSON). **Ogg Vorbis and FLAC decode with defaults; WAV too.**
- **Opus does not decode.** The README codec table lists "Opus | - | - | `opus` | Yes | `symphonia-codec-opus`" (status dash, no link) and crates.io has no `symphonia-codec-opus` (API returns not found). The 0.6.1 release note about "Opus channel info for ISO BMFF" is demuxer metadata only (`external-stack-research.md` line 36). An Opus preview therefore needs a second decoder path: `opus` (FFI) or restsend `opus-rs`, plus `ogg` demux and hand-written RFC 7845 header parsing (or `opus_mux` 0.1.1).
- Browser `<audio>` support (caniuse feature JSON from `Fyrd/caniuse`, read 2026-10-25): **Ogg Vorbis** — Chrome 4+, Firefox 3.5+, Edge 17+, Safari partial from 14.1 ("Supported on macOS Big Sur 11.3 or later"), full in current Safari; iOS Safari partial 17.4, full 18.4+. **Opus** — Chrome 33+, Firefox 15+, Edge 14+, Safari **still partial** in 27.2 (notes: CAF-only on older macOS, WebM container, "requiring macOS 15.4 Sequoia or later" for Ogg); iOS Safari partial from 11, full 18.4+. **FLAC** — all current browsers. For the 06 "browser audition" front-end, Ogg Vorbis today reaches at least as many browsers as Ogg Opus.

## 5. Preview format: Vorbis as written, or Opus

Facts in favour of keeping `preview/render.ogg` as **Ogg Vorbis** for 2.0:

1. One decoder: `symphonia` with default features already plays it in-app (§4); Opus would add a second codec stack to `xj-content` and to the WASM build.
2. One encoder dependency with no cmake: `vorbis_rs` builds libvorbis with `cc`, which `build.rs` already needs for the engine; `opusic-sys` adds cmake (the engine's own cmake is already present, so this is a smaller point).
3. No muxer to write: `vorbis_rs` emits a complete Ogg stream; Ogg Opus muxing is bespoke RFC 7845 code over an `ogg` encoder the maintainers call less tested.
4. Any sample rate: Vorbis accepts the render's native rate; Opus requires 48 kHz (or 8/12/16/24), so a 44.1 kHz template needs a rubato pass first.
5. Browser reach is equal or better (§4).
6. Cost: 20–40 % more bytes than Opus for equal quality (Xiph's own position since 2013), i.e. roughly 2.4 MB vs 1.5–1.9 MB per two-minute preview, and a C dependency where a pure-Rust Opus encoder is emerging.

Facts in favour of Opus: better coding efficiency; the standardized `R128_TRACK_GAIN` tag; `.opus` is the RFC-recommended extension (so the file would be `render.opus`, not `render.ogg`); a pure-Rust encoder exists (restsend `opus-rs`) but is eight months old, and no pure-Rust decoder path is in symphonia.

**Recommendation to cite:** keep Vorbis for 2.0, name the file `preview/render.ogg`, encode with `vorbis_rs` `QualityVbr { target_quality: 0.4 }` (≈ q4, ~128 kbit/s nominal) at the render's native rate, planar f32 blocks of 1024, with Vorbis comments `TITLE`, `ARTIST`, `LICENSE`, `XJ_PACK`, `XJ_TEMPLATE`, `XJ_ENGINE`, and the measured `XJ_LOUDNESS_LUFS` / `XJ_TRUE_PEAK_DBTP` so the auditioner can level-match previews across packs. Reserve `preview/render.opus` in the manifest schema for when symphonia ships `symphonia-codec-opus` (the feature flag `opus` is already reserved in its README).

## 6. A defensible level-matching rule for the lint

### 6.1 Constraints the measurement must respect

- Integrated loudness (BS.1770 gated) needs at least one 400 ms block; `ebur128` returns `-inf` otherwise (§3.1). 39 demo audios (1.3 %) are shorter than that; 259 (8.6 %) are under 1 s, where the gate sees 1–7 blocks and the "integrated" value is just the loudest 400 ms. R 128 s1 says explicitly that LRA is meaningless for short items; the same statistics argument applies to one-shots.
- Peak is defined for any length. True peak (4× oversampled) is what R 128 and ebur128 measure; Splice's "−1 dB peak" is sample peak in practice.
- The engine does not play files at unity. Effective level = file level + `20·log10(audio.volume × instrument.volume × dubMasterVolume)` (ActiveAudio.java:40-41), and 33 % of demo audios rely on a non-unity `volume`. A rule on the file alone would force authors to re-render files that are already balanced through `volume`; a rule on the effective level preserves the V1 authoring model.
- The mixer sums up to ten buses through a log stage that caps in-range sums at ≤ 0.618 FS and has no limiter (java map §3.6, §3.9). V2 adds a true-peak limiter at the master (05 line 132), so the pack target protects *balance*, not *safety*.
- Mono vs stereo: BS.1770 sums channel energies with weight 1.0 for L/R; a mono file duplicated to both channels at playback measures ~3 dB louder than the file measured as mono. The lint must measure in the layout the DSP will play (duplicate mono to stereo before measuring, or document the 3 dB correction).

### 6.2 Proposed rule (to be written into `xj-base@1` and `xj-content lint`)

| Class | Classification | Measure | Target (proposal) | Tolerance |
|---|---|---|---|---|
| Sustained | `Instrument.mode == Loop`, or `lengthSeconds ≥ 3.0` | BS.1770-4 integrated loudness via `ebur128` `Mode::I`, measured on the file decoded to f32 at native rate, mono duplicated to stereo, then **effective** = measured + `20·log10(audio.volume × instrument.volume)` | **−18 LUFS effective** | ±3 LU |
| One-shot | everything else | Max momentary loudness (`Mode::M`, 400 ms ungated; zero-pad files shorter than 400 ms to 400 ms before measuring and say so) and true peak | No loudness target; **floor** −30 LUFS effective max-momentary (catches silent or broken files) | — |
| Every file | — | True peak via `Mode::TRUE_PEAK` on the raw file at unity | **≤ −1.0 dBTP** | +0.3 dB (R 128 s1's own tolerance) |
| Pack | — | `loudness_global_multiple` over all sustained audio | Reported in the manifest/index as `loudness.sustained_lufs`, not enforced | — |

Why −18 and not −23, −16 or −14: −23 LUFS is a finished-programme target and would discard 5 dB of a layer's headroom for no reason; −14/−16 (streaming) leaves 13–15 dB of peak-to-loudness room at a −1 dBTP ceiling, which percussive one-shots cannot meet and which pushes a ten-bus sum toward the log stage's knee; −18 LUFS is R 128 s1's short-term ceiling and Sony's mobile target, i.e. a published "as loud as a component should get" number, and a four-to-six-layer sum of −18 LUFS parts lands around −8 to −11 LUFS before master dynamics, which is loud but not clipped. **This is a proposal, not a standard; the spec should say so and should bind the number only after `xj-content lint --report` has measured the four demo packs.** If the demo median differs materially from −18, adopt the demo median rounded to the nearest LU and document that provenance.

Measurement method to cite in the lint text: "Loudness per ITU-R BS.1770-4 K-weighting and gating as implemented by the `ebur128` crate (port of libebur128 1.2.6, passes EBU Tech 3341/3342); true peak per BS.1770 Annex 2 with 4× oversampling."

## 7. Capture, bounce and Build-for-engine formats

| Output | Proposal | Grounded recommendation |
|---|---|---|
| Build for engine (`build/*.wav`, Unreal) | "exactly as V1" (05 line 181) | `hound`, `SampleFormat::Int`, 16-bit, 48 kHz, 2 ch — matches `application.properties:2-5` and what the Unreal plugin reads today. Writes `PCMWAVEFORMAT` for this spec |
| Capture / bounce master | WAV sink (05 "Sinks"); 8-hour soak in CI (09 line 61) | `hound` with `Float` 32-bit or `Int` 24-bit (both switch the header to `WAVEFORMATEXTENSIBLE`, `write.rs:377`). **4 GiB cap: 3.1 h (f32) / 4.1 h (24-bit) / 6.2 h (16-bit) at 48 kHz stereo.** The spec must either (a) segment captures at a size or boundary, reinstating V1's `OutputFileMode {CONTINUOUS, SEGMENT, MAIN, MACRO}` snapping (`java-mixer-ship-audio.md` §5.2), or (b) ship an in-house RF64/W64 writer. Pick (a) for 2.0 |
| Per-bus capture | optional (05) | Same writer, one file per bus; ten buses × f32 multiplies the cap problem by ten — segmenting is mandatory here |
| Archival capture | — | `flacenc` 24-bit (float mix limited then converted; `MAX_BITS_PER_SAMPLE 24`), encode on a worker after the WAV is closed, verify by decoding with symphonia; roughly 50–60 % of the WAV size for music |
| Pack preview | `render.ogg` | `vorbis_rs` as §5 |
| HLS (later) | AAC via ffmpeg in V1 (java map §5.3) | No Rust AAC encoder exists in this stack; Opus or Vorbis in fMP4/WebM, or re-introducing ffmpeg, is a Phase-later decision — note it, do not spec it now |

## 8. Implications for V2 specs

1. **Vorbis has no pure-Rust encoder; Opus has no symphonia decoder.** The stated "pure Rust, no ffmpeg" stack (05 line 83) holds for decoding only. `xj-content render` adds a C dependency either way (libvorbis via `cc`, or libopus via cmake) unless the project bets on the eight-month-old restsend `opus-rs`. Say this in the `xj-content` crate spec rather than letting a reviewer discover it.
2. **Keep `preview/render.ogg` as Ogg Vorbis for 2.0** and reserve `render.opus`. Reasons: symphonia default features decode it, Safari/iOS support is at least as good as Ogg Opus, no RFC 7845 muxer needs writing, any sample rate is accepted. Cite §4–5.
3. **`links = "vorbis"` in `aotuv_lancer_vorbis_sys`** means no other crate in the workspace may link libvorbis (e.g. `vorbis-sys`); the dependency policy should forbid `vorbis`, `vorbis-sys`, `vorbis-encoder`, `audiopus`.
4. **Opus input must be 8/12/16/24/48 kHz and frame sizes are fixed** (120…2880 samples at 48 kHz); any Opus path needs rubato in front of it and a 20 ms framing loop. Opus also carries a standard `R128_TRACK_GAIN` tag; Vorbis does not, so the Vorbis preview needs custom `XJ_*` comments.
5. **The lint cannot use integrated LUFS for one-shots.** Below 400 ms `ebur128` returns `-inf`; below ~1 s the value is statistically a single block. Classify audio as sustained vs one-shot (§6.2), use integrated loudness only for sustained, true peak for everything, and define the zero-padding rule for sub-400 ms files in writing.
6. **Measure the effective level, not the file**, because 33 % of demo audio relies on `audio.volume ≠ 1.0` (range 0.0–3.0) and the engine multiplies `audio.volume × instrument.volume × dubMasterVolume` (ActiveAudio.java:40-41). Otherwise the first lint run rejects the Commons' own seed packs.
7. **No sample-pack loudness standard exists.** Splice publishes only "−1 dB peak, 24-bit, ≥ 44.1 kHz WAV"; Loopmasters, Ableton and NI publish nothing. The spec must *define* the target (proposal: −18 LUFS ±3 LU effective for sustained audio, ≤ −1.0 dBTP for all), cite R 128 s1 and ASWG-R001 as the provenance of −18 and −1 dBTP, and calibrate the number against a measurement of the four demo packs before freezing `xj-base@1`.
8. **`ebur128` is the reference meter, not the limiter.** It is MIT, pure Rust, MSRV 1.60, passes Tech 3341/3342, true peak at 4× oversampling, and is allocation-free after construction only in `Mode::HISTOGRAM` (or with `set_max_history`); its last release is two years old but it tracks a C library that is itself finished (1.2.6, 2021). The master true-peak limiter (05 line 132) has no crate to adopt; budget it as in-house DSP with ebur128-based tests ("output true peak ≤ −1.0 dBTP over the 8-hour soak").
9. **hound caps a WAV at 4 GiB with no RF64**, silently wrapping the size in release builds. Capture specs must segment (preferred, reinstating V1's segment/main/macro boundary snapping) or write RF64. The 8-hour CI soak (09 line 61) cannot be a single WAV. hound's MSRV 1.40 is irrelevant to the workspace MSRV (1.90 per `external-stack-research.md`).
10. **Build-for-engine stays 16-bit PCM WAV at 48 kHz stereo** (`application.properties:2-5`); hound writes exactly that header shape, so the Unreal plugin's day-one compatibility promise (05 line 181) is cheap to keep. Switching Build output to 24-bit or > 2 channels silently changes the header to `WAVEFORMATEXTENSIBLE` (`write.rs:377`), which some consumers reject — pin the spec to 16-bit for that artifact.
11. **flacenc is 0.x, int-only, 24-bit max, whole-stream-in-memory by default, multi-threaded by default.** Fine for archival capture on a worker; not a streaming sink. Verify its output with symphonia's FLAC decoder in CI rather than its own experimental `decode` feature.
12. **Mono/stereo measurement layout matters by 3 dB.** The V1 cache keeps mono files mono (java map §4.1); the lint must measure in the playback layout and the DSP spec must say how a mono voice is placed on a stereo bus (duplicate at −3 dB, or at 0 dB) so the lint and the mixer agree.
13. **Browser audition** (06 "Later"): Ogg Vorbis plays in current Safari/iOS (full since iOS 18.4, macOS 11.3+ partial); Ogg Opus is still partial on macOS Safari (15.4+ only). FLAC plays everywhere but is 5–7× the size. If the web front-end ships before symphonia has Opus, Vorbis is the only format that works both in-app and in every browser.
14. **Version pins to cite in specs:** hound 3.5.1; vorbis_rs 0.5.6 (aotuv_lancer_vorbis_sys 0.1.6, ogg_next_sys 0.1.5); opus 0.4.0 / opusic-sys 0.7.5 (libopus 1.6.1); ogg 0.9.2; flacenc 0.5.1; ebur128 0.1.10; symphonia 0.6.1. Standards: ITU-R BS.1770-5 (11/2023), EBU R 128 v5.0 (2023-11), R 128 s1 v3 (2020-08), R 128 s2 v3 (2023-11), RFC 7845.
15. **What this map did not do:** measure any real audio (no demo WAVs or ffmpeg on this machine), run an allocation test on `ebur128::add_frames`, retrieve the ASWG-R001 primary PDF, or verify the `vorbis_rs` default bitrate strategy. Each is a one-hour task that the relevant spec should list as a prerequisite.
