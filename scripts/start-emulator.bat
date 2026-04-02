@echo off
REM Start Firebase emulators (run from Android Studio terminal)
cd /d "%~dp0.."
firebase emulators:start --project the-day-to
