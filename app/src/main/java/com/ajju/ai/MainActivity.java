package com.ajju.ai;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int SPEECH_REQUEST = 101;
    private LinearLayout messages;
    private ScrollView scrollView;
    private EditText input;
    private TextToSpeech tts;
    private boolean ttsReady = false;
    private boolean nativeReady = false;

    static {
        try {
            System.loadLibrary("ajju_native");
        } catch (UnsatisfiedLinkError ignored) {
            // Java fallback keeps the app usable if the native library is unavailable.
        }
    }

    private native String classifyCommand(String text);

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(10, 14, 28));
        getWindow().setNavigationBarColor(Color.rgb(10, 14, 28));
        buildUi();
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(new Locale("hi", "IN"));
                ttsReady = result != TextToSpeech.LANG_MISSING_DATA
                        && result != TextToSpeech.LANG_NOT_SUPPORTED;
            }
        });
        try {
            classifyCommand("hello");
            nativeReady = true;
        } catch (UnsatisfiedLinkError ignored) {
            nativeReady = false;
        }
        addMessage("Ajju AI", "नमस्ते! मैं Ajju AI हूँ। माइक दबाकर बोलो या नीचे अपना मैसेज लिखो।", false);
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(14), dp(16), dp(12));
        root.setBackgroundColor(Color.rgb(10, 14, 28));

        TextView title = new TextView(this);
        title.setText("✦  AJJU AI");
        title.setTextColor(Color.rgb(157, 190, 255));
        title.setTextSize(24);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        root.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("PERSONAL VOICE ASSISTANT  •  HINDI + ENGLISH");
        subtitle.setTextColor(Color.rgb(145, 155, 180));
        subtitle.setTextSize(10);
        subtitle.setPadding(0, dp(4), 0, dp(14));
        root.addView(subtitle);

        scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        messages = new LinearLayout(this);
        messages.setOrientation(LinearLayout.VERTICAL);
        messages.setPadding(dp(2), dp(4), dp(2), dp(8));
        scrollView.addView(messages);
        root.addView(scrollView, new LinearLayout.LayoutParams(-1, 0, 1));

        LinearLayout composer = new LinearLayout(this);
        composer.setGravity(Gravity.CENTER_VERTICAL);
        composer.setPadding(0, dp(10), 0, 0);

        input = new EditText(this);
        input.setSingleLine(false);
        input.setMinLines(1);
        input.setMaxLines(3);
        input.setHint("Ajju se kuch poochho...");
        input.setHintTextColor(Color.rgb(130, 140, 160));
        input.setTextColor(Color.WHITE);
        input.setTextSize(15);
        input.setPadding(dp(14), dp(10), dp(14), dp(10));
        input.setBackgroundColor(Color.rgb(27, 34, 55));
        composer.addView(input, new LinearLayout.LayoutParams(0, -2, 1));

        Button mic = new Button(this);
        mic.setText("🎙");
        mic.setTextSize(18);
        composer.addView(mic, new LinearLayout.LayoutParams(dp(56), dp(52)));
        mic.setOnClickListener(v -> startSpeechInput());

        Button send = new Button(this);
        send.setText("➤");
        send.setTextSize(18);
        composer.addView(send, new LinearLayout.LayoutParams(dp(56), dp(52)));
        send.setOnClickListener(v -> sendMessage());

        root.addView(composer);
        setContentView(root);
    }

    private void startSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Ajju AI se bolo...");
        try {
            startActivityForResult(intent, SPEECH_REQUEST);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(this, "Is phone par speech recognition app available nahi hai.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SPEECH_REQUEST && resultCode == RESULT_OK && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                input.setText(results.get(0));
                sendMessage();
            }
        }
    }

    private void sendMessage() {
        String text = input.getText().toString().trim();
        if (text.isEmpty()) return;
        input.setText("");
        addMessage("You", text, true);
        String response = handleCommand(text);
        addMessage("Ajju AI", response, false);
        if (ttsReady) tts.speak(response, TextToSpeech.QUEUE_FLUSH, null, "ajju-reply");
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(input.getWindowToken(), 0);
    }

    private String handleCommand(String text) {
        String lower = text.toLowerCase(Locale.ROOT);
        String kind = "CHAT";
        if (nativeReady) {
            try { kind = classifyCommand(text); } catch (UnsatisfiedLinkError ignored) {}
        } else if (lower.contains("call") || lower.contains("कॉल")) {
            kind = "CALL";
        } else if (lower.contains("message") || lower.contains("मैसेज") || lower.contains("sms")) {
            kind = "MESSAGE";
        }

        if ("CALL".equals(kind)) {
            String number = text.replaceAll("[^0-9+]", "");
            if (number.length() >= 7) {
                try {
                    startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(number))));
                    return "Dialer khol diya hai. Number check karke call button tum khud dabao.";
                } catch (Exception ex) {
                    return "Phone dialer nahi khul saka. Phone app se number dial karo.";
                }
            }
            return "Call karne ke liye number likho, jaise: call 9876543210. Main dialer kholunga; call tum confirm karoge.";
        }

        if ("MESSAGE".equals(kind)) {
            return "SMS draft kholne ke liye number aur message is format mein bhejo: sms 9876543210 | Namaste. Sending se pehle tum message check karoge.";
        }

        if (lower.startsWith("sms ") || lower.startsWith("message ")) {
            String rest = text.substring(text.indexOf(' ') + 1);
            String[] parts = rest.split("\\|", 2);
            String number = parts[0].replaceAll("[^0-9+]", "");
            String body = parts.length > 1 ? parts[1].trim() : "";
            if (number.length() >= 7) {
                Intent sms = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + Uri.encode(number)));
                sms.putExtra("sms_body", body);
                try {
                    startActivity(sms);
                    return "SMS draft khul gaya hai. Bhejne se pehle recipient aur text check kar lo.";
                } catch (Exception ex) {
                    return "SMS app nahi khul saki.";
                }
            }
        }

        if (lower.contains("time") || lower.contains("समय") || lower.contains("टाइम")) {
            return "Phone ki status bar mein current time dekh sakte ho. Main abhi basic starter mode mein hoon.";
        }
        return "Main abhi starter mode mein hoon. Main voice input, Hindi voice reply aur safe dialer/SMS drafts support karta hoon. Full AI answers ke liye secure backend/API setup agle phase mein karna hoga.";
    }

    private void addMessage(String who, String body, boolean user) {
        LinearLayout bubble = new LinearLayout(this);
        bubble.setOrientation(LinearLayout.VERTICAL);
        bubble.setPadding(dp(14), dp(10), dp(14), dp(10));
        bubble.setBackgroundColor(user ? Color.rgb(43, 66, 112) : Color.rgb(25, 32, 51));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                user ? (int)(getResources().getDisplayMetrics().widthPixels * 0.78f) : -1, -2);
        params.gravity = user ? Gravity.RIGHT : Gravity.LEFT;
        params.setMargins(0, dp(5), 0, dp(5));
        TextView label = new TextView(this);
        label.setText(who);
        label.setTextColor(Color.rgb(157, 190, 255));
        label.setTextSize(11);
        label.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        TextView content = new TextView(this);
        content.setText(body);
        content.setTextColor(Color.WHITE);
        content.setTextSize(15);
        content.setPadding(0, dp(4), 0, 0);
        bubble.addView(label);
        bubble.addView(content);
        messages.addView(bubble, params);
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
