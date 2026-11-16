# AGENTS.md

Guidance for coding agents working on UView.

## Project

UView is a Java desktop application for viewing, extracting, and modifying `.unitypackage` files without Unity Editor.
It targets Java 21, uses JDK 25 for development, and builds with Maven. The UI uses Swing with FlatLaf and JavaFX for video playback.

Prioritize package and asset integrity, responsive UI behavior, and compatibility with Linux, macOS, and Windows.

## Core Rules

- Use English for code, comments, documentation, and tests.
- Keep changes focused, reuse existing components, and avoid unrelated refactoring.
- Keep dependencies in `pom.xml`; discuss new dependencies before adding them.
- Preserve asset GUIDs, paths, content, and metadata when changing package operations.
- Preserve validation and error handling around loading, saving, extracting, and overwriting files.
- Keep slow I/O off the Swing event dispatch thread; update Swing components on that thread and JavaFX components on the JavaFX application thread.

## Repository Layout

- `src/main/java/io/github/pixelclover/uview/App.java`: Application entry point and UI initialization.
- `src/main/java/io/github/pixelclover/uview/cli/`: Headless `list` and `extract` commands, help, and exit codes.
- `src/main/java/io/github/pixelclover/uview/core/`: `PackageManager` controls package operations and unsaved state; `SettingsManager` persists preferences.
- `src/main/java/io/github/pixelclover/uview/io/PackageIO.java`: Reads and writes gzip-compressed tar archives.
- `src/main/java/io/github/pixelclover/uview/model/`: `UnityAsset` and `UnityPackage` represent assets and package contents.
- `src/main/java/io/github/pixelclover/uview/gui/`: Swing windows, package browsing, editors, and asset viewers, including the JavaFX video panel.
- `src/main/java/io/github/pixelclover/uview/gui/tree/`: Tree entry types used by package browsing.
- `src/main/resources/`: Icons, fonts, logo, and Log4j configuration.
- `src/test/java/io/github/pixelclover/uview/`: JUnit Jupiter tests mirroring the main packages.
- `test_assets/`: Sample assets for tests and manual checks.
- `pom.xml`: Java release, dependencies, build plugins, style checks, coverage, and shaded JAR configuration.
- `Makefile`: Development commands wrapping Maven.
- `flake.nix` / `flake.lock`: Nix development tools and pinned nixpkgs revision.
- `.pre-commit-config.yaml`: Git hooks for formatting, linting, tests, and repository hygiene.
- `.github/workflows/`: CI and release automation.
- `scripts/test_makefile.sh`: Exercises Make targets, including formatting that modifies source files.

## Development Environment

Use `nix develop` on x86_64/aarch64 Linux or Apple Silicon macOS to enter the development shell. It provides the full JDK 25, Maven using that JDK,
GNU Make, Git, pre-commit, and a Nix formatter. Java libraries, including JavaFX, remain managed by Maven.
Keep `maven.compiler.release` at 21 and JavaFX on the 21.x line to preserve Java 21 runtime compatibility. CI tests JDKs 21 and 25.
Nix flakes must be enabled. Commit `flake.lock` with intentional input updates so contributors use the same tool versions.
Windows contributors can use JDK 25 and Maven directly, or Nix under WSL for development.

Commands can also run without an interactive shell: `nix develop -c make test`.
The first build needs network access to download Maven dependencies. Running the GUI needs a graphical desktop session;
the shell supplies Linux native libraries needed by the Maven JavaFX binaries.

| Task | Command | Result |
|------|---------|--------|
| Help | `make help` | Lists the actual Make targets |
| Package | `make package` | Builds the shaded `target/uview.jar` and runs tests |
| Run | `make run` | Packages and launches UView |
| Verify | `make test` | Runs `mvn -B verify`, including tests, packaging, Spotless, and Checkstyle |
| Format | `make format` | Applies Google Java Format and removes unused imports |
| Format Check | `make format-check` | Checks formatting without modifying sources |
| Lint | `make lint` | Runs Checkstyle with Google checks |
| Release Package | `make package-release` | Builds `target/uview-<version>.jar` using the release profile |
| Clean | `make clean` | Removes Maven build output |
| Install Hooks | `make setup-hooks` | Installs pre-commit and pre-push hooks; run only when requested |
| Nix Format | `nix fmt` | Formats the flake |
| Flake Check | `nix flake check` | Validates flake outputs on the current system |

Without Nix, install JDK 25 and Maven, plus GNU Make for the Make targets.
Do not replace the Maven build with a separate Nix build or move Java dependencies into the shell.

## Code and Testing Conventions

- Follow the existing Java package structure and Google Java Style enforced by Spotless and Checkstyle.
- Use `UpperCamelCase` for types, `lowerCamelCase` for methods and fields, and `UPPER_SNAKE_CASE` for constants.
- Put package serialization in `PackageIO`, package operations in `PackageManager`, and UI behavior in `gui/`.
- Add focused JUnit Jupiter tests under `src/test/java/` for changed behavior; use temporary directories for file operations.
- For package changes, cover the affected load/save round trip and verify asset content and metadata are preserved.
- Run `make test` for Java changes. Run `make format-check` and `make lint` when checking style separately.
- Coverage reports are generated under `target/site/jacoco/`.
- For GUI changes, manually check the affected flow when a desktop is available; state clearly when it was not checked.
- For flake changes, run `nix fmt`, `nix flake check`, and verify `java -version` and `mvn -version` inside `nix develop`.
- Report checks actually run, failures, and unverified platform behavior. Keep commits and PRs scoped to the requested change.
