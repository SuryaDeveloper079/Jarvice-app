# J.A.R.V.I.S. AI ASSISTANT

A complete, production-grade native Android personal AI assistant inspired by Tony Stark's Iron Man J.A.R.V.I.S., engineered with **Kotlin**, **Jetpack Compose**, **Google Gemini 3.8 Flash** via **Firebase AI Logic**, Android **SpeechRecognizer**, **TextToSpeech**, **Room Database**, and native system capabilities.

---

## Key Capabilities

### 1. Natural Language Intent Planning & Reasoning Architecture
- **No fixed keyword templates**: JARVIS processes natural human speech, extracts user intentions dynamically, handles context from previous conversations, and selects appropriate device tools.
- **Example**: *"Open WhatsApp and send Rahul a message saying I will arrive tomorrow"* is intelligently parsed into recipient resolution, message composition, and safety confirmation.
- **Clarification Dialog**: Asks the user follow-up questions when critical parameters are missing.

### 2. Dynamic Application Launching
- Dynamically discovers all installed, launchable applications via Android `PackageManager` (using `<queries>` for modern Android package visibility).
- Launches any app by name or alias (e.g., Google Maps, YouTube, WhatsApp, Instagram, Spotify, Chrome, Camera, Calculator, Settings).
- Reports honestly if an application is not installed on the device.

### 3. WhatsApp Integration with Safety Confirmation
- Resolves contact names and phone numbers via `ContactsContract`.
- Prepares message text and recipient contact.
- **Confirmation Screen**: Displays recipient name, phone, and message preview. Requires explicit user confirmation ("SEND VIA WHATSAPP" / "ABORT") before launching WhatsApp.
- Does not falsely claim a message was transmitted without verified user handoff.

### 4. General Phone Automation
- **Phone Calls**: Resolves contacts and prepares calls via the system dialer (`Intent.ACTION_DIAL`) with user confirmation.
- **Tactical Flashlight**: Hardware flashlight toggling via Android `CameraManager.setTorchMode`.
- **Chronometer**: Reads localized date and time in the user's spoken language.
- **Reminders & Timers**: Sets alarms and timers via `AlarmClock.ACTION_SET_TIMER`.
- **System Settings**: Quick navigation to Wi-Fi, Bluetooth, Sound, Display, and Voice Assistant settings.
- **Web Navigation**: Web search and direct URL launching.

### 5. Multilingual AI (15 Languages Supported)
- **Languages**: English, Tamil, Hindi, Telugu, Kannada, Malayalam, Bengali, Marathi, Gujarati, Punjabi, Urdu, Arabic, French, Spanish, and Japanese.
- **Code-Switching**: Recognizes regional mixed speech including **Tanglish** (*"Rahul ku WhatsApp la message anupu"*) and **Hinglish** (*"WhatsApp pe Rahul ko message bhejo"*).
- **Adaptive Speech Output**: Detects language dynamically and outputs speech via Android `TextToSpeech` with matching locale and pitch/speed calibration.
- **Language Selector**: Manual language picker with native language names + automatic detection.

### 6. Wake-Word & System Integration
- **Mode A (In-App)**: Automatically activates audio sensors upon app launch once microphone permission is granted.
- **Mode B (VoiceInteractionService)**: Declares Android `VoiceInteractionService` so JARVIS can be selected as the system-level default digital assistant in Android Settings.
- **Background Foreground Service**: Configured with `FOREGROUND_SERVICE_MICROPHONE` and persistent notification with a quick "Deactivate" action.

### 7. Local Persistence (Room Database)
- **`conversations` table**: Records user query, AI reasoning, detected language, tool execution status, and timestamp.
- **`user_preferences` table**: Persists user salutation ("Sir", "Tony", "Boss"), speech rate, pitch, selected language, and wake-word toggles.

---

## Generating & Installing the Debug APK

### Method 1: Via Android Studio / Gradle CLI

1. **Build the Debug APK**:
   ```bash
   gradle :app:assembleDebug
   ```

2. **Locate the Output APK**:
   The generated APK will be at:
   `app/build/outputs/apk/debug/app-debug.apk`

3. **Install on Device via ADB**:
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

### Method 2: Via AI Studio Interface
1. Click the **Settings** or **Export** menu in the top-right toolbar.
2. Select **Export as APK** or **Generate APK**.
3. Download the signed APK directly to your phone or computer.
