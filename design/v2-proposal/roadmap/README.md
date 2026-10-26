# Roadmap: the proposal as a waterfall of issues

This folder turns the V2 proposal into work. It holds the decomposition the GitHub tracker was generated from, the rules the specifications were written to, and the tool that keeps the tracker in step with them.

| File | What it is |
|---|---|
| [master.json](master.json) | The decomposition: every epic and task with its phase, area, size, estimate, summary, proposal references and `blocked_by` edges. The acyclic graph of those edges is the waterfall. |
| [schedule.json](schedule.json) | Start and target dates per task, epic and phase, from a list schedule with two engineers, working days only, phase floors at the decision gates, starting 2026-11-16. Planning numbers, not commitments. |
| [issue-map.json](issue-map.json) | Each decomposition key mapped to its GitHub issue number and URL. |
| [spec-status.json](spec-status.json) | How far each issue's specification has come: `placeholder`, `draft`, `reviewed` or `consistent`. |
| [frame.md](frame.md) | The fixed frame every decomposition had to fit: governing rules, phases, exit demonstrations, areas, issue conventions. |
| [contract-register.md](contract-register.md) | The names the specifications must use verbatim: C ABI, foundation record, crates, DSP command set, session messages, mapping, Commons formats, versions. |
| [spec-template.md](spec-template.md) | The mandatory shape of every issue body. |
| [decomposition-rationale.md](decomposition-rationale.md) | How the breakdown was chosen (three independent decompositions, a judge panel, synthesis, three audit rounds) and the judgment calls it made. |
| [tools/](tools/) | `gh_sync.py` publishes and syncs the tracker; `schedule.py` computes the dates; two validators check the decomposition and the specifications. |

The research the specifications cite is in [../research/](../research/): grounded maps of the engine, the Java workstation, the demos, the external crates and protocols, each with file and line references against this repository at 1.9.6.

## Where the work is tracked

- **Project:** [v2.0 Collaborative Musical Instrument](https://github.com/orgs/xjmusic/projects/7), with Roadmap, Waterfall and Board views.
- **Milestones:** one per phase, `v2 · Phase N · Name`.
- **Issues:** an umbrella roadmap issue; one epic per capability (issue type Feature, label `epic`) as its sub-issues; one task per unit of work (issue type Task) as sub-issues of its epic. Every task carries formal *blocked by* links to the work it consumes.
- **Labels:** `v2` on everything, `area/<area>` for the owning crate or discipline, plus `spike` and `decision`.
- **Issue fields:** Start date, Target date, Effort and Priority, set from the schedule.

## How the tracker stays current

The issues are the home of the specifications. `tools/gh_sync.py` is idempotent: a full pass creates whatever is missing, re-renders each issue body from its specification, links sub-issues and dependencies, and sets the issue fields. `--bodies` pushes only changed bodies, and `--epic <key>` limits a pass to one epic. Issues were published early, as work in progress, and are updated as each specification is written, reviewed and checked for consistency, so the tracker moves forward in visible steps even if the work that produces it is interrupted.

Once people start working the issues, the tracker is the source of truth for status and detail. Change an issue first; regenerate this folder's data only when the shape of the plan changes.
