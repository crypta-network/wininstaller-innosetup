Crypta for Windows — Quick Guide

Overview
- Crypta is a privacy-preserving P2P node and client.
- This installer ships a self-contained jlink runtime; no external JRE is required.

Install Location
- Default: %LOCALAPPDATA%\Crypta
- Contents: bin\ (launchers), lib\ (modules + cryptad.jar), conf\, legal\.

Starting Crypta
- After setup, launch Crypta from the Start Menu (Crypta) or run:
  %LOCALAPPDATA%\Crypta\bin\cryptad-launcher.bat
- On first run, Crypta picks free ports and creates cryptad.ini.

Accessing the UI
- Local web UI (fproxy) runs on http://127.0.0.1:8888/ by default.
- If 8888 is in use, the installer chooses the next free port.

Default Ports
- fproxy: 8888 (HTTP UI)
- FCP:    9481 (client protocol)

Configuration
- File: %LOCALAPPDATA%\Crypta\cryptad.ini
- Keys created by the installer:
  - fproxy.port=<port>
  - fcp.port=<port>
  - node.downloadsDir=.\downloads
- Stop Crypta before editing. Restart to apply changes.

Updating
- Download and run a newer Crypta installer. It overwrites in place and keeps
  cryptad.ini and your data.

Uninstalling
- Windows Settings -> Apps -> Installed apps -> Crypta -> Uninstall.

Troubleshooting
- Port in use: Edit cryptad.ini and pick an unused port for fproxy.port/FCP.
- Firewall: Allow Crypta on first start; ensure localhost (127.0.0.1) is permitted.
- Antivirus: If startup is blocked, whitelist the install folder.
- Still stuck? See the links below.

More Info
- Project: https://crypta.network/
- Source & issues: https://github.com/crypta-network/cryptad

Licenses for bundled components are in %LOCALAPPDATA%\Crypta\legal.
