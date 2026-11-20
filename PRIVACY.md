# Privacy: usage reporting in the XJ music workstation

The **XJ music workstation** (the Java desktop app in [`workstation/`](workstation/)) can send a few anonymous usage numbers to **Signal**, Outright Mental's own first-party, aggregate-only analytics service. This page says exactly what is sent, where it goes, what is never sent, and how to say no. The privacy policy is at [xjmusic.com/privacy#workstation](https://xjmusic.com/privacy#workstation).

**In short**

- **It asks first.** The first time the workstation opens, it asks *Help improve the XJ music workstation?*, with **Yes, share** and **No thanks** side by side and equal. **Nothing is sent unless you say yes.** A no is remembered and never asked again.
- **It is a few numbers, once per session.** When you quit, the workstation sends one summary of a few dozen bytes: how long it was open, which views and modes you used, which major features you touched, whether music played, and whether anything failed. Every value is a number.
- **It is never about you or your work.** No name, account or identifier of any kind. No project, file, folder, template, program, instrument or meme names. No music or audio. Nothing you type.
- **You can see it and change your mind.** **Settings › Privacy** shows the exact bytes of the next summary and what each part means, the last five that were sent, and the switch *Share anonymous usage numbers*.
- **You can turn it off for good**, whatever was answered: set the environment variable `XJ_SIGNAL=off`.
- **Only the workstation.** The C++ engine ([`engine/`](engine/)) and the Unreal plugin never report anything: they run inside other people's games.

## What the workstation asks

Once the main window is up, before any project is open, the workstation asks:

> **Help improve the XJ music workstation?**
>
> If you say yes, the workstation sends XJ Music a few anonymous numbers each time you quit: how long it was open, which views you used, which features you touched, whether music played, and whether anything failed. Never your name, an account or any identifier, never your projects, files or music, and nothing you type. They go to Signal, Outright Mental's own analytics service, which keeps only daily totals. Nothing is sent unless you say yes. You can see exactly what is sent, and change your mind any time, in Settings › Privacy.
>
> **[ Yes, share ]  [ No thanks ]**
>
> *What exactly is sent?* · *Privacy policy*

- The two answers are the same size and the same weight, and neither is the default.
- *What exactly is sent?* opens Settings › Privacy, with the live summary.
- Closing the window without answering is no answer. Nothing is sent, and the question comes back at the next launch.
- Before you answer, the session is counted **in memory only**. If you say no, or never answer, it is thrown away when you quit. Nothing is written to your disk before you answer.

## Exactly what is sent

One summary per session, posted once, when the workstation quits. A typical one is about 115 bytes. This is the full list, matching the catalog in [`.signal/catalog.yaml`](.signal/catalog.yaml). Nothing outside this list can be sent: the summary is a fixed binary layout of numbers, and Signal has no field that could carry text.

Times are sent as one of 16 ranges ("buckets"), never as exact values. Counts are per session.

### The summary's header

| Field | What it is |
|---|---|
| Catalog digest | 8 bytes that say which version of this list the summary follows |
| Nonce | 8 random bytes, new for every summary, so a summary sent twice is counted once. Signal keeps it 48 hours, only to drop duplicates. It is not an identifier: the next summary has a new one |
| Local day | Which day it is where you are, as days since 2026-01-01 |
| Install age | Days since the workstation was first used on this computer, capped at 400 |
| Flags | Whether this is the first summary today, this week, this month, and from a new install. This is how Signal counts daily, weekly and monthly users without any identifier |
| Channel | `download` (installed from GitHub Releases or xjmusic.com) or `source` (run from a checkout) |
| Platform | Windows, macOS or Linux |
| Device class | One of four tiers, worked out on your computer from the number of CPU cores and the amount of memory. Never a model, a GPU or any hardware name |
| Language | `en` if your computer is set to English, otherwise `other` |
| Traits | One bit: whether a demo project was opened on the install's first day |

### Signal's core numbers (every Signal stream has these)

