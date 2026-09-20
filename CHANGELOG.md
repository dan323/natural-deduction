# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

The project has never been versioned or tagged, since it was not meant for public use. Instead, each calendar month with changes is treated as one release, named `mon-yy` (for example `sep-26`). Releases are listed newest first, and entries use the commit or PR titles from git history.

## sep-26

### Added
- This changelog, with one release per month
- Copyright notice and GPL-3.0-only license metadata in the poms and `frontend/package.json`
- `done` flag in `ProofResponse`, taken from the domain's `Proof.isDone()`; the UI now uses it instead of computing success itself (#108)
- Typed action descriptors replace the reflection-derived action strings, and the solver is now exposed through the REST API (#106)
- Test that action discovery works from the packaged fat jar (#105)
- Smoke test of the Docker image (actions endpoints, UI, non-root user) before it is published (#107)
- `SpringVersionAlignmentTest`, which fails when the root pom's `spring.version` drifts from the version the Spring Boot parent manages (#107)
- `typecheck` script and a type-check step in the frontend CI workflow (#107)
- Non-root user and `HEALTHCHECK` in the Dockerfile (#107)

### Changed
- `CLAUDE.md` and the prose docs rewritten to match the code (endpoints, modules, setup, languages); the fictional "earlier design" API section is removed
- Docker image is built from the jar that passed `mvn -B verify` and is only pushed once the smoke test passes; the workflow is renamed "Publish Docker image" (#107)
- Backend CI workflow collapsed into a single `mvn -B verify`, with concurrency cancellation for superseded PR runs (#107)
- Frontend CI uses `npm ci`; `typescript`, `ts-node` and `@testing-library/*` moved to `devDependencies`; `@types/node` aligned with Node 20 (#107)
- springdoc version is now a property in `executable/pom.xml` (#107)
- Modal dialog accessibility: focus returns to the opener on close and every input has a label (#108)
- `docs/*.md` and the README corrected against the code (Spring Boot 3.5.3, real `/logic/{logic}/...` endpoints, package names) (#107, #108)
- Bump sonarqube-scan-action to v8.2.2 (#103)
- Dependencies updated: Spring Boot 3.5.3 to 3.5.16 (Spring Framework 6.2.19), springdoc 2.8.6 to 2.9.1, JUnit 5.12.2 to 6.1.3, Mockito 5.18.0 to 5.23.0, SLF4J 2.0.17 to 2.0.19, javaluator 3.0.5 to 3.0.6, PIT 1.19.1 to 1.30.0, JaCoCo 0.8.12 to 0.8.15, plus the compiler, surefire and versions Maven plugins; React 19.3, Vite 8, Jest 30, jsdom 29 and the other frontend packages; `actions/checkout` v7, `actions/setup-java` v6, `actions/setup-node` v7 and `peaceiris/actions-gh-pages` v4.1.0. The docs now state Spring Boot 3.5.16, JUnit 6, Maven 3.6.3+ and Node 20.19+.

### Fixed
- REST API returns proper errors instead of 500s (#102)
- Frontend bugs from the project review (#104)
- `OnMerge.yml` no longer fails when there are no PR reports to clean, and `ad-m/github-push-action` is pinned to a commit SHA (#107)
- Confetti no longer calls `Math.random()` during render and reshuffles on re-render (#108)
- `ProofViewer` keys use the step index only (#108)
- NOP logger warning in unit tests, by adding an SLF4J provider (#108)

### Removed
- Unused `React` default imports (#108)

## mar-26

### Added
- UI review with suggested improvements (#88)

### Changed
- Swap `react-scripts` for Vite (#81)
- Update Java and JavaScript dependencies (#77)
- Update docs (#68, #86)
- Update JavaScript dependencies (#85)

### Fixed
- Failing CI workflows investigated and fixed (#71)

## dec-24

### Changed
- Update `CompileAndTest.yml` (#51, #54)
- Dependabot bumps in `/frontend`: path-to-regexp, express, axios, sonarqube-scanner, cross-spawn, rollup, http-proxy-middleware and nanoid (#46, #48, #49, #50, #52, #53, #55)

## sep-24

### Added
- Docker publish workflow (#40)
- Test coverage work on the backend and frontend, including Sonar coverage reporting (#37, #38, #39)

### Changed
- TypeScript quality improvements (#36)
- Bump express from 4.19.2 to 4.21.0 in `/frontend` (#45)

## aug-24

### Added
- React front end (#33)
- Init proof (#34)
- Integration tests (#28)
- Modal and model work (#29), more modal tests (#30)
- Proof parsing (#27)
- Sonar analysis (#25)

### Changed
- Java 21 (#24)
- Outward model revisited (#26)
- Simplify solvers (#31)
- Proof reason improvements (#32)
- Update (#35)
- Package before Sonar in CI

### Fixed
- Beans no longer displayed (#23)
- Assorted bug fixes

## apr-23

### Fixed
- Diamond rule fix (#21)

## mar-23

### Changed
- Project rearranged (#19)

## feb-23

### Added
- Actions

### Changed
- Java 17; `equals` and `hashCode` updates
- Project rearranged (#18)
- pitest and Sonar updates and fixes

## sep-20

### Added
- First REST service iteration
- Parsing of `RelationalAction` subclasses
- New CI flow to test
- Many more tests, with `equals` and `hashCode` added to several classes and `ModalAssume#equals`

### Changed
- Update to Java 14; Sonar moved to Java 14
- Project restructured, with Jigsaw (JPMS) test fixes
- Natural deduction automation made more readable
- README expanded
- Some code smells and Sonar issues removed
- `deduction.service` removed

### Fixed
- Custom "or elimination" rules bug
- Natural deduction automation
- Modal `OrI` parsing

## apr-20

### Added
- Parser of proofs into `List<Action>`
- New CI flow (and fixes to it on merge)
- `NaturalDeduction` tests
- `CNAME` created, then deleted

### Changed
- Simplified the framework-provided, classic and modal rules
- Packages renamed; automatic proof extracted to an internal class
- Interfaces and generics reshuffled
- All modules reverted to version 0.1

### Fixed
- Diamond rule `ModalDiaE`
- `ModalNaturalDeduction#stateIsUsedBefore`
- `Action#getLastAssumptionLevel` for an empty proof
- Some Sonar issues

## feb-20

### Added
- More tests

## jan-20

### Added
- More tests
- `equals` and `hashCode`

### Changed
- Constants moved into an interface
- Some Sonar issues fixed; nullable variable handling

## nov-19

### Changed
- Improvements

## oct-19

### Added
- Pitest
- REST interface
- More tests

### Changed
- Project reordered; improved `.gitignore`
- Constants are checked for falsehood, are created from doubles rather than ints, and are no longer created eagerly; operator strings moved into interfaces

## may-19

### Added
- First project commit: classical and modal natural deduction
- Complex rules
- More tests

### Changed
- Generics are abstract, some classes are final, `Construct` removed except for constants
- Formatting

### Fixed
- Modal states
- One Sonar issue
