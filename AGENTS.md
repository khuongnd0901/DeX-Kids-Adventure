
# DeX Kids Adventure agent guide

Main Agent owns architecture, task acceptance, integration and release. Specialized agents may own Game, Geo, Story, Android/DeX and QA, with distinct file scopes.

## Every new session
1. Read .agent/STATUS.md.
2. Read .agent/TASKS.md and the relevant active ExecPlan only.
3. Read relevant code and architectural decisions, not every document.
4. Implement a measurable slice, verify it and append evidence.
5. Update TASKS/STATUS before ending work.

## Working protocol
- Record changes as T-### with owner, dependencies, acceptance gates and evidence.
- Complex or cross-module changes require a plan in .agent/plans/active/.
- Branch naming: feat/T-###-description. Avoid overlapping file edits.
- Commit only verified source/document changes; no fake benchmark or PASS markers.
- Transition plan to completed only after acceptance and tests.
- Do not access or change DeX-Assistant/Dex-Assistant-UI without explicit integration task and a separate reviewed PR.
- Keep original device owner/Accessibility, voice path and navigation untouched.
- Do not invent locations, historical facts, device measurements or asset licenses.
- Do not silently send precise GPS, audio or children's data to any server.
- Never auto fallback DeX external display to phone for a child session without parent authorization.

## Definition of Done
The code compiles on intended target, tests pass, evidence is recorded, architecture/docs reflect changes, and all task acceptance criteria are met. If equipment is missing mark BLOCKED/NOT VERIFIED, not DONE.

Use .agent/DECISIONS.md for durable decisions.
