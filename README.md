Crypta Windows Installer (Inno Setup)

This repository contains the Inno Setup–based Windows installer for Crypta. It packages the jpackage app image produced by the Crypta node ("cryptad"), so no system JRE is required.

What’s here
- `CryptaInstaller_InnoSetup.iss` — main installer script; stages the jpackage app image (Crypta.exe, app/, runtime/).
- `resources/` — icons and wizard images used by the installer UI.
- `translations/` — localized messages (UTF‑8 for source control; Inno Setup reads them as ANSI at build time).
- `build.gradle.kts` — Gradle tasks that stage the jpackage image and generate `cryptad_version.iss` used by the script.
- `.github/workflows/` — reusable CI workflow that builds the jlink image on Windows and then packages the installer for x64 and ARM64.

Requirements
- Windows: Inno Setup 6 on PATH (`iscc.exe`), JDK 21, Git.
- Linux/macOS: Wine to run `iscc.exe` if building outside Windows.

Quick start (local build)
1) Obtain a jpackage app image produced by the Crypta node build.
   - Option A: Build `cryptad` on Windows; find it under `cryptad/build/jpackage/Crypta/`.
   - Option B: Download the `cryptad-jpackage-<arch>` artifact from CI and place its contents under `./jpackage/`.
2) Stage and generate installer includes:
   - Windows: `gradlew.bat stageJpackage updateSetupFile`
   - Linux (with Wine): `./gradlew stageJpackage updateSetupFile`
3) Build the installer with Inno Setup:
   - x64:   `iscc.exe CryptaInstaller_InnoSetup.iss /DArch=x64`
   - arm64: `iscc.exe CryptaInstaller_InnoSetup.iss /DArch=arm64`
4) Output is written to `Output/CryptaInstaller.exe`.

CI builds
- Reusable workflow: `.github/workflows/build.yml` (callable from other repos and also runnable manually from Actions).
- Dispatcher for manual runs in this repo: `.github/workflows/build_dispatch.yml` (optional; kept for convenience).
- Inputs:
  - `cryptad-git-ref` (default `main`), `jdk-version` (default `21`), `os-matrix` (default `windows-latest` and `windows-11-arm`).
- Artifacts: `cryptad-jpackage-<arch>` (from the build job) and `cryptainstaller-<arch>` (installer).

Consuming as a reusable workflow (from another repo)
```
jobs:
  build-installer:
    uses: crypta-network/wininstaller-innosetup/.github/workflows/build.yml@master
    with:
      cryptad-git-ref: main
      jdk-version: "21"
      os-matrix: '["windows-latest","windows-11-arm"]'
      upload-installer: true
```

Smoke test checklist
- Install in a clean Windows VM; ensure first run launches Crypta and shows the welcome page.
- Confirm default ports (fproxy 8888, FCP 9481) are usable in your environment; the installer no longer probes/rewrites ports.
- Uninstall and verify `%LOCALAPPDATA%/Crypta` is cleaned up.

Differences from hyphanet/wininstaller-innosetup (since fork on 2025‑07‑26)
- Project rebrand: Freenet → Crypta (installer name, icons, wizard artwork, translations).
- Runtime: switched from external JRE → bundled image, and now to a jpackage app image built by `cryptad`.
- Installer logic: stages the jpackage image; removes legacy .NET and port‑probing logic from the installer.
- Architecture matrix: builds x64 and ARM64 via `/DArch` and GitHub Actions matrix.
- Build system: migrated Gradle Groovy → Kotlin DSL; added `unpackJlink` and `updateSetupFile` tasks to derive version and unpack the distribution.
- CI: Windows workflows now build `cryptad` jlink on the runner, publish the tar, and package the installer; refactored into a reusable workflow.

Notes on translations and encoding
- Translations are stored in UTF‑8. Inno Setup 6 (Unicode) supports UTF‑8 `.isl` message files directly.

License
- Crypta installer assets and scripts are under their respective licenses. See `install_node/licenses/` and project LICENSE files as applicable.
