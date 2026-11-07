#!/usr/bin/env python3
"""Generate design/v2-proposal/12-work-breakdown.md from the tracker.

  python3 -I tools/work_breakdown.py        # writes ../12-work-breakdown.md

The chapter is a view of master.json, schedule.json and issue-map.json: the phases
with their epics and tasks in dependency order, the epic dependency graph, the
critical path and the two-engineer schedule. Regenerate it whenever the tracker
changes; do not edit it by hand.
"""
import collections, json, os

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
PROPOSAL = os.path.dirname(ROOT)
REPO_URL = "https://github.com/xjmusic/xjmusic"
PHASES = [("0", "Spikes and decisions"), ("1", "Engine 2.0"), ("2", "Station core"), ("3", "Workstation 2.0, Play mode"), ("4", "Commons"), ("5", "Sessions"), ("6", "Make mode parity"), ("7", "The show")]
BANDS = {"0": "1.5–2.5", "1": "3–4", "2": "4–5", "3": "5–7", "4": "2–3", "5": "3–4", "6": "5–8", "7": "not engineering"}


def topo(tasks):
    keys = {t["key"]: t for t in tasks}
    indeg = {k: 0 for k in keys}
    out = {k: [] for k in keys}
    for t in tasks:
        for b in t.get("blocked_by", []):
            if b in keys:
                indeg[t["key"]] += 1
                out[b].append(t["key"])
    order, ready = [], sorted(k for k, d in indeg.items() if d == 0)
    while ready:
        k = ready.pop(0)
        order.append(k)
        for n in out[k]:
            indeg[n] -= 1
            if indeg[n] == 0:
                ready.append(n)
                ready.sort()
    return [keys[k] for k in order] + [t for t in tasks if t["key"] not in set(order)]


