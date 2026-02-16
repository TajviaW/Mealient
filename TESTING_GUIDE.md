# Mealient Testing Guide

**Last Updated**: 2026-02-14
**Purpose**: Comprehensive testing instructions for modernized Mealient app

---

## 🚀 Quick Start

### 1. Build and Install

```bash
# Clean build
./gradlew clean assembleDebug

# Install to connected device/emulator
./gradlew installDebug

# Or build and install in one step
./gradlew clean installDebug
```

**APK Location**: `app/build/outputs/apk/debug/app-debug.apk`

### 2. Run Tests

```bash
# Run all unit tests
./gradlew test

# Run all instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest

# Run Detekt static analysis
./gradlew detekt
```

---

## 📱 Testing Material 3 Adaptive Layouts

### Phone Testing (Compact Width)

**Devices**: Any phone in portrait mode

1. Launch app and navigate to Recipes screen
2. **Expected**: Single column list view
3. Rotate to landscape
4. **Expected**: Should switch to 2-column grid (if screen is wide enough)

### Tablet Testing (Medium/Expanded Width)

**Devices**: 7" tablets, 10" tablets, foldables

1. **Tablet Portrait** (Medium Width):
   - Navigate to Recipes screen
   - **Expected**: 2-column grid layout
   - Items should be evenly spaced with proper padding

2. **Tablet Landscape** (Expanded Width):
   - Rotate tablet to landscape
   - **Expected**: 3-column grid layout
   - More efficient use of screen space

### Desktop/Chrome OS Testing

**Devices**: Chromebooks, Android desktop mode

1. Resize window to different widths
2. **Expected**: Layout adapts dynamically:
   - < 600dp width: 1 column
   - 600dp - 840dp: 2 columns
   - > 840dp: 3 columns

### Testing with Android Studio Emulators

Create emulators with different configurations:

```bash
# Phone (Compact)
- Pixel 6 (411 x 915 dp)
- Pixel 4a (393 x 851 dp)

# Tablet (Medium)
- Pixel Tablet (1280 x 800 dp)
- 7" Tablet (600 x 1024 dp)

# Large Tablet (Expanded)
- 10" Tablet (1280 x 800 dp)
- Foldable Unfolded (1768 x 2208 dp)
```

**How to verify**:
- Open each emulator
- Check that grid columns match expected count
- Verify smooth transitions between orientations

---

## 🔌 Testing Offline-First Database

### Setup: Enable Developer Options

1. Go to Settings → About
2. Tap "Build number" 7 times to enable developer options
3. Go to Settings → Developer options

### Test Scenario 1: View Cached Data Offline

1. **With network connection**:
   - Launch app and browse recipes
   - View several recipes in detail
   - Browse shopping lists
   - **Expected**: Data loads from network and caches to database

2. **Enable airplane mode**:
   - Swipe down notification shade
   - Enable airplane mode (or Settings → Network → Airplane mode)

3. **Verify offline access**:
   - Force close app (Settings → Apps → Mealient → Force stop)
   - Relaunch app
   - Navigate to previously viewed recipes
   - **Expected**: Recipes load instantly from local database
   - **Expected**: No network error messages for cached data

### Test Scenario 2: Database Persistence

1. Browse 10+ recipes with network on
2. Force close app
3. Clear app from recent apps
4. Enable airplane mode
5. Relaunch app
6. **Expected**: All previously viewed recipes available offline

### Test Scenario 3: Shopping Lists (Foundation Only)

> **Note**: Shopping lists repository layer is not yet implemented (Phase 2 pending).
> Current test verifies database schema only.

1. Check database is created:
   ```bash
   adb shell
   run-as gq.kirmanak.mealient
   ls databases/
   # Expected: app.db file exists
   ```

2. Verify schema version:
   ```bash
   sqlite3 databases/app.db
   PRAGMA user_version;
   # Expected: 14
   .tables
   # Expected: shopping_lists, shopping_list_items, meal_plans tables exist
   ```

### Test Scenario 4: Database Migration

1. **Install previous version** (if available):
   ```bash
   adb install app-v1.0.apk
   ```

2. Use app to create some data

3. **Install new version**:
   ```bash
   adb install -r app-debug.apk
   ```

4. **Expected**:
   - App launches successfully
   - Database migrates from v13 → v14
   - No data loss (using fallbackToDestructiveMigration for now)

---

## 🎮 Testing Haptic Feedback

### Prerequisites

