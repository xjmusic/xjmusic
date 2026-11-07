#!/usr/bin/env python3
"""Render the umbrella roadmap issue body from the tracker and, with --push, publish it.

  python3 -I tools/umbrella.py           # print the body
  python3 -I tools/umbrella.py --push    # PATCH the umbrella issue named in issue-map.json

Reads master.json, schedule.json and issue-map.json beside this folder. Milestone
numbers are looked up from GitHub by title (they are not stored in the tracker).
"""
import collections, json, os, subprocess, sys, tempfile

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
REPO = os.environ.get("XJ_REPO", "xjmusic/xjmusic")
PHASES = [("0", "Spikes and decisions"), ("1", "Engine 2.0"), ("2", "Station core"), ("3", "Workstation 2.0, Play mode"), ("4", "Commons"), ("5", "Sessions"), ("6", "Make mode parity"), ("7", "The show")]
EXIT = {
    "0": "every spike row has a recorded decision; the C ABI spike seeds `xj-engine-sys`. Gate: toolkit, stretch library, Link path, go/no-go on the plan.",
    "1": "the two-engine follow-mode test is green on three platforms; `engine-v2.0.0` is tagged with ABI 1.0.0; the Unreal plugin builds against it unchanged.",
    "2": "`xj-station play` on a V1 demo with a MIDI controller sounds at least as good as the Java workstation, runs 8 hours without a dropout, and `replay` reproduces a capture byte for byte. Gate: does the station core sound good enough to build a product on?",
    "3": "first launch to sound in under 10 s on a 2019 laptop; a musician new to XJ performs 20 minutes with a pad controller after the tour; five usability sessions filed as issues. Gate: do musicians want to play it? Release: workstation 2.0.0.",
    "4": "a fresh install fetches a show from the registry, writes a lockfile, and two machines with the same lockfile load byte-identical content; an external contributor publishes a pack that passes lint. Release: workstation 2.1.",
    "5": "two workstations and one MPC on Link play a 30-minute duet with two handoffs and no dropout, captured on both stations and published. Gate: did the duet work in a real room? Release: workstation 2.2.",
    "6": "every editing operation in the Java workstation has an equivalent in the Rust workstation; the Java folder is archived with a pointer. Releases: workstation 2.3 and 2.4.",
    "7": "a real night with three or more stations, artists who did not build the software, and an audience; a recording that stands beside *Live in Boulder*.",
}
GATES = {"0": "p0-dec-go-no-go-gate", "2": "p2-gate-ab-listening-record", "3": "p3-ux-gate-decision", "5": "p5-dec-gate-duet-q13"}
RELEASES = {"1": ["p1-rel-engine-2-0-0-tag"], "2": ["p2-rel-cli-release-checksums"], "3": ["p3-pkg-release-workstation-2-0-0"], "4": ["p4-rel-workstation-2-1"], "5": ["p5-rel-workstation-2-2"], "6": ["p6-rel-workstation-2-3-instrument-editor", "p6-par-java-archive-and-roundtrip-retire"]}
AMEND = {"1": "p1-docs-amend-proposal-phase-1", "2": "p2-docs-amend-proposal-phase-2", "3": "p3-docs-amend-proposal-phase-3", "4": "p4-docs-amend-proposal-phase-4", "5": "p5-docs-amend-proposal-phase-5"}


def milestone_numbers():
    r = subprocess.run(["gh", "api", f"repos/{REPO}/milestones?state=all&per_page=100", "--jq", ".[] | [.number, .title] | @tsv"], capture_output=True, text=True)
    out = {}
    for line in r.stdout.splitlines():
        num, title = line.split("\t", 1)
        out[title] = int(num)
    return out


