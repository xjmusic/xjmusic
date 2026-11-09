#!/usr/bin/env python3
"""Waterfall scheduler: assigns working-day start/target dates to tasks from
estimate_days + blocked_by, with a resource cap (engineers), then rolls up
epics and milestones. Non-engineering tasks (area in WAIT_AREAS) consume no
engineer and run as elapsed waiting time. Writes schedule.json."""
import json, os, sys, datetime as dt

HERE = os.path.dirname(os.path.abspath(__file__))
ENGINEERS = int(os.environ.get("XJ_ENGINEERS", "2"))
START = dt.date.fromisoformat(os.environ.get("XJ_START", "2026-11-16"))
WAIT_AREAS = {"legal-paperwork", "decision", "show", "commons-content"}
# phase floors: a task may not start before these gate tasks finish (09-roadmap phase dependencies)
FLOORS = {"1": ["p0-dec-go-no-go-gate"], "2": ["p0-dec-go-no-go-gate"], "3": ["p2-gate-ab-listening-record"], "4": ["p2-gate-ab-listening-record"], "5": ["p3-ux-gate-decision"], "6": ["p3-shell-app-skeleton"], "7": ["p5-dec-gate-duet-q13"]}

def add_workdays(d, n):
    while n > 0:
        d += dt.timedelta(days=1)
        if d.weekday() < 5:
            n -= 1
    return d

def next_workday(d):
    while d.weekday() >= 5:
        d += dt.timedelta(days=1)
    return d

def main():
    m = json.load(open(os.path.join(os.path.dirname(HERE), "master.json")))
    PH = {"0":"v2 · Phase 0 · Spikes and decisions","1":"v2 · Phase 1 · Engine 2.0","2":"v2 · Phase 2 · Station core","3":"v2 · Phase 3 · Workstation 2.0, Play mode","4":"v2 · Phase 4 · Commons","5":"v2 · Phase 5 · Sessions","6":"v2 · Phase 6 · Make mode parity","7":"v2 · Phase 7 · The show"}
    for t in m["tasks"]: t["milestone"] = PH[t["phase"]]
    m["milestones"] = [{"title": v} for v in PH.values()]
    tasks = {t["key"]: t for t in m["tasks"]}
    # longest-path priority (critical path first)
    memo = {}
    succ = {k: [] for k in tasks}
    for t in m["tasks"]:
        for b in t.get("blocked_by", []):
            succ[b].append(t["key"])
    def tail(k):
        if k in memo: return memo[k]
        v = tasks[k]["estimate_days"] + max([tail(s) for s in succ[k]] or [0])
        memo[k] = v; return v
    done = {}
    free = [START] * ENGINEERS
    remaining = set(tasks)
    phase_rank = {p: i for i, p in enumerate(["0","1","2","3","4","5","6","7"])}
    while remaining:
        def deps(k): return list(tasks[k].get("blocked_by", [])) + [g for g in FLOORS.get(tasks[k]["phase"], []) if g in tasks and g != k]
        ready = [k for k in remaining if all(b in done for b in deps(k))]
        if not ready:
            raise SystemExit("cycle or missing dependency")
        ready.sort(key=lambda k: (phase_rank.get(tasks[k]["phase"], 9), -tail(k), k))
        k = ready[0]
        t = tasks[k]
        earliest = max([done[b]["target"] for b in deps(k)] or [START])
        earliest = next_workday(earliest)
        if t["area"] in WAIT_AREAS:
            start = earliest
        else:
            i = min(range(ENGINEERS), key=lambda i: max(free[i], earliest))
            start = next_workday(max(free[i], earliest))
        target = add_workdays(start, max(1, int(round(t["estimate_days"]))))
        if t["area"] not in WAIT_AREAS:
            free[i] = target
        done[k] = {"start": start, "target": target}
        remaining.remove(k)
    out = {"tasks": {k: {"start": v["start"].isoformat(), "target": v["target"].isoformat()} for k, v in done.items()}, "epics": {}, "milestones": {}}
    for e in m["epics"]:
        kids = [done[t["key"]] for t in m["tasks"] if t["epic"] == e["key"]]
        if kids:
            out["epics"][e["key"]] = {"start": min(x["start"] for x in kids).isoformat(), "target": max(x["target"] for x in kids).isoformat()}
    for ms in m["milestones"]:
        kids = [done[t["key"]] for t in m["tasks"] if t["milestone"] == ms["title"]]
        if kids:
            out["milestones"][ms["title"]] = {"start": min(x["start"] for x in kids).isoformat(), "due": max(x["target"] for x in kids).isoformat()}
    json.dump(out, open(os.path.join(os.path.dirname(HERE), "schedule.json"), "w"), indent=1, sort_keys=True)
    for title, v in out["milestones"].items():
        print(f"{title}: {v['start']} -> {v['due']}")
    print("tasks scheduled:", len(done))

if __name__ == "__main__":
    main()
