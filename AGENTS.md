# Repository Guidelines

## Project Structure & Module Organization
- `CryptaInstaller_InnoSetup.iss`: Main Inno Setup script (includes `cryptad_version.iss` and `cryptad_deps.iss`).
- `CryptaInstaller_InnoSetup_library/`: Lazarus/Pascal DLL used by the installer to check ports and memory.
- `install_node/`: Runtime assets bundled into the installer (wrapper, updater, plugins, licenses).
- `resources/`, `translations/`: Icons/bitmaps and localized messages.
- `build.gradle.kts`, `settings.gradle`, `gradlew*`: Gradle tasks that derive the app version and jar list used by the installer.
- `.github/workflows/build.yml`: CI that builds x64/arm64 installers on Windows runners.

## Build, Test, and Development Commands
- Prereqs (Windows): Inno Setup 6 (`iscc.exe` on PATH), JDK 21, Git. For Linux, use Wine to run `iscc`.
- Generate installer includes: `./gradlew.bat updateSetupFile`
  - Produces `cryptad_version.iss` and `cryptad_deps.iss` from the `:cryptad` runtime classpath.
- Build installer: `iscc.exe CryptaInstaller_InnoSetup.iss /DArch=x64` (or `/DArch=arm64`)
  - Output: `Output/CryptaInstaller.exe`.
- CI build (recommended): use the “Build Crypta Installer” workflow; it downloads JRE, builds `cryptad.jar` and `CryptaTray`, then runs `iscc`.

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
- Do not commit secrets or signing material. Keep large binaries out of git; CI fetches the JRE. Prefer updating jars via Gradle rather than manual copies.