def render():
    m = json.load(open(os.path.join(ROOT, "master.json")))
    imap = json.load(open(os.path.join(ROOT, "issue-map.json")))
    sched = json.load(open(os.path.join(ROOT, "schedule.json")))
    ms = milestone_numbers()
    tasks_by_epic = collections.defaultdict(list)
    for t in m["tasks"]:
        tasks_by_epic[t["epic"]].append(t)

    def ref(k):
        return f"#{imap[k]['number']}" if k in imap else f"`{k}`"

    total_days = sum(t["estimate_days"] for t in m["tasks"])
    out = []
    out.append("Roadmap index for the [V2 proposal](https://github.com/xjmusic/xjmusic/tree/main/design/v2-proposal) and [project #7](https://github.com/orgs/xjmusic/projects/7): the plan that takes XJ music from version 1.9.6 to a Rust performance workstation on the C++ engine, a shared content Commons, and multi-station sessions. Epics are sub-issues of this issue; tasks are sub-issues of their epics; formal *blocked by* links form the waterfall. One milestone per phase carries the phase's exit demonstration and due date.")
    out.append("")
    out.append(f"**Shape:** {len(m['epics'])} epics · {len(m['tasks'])} tasks · {total_days:g} engineer-days of estimates · schedule for two engineers from {min(v['start'] for v in sched['milestones'].values())} to {max(v['due'] for v in sched['milestones'].values())} (planning numbers, ±50%, re-estimated at each gate, not commitments).")
    out.append("")
    out.append("## How to read the tracker")
    out.append("")
    out.append("- **Milestones** are the phases of [09-roadmap.md](https://github.com/xjmusic/xjmusic/blob/main/design/v2-proposal/09-roadmap.md); a phase is done when its exit demonstration below has happened, not when its issues are closed.")
    out.append("- **Epics** (type Feature, label `epic`) group one capability per phase. Each epic body lists its tasks in dependency order. **Tasks** (type Task) are 1 to 12 engineer-days each and carry a full specification: context, scope, design, acceptance criteria, test plan, dependencies, risks, references.")
    out.append("- **Labels:** `v2` on everything; `area/<crate or discipline>`; `spike` for time-boxed experiments; `decision` for decision records. **Fields:** Start date, Target date, Effort and Priority come from the schedule.")
    out.append("- **Spec status** is the first line of every body: 🚧 placeholder, 📝 draft, 🔍 reviewed, ✅ consistent. Edit issues freely; when the shape of the plan changes, regenerate the tracker from `design/v2-proposal/roadmap/`.")
    out.append("- **Decision gates** close a phase and may re-plan the next one: " + ", ".join(f"after Phase {p} ({ref(k)})" for p, k in GATES.items()) + ". After each gate, one task writes the phase's recorded departures back into the proposal chapters, so the proposal stays the single description of the plan: " + ", ".join(ref(k) for k in AMEND.values()) + ".")
    out.append("")
    out.append("## Phases")
    for p, name in PHASES:
        epics = [e for e in m["epics"] if e["phase"] == p]
        ntasks = sum(len(tasks_by_epic[e["key"]]) for e in epics)
        days = sum(t["estimate_days"] for e in epics for t in tasks_by_epic[e["key"]])
        title = f"v2 · Phase {p} · {name}"
        msd = sched["milestones"].get(title, {})
        mslink = f"[Milestone](https://github.com/{REPO}/milestone/{ms[title]})" if title in ms else "Milestone"
        out.append("")
        out.append(f"### Phase {p} · {name}")
        out.append("")
        out.append(f"{mslink} · {len(epics)} epics · {ntasks} tasks · {days:g} engineer-days · scheduled {msd.get('start', '?')} → {msd.get('due', '?')}")
        out.append("")
        out.append(f"**Exit:** {EXIT[p]}")
        extra = []
        if p in GATES:
            extra.append(f"gate {ref(GATES[p])}")
        if p in RELEASES:
            extra.append("release " + ", ".join(ref(k) for k in RELEASES[p]))
        if extra:
            out.append("")
            out.append("**Closing tasks:** " + "; ".join(extra) + ".")
        out.append("")
        out.append("| Epic | Area | Tasks | Days | Blocked by |")
        out.append("|---|---|---|---|---|")
        for e in epics:
            bb = ", ".join(ref(b) for b in e.get("blocked_by", [])) or "—"
            out.append(f"| {ref(e['key'])} {e['title']} | `{e['area']}` | {len(tasks_by_epic[e['key']])} | {sum(t['estimate_days'] for t in tasks_by_epic[e['key']]):g} | {bb} |")
    out.append("")
    out.append("## Phase dependencies")
    out.append("")
    out.append("```mermaid")
    out.append("flowchart LR")
    out.append("  P0[Phase 0<br/>Spikes] --> P1[Phase 1<br/>Engine 2.0]")
    out.append("  P0 --> P2[Phase 2<br/>Station core]")
    out.append("  P1 --> P2")
    out.append("  P2 --> P3[Phase 3<br/>Play mode]")
    out.append("  P2 --> P4[Phase 4<br/>Commons]")
    out.append("  P3 --> P4")
    out.append("  P1 --> P5[Phase 5<br/>Sessions]")
    out.append("  P2 --> P5")
    out.append("  P3 --> P5")
    out.append("  P4 --> P5")
    out.append("  P3 --> P6[Phase 6<br/>Make parity]")
    out.append("  P4 --> P7[Phase 7<br/>The show]")
    out.append("  P5 --> P7")
    out.append("```")
    out.append("")
    out.append("The P3→P4 and P4→P5 edges are real in the task graph (the Commons UI ships after 2.0.0, and 2.2 ships after 2.1) and are written back into 09-roadmap.md by " + ref("p0-docs-amend-proposal-after-spikes") + ".")
    out.append("")
    out.append("## Versions along the way")
    out.append("")
    out.append("| Tag | When | Owner |")
    out.append("|---|---|---|")
    out.append(f"| `engine-v2.0.0` with C ABI 1.0.0 | end of Phase 1 | {ref('p1-rel-engine-2-0-0-tag')} |")
    out.append(f"| `workstation-v2.0.0-alpha.N` (headless `xj-station` CLI) | Phase 2 | {ref('p2-rel-cli-release-checksums')} |")
    out.append(f"| `workstation-v2.0.0` (Play and Ship modes) | end of Phase 3 | {ref('p3-pkg-release-workstation-2-0-0')} |")
    out.append(f"| `workstation-v2.1.0` (Commons) | end of Phase 4 | {ref('p4-rel-workstation-2-1')} |")
    out.append(f"| `workstation-v2.2.0` (Sessions) | end of Phase 5 | {ref('p5-rel-workstation-2-2')} |")
    out.append(f"| `workstation-v2.3.0` (instrument and audio editor), `workstation-v2.4.0` (Make mode complete, Java archived) | Phase 6 | {ref('p6-rel-workstation-2-3-instrument-editor')}, {ref('p6-par-java-archive-and-roundtrip-retire')} |")
    out.append(f"| Java workstation 1.9.x | maintenance line from the end of Phase 1 | {ref('p1-java-maintenance-mode')} |")
    out.append("")
    out.append("<sub>Generated from `design/v2-proposal/roadmap/` (master.json, schedule.json, issue-map.json) by `tools/umbrella.py`. Regenerate when the plan changes.</sub>")
    return "\n".join(out) + "\n"


def main():
    body = render()
    if "--push" in sys.argv:
        imap = json.load(open(os.path.join(ROOT, "issue-map.json")))
        num = imap["umbrella"]["number"]
        fd, tmp = tempfile.mkstemp(suffix=".json")
        json.dump({"body": body}, os.fdopen(fd, "w"))
        r = subprocess.run(["gh", "api", "-X", "PATCH", f"repos/{REPO}/issues/{num}", "--input", tmp, "--jq", ".number"], capture_output=True, text=True)
        os.unlink(tmp)
        print("pushed" if r.returncode == 0 else f"failed: {r.stderr.strip()}", f"#{num}")
    else:
        sys.stdout.write(body)


if __name__ == "__main__":
    main()
