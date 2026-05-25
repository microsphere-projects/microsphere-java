# Onboarding Plan — Microsphere Java

> A personalized phased onboarding guide for experienced developers new to the Microsphere Java stack.

---

## Phase 1 — Foundation (Days 1–3)

### Environment Setup

1. **Prerequisites**
   - Java 8, 11, 17, 21, or 25 (all tested in CI — pick 17 or 21 as the safest modern LTS)
   - Maven 3.6+ or just use the included wrapper (`./mvnw` / `mvnw.cmd`) — no install needed

2. **Clone & first build**
   ```bash
   git clone https://github.com/microsphere-projects/microsphere-java.git
   cd microsphere-java
   ./mvnw package -DskipTests   # fast first build to download deps
   ```

3. **Run the full test suite** to confirm everything is green:
   ```bash
   ./mvnw test
   ```
   With coverage:
   ```bash
   ./mvnw test --activate-profiles test,coverage
   ```

4. **Troubleshooting tips**
   - If Maven Central is slow, add `-o` (offline) once deps are downloaded locally.
   - Module compilation errors on JDK 17+ with `--add-opens` are expected in some edge-case tests — check CI logs in `.github/workflows/maven-build.yml` for how the matrix handles each JDK.
   - The `revision` property (`0.3.4-SNAPSHOT`) drives all module versions — don't change it manually.

### Priority Documentation to Read First (in order)

| # | Resource | Why |
|---|----------|-----|
| 1 | [`README.md`](./README.md) | Architecture overview, module map, usage examples |
| 2 | [`release-notes.md`](./release-notes.md) | Understand what changed and why — reveals design intent |
| 3 | [`CODE_OF_CONDUCT.md`](./CODE_OF_CONDUCT.md) | Team norms before contributing |
| 4 | [DeepWiki AI docs](https://deepwiki.com/microsphere-projects/microsphere-java) | Queryable deep-dive into the codebase |
| 5 | [JavaDoc — microsphere-java-core](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-java-core) | The most important module's API |

---

## Phase 2 — Exploration (Days 4–10)

### Module Tour (hands-on order, simplest → most complex)

1. **`microsphere-java-annotations`** — Start here. It's tiny: just `@Nullable`, `@Nonnull`, `@Immutable`, `@Experimental`, `@Since`. Read all sources in ~15 minutes. Zero dependencies.

2. **`microsphere-java-core`** — The main codebase. Its `src/main/java/io/microsphere/` has 22 packages:
   - Start with `util/` (StringUtils, CollectionUtils, Version) — pure utilities, easy to read
   - Move to `reflect/` (FieldUtils, MethodUtils) — reflection helpers, great for understanding the framework's approach
   - Then `convert/` (Converters, Converter SPI) — an extensible SPI pattern used throughout
   - Then `event/` (EventDispatcher) — the custom event system
   - Leave `classloading/`, `net/`, `nio/`, `metadata/` for later

3. **`microsphere-java-test`** — Read the test base classes; they reveal how the team writes and structures tests (JUnit 5).

4. **`microsphere-annotation-processor`** — Compile-time `@ConfigurationProperty` processor; read after you understand the annotations module.

### Running Tests to Understand Workflows

```bash
# Run tests for a single module to keep iteration fast
./mvnw test -pl microsphere-java-core

# Run a single test class
./mvnw test -pl microsphere-java-core -Dtest=StringUtilsTest

# Look at what tests exist
find microsphere-java-core/src/test -name "*Test.java" | head -30
```

### Open Issues & First Tasks

There is currently one actively tracked open issue:

- **[#221 — \[Modular\] Make fine-grain modules on different JDK versions](https://github.com/microsphere-projects/microsphere-java/issues/221)**
  This is a larger architectural feature (splitting `microsphere-java-core` into `microsphere-java-base`, `microsphere-java-11`, `microsphere-java-17`, etc.). As a new contributor, don't tackle this directly — but reading it gives you crucial context about the project's roadmap.

**Best first-contribution opportunities** (high-value and low-risk):

| Task | Where | Why It's Good |
|------|-------|---------------|
| Add missing Javadoc to public utility methods | `microsphere-java-core/src/main/java/io/microsphere/util/` | No logic changes, builds domain knowledge |
| Add test coverage for edge cases | `microsphere-java-core/src/test/java/io/microsphere/` | Forces you to understand each API |
| Improve README examples | `README.md` | Safe, visible contribution |
| Add `@Since` / `@Experimental` annotations to undocumented APIs | Any module | Aligns with the existing annotation vocabulary |

---

## Phase 3 — Integration (Weeks 2–3)

### Team Processes

1. **Branch & PR model** — Fork the repo, create a feature branch, submit a PR against `main`. See `README.md → Contributing`.
2. **CI gates** — Every PR runs `.github/workflows/maven-build.yml` against a JDK matrix (8, 11, 17, 21, 25). Your PR must be green on all.
3. **Code review** — Lead: [Mercy Ma (@mercyblitz)](https://github.com/mercyblitz). One reviewer is sufficient for most PRs.
4. **Discussions** — Use [GitHub Discussions](https://github.com/microsphere-projects/microsphere-java/discussions) for questions before opening issues.

### First Contribution Workflow

```bash
# 1. Fork on GitHub, then clone your fork
git clone https://github.com/YOUR_USERNAME/microsphere-java.git
git remote add upstream https://github.com/microsphere-projects/microsphere-java.git

# 2. Create a feature branch
git checkout -b docs/improve-javadoc-stringutils

# 3. Make changes, then run tests
./mvnw test -pl microsphere-java-core

# 4. Commit & push to your fork
git push origin docs/improve-javadoc-stringutils

# 5. Open a PR against microsphere-projects/microsphere-java main
```

### Early Wins Checklist

- [ ] Build passes locally on your JDK version
- [ ] You can run a single test in isolation
- [ ] You understand the `Converter` SPI by implementing a toy converter in a scratch file
- [ ] You understand the `EventDispatcher` by writing a small demo with a custom event
- [ ] You've opened your first PR (even if documentation-only)
- [ ] You've commented or asked a question in GitHub Discussions

### Recommended Resources

| Resource | Purpose |
|----------|---------|
| [DeepWiki](https://deepwiki.com/microsphere-projects/microsphere-java) | AI-powered Q&A on the codebase — use this liberally |
| [javadoc.io — microsphere-java-core](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-java-core) | API reference |
| [Apache Maven docs](https://maven.apache.org/guides/) | If Maven multi-module builds are new to you |
| [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/) | Testing framework used throughout |
| [GitHub Wiki](https://github.com/microsphere-projects/microsphere-java/wiki) | Additional project-specific notes |
