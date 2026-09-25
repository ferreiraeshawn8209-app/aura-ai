# T1000 / Aura Amalgamation Architecture

## Purpose

Aura is the Android foundation and user-facing assistant. The T1000 architecture is the autonomous capability layer. The result is one agent, not a collection of feature-specific assistants.

## Core interaction

**Hey Aura → understand intent → plan → select tools → permission check → execute → verify → respond → remember → improve**

Users should not need separate primary menus for coding, DJ, quotes, GPS, phone, media, finance, or other capabilities. Those are tools/capabilities selected by the central agent from natural-language intent.

## Autonomous behavior

The autonomous core from the earlier Aura prototype is preserved conceptually but implemented using Android-safe persistent components rather than an in-memory map and raw timer. Pending work belongs in durable storage; background work should use WorkManager/appropriate foreground services; high-impact actions remain permission/confirmation gated.

## Capability domains

- Voice and wake phrase (`Hey Aura`) with Android background-service constraints respected.
- Natural conversation, memory, preferences and learned skills.
- Phone dialing and supported call workflows.
- SMS, contacts, calendar and reminders.
- GPS/location and navigation integrations.
- Coding: inspect repositories, edit files, build, test, diagnose and iterate.
- App generation/build workflows where the environment and credentials permit them.
- Image and video generation/editing through configured providers/tools.
- BeatKulture quote and event workflows.
- DJ assistance and music-planning workflows.
- Finance/trading workflows with explicit action boundaries.
- Web research/browser automation where permitted.
- Android app/device control through explicit platform permissions.

## Learning model

Learning means storing durable facts, preferences, successful procedures, failures and skill statistics, then using that history to improve future plans. It does not mean unrestricted self-modification or bypassing platform/security controls.

## Safety and control

Aura may act autonomously on authorized, reversible or low-impact work. Calls, messages, purchases, financial transactions, destructive file operations, credential use and other consequential actions require the applicable Android permission, provider authorization or user confirmation. The agent must never bypass security controls, MFA, CAPTCHA, access controls or platform restrictions.

## Merge rule

Preserve Aura's existing working Android foundation and transplant T1000 capabilities selectively. Do not replace working functionality merely because T1000 contains a parallel implementation. Prefer the implementation with stronger persistence, lifecycle handling, verification, testability and Android compatibility.
