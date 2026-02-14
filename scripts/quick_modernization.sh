#!/bin/bash
# Quick Modernization Script for Mealient
# Implements easy, low-risk improvements

set -e

echo "🚀 Starting Quick Modernization..."

# 1. Update SDK versions
echo "📱 Updating SDK versions to Android 15..."
sed -i 's/const val TARGET_SDK_VERSION = 34/const val TARGET_SDK_VERSION = 35/' \
    build-logic/convention/src/main/kotlin/gq/kirmanak/mealient/Versions.kt
sed -i 's/const val COMPILE_SDK_VERSION = 34/const val COMPILE_SDK_VERSION = 35/' \
    build-logic/convention/src/main/kotlin/gq/kirmanak/mealient/Versions.kt

# 2. Update Compose BOM
echo "🎨 Updating Compose BOM..."
sed -i 's/composeBom = "2024.09.03"/composeBom = "2024.12.01"/' \
    gradle/libs.versions.toml

# 3. Update Kotlin Coroutines
echo "⚡ Updating Kotlin Coroutines..."
sed -i 's/coroutines = "1.8.1"/coroutines = "1.9.0"/' \
    gradle/libs.versions.toml

# 4. Enable R8 full mode
echo "📦 Enabling R8 full mode for better optimization..."
if ! grep -q "android.enableR8.fullMode" gradle.properties; then
    echo "android.enableR8.fullMode=true" >> gradle.properties
fi

# 5. Enable configuration cache
echo "⚙️  Enabling Gradle configuration cache..."
if ! grep -q "org.gradle.configuration-cache" gradle.properties; then
    echo "org.gradle.configuration-cache=true" >> gradle.properties
fi

echo "✅ Quick modernization complete!"
echo ""
echo "Next steps:"
echo "1. Run: ./gradlew clean build"
echo "2. Test the app thoroughly"
echo "3. Review MODERNIZATION_RECOMMENDATIONS.md for more improvements"
