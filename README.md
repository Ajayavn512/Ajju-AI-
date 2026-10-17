# Ajju AI — Android Assistant

Ajju AI is a separate Android project for the Lava Z3 (Android 12). It does not modify Avani AI.

## Current starter features
- Native Android app written in Java with a dark, responsive chat interface
- Speech input through Android's speech-recognition activity
- Hindi text-to-speech replies
- Safe phone actions: open the dialer with a number and open an SMS draft (the user confirms the action)
- C++ JNI command classifier used by the Java app
- Optional Python/FastAPI backend scaffold for future AI integration

## Important limits
This is a starter source project, not a tested APK. It has not been built or tested on a physical Lava Z3 in this environment.
"Hey Ajju" continuous wake-word detection while the screen is off is **not implemented yet**. Android restricts background microphone access, and battery-management settings can stop background work. A visible, user-started foreground service and explicit microphone permission are required for any later background listening implementation. The secure lock screen will not be bypassed.
The Python backend is optional and is not connected to the Android app yet. Never put an AI API key in Android source code or commit it to GitHub.

## Build Android app
1. Install Android Studio on your Windows PC.
2. Open this repository folder in Android Studio.
3. Let Gradle sync and install the Android SDK components it requests.
4. Connect the Lava Z3 with USB debugging enabled, or use an emulator.
5. Press **Run** to install the debug build.

## Python backend (optional scaffold)
Use Python 3.10+:
```bash
cd backend
python -m venv .venv
# Windows PowerShell:
.venv\Scripts\Activate.ps1
pip install -r requirements.txt
uvicorn app:app --reload --host 0.0.0.0 --port 8000
```
The backend currently exposes a health endpoint and a safe demo chat endpoint. AI provider integration must be configured separately with a server-side environment variable.

## Languages
- **Java:** Android UI, speech input/output, phone intents
- **C++:** small native command classifier through JNI
- **Python:** optional backend scaffold