- Physical device (emulators may not support haptics)
- Device vibration enabled (Settings → Sound → Vibration)

### Test Locations

1. **Recipe List**:
   - Long-press on any recipe card
   - **Expected**: Light haptic pulse

2. **Recipe Details**:
   - Tap favorite button (star icon)
   - **Expected**: Light haptic pulse on favorite/unfavorite

3. **Shopping Lists** (when implemented):
   - Check/uncheck items
   - **Expected**: Light haptic confirmation

4. **Pull-to-Refresh**:
   - Pull down on recipe list
   - Release to trigger refresh
   - **Expected**: Light haptic at release point

### Verify Haptic Settings Respect System

1. Disable vibration in system settings
2. Perform actions listed above
3. **Expected**: No haptic feedback (respects system settings)

---

## 🔄 Testing Pull-to-Refresh

### Material 3 Pull-to-Refresh

1. Navigate to Recipes screen
2. Scroll to top of list
3. Pull down on the list
4. **Expected**:
   - Circular progress indicator appears
   - Smooth animation follows finger
   - Indicator shows loading state

5. Release finger
6. **Expected**:
   - List refreshes
   - Loading indicator animates
   - New data appears (if any)

### Test on Different Screens

- [x] Recipe list
- [x] Recipe search results
- [x] Shopping lists (when implemented)
- [x] Meal plans (when implemented)

### Edge Cases

1. **Pull while already loading**:
   - Navigate to recipes
   - Wait for initial load to start
   - Try to pull-to-refresh
   - **Expected**: Refresh indicator appears but doesn't trigger duplicate request

2. **Pull at bottom of list**:
   - Scroll to bottom
   - Try to pull down
   - **Expected**: No refresh (only works at top)

3. **Fast swipe vs slow drag**:
   - Try fast swipe down
   - Try slow drag down
   - **Expected**: Both trigger refresh correctly

---

## 🔍 Testing with Detekt Static Analysis

### Run Full Analysis

```bash
./gradlew detekt
```

**Expected**: Build succeeds with baseline violations only

### View Report

```bash
# HTML report
open build/reports/detekt/detekt.html

# XML report (for CI/CD)
cat build/reports/detekt/detekt.xml
```

### Test Individual Modules

```bash
./gradlew :app:detekt
./gradlew :database:detekt
./gradlew :ui:detekt
./gradlew :datasource:detekt
```

### Update Baselines (if needed)

```bash
./gradlew detektBaseline
```

**When to update**:
- After fixing detekt violations
- After adding new code that passes detekt
- NOT for new violations (fix them instead)

---

## 🧪 Manual Testing Scenarios

### Scenario 1: First-Time User Flow

1. **Install fresh**:
   ```bash
   adb uninstall gq.kirmanak.mealient
   ./gradlew installDebug
   ```

2. Launch app
3. **Expected**: Base URL setup screen
4. Enter valid Mealie server URL
5. **Expected**: Login screen appears
6. Enter credentials and log in
7. **Expected**: Recipe list loads
8. Browse recipes
9. Force close and reopen
10. **Expected**: Recipes load from cache instantly

### Scenario 2: Network Interruption

1. Launch app with network on
2. Start browsing recipes
3. While scrolling, enable airplane mode
4. **Expected**:
   - Cached items continue to display
   - New network requests show error
   - App doesn't crash

### Scenario 3: Screen Rotation

1. Navigate to recipe details
2. Rotate device multiple times
3. **Expected**:
   - State persists (scroll position, data)
   - Layout adapts correctly
   - No crashes or data loss

### Scenario 4: Memory Pressure

1. Enable "Don't keep activities" in Developer Options
2. Launch Mealient
3. Browse several screens
4. Switch to another app
5. Return to Mealient
6. **Expected**:
   - App restores state
   - Data reloads from cache
   - No blank screens

### Scenario 5: Large Dataset

1. Connect to Mealie server with 500+ recipes
2. Browse recipe list
3. **Expected**:
   - Pagination works smoothly
   - Scroll performance is good
   - No memory issues

---

## 📊 Performance Testing

### Check Frame Rendering

1. Enable GPU rendering profile in Developer Options
2. Set to "On screen as bars"
3. Navigate through app
4. **Expected**: Most bars stay below 16ms line (60fps)

### Check Memory Usage

```bash
# Monitor memory while using app
adb shell dumpsys meminfo gq.kirmanak.mealient

# Check for memory leaks
adb shell dumpsys meminfo gq.kirmanak.mealient | grep TOTAL
```

