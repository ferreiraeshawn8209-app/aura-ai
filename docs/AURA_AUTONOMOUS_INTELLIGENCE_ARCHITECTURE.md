# Aura Autonomous Intelligence Architecture
Status: Architecture proposal for review. This document does not claim implementation or a successful build.
Created: 2026-11-20

## 1. Product objective

Evolve the existing Aura/T1000 Android assistant into one persistent, proactive personal AI system. Keep `aura-ai` as the product hub. Reuse the existing agent orchestrator, learning/skill system, finance/trading packages, coding package, Room persistence, and voice layer where they pass audit and tests. Do not create a second assistant or duplicate existing engines.

Aura should be able to research, plan, schedule, execute permitted work, verify outcomes, learn from results, and resume after app/process restarts. The Android app is the owner's voice, control, notification, and approval surface. Long-running autonomy must ultimately run in a durable backend/PC service, not depend on an Android process staying alive indefinitely.

## 2. Standing mission order

1. **Build legitimate, measurable income.** Discover customer problems and business opportunities; create and sell useful services/products; track collected revenue, direct costs, operating costs, refunds and net profit. Daily income is an aspiration, never a guarantee.
2. **Research human longevity and consciousness.** Monitor credible biomedical research and carefully investigate unusual claims. Grade evidence, preserve uncertainty and conflicting results, and turn well-supported findings into safe, practical summaries. Never present immortality, consciousness transfer, afterlife claims, or experimental treatments as proven.
3. **Improve Aura's capabilities in service of missions 1 and 2.** Research, write code in an isolated workspace, run tests, learn verified reusable procedures, document changes, and prepare reviewable releases.
4. **Maintain continuity.** Preserve mission state, memory, task history, sources, experiments, decisions, approvals and handover notes across sessions and devices.

## 3. System map

```text
OWNER
  | voice / text / approvals / priorities
  v
AURA ANDROID APP  <---- notifications, status, approvals ----+
  | chat, voice, settings, audit viewer                       |
  v                                                          |
AURA CORE RUNTIME (durable PC or cloud service)                |
  |                                                          |
  +-- Mission Manager --> Goal Planner --> Task Scheduler     |
  |         |                   |              |              |
  |         +-------------------+--------------+              |
  |                            v                             |
  |                    Agent Orchestrator                     |
  |               understand -> plan -> act -> verify         |
  |                            |                             |
  |       +--------------------+---------------------+        |
  |       |                    |                     |        |
  |  INCOME ENGINE       LONGEVITY ENGINE       SELF-DEV ENGINE|
  |  opportunities       research monitor       code workspace|
  |  customer discovery  evidence grading       tests/builds  |
  |  service/product     practical summaries    PR proposals  |
  |  experiments         claim tracking          rollback      |
  |       |                    |                     |        |
  |       +--------------------+---------------------+        |
  |                            |                             |
  |                    TRADING INTELLIGENCE                   |
  |              data -> hypotheses -> backtests              |
  |                    -> paper trading                       |
  |                    -> approval-gated live mode            |
  |                            |                             |
  +----------------------------+-----------------------------+
                               v
             TOOL REGISTRY + POLICY / APPROVAL GATEWAY
        scoped credentials | budgets | rate limits | audit log
                               |
           GitHub / research sources / business tools /
           market-data providers / approved integrations
                               |
                               v
           PERSISTENT MEMORY + EVIDENCE + EVENT STORE
```

## 4. Core architectural components

### 4.1 Mission manager and scheduler
- Store standing missions, measurable objectives, priorities, due dates, dependencies, status and stop conditions.
- Generate small tasks from missions; schedule recurring research and reviews.
- Support pause, resume, cancel, retry, escalation and owner reprioritisation.
- Use durable jobs and idempotency keys. Do not rely on in-memory timers or an Android process remaining alive.
- Every task has an owner (Aura or owner), risk class, required capabilities, budget ceiling, timeout, success criteria and evidence requirement.

### 4.2 Agent orchestration
- Reuse and audit the existing `AgentOrchestrator`, `AgentPipeline`, `ToolRegistry`, `LearningEngine`, memory and skills.
- Standard task loop: understand -> plan -> check policy -> request approval if required -> execute -> independently verify -> record evidence -> learn.
- A model response is not proof of execution. Tools return structured results; verification checks observable state.
- Limit retries, tool calls, task duration, concurrency and spend. Prevent recursive agent loops and duplicate jobs.

