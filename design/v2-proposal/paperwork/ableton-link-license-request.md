# Ableton Link proprietary license request

Draft email for the week-one paperwork task in [09-roadmap.md](../09-roadmap.md), resolving Q3 in [11-resolutions.md](../11-resolutions.md#q3--ableton-link-licensing). Ableton's README directs proprietary-application requests to link-devs@ableton.com. Fill in the bracketed fields, remove this header, and send from a company address.

---

**To:** link-devs@ableton.com
**Subject:** Proprietary Link license request for XJ music workstation (open codebase, non-GPL license)

Hello Link team,

I'm Nick Charney Kaye, founder of XJ Music Inc. and inventor on U.S. Patent 10,446,126, which covers the XJ music engine. I'd like to request a proprietary license to incorporate Ableton Link into our software, as your README invites.

**What XJ music is.** XJ music is an adaptive music engine and workstation. Composers prepare partial compositions and sampled instruments, and the engine fabricates a continuous, never-repeating performance from them in real time, choosing programs, chords, voicings and samples segment by segment. Today it ships as a desktop workstation for composers and a C++ runtime embedded in video games (there is an Unreal Engine plugin). The codebase is public at https://github.com/xjmusic/xjmusic.

**Why Link.** We are planning the next major version around live performance. The design has several XJ "stations" in one venue sharing tempo, beat phase and chord changes, with each performer playing their own material against that shared foundation. Link is the obvious way to put every station, and every Link-enabled instrument in the room, on one beat grid. The Akai MPC line matters to us in particular: XJ began in 2001 as a live set on two MPC 2000s sharing a hard drive, and the MPC One, Live and X all speak Link today. Being able to lock an XJ station to a performer's MPC closes a loop that is twenty-five years old for us.

**How we would integrate it.** Through your `abl_link` C extension, via the `rusty_link` Rust wrapper, statically linked into a Rust application that embeds our C++ engine. Link would provide tempo, beat phase and start/stop; we carry our own harmonic timeline on a separate protocol. We would support Link in the desktop workstation and in a headless runtime used in venue racks (the same code, without a GUI).

**Why we need the proprietary license.** Our software license (https://github.com/xjmusic/xjmusic/blob/main/LICENSE.txt) is source-available and free for projects under a budget threshold, with commercial terms above it and separate patent terms. It is not GPL-compatible, so the GPLv2+ option does not work for us, and we would not want to ship a derivative under terms we cannot honor.

**What I'd like to know:**

1. Whether a proprietary license is available for an application distributed under a source-available, non-GPL license like ours, and on what terms.
2. Whether there is a fee, and if so what it is.
3. Any attribution, trademark or "Link-enabled" branding requirements we should plan for in the UI and documentation.
4. Whether one license covers both the desktop application and the headless runtime, and whether it covers builds for Windows, macOS and Linux.
5. Whether we may use Link under the GPL terms for internal, non-distributed prototyping while the license is in progress. Our plan has a two-week spike in the first phase that tests `rusty_link` against an MPC and Ableton Live on a LAN.

Our planning documents for this version are public in the repository under `design/v2-proposal/` if any of the detail above is useful; the session design is in `07-session-protocol.md`.

Thank you for making Link available to developers. It is the reason "every instrument in the room on one clock" is a design assumption for us rather than a research problem.

Best regards,

Nick Charney Kaye
Founder, XJ Music Inc.
[email]
[phone, optional]
https://xjmusic.com

---

## Notes for sending

- **From address.** Use a company address rather than a personal one; `licensing@xjmusic.com` already appears in the README as the licensing contact and is a natural choice.
- **What to record when the answer arrives.** Add the outcome to Q3 in [11-resolutions.md](../11-resolutions.md): terms, fee, attribution requirements, and which builds are covered. If Ableton declines or the terms are unworkable, the fallback is already designed: the `session` implementation of `BeatClock` plus MIDI clock out, described in [07-session-protocol.md](../07-session-protocol.md).
- **Before shipping anything.** Nothing containing Link may be distributed, including beta or test builds to people outside the company, until the license is in hand. The Phase 0 spike may run on company machines under the GPL terms because no distribution occurs; question 5 above asks Ableton to confirm that reading.
- **Attribution.** Expect to be asked to show the Link logo and name in a settings panel and in documentation; Ableton has a Link brand guideline. Leave room for it on the transport strip in the performance surface.
