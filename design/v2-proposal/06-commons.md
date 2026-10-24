# 06 · The Commons

The shared drive, rebuilt. In 2002 it was one hard disk between two samplers. In V2 it is a pack format, a show manifest, a registry, a license and a quality bar. The engineering is modest. The curation is the hard part, and this document says so.

## Three tiers of library

| Tier | What it holds | Who controls it | Where it lives |
|---|---|---|---|
| **Personal** | Your projects and packs | You | Your disk |
| **Show** | The pack set a session agrees to play from, pinned to exact versions | The session's conductor | Every station at the venue, byte-identical |
| **Commons** | Published packs anyone may fetch | Maintainers and contributors | A registry, mirrored, usable offline |

The vision's "common library that is part of that show" is the Show tier. The Commons is where shows draw from. The personal tier is where the artist's own material lives, and it is what they bring to the venue.

## Pack format

A pack is a directory, or a zip of one, with this layout:

```
My-Deep-House-Kit.xjpack/
├── pack.toml                 # manifest
├── README.md                 # what it is, how to use it, credits
├── content/
│   ├── libraries/<Library>/<Program or Instrument>/<name>.json   # exactly the V1 shape
│   └── templates/<Template>/<name>.json                           # optional
├── audio/
│   └── <blake3-hash>.<ext>   # content-addressed audio
└── preview/
    └── render.ogg            # optional 60–120 s render for audition
```

The `content/` tree is the V1 project format, unchanged. **A V1 project is a pack with an implicit manifest**, and the workstation opens either without asking.

`pack.toml`:

```toml
[pack]
name = "deep-house-kit"
version = "1.2.0"
title = "Deep House Kit"
authors = ["Jane Doe <jane@example.com>"]
license = "CC-BY-4.0"            # SPDX identifier
description = "Four-on-the-floor drums, sub bass, warm pads. 118–126 bpm."
tags = ["house", "drums", "bass", "pads"]
engine = ">=2.0.0"               # minimum engine
taxonomy = "xj-base@1"           # which technical conventions the memes follow

[taxonomy]
palettes = ["ELEMENT[EARTH, WIND, FIRE, WATER]"]   # this pack's exclusive categories

[dependencies]
xj-starter-drums = "^1.0"        # packs this one binds to or extends

[provenance]
source = "https://github.com/jane/deep-house-kit"
```

Rules:

- Audio files are named by the BLAKE3 hash of their bytes. The original filename lives in the `InstrumentAudio.waveformKey` field as today, and the hash is added as a new optional `contentHash` field. Two packs that ship the same sample share it in the cache.
- Entity ids remain UUIDs. Each entity gains optional `originPack` and `originVersion` fields so that a forked program can be traced to its source and so that the same program appearing in two packs is recognized as the same program.
- A pack may depend on other packs. A template in one pack may bind programs and instruments from a dependency.
- Packs are immutable once published. A new version is a new pack.

## Shows

A show is what a session loads. It is a manifest plus a lockfile:

```toml
# show.toml
[show]
name = "Boulder Night 3"
templates = ["deep-house-kit/Main", "ambient-commons/Interlude"]

[packs]
deep-house-kit = "^1.2"
ambient-commons = "^0.9"
xj-starter-drums = "^1.0"

[session]
taxonomy = "xj-base@1"
commit_horizon_beats = 1.0
follow_hold_max_bars = 4
```

```toml
# show.lock  (generated, committed)
[[pack]]
name = "deep-house-kit"
version = "1.2.0"
hash = "blake3:9f3c…"
```

The lockfile pins every pack to an exact version and hash. When a station joins a session, the conductor sends the lock; the follower verifies it has identical content before it is allowed to follow. Byte-identical content across stations is what makes layer claims and shared main programs mean the same thing on every machine.

## Registry

Modeled on a sparse package index, because the problem is the same: name-and-version lookup, immutable artifacts, mirrors, offline use. The hosting decision is worked through in [11-resolutions.md](11-resolutions.md), Q7.