### 4.3 Income engine (first commercial priority)
- Opportunity research: customer pain, market size, competitors, willingness-to-pay signals, startup cost, time-to-first-sale, delivery burden, legal/privacy obligations and confidence.
- Initial service experiments: AI enquiry/quotation/follow-up automation, small-business websites and workflow automation, and content/marketing operations.
- Use BeatKulture as an optional first internal pilot, with owner-controlled access to its data and no unsolicited customer contact.
- Build a customer pipeline: prospect -> qualified -> proposal -> approved -> delivered -> invoiced -> paid -> reviewed.
- Track cash actually received separately from leads, bookings, invoices and projected revenue.
- Profit reporting: gross revenue, refunds, direct costs, operating costs, tax reserve estimate (clearly labelled, not tax advice), net operating result and cash position.
- No deceptive marketing, spam, fabricated testimonials, scraped private data, undisclosed affiliate promotion or promises of guaranteed returns.
- Aura may research, draft proposals, create prototypes and prepare campaigns autonomously. Owner approval is required before sending external campaigns, accepting contracts, spending money, accessing customer accounts or making binding commitments.

### 4.4 Trading intelligence (research and simulation first)
- Separate market data, research, strategy definitions, backtesting, paper portfolio, performance analytics and broker execution adapters.
- Initially study equities, FX, indices and crypto as separate markets; account for different market hours, fees, liquidity, spreads, slippage and regulations.
- Required metrics: net return after costs, benchmark-relative return, win rate, average win/loss, expectancy, profit factor, max drawdown, exposure, turnover and performance by regime.
- Prevent look-ahead bias, survivorship bias, data leakage and overfitting. Use out-of-sample and walk-forward tests.
- Default mode is research/backtest/paper trading. Live execution is disabled unless the owner explicitly enables a defined broker, account, instrument set, risk budget and time window. Each live order requires the configured authorisation policy. No leverage or borrowing by default.
- Hard limits: per-trade risk, daily loss, max drawdown, position size, open exposure, loss-streak pause and emergency kill switch. Fail closed on missing/stale data or broken risk checks.
- Never describe a backtest or paper result as guaranteed future profit.

### 4.5 Longevity and consciousness research
- Monitor peer-reviewed journals, clinical-trial registries, government health agencies, patents and primary historical/archaeological sources.
- Maintain a claim ledger with source, publication date, study type, sample size, methods, conflicts of interest, limitations, replication status and evidence grade.
- Evidence grades: established; strong; promising/preliminary; speculative; unverified; contradicted.
- Separate biological ageing, regenerative medicine, cryonics, brain preservation, digital replicas/whole-brain emulation, consciousness studies, near-death experiences, historical traditions and extraordinary entity/alien claims.
- Distinguish primary evidence from testimonials, marketing, folklore and inference. Keep rejected claims searchable with reasons.
- Lifestyle guidance must be conservative, evidence-based and risk-aware; not diagnosis or personalised treatment. Refer medical decisions to qualified clinicians. Never run experiments on the owner or others.

### 4.6 Self-development and coding
- Use a dedicated, isolated workspace/branch per change. Never edit the running production app or main branch directly.
- Workflow: inspect -> issue/spec -> plan -> code -> unit tests -> integration tests -> static/security checks -> build -> evidence report -> pull request.
- Aura may autonomously fix issues in its sandbox and open PRs. It cannot merge, release, deploy, expand permissions, change its own approval policy, or alter audit logging without owner approval.
- No self-replication, persistence outside approved infrastructure, privilege escalation, secret extraction or disabling security controls.
- Keep changes small, reversible and attributable. Maintain rollback instructions and known-good build artifacts.

### 4.7 Memory and continuity
Persist structured records for:
- mission and goal state;
- task queue, execution attempts and verified outcomes;
- decisions and rationale;
- skills and their measured success rates;
- research claims and source metadata;
- business opportunities, customers (only with lawful access), experiments and actual revenue events;
- trading strategies, datasets, backtests and paper trades;
- code changes, test/build results, PRs and releases;
- approvals, denials, budgets, policy decisions and audit events.

Memory must support correction, retention limits, export and deletion workflows. Sensitive credentials are stored in a secure secret store, never in prompts, logs or ordinary memory.

## 5. Authority model

| Level | Aura may do without asking | Examples |
|---|---|---|
| A: Read and reason | Yes, within configured sources | Research, analysis, planning, draft proposals, market-data analysis |
| B: Reversible sandbox work | Yes, within budgets and scope | Local prototypes, tests, paper trading, isolated code changes |
| C: External or consequential actions | Explicit owner approval | Contact customers, publish campaigns, send messages, spend, contracts, live orders, financial transfers |
| D: Production/security authority | Explicit owner approval for each defined change | Merge to main, release APK, deploy backend, grant permissions, change policies, delete production data |

