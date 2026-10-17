"""Optional Ajju AI backend scaffold. No API keys are stored in this repository."""
import os
from fastapi import FastAPI
from pydantic import BaseModel, Field

app = FastAPI(title="Ajju AI API", version="0.1.0")


class ChatRequest(BaseModel):
    message: str = Field(min_length=1, max_length=4000)


class ChatResponse(BaseModel):
    reply: str
    mode: str


@app.get("/health")
def health():
    return {"status": "ok", "service": "Ajju AI", "mode": "demo"}


@app.post("/chat", response_model=ChatResponse)
def chat(payload: ChatRequest):
    # This safe demo endpoint deliberately does not pretend to be an AI model.
    # Add a real provider here using a server-side environment variable; never ship keys in the APK.
    if not os.getenv("AJJU_AI_API_KEY"):
        return ChatResponse(
            reply="Ajju AI backend is running in demo mode. Configure AJJU_AI_API_KEY on the server to enable a model provider.",
            mode="demo",
        )
    return ChatResponse(
        reply="API key detected. Provider integration is not configured yet; add your chosen provider call in backend/app.py.",
        mode="configured-not-connected",
    )
