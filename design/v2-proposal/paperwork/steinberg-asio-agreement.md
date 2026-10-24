# Steinberg ASIO SDK agreement: what it is and what to do

Companion to the Link request, for the week-one paperwork task in [09-roadmap.md](../09-roadmap.md). Resolves the platform question Q10 in [11-resolutions.md](../11-resolutions.md#q10--minimum-platforms). Unlike Link, this is not an email to draft; it is a form to sign and a process to start early because it is reported to be slow.

## Why XJ wants ASIO at all

On Windows, `cpal` gives us WASAPI out of the box, and WASAPI in exclusive mode reaches latencies most performers will accept. ASIO is still what musicians with audio interfaces expect to see in a device list, and some interfaces behave better through their vendor's ASIO driver than through WASAPI. The plan treats ASIO as a feature flag, not a requirement: WASAPI shared is the default, WASAPI exclusive is an option, and ASIO is enabled in official builds only once the paperwork below is complete ([05-workstation.md](../05-workstation.md), technology stack).

## The license facts, as found

- **Dual license since October 2025.** Steinberg announced that ASIO is available under GPLv3 as well as the existing proprietary license ([Heise](https://heise.de/-10963451), [LibreArts](https://librearts.org/2025/11/steinberg-relicenses-vst3-and-asio/)). The GPLv3 option is no more usable for XJ than Link's GPLv2 is, for the same reason: the XJ license is not GPL-compatible.
- **The proprietary license is free of charge but must be signed and countersigned.** The SDK license "is free of charge, however a proprietary SDK license agreement has to be signed" ([Wikipedia, ASIO](https://en.wikipedia.org/wiki/Audio_Stream_Input/Output)). A developer on Steinberg's forum summarizes the agreement's own text: before publishing a proprietary application, you need a copy of the agreement signed by Steinberg ([forum thread](https://forums.steinberg.net/t/where-do-i-submit-the-proprietary-asio-sdk-license-agreement/1035889)).
- **Where to submit.** In that thread a Steinberg developer answered the "where do I submit it" question with a link to a proprietary-SDK page under `steinberg.net/developers/`. The page could not be read by the tooling used for this proposal (it renders client-side), so the exact route should be confirmed by opening it in a browser; the agreement PDF inside the SDK package also states where to return it.
- **Turnaround is slow.** A forum post from October 2026 describes an agreement submitted in September still unanswered weeks later ([forum thread](https://forums.steinberg.net/t/proprietary-asio-licence-for-an-individual-developer-before-business-registration/1042871)). Plan for months, not days.
- **The SDK is not redistributable.** `cpal`'s `asio` feature (through `asio-sys`) downloads the SDK from Steinberg at build time unless `CPAL_ASIO_DIR` points at a local copy ([asio-sys](https://docs.rs/crate/asio-sys/latest)). The XJ repository must never vendor the SDK, and CI must fetch it rather than cache it in the repository.

## Checklist

1. **Download the SDK** from Steinberg's ASIO SDK page (steinberg.net/asiosdk). Read the licensing agreement PDF inside the package before anything else; it is the authoritative text and this note is not.
2. **Fill in the agreement.** Licensee: XJ Music Inc. Product: XJ music workstation (and the headless XJ station runtime, if the form asks for product names individually). Contact: a company address that will still be read in six months. Describe the use as "audio output from a desktop music application via a statically linked Rust binary using bindings generated from the SDK headers."
3. **Sign and submit** by the route the proprietary-SDK page specifies. Keep a dated copy of what was sent.
4. **Record the submission date** in Q10 of [11-resolutions.md](../11-resolutions.md) and set a reminder to follow up at six weeks if nothing has arrived, using the developer forum if email goes unanswered; that is where staff have answered these questions before.
5. **When the countersigned copy arrives,** file it with the company's contracts, record the date in Q10, and enable the `asio` feature in the official Windows build. Until then, official builds ship WASAPI only.

## Engineering stance while waiting

- Developers may build locally with `cargo build --features asio` for their own testing; the agreement governs development as well as distribution, so read its development provisions, but internal use without distribution is the ordinary case the SDK exists for.
- No build containing ASIO leaves the company, including beta builds, until the countersigned copy exists. This is the same rule as for Link.
- If the countersignature never arrives, the fallback is Audacity's historical position: official builds without ASIO, and users who want it build from source themselves. WASAPI exclusive mode is the supported low-latency path in that case.

## Questions to ask Steinberg only if the agreement text leaves them open

- Whether a statically linked Rust application using bindgen-generated bindings is within the agreement's definition of an application using the SDK (it should be; the bindings are generated from the SDK headers at build time and the SDK itself is not shipped).
- Whether the open-source publication of XJ's own source code, which does not include the SDK, raises any issue. It should not, since nothing of Steinberg's is redistributed.
- Whether the "Third-Party Modified Version" clause in XJ's license has any bearing (someone forking XJ and building with ASIO would need their own agreement; the note in the XJ README should say so once ASIO ships).