Approval must be specific, time-bounded and scoped to the action, amount, account, environment and risk. Silence is not consent. The approval gateway must be enforced in code, not only in prompts.

## 6. Runtime and deployment topology

- **Android:** Compose UI, chat/voice, wake-word controls, owner dashboard, task approvals, notifications and audit view.
- **Core service:** durable scheduler, orchestrator, tools, memory API and mission engines on the owner's PC or a hosted service. PC sleep/offline state must be visible; cloud fallback is configurable.
- **Storage:** encrypted database and backups, explicit retention policies, migrations and restore tests.
- **Model routing:** configurable local Ollama and cloud providers with health checks, timeout, cost ceilings, privacy classification and fallback. Never send secrets or sensitive records to a model provider without explicit configuration.
- **Integrations:** narrow scoped adapters, least-privilege credentials, per-tool policy and audit records.
- **Observability:** health, task status, model/provider usage, costs, errors, verification results and alerts.

Android WorkManager can handle bounded scheduled work and retries, but it is not the sole always-on autonomy runtime.

## 7. Implementation roadmap and gates

### Phase 0: Baseline and consolidation
- Inventory current main, candidate branches, open PRs, build workflows, databases, tools and permissions.
- Compare `feature/self-development-agent`, `feature/trading-engine`, `feat/skills-and-improvements`, voice integration branches and current main.
- Record what is implemented, tested, incomplete, duplicated or unsafe. Run the current unit tests and debug build where CI permits.
- Gate: verified architecture map and baseline report; no unreviewed merge.

### Phase 1: Persistent mission foundation
- Reuse existing agent orchestration and memory.
- Add or consolidate mission, goal, durable task queue, task event, approval request and audit records.
- Add scheduler/worker, pause/resume/retry, idempotency and status reporting.
- Gate: tasks survive app/service restart; permissions fail closed; repeat tasks do not duplicate side effects.

### Phase 2: First income vertical slice
- Opportunity tracker and evidence-backed scoring.
- Draft service package, quote/proposal and customer workflow.
- BeatKulture pilot only after owner chooses the scope and provides authorised data.
- Gate: one end-to-end test opportunity, proposal, owner approval, delivery record and reconciled actual revenue/cost reporting. No fabricated earnings.

### Phase 3: Longevity research vertical slice
- Source registry, scheduled research jobs, claim ledger and evidence grading.
- Gate: citations and dates retained; unsupported claims clearly labelled; summaries reproducible.

### Phase 4: Trading research vertical slice
- Market-data adapter, strategy registry, backtest engine, paper portfolio and risk analytics.
- Gate: tests for fees/slippage, leakage, stale data, drawdown stops and reproducible out-of-sample reports. Live mode remains disabled.

### Phase 5: Bounded self-development
- Isolated coding workspace, branch/PR automation, test/build runner and signed audit trail.
- Gate: no direct production mutation; all generated changes reviewed; rollback verified.

### Phase 6: Voice, devices and continuity
- Stabilise Android voice, interruption handling, wake phrase and cloud/local fallback.
- Connect Android to Core with authenticated, scoped APIs.
- Gate: voice actions verified end-to-end; PC/cloud availability and offline behavior visible.

### Phase 7: Controlled expansion
- Add more revenue channels and integrations only when earlier experiments show positive measured value.
- Add optional physical embodiment only as a separate future project if explicitly requested.

## 8. Definition of done

A feature is not complete because code exists or an AI says it worked. Completion requires:
1. A reproducible test or build result.
2. Observable verification of the intended behavior.
3. Failure and permission-denial tests.
4. Budget, privacy and security review appropriate to the feature.
5. Updated documentation and handover.
6. Owner approval before production merge or deployment.

## 9. Immediate next engineering actions

1. Finish the branch and PR audit; compare code rather than branch names.
2. Inspect the existing Room schema, WorkManager workers, DI graph, agent tools, finance/trading/coding repositories and CI workflows.
3. Establish a reproducible baseline build/test result.
4. Select one canonical orchestrator, one tool registry and one permission gateway; document any duplicates for consolidation.
5. Implement Phase 1 as the first code change, on a dedicated branch, with migrations and tests.
6. Build Phase 2 only after Phase 1 passes its acceptance tests.

## 10. Current status and honesty

This file is an architecture proposal. The current repository already contains agent, coding, finance and trading packages, but their existence does not prove they are complete, integrated, secure or passing tests. The separate feature branches are audit candidates, not automatically safe to merge. No production deployment, live trading, customer outreach or successful build is claimed by this document.
