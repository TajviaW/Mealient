# Mealie v2 API Features & Implementation Opportunities

**Research Date**: 2026-02-14
**Status**: Comprehensive analysis complete

---

## ✅ Already Implemented in Mealient

- [x] Version detection and v2 validation
- [x] Shopping lists (CRUD operations)
- [x] Meal plans (viewing, backend CRUD ready)
- [x] Recipe management (list, view, create, update, delete)
- [x] User authentication and API tokens
- [x] Favorites system
- [x] Household-aware endpoints (/api/households/*)

---

## 🆕 New Mealie v2 Features Not Yet Implemented

### High Priority - User-Facing Features

#### 1. **Recipe Tags & Categories** 📁
**API Endpoints**:
- `GET /api/organizers/tags`
- `GET /api/organizers/categories`
- `GET /api/organizers/tools`
- `POST /api/recipes/{id}/tags`

**Implementation Value**: HIGH
- Better recipe organization
- Filtering recipes by category
- Tag-based search
- User-requested feature

**Effort**: Low-Medium (2-3 days)

---

#### 2. **Cookbooks** 📚
**API Endpoints**:
- `GET /api/households/cookbooks`
- `POST /api/households/cookbooks`
- `GET /api/households/cookbooks/{id}`

**Description**: Curated collections of recipes with query filters
**Implementation Value**: HIGH
- Organize recipes into themed collections
- Share recipe collections
- Quick access to favorite recipe groups

**Effort**: Medium (3-5 days)

---

#### 3. **Recipe Extras (Custom Properties)** 🔧
**API Feature**: Custom JSON key-value pairs on recipes
**Use Cases**:
- Store custom nutritional data
- Third-party app integration
- Custom recipe metadata
- Automation workflows

**Implementation Value**: MEDIUM
**Effort**: Low (1-2 days)

---

#### 4. **Advanced Query Filters** 🔍
**Features**:
- Logical operators (AND, OR) with grouping
- Nested property filtering
- SQL-style operators (IN, LIKE, CONTAINS)
- Placeholder keywords (`$NOW` with time offsets)

**Implementation Value**: HIGH
- Power user feature
- Advanced recipe search
- Smart meal plan filtering
- Cookbook automation

**Effort**: Medium-High (5-7 days)

---

#### 5. **Webhooks & Notifiers** 🔔
**API Endpoints**:
- `GET /api/households/webhooks`
- `POST /api/households/webhooks`
- Automatic notifications for meal plans

**Use Cases**:
- Home Assistant integration
- Slack/Discord notifications
- Shopping list reminders
- Meal plan alerts

**Implementation Value**: MEDIUM
**Effort**: Medium (3-4 days)

---

### Medium Priority - Enhancement Features

#### 6. **Timeline Events** 📅
**API Endpoint**: `/api/recipes/{id}/timeline`
**Description**: Track recipe history and modifications
**Implementation Value**: LOW-MEDIUM
**Effort**: Low (1-2 days)

---

#### 7. **Foods & Units Management** 🥕
**API Endpoints**:
- `GET /api/foods` (already used for autocomplete)
- `POST /api/foods`
- `GET /api/units`
- `POST /api/units`

**Current**: Read-only usage
**Enhancement**: Allow users to manage foods and units
**Implementation Value**: MEDIUM
**Effort**: Medium (3-4 days)

---

#### 8. **Recipe Sharing Permissions** 🔐
**Feature**: Cross-household recipe browsing with permissions
**API**: Built into household system
**Implementation Value**: LOW (single household app)
**Effort**: Low (1-2 days if needed)

---

#### 9. **OpenAI Integration** 🤖
**API**: Custom headers/parameters for OpenAI
**Use Cases**:
- AI-generated meal plans
- Recipe suggestions
- Ingredient substitutions
- Cooking tips

**Implementation Value**: MEDIUM-HIGH
**Effort**: Medium (4-5 days)

---

### Low Priority - Nice-to-Have

#### 10. **Recipe Creation from HTML/JSON**
**Current**: Only URL scraping
**Enhancement**: Direct HTML or JSON import
**Implementation Value**: LOW
**Effort**: Low (1-2 days)

---

#### 11. **Recipe Tools Management**
**API**: `/api/organizers/tools`
**Description**: Track cooking tools needed per recipe
**Implementation Value**: LOW
**Effort**: Low (1-2 days)

---

#### 12. **Nutrition Enhancements**
**Feature**: Extended nutrition properties from schema.org
**Implementation Value**: LOW-MEDIUM
**Effort**: Medium (2-3 days)

---

## 🎯 Recommended Implementation Priority

### Phase 1: Core Organization (Week 1-2)
1. ✅ Recipe Tags & Categories
2. ✅ Advanced Recipe Search/Filtering
3. ✅ Cookbooks

**Impact**: Major UX improvement for recipe organization

---

### Phase 2: Automation & Integration (Week 3-4)
4. ✅ Webhooks & Notifiers
5. ✅ Recipe Extras (custom properties)
6. ✅ Timeline Events

**Impact**: Power user features, integration capabilities

---

### Phase 3: Management Tools (Month 2)
7. ✅ Foods & Units Management
8. ✅ Recipe Tools
9. ✅ OpenAI Integration (if desired)

**Impact**: Advanced features for recipe management

---

## 📊 Feature Comparison Matrix

| Feature | User Value | Dev Effort | Priority |
|---------|-----------|------------|----------|
| Tags & Categories | ⭐⭐⭐⭐⭐ | 🔨🔨 | HIGH |
| Cookbooks | ⭐⭐⭐⭐⭐ | 🔨🔨🔨 | HIGH |
| Advanced Filters | ⭐⭐⭐⭐ | 🔨🔨🔨🔨 | HIGH |
| Webhooks | ⭐⭐⭐ | 🔨🔨🔨 | MEDIUM |
| Recipe Extras | ⭐⭐⭐ | 🔨 | MEDIUM |
| Foods/Units Mgmt | ⭐⭐⭐ | 🔨🔨🔨 | MEDIUM |
| Timeline Events | ⭐⭐ | 🔨 | LOW |
| Recipe Sharing | ⭐⭐ | 🔨🔨 | LOW |
| OpenAI Integration | ⭐⭐⭐⭐ | 🔨🔨🔨 | MEDIUM |

---

## 🛠️ Technical Implementation Notes

### API Access Pattern
All endpoints follow Mealie's standard pattern:
```
GET /api/households/{householdId}/{resource}
POST /api/households/{householdId}/{resource}
PUT /api/households/{householdId}/{resource}/{id}
DELETE /api/households/{householdId}/{resource}/{id}
```

### Pagination & Filtering
Standard params across endpoints:
- `page`, `perPage`
- `queryFilter` (advanced filtering)
- `orderBy`, `orderDirection`
- `orderByNullPosition`

### Authentication
- Long-lived API tokens (current implementation)
- Tokens managed at `/user/profile/api-tokens`

---

## 📚 API Documentation Sources

- [Mealie API Docs](https://docs.mealie.io/documentation/getting-started/api-usage/)
- [Mealie Features](https://docs.mealie.io/documentation/getting-started/features/)
- [v2.0.0 Release Notes](https://github.com/mealie-recipes/mealie/releases/tag/v2.0.0)
- Interactive API Docs: `{your-mealie-instance}/docs`

---

## 🎬 Next Steps

1. **Immediate**: Implement Recipe Tags & Categories
2. **Short-term**: Add Cookbooks support
3. **Medium-term**: Advanced filtering system
4. **Long-term**: Webhooks and automation features

---

**Last Updated**: 2026-02-14
**Researched By**: Claude Sonnet 4.5
