# Ajju AI — Android Assistant

Ajju AI is a separate Android assistant project for the Lava Z3 (Android 12). It does not modify Avani AI.

## Current project status

- Native Android starter app written in Java
- Speech input through Android speech-recognition activity
- Hindi text-to-speech replies
- Safe phone actions: opens the dialer with a number and opens an SMS draft for user confirmation
- C++ JNI command classifier
- Optional Python/FastAPI backend with Gemini API integration
- Professional GitHub Pages landing page at https://ajayavn512.github.io/Ajju-AI-/

**Important:** The landing page is not itself a live AI chat client. The backend must be deployed and configured with your own Gemini API key before real AI replies work. The Android APK build has not yet been verified on a physical Lava Z3.

## Secure Gemini backend setup

1. Create a Gemini API key in [Google AI Studio](https://aistudio.google.com/app/apikey).
2. Keep the key private. Do not paste it into HTML, JavaScript, Android source, chat messages, or GitHub files.
3. Deploy the `backend` folder to a Python hosting service that supports FastAPI.
4. Add the environment variable `GEMINI_API_KEY` in that hosting service's dashboard. You may optionally set `GEMINI_MODEL` (default: `gemini-2.5-flash`).
5. Set `AJJU_ALLOWED_ORIGINS` to the exact website origin when deploying, for example `https://ajayavn512.github.io`.
6. Install dependencies from `backend/requirements.txt` and start the service with:
   ```bash
   uvicorn app:app --host 0.0.0.0 --port 8000
   ```
7. Test `/health` and then send a JSON POST to `/chat`, such as `{"message":"Namaste Ajju AI" }`.

The backend exposes:
- `GET /health` — status and whether a server-side key is configured
- `POST /chat` — Gemini response for a JSON body containing `message`

Never commit a real API key. Hosting may have its own costs, quotas, and usage limits; check the provider's terms before deploying.

## Build Android app

1. Install Android Studio on your Windows PC.
2. Open this repository folder in Android Studio.
3. Let Gradle sync and install the Android SDK components it requests.
4. Connect the Lava Z3 with USB debugging enabled, or use an emulator.
5. Press **Run** to install the debug build.

The GitHub Actions workflow also attempts to build a debug APK. Open the repository's **Actions** tab to check its status and download the APK artifact if the build succeeds.

## Python backend (optional)

Use Python 3.10+:
```bash
cd backend
python -m venv .venv
# Windows PowerShell:
.venv\\Scripts\\Activate.ps1
pip install -r requirements.txt
uvicorn app:app --reload --host 0.0.0.0 --port 8000
```

## Safety and limitations

- Continuous wake-word detection while the screen is off is not implemented.
- Android background microphone access is restricted; a visible, user-started foreground service and explicit microphone permission would be required for any later background listening implementation.
- The secure lock screen will not be bypassed.
- Never place AI API keys in Android source code or public repository files.