| Id | Name | What it is |
|---|---|---|
| 1 | `sessions` | 1: one session ended |
| 2 | `playtime_s` | Seconds the workstation was open |
| 3 | `session_length` | The same, as a range |
| 4 | `core_milestones` | Steps reached for the first time on this install: launched, first click or key, main window up, music started playing, a preview played for a minute or more, music started playing a second time, Settings opened, quit cleanly |
| 5 | `time_to_core_loop` | First session only: seconds from launch to the first music playing, as a range |
| 6 | `exit_node` | The view the session ended on (from the list of views below) |
| 7 | `return_from_node` | The view the previous session ended on, sent with the next session |
| 8 | `return_gap` | Days since the previous session, as a range |
| 9 | `unclean_prev_exit` | The previous session did not end cleanly: the workstation crashed or was killed. This is how crashes are counted, with no crash report |
| 11 | `input_device` | Whether the session was used with a mouse and keyboard |
| 15 | `consent_events` | Counted once, in the session where you say yes |
| 16 | `session_outcome` | What kind of session it was, worked out on your computer: bounced (under a minute, no music), explored (no preview played a minute), played (a preview played a minute or more) or deep (three or more such previews). Signal's fifth kind, stuck, is never used by the workstation |

Signal's core also has ids 10 (frame rate), 12 (a "how is it going?" pulse), 13 (a store review request) and 14 (a feedback form). **The workstation never records any of them**: it has no pulse, no review request and no feedback form, so those records are never in its summaries.

### The workstation's own numbers

| Id | Name | What it is | Answers |
|---|---|---|---|
| 32 | `views` | Moves from one view to the next (from the list of views below), and how many times each move was made | Q1 |
| 33 | `time_in_mode` | Seconds spent in each mode, Content, Templates and Preview, as a range for each mode used | Q1 |
| 34 | `program_editor_mode` | Times the program editor was switched into Bind or Edit mode | Q1 |
| 35 | `getting_started` | Steps reached for the first time on this install: created a project, opened a project, opened a demo, edited content, started a preview, music playing, built a project, exported a template | Q2 |
| 36 | `time_to_music` | Seconds from launch to the first music playing, as a range, in a session where music played | Q2 |
| 37 | `features` | Times each major feature was used: new project, open project, open demo, save, save as, build, export template, start preview, macro override, meme override, intensity override, import audio, show logs | Q4 |
| 38 | `preview_control_mode` | Previews started, by control mode: Auto, Macro or Taxonomy | Q4 |
| 39 | `preview_length` | Seconds each preview played music for, as a range | Q3 |
| 40 | `failures` | Times a preview, a project task (opening, creating, saving, exporting) or a build ended in failure. Only the count: never the error, the message or the file | Q5 |
| 41 | `errors_shown` | Error dialogs the workstation showed. Only the count: never what they said | Q5 |

**The views**, the only places a move can be between: `start` (no project open), `content_library_browser`, `content_library_editor`, `content_program_browser`, `content_program_editor`, `content_instrument_browser`, `content_instrument_editor`, `content_instrument_audio_editor`, `template_browser`, `template_editor`, `fabrication_timeline`, `fabrication_segment`. These are the workstation's own screens, never the names of what is in them.

Each number answers a question a maintainer acts on, in [`.signal/questions.yaml`](.signal/questions.yaml): Q1 which modes and views are used; Q2 how quickly a new user gets to music; Q3 how long sessions and previews are, and whether people come back; Q4 which features are used; Q5 how often it crashes or fails; Q6 how many say yes.

## What is never sent

- Your name, email, account, or any identifier: no install id, device id, machine name, user name, serial number or advertising id.
- Your projects: no project, file or folder names or paths, and no template, library, program, instrument, sequence, pattern, meme or chord names.
- Your music: no audio, no notes, no content of any kind.
- Anything you type.
- Logs, error messages, stack traces or crash reports.
- Your IP address is never stored (see below).
- Your precise location, your hardware model, or your exact screen, CPU or GPU.

## Where it goes

- To **Signal**, Outright Mental's own analytics service, run by the same founder as XJ Music, at `https://in.signal.outright.io/v1/s`. It is first-party: **no third-party analytics company receives any of it**, and nothing is sold or shared.
- Signal runs on Amazon Web Services in the United States. As with any web request, its edge sees your IP address in order to answer; it adds the **country** the request came from to the day's totals, and **does not store the IP address**.
- Signal **adds each summary into daily totals and keeps only the totals**. The summary itself is not kept. A total is never tied to a person or a computer, and any count under 10 is never shown.
- The key in the request, `xjmusic-workstation.e1a9f6512e6bbdf57309469d`, names the workstation's stream. It is the same in every copy of the workstation and is public: it says which app the numbers are for, not who sent them.

## What is kept on your computer

Nothing, until you answer. After that, two small files in the workstation's own folder:

