#!/bin/bash
# Build script for 123panNextGen Android APK
# Requires: Android SDK with build-tools, Android NDK

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ANDROID_DIR="$SCRIPT_DIR/android"

echo "=== 123panNextGen Android Builder ==="
echo ""

# Check for Android SDK
if [ -z "$ANDROID_HOME" ] && [ -z "$ANDROID_SDK_ROOT" ]; then
    echo "Error: ANDROID_HOME or ANDROID_SDK_ROOT not set."
    echo "Please set it, e.g.:"
    echo "  export ANDROID_HOME=\$HOME/Android/Sdk"
    echo ""
    echo "Or build using Android Studio:"
    echo "  1. Open the 'android' directory in Android Studio"
    echo "  2. Wait for Gradle sync to complete"
    echo "  3. Build > Build Bundle(s) / APK(s) > Build APK(s)"
    echo "  4. Find APK at android/app/build/outputs/apk/debug/"
    exit 1
fi

SDK_DIR="${ANDROID_HOME:-$ANDROID_SDK_ROOT}"

# Check for gradle wrapper
if [ ! -f "$ANDROID_DIR/gradlew" ]; then
    echo "Downloading Gradle wrapper..."
    cd "$ANDROID_DIR"
    # Create minimal gradlew
    cat > gradlew << 'GRADLEOF'
#!/bin/sh
# Gradle wrapper script
APP_NAME="Gradle"
APP_BASE_NAME=$(basename "$0")
DIRNAME=$(dirname "$0")
GRADLE_HOME="${DIRNAME}/gradle/wrapper/gradle-wrapper.jar"
if [ ! -f "$GRADLE_HOME" ]; then
    echo "Downloading Gradle wrapper jar..."
    GRADLE_VERSION=$(grep 'distributionUrl' "${DIRNAME}/gradle/wrapper/gradle-wrapper.properties" | sed 's/.*gradle-\([0-9.]*\)-.*/\1/')
    WRAPPER_URL="https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"
    echo "Need Gradle ${GRADLE_VERSION}. Please install Gradle and run:"
    echo "  cd ${DIRNAME} && gradle wrapper --gradle-version ${GRADLE_VERSION}"
    exit 1
fi
java -jar "$GRADLE_HOME" "$@"
GRADLEOF
    chmod +x gradlew
    echo "Gradle wrapper script created at gradlew"
    echo "Run './gradlew assembleDebug' to build the APK"
fi

echo "Building APK..."
cd "$ANDROID_DIR"

# Try to use gradle wrapper
if [ -f "gradlew" ]; then
    ./gradlew assembleDebug
else
    gradle assembleDebug
fi

echo ""
echo "=== Build Complete ==="
echo "APK location: $ANDROID_DIR/app/build/outputs/apk/debug/"
ls -la "$ANDROID_DIR/app/build/outputs/apk/debug/" 2>/dev/null || echo "(build may have failed)"