# Repository Guidelines

## Project Structure & Module Organization
- `CryptaInstaller_InnoSetup.iss`: Main Inno Setup script (includes `cryptad_version.iss`).
- `CryptaInstaller_InnoSetup_library/`: Legacy Lazarus/Pascal DLL (port/memory checks); currently not used by the installer.
- `install_node/`: Minimal README and licenses bundled alongside the app image.
- `resources/`, `translations/`: Icons/bitmaps and localized messages.
- `build.gradle.kts`, `settings.gradle`, `gradlew*`: Gradle tasks (`stageJpackage`, `updateSetupFile`) that stage the jpackage app image and derive the app version.
- `.github/workflows/build.yml`: Reusable CI workflow that builds the jpackage image and packages x64/arm64 installers on Windows runners.

## Build, Test, and Development Commands
- Prereqs (Windows): Inno Setup 6 (`iscc.exe` on PATH), JDK 21, Git. For Linux, use Wine to run `iscc`.
- Stage jpackage + generate includes: `./gradlew.bat stageJpackage updateSetupFile`
  - Produces `cryptad_version.iss` from the staged image (`app/cryptad-dist/lib/cryptad.jar`).
- Build installer: `iscc.exe CryptaInstaller_InnoSetup.iss /DArch=x64` (or `/DArch=arm64`)
  - Output: `Output/CryptaInstaller.exe`.
- CI build (recommended): use “Build Crypta Installer” (`build_dispatch.yml`) which calls the reusable workflow. CI builds the `cryptad` jpackage image on Windows, then runs `iscc` to package installers.

## Coding Style & Naming Conventions
- Inno Setup (`.iss`): 2-space indent, PascalCase for identifiers, keep lines ≤ 120 chars, comment with `;`.
- Gradle Kotlin DSL: follow existing style; use descriptive task names and avoid broad reformatting.
- Files/paths: keep new assets under `resources/` or `install_node/`; don’t commit temporary build output.

## Testing Guidelines
- No unit test suite; perform smoke tests:
  - Install in a clean Windows VM; verify first run launches Crypta Tray and opens the welcome screen.
  - Defaults: fproxy port 8888, FCP port 9481; confirm they are available and configured.
  - Uninstall and verify `%LOCALAPPDATA%/Crypta` is cleaned and services stopped.

## Commit & Pull Request Guidelines
- Commits: imperative present (“Add…”, “Fix…”), small, focused; update related scripts/resources together.
- PRs: include rationale, linked issues, and any UI screenshots (wizard pages/icons). Confirm local build passes and CI is green for x64 and arm64.

## Security & Configuration Tips
- Do not commit secrets or signing material. Keep large binaries out of git; CI builds the jpackage image. Prefer updating generated files via Gradle rather than manual copies.
- Avoid committing jpackage images; keep local staging under `jpackage/` or rely on CI artifacts.
