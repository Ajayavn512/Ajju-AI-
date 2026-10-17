#include <jni.h>
#include <string>
#include <algorithm>
#include <cctype>

static std::string lowerAscii(std::string value) {
    std::transform(value.begin(), value.end(), value.begin(),
        [](unsigned char c) { return static_cast<char>(std::tolower(c)); });
    return value;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_ajju_ai_MainActivity_classifyCommand(JNIEnv* env, jobject, jstring input) {
    const char* chars = env->GetStringUTFChars(input, nullptr);
    std::string text = chars ? chars : "";
    if (chars) env->ReleaseStringUTFChars(input, chars);
    std::string lower = lowerAscii(text);
    std::string result = "CHAT";

    if (lower.find("call") != std::string::npos ||
        lower.find("dial") != std::string::npos ||
        lower.find("phone") != std::string::npos) {
        result = "CALL";
    } else if (lower.find("sms") != std::string::npos ||
               lower.find("message") != std::string::npos ||
               lower.find("text ") != std::string::npos) {
        result = "MESSAGE";
    } else if (lower.find("open ") != std::string::npos ||
               lower.find("launch ") != std::string::npos) {
        result = "APP";
    }
    return env->NewStringUTF(result.c_str());
}