- **Index:** a git repository, `xjmusic/commons`, holding a static JSON tree: `/index/<name>.json` lists versions, hashes and manifests. Published through GitHub Pages or served raw; readable with no account. Git history is the audit log, and a mirror of the index is a `git clone`.
- **Publishing is a pull request** to that repository. CI verifies the signature, runs the lint, renders the preview, uploads the artifact, and only then merges the index entry. This is the same path code contributions take, which is what lets content contributions earn equity on the same terms (Q5).
- **Artifacts:** zipped packs on Cloudflare R2 behind a custom domain, addressed by content hash. R2 charges no egress, which for a registry is the whole bill: at 100 GB of packs the storage cost is about $1.50 a month. Because artifacts are hash-addressed, any mirror (a venue's NAS, a USB stick, another bucket) is interchangeable and the workstation tries mirrors in order.
- **Signing:** packs are signed with the author's key (ed25519, `minisign` style). The index records the public key on first publish; later versions must be signed by the same key or by a maintainer key. No server-side accounts in 2.0.
- **Offline:** the workstation keeps a local mirror of every pack it has fetched. A venue needs no internet once its show is fetched. `xj-content fetch --show show.toml` on one machine and a USB stick is a supported workflow, because that is what venues are actually like.
- **Later:** a web front-end over the index with search and browser audition via the WebAssembly build mentioned in [03-architecture.md](03-architecture.md).

## Licensing

Three different things are licensed and the proposal keeps them apart.

1. **The software** keeps the current XJ music license: free for projects under the stated budget, commercial license above it, patent license separate. Nothing here changes it.
2. **Commons content** carries an SPDX license per pack. The default is **CC BY 4.0**, worked through in [11-resolutions.md](11-resolutions.md), Q6: its definition of sharing explicitly includes public performance, and its attribution rule ("in any reasonable manner based on the medium, means, and context") is written for exactly what a live set or a game credits screen can provide. The **starter pack is CC0** so that the first thing a new user makes carries no obligations, and any pack may choose CC0. Share-alike and non-commercial variants are not accepted into the Commons: the first would contaminate game soundtracks, the second would forbid a paid show. The workstation shows the license before a pack is added to a show, and it makes attribution automatic by writing a `CREDITS.md` into every capture, bounce, build and exported pack, listing each pack's authors, license and link from its manifest. On publish, the submitter signs a short declaration that they own or have cleared every sample in the pack, and the registry records who declared.
3. **The output audio** is already granted to the user by the software license provided they own the rights to the inputs. Pack licenses are what make that provision true for Commons content.

Whether contributing content earns equity on the same terms as contributing code is the company's decision. The second pass recommends that it does, with no new mechanism: a pack is published through a pull request that carries its hours log and passes the same review and CI gate as code ([11-resolutions.md](11-resolutions.md), Q5).

## The quality bar

A registry full of mediocre packs is worse than no registry. Publishing to the Commons requires:

**Automated (`xj-content lint`):**
- Every `Event`-mode and `Chord`-mode instrument either covers its declared note range with sampled tones or sets `allowPitchShift` with a stated range.
- Every `Loop`-mode audio declares `tempo` and `loopBeats`; its length agrees with them within a tolerance.
- Every program and instrument carries memes that are either technical conventions from `xj-base` or members of a palette category the pack declares; every declared category is used by at least one piece of content.
- Audio is level-matched to a documented target so packs can be combined without re-balancing.
- The pack renders ten minutes on each of its templates with zero engine error messages.
- The manifest validates; the license is a known SPDX identifier; every audio file's hash matches.

**Human:**
- Two maintainers listen to the preview render and to a ten-minute live render with intensity swept, and both approve.
- The README says what the pack is for and what it is not for.

## A shared taxonomy

Packs combine only if their memes mean the same thing. V1 lets every template define its own `memeTaxonomy`, and the four demo projects show what artists do with that freedom: each invents its own exclusive palette (seasons, elements, story beats, cities) and none overlaps with another. Underneath those palettes they share technical conventions that nobody wrote down: `85BPM`-style numeric memes for recorded tempo, `3NOTE` and `4NOTE` for voicing density, `SMALL` and `LARGE` for instrument size, `BREAK` on sequence bindings, and the engine's own grammar of `!X`, `X!` and `$X`. The analysis is in [11-resolutions.md](11-resolutions.md), Q4.

V2 therefore does two things rather than inventing mood categories:

1. **`xj-base@1` codifies the technical conventions**: tempo, density, size, structure, a small open list of harmonic-character memes, and the meme grammar. It is a short document packs cite in their manifest.
2. **Palettes stay per pack and are declared.** `pack.toml` gains a `[taxonomy]` table listing the pack's exclusive categories in the engine's own `CATEGORY[A, B, C]` syntax. A show's template taxonomy is the union of its packs' categories, which the engine already supports: a Deep House pack and a Lofi pack in one show give the performer two palette rows, ELEMENT and CITY, and nothing conflicts.

The lint checks what the demos show drifts in practice: a declared category whose memes appear on no content fails, as does content using a palette-like meme no declared category contains, as does the same word appearing in two packs' categories with different meanings.

## Seeding the Commons

The four demo projects in `xjmusic/xjmusic-demos` become the first four packs, tagged against the base taxonomy, with previews rendered. One of them, or a new pack built from their best material, becomes the **starter pack** bundled with the workstation so that the first launch has something to play. The starter pack is also the fixture for the engine's follow-mode and determinism tests, which keeps it honest.

## Tooling

A `xj-content` CLI, which is the same crate the workstation uses:

| Command | Does |
|---|---|
| `xj-content pack <project>` | Builds a pack from a V1 project: hashes audio, writes the manifest, lints |
| `xj-content lint <pack>` | Runs the automated quality bar |
| `xj-content verify <pack>` | Checks signatures and hashes |
| `xj-content publish <pack>` | Signs, uploads the artifact, and opens the index pull request |
| `xj-content fetch --show <show.toml>` | Resolves, downloads, writes `show.lock` |
| `xj-content lock <show.toml>` | Re-resolves and rewrites the lockfile |
| `xj-content render <pack> --template <t> --minutes 2` | Renders the preview |

## What it replaces

Cloning the demos repository from inside the workstation, the Build step's role as the only way to make content playable, and ffmpeg via javacpp. Decoding moves to `symphonia`; the cache is keyed by content hash and device format, so a sample decoded once is never decoded again on that machine.
