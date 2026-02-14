# Mealient v2 Migration - Suggested Improvements

## High Priority

### 1. Complete Meal Plans UI
**Status**: Backend complete, UI basic
**Missing**:
- [ ] Create meal plan dialog with:
  - Date picker
  - Meal type selector (breakfast/lunch/dinner/snack)
  - Recipe search/picker
  - Title and notes fields
- [ ] Edit meal plan dialog (pre-populated with existing data)
- [ ] Delete confirmation dialog
- [ ] Week navigation (Previous/Next week buttons)
- [ ] Recipe picker integration
- [ ] Empty state with helpful message and CTA
- [ ] Pull-to-refresh gesture
- [ ] Loading states and error handling
- [ ] Meal plan click navigation to recipe details

### 2. Add Meal Plans to Navigation Drawer
**Current**: Meal plans screen exists but no navigation entry
**Needed**:
- Add "Meal Plans" menu item to DrawerContent.kt
- Add appropriate icon (Calendar or similar)
- Wire up navigation to MealPlansScreenDestination

### 3. Localization (i18n)
**Status**: Hardcoded English strings in meal plans
**Needed**: Create string resources in:
- `features/meal_plans/src/main/res/values/strings.xml`
- Strings needed:
  - Screen title, empty state, error messages
  - Button labels, dialog titles
  - Meal type labels, date formats

### 4. Fix Failing Unit Tests
**Status**: Tests failing in datasource module
**Action**: Update tests to account for new v2 endpoints and models

## Medium Priority

### 5. Server Information Display
**Purpose**: Help users understand their server version
**Implementation**:
- Create Settings/About screen
- Display:
  - Server URL
  - Server version (e.g., "v2.1.0")
  - API compatibility status
  - App version
- Add to navigation drawer

### 6. Enhanced Error Handling
**Current**: Basic error messages
**Improvements**:
- Retry mechanisms for network failures
- Offline mode indicators
- More specific error messages (e.g., "Recipe not found in meal plan")
- Toast notifications for quick actions
- Better error recovery flows

### 7. Meal Plans Enhancements
**Features**:
- Filter by meal type (breakfast/lunch/dinner/snack)
- Search meal plans
- Copy meal plan to another day
- Bulk operations (delete multiple, move week)
- Calendar view mode (in addition to list view)
- Export meal plans to shopping list
- Meal plan templates/recurring meals

### 8. Performance Optimizations
**Areas**:
- Cache meal plans locally (Room database)
- Pagination for large meal plan ranges
- Image loading optimization for recipe thumbnails
- Lazy loading for off-screen content

## Low Priority

### 9. Code Quality
**Actions**:
- [ ] Fix deprecation warnings (Icons.AutoMirrored usage)
- [ ] Add KDoc comments to public APIs
- [ ] Remove any unused imports
- [ ] Add Proguard/R8 rules if needed
- [ ] Code formatting consistency check

### 10. Accessibility
**Improvements**:
- Content descriptions for all icons
- Semantic labels for screen readers
- Keyboard navigation support
- High contrast mode testing
- Font scaling support

### 11. Analytics & Monitoring
**Add tracking for**:
- Meal plan creation/edit/delete
- Most used meal types
- Server version distribution
- Error rates and types
- Feature adoption metrics

### 12. Advanced Features
**Nice-to-have**:
- Meal plan sharing (export/import)
- Meal prep mode (group by ingredient)
- Nutritional information integration
- Recipe recommendations based on history
- Grocery list generation from meal plans
- Multi-household support (if user has access)

## Testing Checklist

### Manual Testing Needed
- [ ] Fresh install with v2 server → App works
- [ ] Fresh install with v1 server → Clear error shown
- [ ] Shopping lists CRUD operations
- [ ] Meal plans view/create/edit/delete
- [ ] Navigation between screens
- [ ] Offline behavior
- [ ] Screen rotation
- [ ] Low memory scenarios
- [ ] Various Android versions

### Automated Testing Needed
- [ ] Unit tests for ApiVersion parsing
- [ ] Unit tests for ServerInfoRepoImpl version validation
- [ ] Unit tests for MealPlansRepo
- [ ] Unit tests for MealPlansViewModel
- [ ] Integration tests with mock Mealie v2 server
- [ ] UI tests for meal plans screen

## Documentation

### User-Facing
- [ ] Update README with v2 requirements
- [ ] Add migration guide for existing users
- [ ] Update screenshots
- [ ] Add meal plans feature documentation

### Developer-Facing
- [ ] Architecture decision records (ADRs)
- [ ] API documentation
- [ ] Contributing guide updates
- [ ] Release notes for v2 migration

## Release Preparation

### Before Release
- [ ] Bump app version number
- [ ] Update changelog
- [ ] Run full test suite
- [ ] Security audit
- [ ] Performance profiling
- [ ] Beta testing period
- [ ] Play Store assets update
- [ ] Privacy policy review (if data handling changed)

## Estimated Effort

| Category | Time Estimate |
|----------|---------------|
| Complete Meal Plans UI | 3-5 days |
| Navigation & i18n | 1 day |
| Fix Tests | 1-2 days |
| Settings Screen | 1-2 days |
| Enhanced Error Handling | 1-2 days |
| **Total Critical Path** | **7-12 days** |
| Advanced Features | 2-4 weeks |
| Full Testing & QA | 1 week |

## Priority Order Recommendation

1. **Week 1**: Navigation drawer entry, localization, complete meal plans UI
2. **Week 2**: Fix failing tests, enhanced error handling
3. **Week 3**: Settings screen, polish & testing
4. **Week 4+**: Advanced features based on user feedback
