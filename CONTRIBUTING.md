# Contributing to BuildPulse

BuildPulse optimizes for accurate learning, not feature count.

## Good contributions

- Correct an inaccurate coroutine explanation.
- Make a mental model clearer without weakening the exact semantics.
- Add a test that exposes a real stream-behavior regression.
- Improve accessibility or readability.
- Repair a broken article, tag, release, or comparison link.

## Scope rules

- Do not introduce a concept before its article milestone.
- Keep the Android simulator local and deterministic.
- Avoid new frameworks unless the lesson requires them.
- Do not paste complete project files into articles.
- Keep article explanations self-contained; repository access must remain optional.
- Use official Kotlin, Android, and kotlinx.coroutines sources for technical claims.

## Before submitting

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

Explain which reader misunderstanding or software behavior your change improves.