| System | Folder |
|---|---|
| Windows | `%APPDATA%\XJ Music\workstation\signal` |
| macOS | `~/Library/Application Support/XJ Music/workstation/signal` |
| Linux | `$XDG_DATA_HOME/xjmusic/workstation/signal` (usually `~/.local/share/xjmusic/workstation/signal`) |

- `state.json`: your answer; after a yes, the running totals the next summary's flags are worked out from (first day used, last day seen, which steps were ever reached); and the last five summaries sent, so Settings › Privacy can show them.
- `spool.bin`: after a yes, summaries waiting to be sent, if the last send failed (at most 32, and 8 KB). They are sent at the next launch.

If you say no, the only thing kept is the no itself, so the question is not asked again. **Reset local stats** in Settings › Privacy deletes the rest and keeps your answer.

## How to say no, change your mind, or turn it off for good

- **Say no** at the first-run question. Nothing is sent, ever, and you are not asked again.
- **Change your mind** any time in **Settings › Privacy** (Project › Settings › Privacy, or Help › Usage Reporting Settings...): switch *Share anonymous usage numbers* on or off. Switching it off throws away the session so far and anything waiting to be sent, at once, and nothing more leaves.
- **Turn it off for good on a computer**, whatever was answered, for CI, a lab, or because you want to be sure. Any one of these stops it before it starts: nothing is counted, written or sent, and the question is never shown.
  - the environment variable `XJ_SIGNAL=off`;
  - the system property `-Dxj.signal=off`;
  - Signal's own switches: the argument `--no-telemetry`, the environment variable `SIGNAL_DISABLE=1`, or an empty file named `signal-disable` in your configuration folder (`%APPDATA%` on Windows, `~/.config` or `~/Library/Application Support` on macOS and Linux, or `/etc`).

  The workstation's own tests run with `-Dxj.signal=off`.
- **Help › Privacy & Usage Reporting** opens the privacy policy.

## Where the code is

All of it is in this repository, to read and to build:

- [`.signal/`](.signal/): the catalog Signal reads the numbers against. [`catalog.yaml`](.signal/catalog.yaml) is the list above; [`questions.yaml`](.signal/questions.yaml) says what each number is for; [`consent.yaml`](.signal/consent.yaml) says it is opt-in; [`copy/en.yaml`](.signal/copy/en.yaml) is the wording of the question and the panel.
- [`workstation/src/main/java/io/xj/gui/usage/`](workstation/src/main/java/io/xj/gui/usage/): the workstation's side.
  - [`UsageService.java`](workstation/src/main/java/io/xj/gui/usage/UsageService.java): starts usage reporting, reads the hard off switch, and sends at quit.
  - [`UsageObserver.java`](workstation/src/main/java/io/xj/gui/usage/UsageObserver.java): what the workstation's state is counted as.
  - [`UsageQuestion.java`](workstation/src/main/java/io/xj/gui/usage/UsageQuestion.java): the first-run question.
  - [`UsagePrivacyPanel.java`](workstation/src/main/java/io/xj/gui/usage/UsagePrivacyPanel.java): Settings › Privacy.
  - [`XjSignalCatalog.java`](workstation/src/main/java/io/xj/gui/usage/XjSignalCatalog.java): the catalog's constants, generated from `.signal/` by Signal's command line.
  - The features are counted where they happen: `ProjectServiceImpl`, `FabricationControlsController`, `SettingsModalController`.
- [`workstation/vendor/signal-core/`](workstation/vendor/signal-core/): Signal's Java core, vendored, unchanged. It adds the session's numbers up in memory, makes the summary's bytes, and sends it. It has no dependencies. Its golden fixtures are vendored beside it, and [`SignalCoreGoldenTest`](workstation/src/test/java/io/xj/gui/usage/SignalCoreGoldenTest.java) holds it to them byte for byte.
- [`.github/workflows/workstation_distro.yml`](.github/workflows/workstation_distro.yml): a release is stopped unless Signal knows this catalog, so no build ships sending numbers Signal would drop.

## Changing what is sent

The catalog only grows: a number, once Signal knows it, keeps its meaning for ever, and one that is no longer wanted is retired, never reused. Any change to what is sent is a change to `.signal/` in this repository, in the open, and to this page. A new kind of data comes with a new `consent_version` in [`.signal/consent.yaml`](.signal/consent.yaml), which asks everyone the question again, including those who said yes.
