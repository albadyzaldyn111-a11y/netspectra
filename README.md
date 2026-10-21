# NetSpectra

NetSpectra is an Android network-usage monitoring and data-management application focused on practical visibility into device traffic and app-level consumption.

## Project highlights

- Android application built with Kotlin and Gradle.
- Network traffic and data-usage monitoring foundations.
- Per-application usage insights and configurable data limits.
- Firewall and control-rule concepts for managing network access.
- Reporting and notification foundations for data consumption.

## Technology stack

- Kotlin
- Android SDK
- Gradle Kotlin DSL
- Android platform networking and usage APIs

## Getting started

Open the project in Android Studio, allow Gradle to sync, and run the `app` module on a compatible Android device or emulator.

```bash
git clone https://github.com/albadyzaldyn111-a11y/netspectra.git
cd netspectra
./gradlew assembleDebug
```

## Privacy and permissions

Network and usage-monitoring features require explicit Android permissions and should be tested transparently on the target device. The project must not collect or transmit personal traffic data without clear user consent.

## Status

Active prototype and research project. Device and Android-version support may vary while the monitoring and control features continue to evolve.
