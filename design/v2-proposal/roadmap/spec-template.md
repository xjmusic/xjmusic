# Spec template (issue body). Headings and order are mandatory.

Write in plain, precise prose. Cite real code as `path:line` and proposal sections as `04-engine.md §3`. No placeholders, no "TBD" without a named owner and date. 700–1,800 words for a task; 400–900 for an epic. GitHub-flavored Markdown; tables welcome; fenced code for signatures, structs, JSON, TOML.

## Task body

(First paragraph, no heading) Two or three sentences: what this delivers and why it matters to the vision.

### Context
What exists today (cite files and lines from the maps), what the proposal asks for (cite sections), and what decided questions (Q1–Q14) or risks (R1–R15) this touches.

### Scope
**In:** bullet list. **Out:** bullet list (name the issue that owns each excluded thing when one exists, as `{{ref:<task-key>}}`).

### Design
The concrete plan: names, signatures, structs, enums, file paths to create or modify, algorithms, defaults, error behavior, threading rules, data formats. This is the section an engineer implements from. Use the contract register's names verbatim.

### Acceptance criteria
A checklist (`- [ ]`) of observable, testable statements. Include the demonstration where the proposal gives one.

### Test plan
Unit, integration, CI, and manual tests; fixtures; platforms; what runs on every PR versus on a schedule.

### Dependencies and interfaces
What this consumes from its blockers (`{{ref:<key>}}`) and exactly what it exposes to the issues that depend on it. Mention the contract it implements or extends (C ABI, DSP command set, session protocol, content format, mapping TOML, capture format).

### Risks and open points
Specific risks with a mitigation each; open points with an owner and the phase in which they close.

### References
Proposal sections, code `path:line`, external sources with URLs.

### New names introduced
Bullet list of any identifier this spec coins that the contract register lacks (or "none").

## Epic body

(First paragraph) The capability this epic delivers and the phase exit it serves.

### Goal and exit criteria
### Why this order (how the tasks chain, what the first and last task prove)
### Interfaces this epic owns or consumes
### Risks
### References
