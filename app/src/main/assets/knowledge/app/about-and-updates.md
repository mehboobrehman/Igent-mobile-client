---
title: About page, updates, Setup and portable versions
kind: app
tags: [igent, about, version, update, updates, upgrade, install, setup, portable, release-notes, whats-new, download, how-to]
updated: 2026-10-11
sources: []
---

# About page, updates, Setup and portable versions

## Where to get Igent
- Downloads are on the Releases page: https://github.com/sarfraznawaz2005/igent-public/releases
- Igent is for Windows 10 and 11 (64-bit) only. There is no Mac or Linux version.
- Each release has two downloads:
  - **Igent-Setup-X.exe**: the installer (recommended). It installs Igent for your Windows user, adds Start menu and Desktop shortcuts, and can be removed from Windows Settings → Apps.
  - **Igent-X-portable.zip**: the portable version. Unzip it to a folder you can write to (for example `D:\Apps\Igent`, not `C:\Program Files`) and run **Igent.exe**. Nothing is installed.
- Both use the same data folder, `%APPDATA%\Igent` (database, backups, logs). Your data stays when you update, reinstall or delete the portable folder.

## About page
Open **About**, the last link in the sidebar list, below **Sources**. It shows:
- The app name, a short description, the **version** and how Igent runs: **Windows, installed**, **Windows, portable**, or **Running from source** (developer copy).
- **Updates**: the result of the last check, **Check for Updates**, the **Check Automatically** switch and **All Releases** (opens the Releases page in your browser).
- **Release Notes**: what changed in each version.

## How updates work
- With **Check Automatically** on (default), Igent checks GitHub about 45 seconds after it starts and then every 6 hours. Click **Check for Updates** to check now.
- When a newer version is out:
  - **About** in the sidebar turns green, with a pulsing dot and a **New** label.
  - Igent shows one toast, "Igent X is available". Clicking it opens About.
  - About shows the new version, its size and date, and what is new in it.
- Nothing downloads by itself. Click **Download Update**. A progress bar shows the download, then Igent checks the file.
- When it says **Restart and Update**, click it. Igent closes, installs the new version and opens again:
  - **Installed copy**: the new Setup runs silently, then Igent starts again. Igent downloads only the changed parts when it can, and checks the file (SHA-512) before installing.
  - **Portable copy**: Igent downloads the new portable zip and checks it against the release's SHA256SUMS.txt. A small window then waits for Igent to close, copies the new files over the old ones in the same folder, and starts Igent again. It never deletes files in the folder.
- After an update, a **What's New** box shows the notes of every version since the one you had. It shows once. The notes stay on the About page.

## Update problems
- **"No release has been published yet."**: there is nothing to update to yet.
- **"No internet connection, or GitHub cannot be reached."**: check your connection, proxy or firewall, then click **Check for Updates** again.
- **Download fails or "does not match its checksum"**: the file was damaged on the way and was deleted. Click **Try Download Again**.
- **"Updates install only in the built app"**: you are running Igent from source code; update with git instead.
- **Portable copy will not update** ("cannot find its own files" or "could not copy the files"): the folder may be read-only, such as inside `C:\Program Files`. Move the portable folder somewhere you can write to, or download the new zip from the Releases page and unzip it over the old folder yourself while Igent is closed.
- **The installed update did not start Igent again**: start it from the Start menu. Your data is safe.
- You can always update by hand: download the new Setup and run it, or unzip the new portable zip over the old folder. Your data in `%APPDATA%\Igent` is not touched.

## Privacy
An update check asks GitHub for the latest release. It sends no data about you or your money.

## The trial version
Some people get a **trial version** of Igent ("Igent Trial") from the developer. It is the same app with these differences:
- The sidebar shows **Trial version** and the **days left**. The note turns amber in the last 5 days.
- It runs for a set number of days (usually 30) from its first start, and never after a final date fixed in that copy.
- When the trial ends, Igent shows "This trial of Igent has ended" and closes. Your data stays on the PC. Ask the person who gave you the copy for a full version.
- It does not update itself, and About says updates are off. It installs next to a normal Igent and keeps its own data.
- Turning the PC clock back, or editing the trial record, also ends the trial. Reset All Data does not restart it.
