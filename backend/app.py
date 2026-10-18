"""Ajju AI backend with secure server-side Gemini integration."""
import os
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
from google import genai

app = FastAPI(title="Ajju AI API", version="0.2.0")

# Configure AJJU_ALLOWED_ORIGINS as comma-separated frontend origins in production.
_origins = [item.strip() for item in os.getenv("AJJU_ALLOWED_ORIGINS", "*").split(",") if item.strip()]
app.add_middleware(
    CORSMiddleware,
    allow_origins=_origins,
    allow_credentials=False,
    allow_methods=["GET", "POST"],
    allow_headers=["Content-Type"],
)

class ChatRequest(BaseModel):
    message: str = Field(min_length=1, max_length=4000)

class ChatResponse(BaseModel):
    reply: str
    mode: str

def _api_key():
    return os.getenv("GEMINI_API_KEY") or os.getenv("AJJU_AI_API_KEY")

@app.get("/health")
def health():
    return {
        "status": "ok",
        "service": "Ajju AI",
        "mode": "gemini" if _api_key() else "setup-required",
    }

@app.post("/chat", response_model=ChatResponse)
def chat(payload: ChatRequest):
    key = _api_key()
    if not key:
        raise HTTPException(
            status_code=503,
            detail="Gemini API key is not configured on the server. Add GEMINI_API_KEY in your hosting provider's environment variables."
        )
    try:
        client = genai.Client(api_key=key)
        response = client.models.generate_content(
            model=os.getenv("GEMINI_MODEL", "gemini-2.5-flash"),
            contents=(
                "You are Ajju AI, a helpful and safe assistant. Reply naturally in Hindi/Hinglish "
                "when the user writes in Hindi/Hinglish; otherwise use the user's language. "
                "Keep answers clear and practical.\n\nUser: " + payload.message
            ),
        )
        reply = (getattr(response, "text", None) or "").strip()
        if not reply:
            raise HTTPException(status_code=502, detail="AI returned an empty response. Please try again.")
        return ChatResponse(reply=reply, mode="gemini")
    except HTTPException:
        raise
    except Exception as exc:
        print(f"Gemini request failed: {type(exc).__name__}")
        raise HTTPException(
            status_code=502,
            detail="Gemini request failed. Check the server key, API access, model availability, and quota."
        ) from None
