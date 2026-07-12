# AI Context

This document provides context for AI assistants working on Cronis.

Every implementation must follow these rules.

---

# Project

Name: Cronis

Purpose:

Create the cleanest, most modern and most customizable HUD framework for Hypixel SkyBlock.

---

# Target Platform

Minecraft:
1.26.1.2

Loader:
Fabric

Java:
Java 21

Client-side only.

---

# Design Philosophy

Clean.

Modern.

Customizable.

Apple-inspired.

Minimal.

Professional.

Premium.

---

# Core Principles

Every pixel must have a purpose.

If it doesn't look premium, it doesn't ship.

Performance is a feature.

Consistency over complexity.

Great by default. Powerful when needed.

---

# Architecture

Cronis uses a modular architecture.

Main systems:

- Widget Engine
- Theme Engine
- Animation Engine
- Layout Engine
- Profile Engine
- Notification Engine

Every new feature should integrate into existing systems instead of creating duplicates.

---

# Code Style

Follow Java best practices.

Prefer readability over clever code.

Avoid unnecessary abstractions.

Document public APIs.

Write maintainable code.

Keep classes focused on a single responsibility.

---

# Performance

Performance is a priority.

Avoid unnecessary allocations.

Avoid expensive rendering every frame.

Cache when appropriate.

Prefer asynchronous loading when possible.

---

# Hypixel Safety

Never:

- automate gameplay

- send custom packets

- modify packets

- simulate player input

- use macros

- implement features that violate Hypixel rules

---

# UI Guidelines

Apple-inspired.

Rounded corners.

Consistent spacing.

Smooth animations.

Consistent typography.

Premium appearance.

Minimalistic.

---

# Themes

Official themes are maintained by the Cronis team.

Community themes are loaded from the themes folder.

Themes should never break widget functionality.

---

# Widgets

Every widget must:

- support themes

- support profiles

- support layouts

- support animations

- follow the Cronis Design Language

No widget may implement custom rendering behavior that breaks consistency.

---

# Commits

Use Conventional Commits.

Examples:

feat:

fix:

docs:

refactor:

perf:

style:

test:

chore:

---

# Goal

Every contribution should improve the quality, consistency and long-term maintainability of Cronis.