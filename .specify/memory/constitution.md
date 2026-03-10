# Votacao-Assembleia Constitution

## Core Principles

### I. Code Quality First
Deliver clear, correct, and maintainable code. Prefer explicitness over cleverness, handle errors intentionally, and keep behavior deterministic.

### II. Simplicity Over Complexity
Default to the simplest design that solves the problem. Avoid premature abstraction and YAGNI; complexity must be justified.

### III. Minimal Dependencies
Add dependencies only when they provide significant, demonstrable value. Prefer standard library and existing platform capabilities.

### IV. Testing Policy (Supersedes All Other Guidance)
Frontend: **Absolutely no tests** of any kind (no unit, integration, or end-to-end tests). Do not create test tooling, configs, or test-only dependencies for the frontend.
Backend: **Unit tests required** for backend logic. No requirement for integration or end-to-end tests unless explicitly requested.

## Technology Constraints

- Frontend must use Next.js, React, and Tailwind **versions exactly as specified in the project’s package.json**.
- Backend must use **Java 17** and **Spring Boot**.

## Development Workflow

- Keep changes minimal and focused.
- Preserve existing public APIs unless change is explicitly requested.
- Favor small, readable modules and consistent formatting.

## Governance

This constitution supersedes all other guidance, policies, or templates. Any exception requires explicit user approval and documentation.

**Version**: 1.0.0 | **Ratified**: 2026-03-10 | **Last Amended**: 2026-03-10