### Check Database Performance

```bash
adb shell
run-as gq.kirmanak.mealient
sqlite3 databases/app.db

# Check indexes exist
.indices shopping_list_items
# Expected: index_shopping_list_items_item_list_id

# Check query plans
EXPLAIN QUERY PLAN
SELECT * FROM shopping_list_items WHERE item_list_id = 'test';
# Expected: Uses index
```

---

## 🐛 Known Issues to Watch For

### Issue 1: Database Version Conflicts

**Symptom**: App crashes on launch with "Migration" error

**Fix**:
```bash
adb shell pm clear gq.kirmanak.mealient
# Or uninstall and reinstall
```

### Issue 2: Adaptive Layout Doesn't Update

**Symptom**: Grid columns don't change on rotation

**Check**:
- Is activity recreated on rotation? (android:configChanges in manifest)
- Are recompositions happening? (Add logs to rememberWindowSize())

### Issue 3: Haptics Not Working

**Check**:
- Device vibration enabled?
- Running on physical device (not emulator)?
- System haptic feedback enabled?

---

## 📝 Test Checklists

### Before Release Checklist

- [ ] App builds without errors
- [ ] Detekt passes with no new violations
- [ ] All unit tests pass (`./gradlew test`)
- [ ] Adaptive layouts work on 3+ screen sizes
- [ ] Pull-to-refresh works on all list screens
- [ ] Haptic feedback works on physical device
- [ ] Database creates successfully
- [ ] Offline mode shows cached data
- [ ] No crashes on rotation
- [ ] Network errors handled gracefully

### Code Quality Checklist

- [ ] No compiler warnings
- [ ] Detekt report is clean
- [ ] No memory leaks (check with LeakCanary if added)
- [ ] Proper error handling in all network calls
- [ ] Loading states show progress indicators
- [ ] Empty states show helpful messages

---

## 🔧 Troubleshooting

### Build Fails

```bash
# Clean and rebuild
./gradlew clean
./gradlew build

# Check for dependency conflicts
./gradlew app:dependencies
```

### App Won't Install

```bash
# Uninstall old version
adb uninstall gq.kirmanak.mealient

# Install new version
./gradlew installDebug
```

### Database Errors

```bash
# View logs
adb logcat | grep -i "room\|database\|sql"

# Clear app data
adb shell pm clear gq.kirmanak.mealient
```

### Detekt Fails

```bash
# See what violations exist
./gradlew detekt --continue

# Check specific module
./gradlew :app:detekt --info
```

---

## 📚 Additional Resources

### Emulator Setup

1. Open Android Studio → Device Manager
2. Create Virtual Device
3. Select hardware profile (Pixel 6, Pixel Tablet, etc.)
4. Select system image (API 34 recommended)
5. Finish and launch emulator

### ADB Commands Cheat Sheet

```bash
# List connected devices
adb devices

# Install APK
adb install path/to/app.apk

# Uninstall app
adb uninstall gq.kirmanak.mealient

# View logs
adb logcat

# Clear app data
adb shell pm clear gq.kirmanak.mealient

# Enable/disable network
adb shell svc wifi disable
adb shell svc wifi enable
adb shell svc data disable
adb shell svc data enable

# Take screenshot
adb shell screencap /sdcard/screenshot.png
adb pull /sdcard/screenshot.png

# Screen recording
adb shell screenrecord /sdcard/demo.mp4
# (Stop with Ctrl+C after 3 seconds)
adb pull /sdcard/demo.mp4
```

---

## 🎯 Next Steps

### Phase 1 (Current): Foundation Testing
- [x] Build succeeds
- [x] Database schema created
- [x] Adaptive layouts working
- [ ] **Manual testing on physical devices**
- [ ] **Performance profiling**

### Phase 2 (Next): Repository Implementation
- [ ] Implement offline-first repositories
- [ ] Add sync worker
- [ ] Test offline create/update/delete operations
- [ ] Test conflict resolution

### Phase 3 (Future): Mealie v2 Migration
- [ ] Update endpoints from /groups/ to /households/
- [ ] Add meal planning UI
- [ ] Test with Mealie v2 server
- [ ] Version validation testing

---

**Questions or issues?** Check the implementation docs:
- `OFFLINE_FIRST_IMPLEMENTATION.md` - Database details
- `MEALIE_V2_FEATURES.md` - API migration plan
