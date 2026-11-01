# Runner Adventure

A simple portrait Android endless-runner game built with Java and a custom Canvas view.

## Features
- Android 9 (API 28) through Android 14 (API 34) compatible minimum/target range
- Tap to jump over obstacles
- Collect coins
- Score, levels, and saved best score
- About screen with creator information
- GitHub Actions workflow builds a debug APK

## Build with GitHub
1. Create a GitHub repository.
2. Upload the contents of this project, keeping the folder structure.
3. Open the Actions tab. If prompted, enable GitHub Actions.
4. Run the workflow named **Build Android APK** (or push to `main`).
5. Open the completed workflow run and download the `RunnerAdventure-APK` artifact.
6. Extract the downloaded ZIP to get `app-debug.apk`.

The APK is a debug build for testing. For Google Play distribution, create a signed release build.