def main():
    m = json.load(open(os.path.join(ROOT, "master.json")))
    imap = json.load(open(os.path.join(ROOT, "issue-map.json")))
    sched = json.load(open(os.path.join(ROOT, "schedule.json")))
    tk = {t["key"]: t for t in m["tasks"]}
    by_epic = collections.defaultdict(list)
    for t in topo(m["tasks"]):
        by_epic[t["epic"]].append(t)

    def ref(k):
        return f"[#{imap[k]['number']}]({REPO_URL}/issues/{imap[k]['number']})" if k in imap else f"`{k}`"

    def num(k):
        return f"#{imap[k]['number']}" if k in imap else f"`{k}`"

    # critical path: longest chain by estimate, unlimited parallelism
    memo = {}

    def lp(k):
        if k in memo:
            return memo[k]
        best = (tk[k]["estimate_days"], [k])
        for b in tk[k].get("blocked_by", []):
            d, p = lp(b)
            if d + tk[k]["estimate_days"] > best[0]:
                best = (d + tk[k]["estimate_days"], p + [k])
        memo[k] = best
        return best

    end = max(tk, key=lambda k: lp(k)[0])
    crit_days, crit = lp(end)
    total_days = sum(t["estimate_days"] for t in m["tasks"])

    L = []
    L.append("# 12 · Work breakdown")
    L.append("")
    L.append("*Generated from [roadmap/](roadmap/) by `roadmap/tools/work_breakdown.py`. Do not edit by hand; change `master.json` and regenerate.*")
    L.append("")
    L.append("The plan of [09-roadmap.md](09-roadmap.md) as a waterfall of issues: one milestone per phase, one epic per capability, one task per unit of work, with formal *blocked by* links. The issues live in [project #7](https://github.com/orgs/xjmusic/projects/7) under the umbrella " + ref("umbrella") + ". Every task body carries a full specification (context, scope, design, acceptance criteria, test plan, dependencies, risks, references).")
    L.append("")
    L.append(f"**Shape:** {len(m['epics'])} epics, {len(m['tasks'])} tasks, {total_days:g} engineer-days of estimates. Two engineers from {min(v['start'] for v in sched['milestones'].values())} to {max(v['due'] for v in sched['milestones'].values())} on the list schedule below. Estimates are planning numbers with the ±50% uncertainty 09 states, re-estimated at each decision gate by the people doing the work.")
    L.append("")
    L.append("## Phases at a glance")
    L.append("")
    L.append("| Phase | Epics | Tasks | Engineer-days | About engineer-months | 09's band | Scheduled |")
    L.append("|---|---|---|---|---|---|---|")
    for p, name in PHASES:
        epics = [e for e in m["epics"] if e["phase"] == p]
        tasks = [t for t in m["tasks"] if t["phase"] == p]
        days = sum(t["estimate_days"] for t in tasks)
        msd = sched["milestones"].get(f"v2 · Phase {p} · {name}", {})
        L.append(f"| {p} · {name} | {len(epics)} | {len(tasks)} | {days:g} | {days / 21:.1f} | {BANDS[p]} | {msd.get('start', '?')} → {msd.get('due', '?')} |")
    L.append("")
    L.append("Engineer-months divide days by 21. Phase 1 exceeds 09's band because the grounded research found port regressions, seven randomness sites and ABI hardening the proposal did not know about; Phase 6 undershoots it because the shared editing infrastructure absorbs most of the Java editors (see [roadmap/decomposition-rationale.md](roadmap/decomposition-rationale.md)).")
    L.append("")
    L.append("## Critical path")
    L.append("")
    L.append(f"The longest chain of estimates, with unlimited parallelism, is {crit_days:g} engineer-days over {len(crit)} tasks:")
    L.append("")
    cum = 0
    L.append("| Day | Task |")
    L.append("|---|---|")
    for k in crit:
        cum += tk[k]["estimate_days"]
        L.append(f"| {cum:g} | {ref(k)} {tk[k]['title']} |")
    L.append("")
    L.append("## Epic dependency graph")
    L.append("")
    L.append("Each box is an epic; an arrow means *blocked by*. Edges are computed from the tasks' edges (an edge that would close a cycle between two epics whose tasks interleave is left out).")
    L.append("")
    L.append("```mermaid")
    L.append("flowchart LR")
    for p, name in PHASES:
        L.append(f"  subgraph P{p}[\"Phase {p} · {name}\"]")
        for e in m["epics"]:
            if e["phase"] == p:
                L.append(f"    {e['key'].replace('-', '_')}[\"{num(e['key'])} {e['title'][:40]}\"]")
        L.append("  end")
    for e in m["epics"]:
        for b in e.get("blocked_by", []):
            L.append(f"  {b.replace('-', '_')} --> {e['key'].replace('-', '_')}")
    L.append("```")
    for p, name in PHASES:
        L.append("")
        L.append(f"## Phase {p} · {name}")
        msd = sched["milestones"].get(f"v2 · Phase {p} · {name}", {})
        L.append("")
        L.append(f"Scheduled {msd.get('start', '?')} → {msd.get('due', '?')}. Exit criteria are in [09-roadmap.md](09-roadmap.md).")
        for e in m["epics"]:
            if e["phase"] != p:
                continue
            tasks = by_epic[e["key"]]
            L.append("")
            L.append(f"### {ref(e['key'])} {e['title']}")
            L.append("")
            bb = ", ".join(num(b) for b in e.get("blocked_by", [])) or "nothing"
            L.append(f"Area `{e['area']}` · {len(tasks)} tasks · {sum(t['estimate_days'] for t in tasks):g} engineer-days · blocked by {bb}.")
            L.append("")
            L.append("| Task | Size | Days | Blocked by | Scheduled |")
            L.append("|---|---|---|---|---|")
            for t in tasks:
                d = sched["tasks"].get(t["key"], {})
                bbt = ", ".join(num(b) for b in t.get("blocked_by", [])) or "—"
                L.append(f"| {ref(t['key'])} {t['title']} | {t['size']} | {t['estimate_days']:g} | {bbt} | {d.get('start', '?')} → {d.get('target', '?')} |")
    L.append("")
    L.append("## How this chapter is kept current")
    L.append("")
    L.append("`master.json` is the decomposition, `schedule.json` the list schedule (`tools/schedule.py`), `issue-map.json` the key-to-issue map, and `spec-status.json` the review state of each specification. `tools/gh_sync.py` publishes the tracker; `tools/pull_specs.py` recovers the specifications from the live issues; `tools/umbrella.py` renders the umbrella issue; this script renders this chapter. The issues are the source of truth for status and detail; this folder is regenerated when the shape of the plan changes.")
    L.append("")
    with open(os.path.join(PROPOSAL, "12-work-breakdown.md"), "w") as f:
        f.write("\n".join(L))
    print(f"wrote 12-work-breakdown.md: {len(L)} lines; critical path {crit_days:g} days over {len(crit)} tasks")


if __name__ == "__main__":
    main()
