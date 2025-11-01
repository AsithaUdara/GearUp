Shared Libraries — how to use shared code

This folder contains common libraries used across microservices (DTOs, security helpers, event models).

When to add code here

- Put code here if more than one service needs to use it (avoid duplication).

How to depend on a shared lib

1. Add the shared module as a Maven dependency in the consuming service's `pom.xml`.
2. Build the reactor from repo root so shared libs are compiled before services that use them:

```
.\mvnw.cmd -DskipTests package
```

Notes

- Keep shared modules small and focused. Avoid adding large runtime-dependent code that forces services to share too many transitive dependencies.
