Crypta Windows Installer (Inno Setup)

This repository contains the Inno Setup–based Windows installer for Crypta. It bundles a jlink runtime image produced by the Crypta node ("cryptad") and ships minimal assets required to run on Windows without a system JRE.

What’s here
- `CryptaInstaller_InnoSetup.iss` — main installer script; generates `cryptad.ini` on first install and copies the jlink distribution into `bin/`, `lib/`, `conf/`, and `legal/`.
- `CryptaInstaller_InnoSetup_library/` — small Lazarus/Pascal DLL used during setup to check free ports and available memory.
- `resources/` — icons and wizard images used by the installer UI.
- `translations/` — localized messages (UTF‑8 for source control; Inno Setup reads them as ANSI at build time).
- `build.gradle.kts` — Gradle tasks that unpack the jlink archive and generate `cryptad_version.iss` used by the script.
- `.github/workflows/` — reusable CI workflow that builds the jlink image on Windows and then packages the installer for x64 and ARM64.

Requirements
- Windows: Inno Setup 6 on PATH (`iscc.exe`), JDK 21, Git.
- Linux/macOS: Wine to run `iscc.exe` if building outside Windows.

Quick start (local build)
1) Obtain a jlink archive produced by the Crypta node build, named like `cryptad-jlink-vX.Y.Z.tar.gz`.
   - Option A: Build `cryptad` yourself on Windows and copy the resulting tar from `cryptad/build/distributions/`.
   - Option B: Download the artifact from CI and place it in this repo’s root or `artifacts/`.
2) Unpack and generate installer includes:
   - Windows: `gradlew.bat unpackJlink updateSetupFile`
   - Linux (with Wine): `./gradlew unpackJlink updateSetupFile`
3) Build the installer with Inno Setup:
   - x64:   `iscc.exe CryptaInstaller_InnoSetup.iss /DArch=x64`
   - arm64: `iscc.exe CryptaInstaller_InnoSetup.iss /DArch=arm64`
4) Output is written to `Output/CryptaInstaller.exe`.

CI builds
- Reusable workflow: `.github/workflows/build.yml` (triggered via `workflow_call`).
- Dispatcher for manual runs in this repo: `.github/workflows/build_dispatch.yml`.
- Inputs:
  - `cryptad-git-ref` (default `main`), `jdk-version` (default `21`), `os-matrix` (default `windows-latest` and `windows-11-arm`).
- Artifacts: `cryptad-jlink-<arch>` (from the build job) and `cryptainstaller-<arch>` (installer).

Consuming as a reusable workflow (from another repo)
```
jobs:
  build-installer:
    uses: crypta-network/wininstaller-innosetup/.github/workflows/build.yml@main
    with:
      cryptad-git-ref: main
      jdk-version: "21"
      os-matrix: '["windows-latest","windows-11-arm"]'
      upload-installer: true
```

Smoke test checklist
- Install in a clean Windows VM; ensure first run launches Crypta and shows the welcome page.
- Confirm default ports are available (fproxy 8888, FCP 9481) and written to `cryptad.ini`.
- Uninstall and verify `%LOCALAPPDATA%/Crypta` is cleaned up.

Differences from hyphanet/wininstaller-innosetup (since fork on 2025‑07‑26)
- Project rebrand: Freenet → Crypta (installer name, icons, wizard artwork, translations).
- Runtime: switched from downloading a system JRE to bundling a self‑contained jlink image built from `cryptad`.
- Installer logic: generates `cryptad.ini` on install; retains port/memory checks via the Pascal DLL; removed legacy .NET installer steps.
- Architecture matrix: builds x64 and ARM64 via `/DArch` and GitHub Actions matrix.
- Build system: migrated Gradle Groovy → Kotlin DSL; added `unpackJlink` and `updateSetupFile` tasks to derive version and unpack the distribution.
- CI: Windows workflows now build `cryptad` jlink on the runner, publish the tar, and package the installer; refactored into a reusable workflow.

Notes on translations and encoding
- Translations are stored in UTF‑8. Inno Setup 6 (Unicode) supports UTF‑8 `.isl` message files directly.

License
- Crypta installer assets and scripts are under their respective licenses. See `install_node/licenses/` and project LICENSE files as applicable.
