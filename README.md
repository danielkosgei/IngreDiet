# IngreDiet

A modern **recipe management and meal planning** Android application built with Jetpack Compose, featuring a rich collection of Kenyan cuisine recipes, personalized nutrition tracking, and intelligent shopping list management.

<p align="center">
  <!-- TODO: Add app icon/logo here -->
  <img src="app/src/main/ic_launcher-playstore.png" alt="IngreDiet Logo" width="120"/>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-brightgreen?style=flat-square&logo=android" alt="Platform"/>
  <img src="https://img.shields.io/badge/SDK-24%20to%2035-blue?style=flat-square" alt="SDK"/>
  <img src="https://img.shields.io/badge/Kotlin-2.0.0-purple?style=flat-square&logo=kotlin" alt="Kotlin"/>
  <img src="https://img.shields.io/badge/Compose-2024.04-4285F4?style=flat-square&logo=jetpack-compose" alt="Compose"/>
  <img src="https://img.shields.io/badge/Status-Alpha-orange?style=flat-square" alt="Status"/>
  <img src="https://img.shields.io/badge/License-MIT-blue?style=flat-square" alt="License"/>
</p>

---

## Screenshots

<p align="center">
  <img src="https://github.com/user-attachments/assets/19360c06-ebc9-4416-b411-c86bff548a4a" width="180" alt="Home"/>
  <img src="https://github.com/user-attachments/assets/cf3c508a-b259-4d09-b7ca-b4d704c72d6d" width="180" alt="Recipe Detail"/>
  <img src="https://github.com/user-attachments/assets/8b4fdfad-3f6b-4f8f-9fe2-bb4607e102f5" width="180" alt="Meal Planner"/>
  <img src="https://github.com/user-attachments/assets/2339d8d2-e709-4bdb-9ad2-bf23858a4ce3" width="180" alt="Shopping List"/>
</p>
<p align="center">
  <b>Home</b> · <b>Recipe Detail</b> · <b>Meal Planner</b> · <b>Shopping List</b>
</p>

<p align="center">
  <img src="https://github.com/user-attachments/assets/476afb6f-2392-4946-b793-de495b30c46a" width="180" alt="Nutrition Summary"/>
  <img src="https://github.com/user-attachments/assets/ac33c1f9-9029-44c0-8098-693c7fba12d2" width="180" alt="Recipe Finder"/>
  <img src="https://github.com/user-attachments/assets/07dacab9-67ca-40e5-8b54-4e6203314563" width="180" alt="Cooking Mode"/>
</p>
<p align="center">
  <b>Nutrition Summary</b> · <b>Recipe Finder</b> · <b>Cooking Mode</b>
</p>

---

## Architecture

IngreDiet follows **MVVM (Model-View-ViewModel)** architecture with the **Repository Pattern**, ensuring a clean separation of concerns and testability.

```
┌─────────────────────────────────────────────────────────────────┐
│                         Presentation Layer                      │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────────┐ │
│  │   Screens   │  │  ViewModels │  │  Compose UI Components  │ │
│  │ (Composable)│←→│   (State)   │  │    (Reusable Views)     │ │
│  └─────────────┘  └──────┬──────┘  └─────────────────────────┘ │
└──────────────────────────┼──────────────────────────────────────┘
                           │
┌──────────────────────────┼──────────────────────────────────────┐
│                     Domain Layer                                 │
│  ┌───────────────────────┴────────────────────────────────────┐ │
│  │                     Repositories                            │ │
│  │  • RecipeRepository        • ProfileRepository              │ │
│  │  • FavoritesRepository     • MealPlanRepository             │ │
│  │  • ShoppingListRepository  • NutritionRepository            │ │
│  └───────────────────────┬────────────────────────────────────┘ │
└──────────────────────────┼──────────────────────────────────────┘
                           │
┌──────────────────────────┼──────────────────────────────────────┐
│                       Data Layer                                 │
│  ┌───────────────┐  ┌─────────────────┐  ┌──────────────────┐  │
│  │ Network APIs  │  │  Local Storage  │  │  Cache Services  │  │
│  │  (Supabase)   │  │(SharedPrefs/...)│  │(RecipeCacheServ) │  │
│  └───────────────┘  └─────────────────┘  └──────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

### Project Structure

```
app/src/main/java/com/thenewkenya/ingrediet/
├── IngreDietApplication.kt      # Application class with DI setup
├── MainActivity.kt              # Single Activity with Navigation
│
├── data/
│   ├── model/                   # Data classes
│   │   ├── Recipe.kt           # Recipe, DetailedRecipe, NutritionFacts
│   │   ├── KenyanRecipe.kt     # Kenyan cuisine data models
│   │   ├── Profile.kt          # User profile data
│   │   ├── UserMealPlan.kt     # Meal planning models
│   │   └── ...
│   │
│   ├── network/                 # Network layer
│   │   ├── SupabaseClient.kt   # Supabase configuration
│   │   ├── AuthManager.kt      # Authentication handling
│   │   ├── SessionManager.kt   # Token persistence
│   │   ├── CacheManager.kt     # Caching strategies
│   │   └── ...
│   │
│   └── repository/              # Repository pattern
│       ├── RecipeRepository.kt
│       ├── ProfileRepository.kt
│       ├── FavoritesRepository.kt
│       ├── MealPlanRepository.kt
│       ├── ShoppingListRepository.kt
│       └── NutritionRepository.kt
│
├── feature/                     # Feature modules
│   ├── authentication/          # Login, Register, Password Reset
│   ├── recipe/                  # Recipe browsing & details
│   ├── kenyan/                  # Kenyan cuisine section
│   ├── favorites/               # Saved recipes
│   ├── mealplanner/             # Weekly meal planning
│   ├── shopping/                # Shopping list management
│   ├── search/                  # Recipe & ingredient search
│   ├── profile/                 # User settings & profile
│   ├── create/                  # Recipe creation
│   ├── onboarding/              # First-time user experience
│   ├── notifications/           # Notification management
│   ├── security/                # App lock features
│   └── ...
│
└── ui/theme/                    # Material 3 theming
    ├── Color.kt                # Nutritional color palette
    ├── Theme.kt                # Light/Dark theme config
    └── Type.kt                 # Typography definitions
```

---

## Tech Stack

### Android & UI
| Technology | Version | Purpose |
|------------|---------|---------|
| **Kotlin** | 2.0.0 | Primary language |
| **Jetpack Compose** | 2024.04 BOM | Declarative UI framework |
| **Material 3** | 1.2.1 | Design system |
| **Navigation Compose** | 2.8.8 | Screen navigation |
| **Coil 3** | 3.0.0-alpha04 | Image loading |

### Backend & Data
| Technology | Version | Purpose |
|------------|---------|---------|
| **Supabase** | 3.1.1 BOM | Backend-as-a-Service |
| **Ktor** | 3.0.3 | HTTP client (OkHttp engine) |
| **Kotlinx Serialization** | 1.6.2 | JSON parsing |
| **PostgreSQL** | — | Database (via Supabase) |

### Authentication
| Technology | Purpose |
|------------|---------|
| **Supabase Auth** | Email/password authentication |
| **Android Credentials** | Google Sign-In integration |
| **Biometric API** | Fingerprint/Face unlock |

### Utilities
| Technology | Purpose |
|------------|---------|
| **Kotlin Coroutines** | 1.7.3 | Asynchronous programming |
| **Kotlin Flow** | Reactive data streams |
| **Desugaring** | Java 8+ APIs on older Android |

---

## Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/thenewkenya/IngreDiet.git
cd IngreDiet
```

### 2. Configure API Keys

Create an `apikeys.properties` file in the project root:

```properties
SUPABASE_ANON_KEY=your_supabase_anon_key_here
SUPABASE_URL=https://your-project.supabase.co
```

> ⚠️ **Important**: Never commit `apikeys.properties` to version control. It's already in `.gitignore`.

### 3. Supabase Setup (Optional - for Development)

If you want to run your own Supabase instance:

```bash
# Install Supabase CLI
npm install -g supabase

# Start local Supabase
cd supabase
supabase start

# Apply migrations
supabase db reset
```

### 4. Build and Run

Open the project in Android Studio and:

1. Click **"Sync Project with Gradle Files"** 
2. Select a device or emulator (API 24+)
3. Click **Run**

Or via command line:

```bash
./gradlew assembleDebug
./gradlew installDebug
```

---

## Database Schema

IngreDiet uses PostgreSQL via Supabase with Row Level Security (RLS) enabled on all tables.

### Core Tables

```
┌─────────────────┐     ┌─────────────────────┐     ┌─────────────────┐
│    profiles     │     │    user_favorites   │     │  user_meal_plans│
├─────────────────┤     ├─────────────────────┤     ├─────────────────┤
│ id (UUID, PK)   │──┐  │ id (SERIAL, PK)     │     │ id (UUID, PK)   │
│ email           │  │  │ user_id (FK)────────┼──┐  │ user_id (FK)    │
│ first_name      │  │  │ recipe_id (FK)      │  │  │ day_of_week     │
│ last_name       │  │  │ created_at          │  │  │ meal_type       │
│ dietary_pref[]  │  │  └─────────────────────┘  │  │ recipe_id       │
│ allergies[]     │  │                           │  │ meal_name       │
│ calorie_target  │  └───────────────────────────┘  │ calories        │
│ weight_goal     │                                 │ time            │
└─────────────────┘                                 └─────────────────┘
         │
         └──────────────────────────┐
                                    ▼
┌─────────────────┐     ┌─────────────────────┐
│ shopping_items  │     │  kenyan_recipes     │
├─────────────────┤     ├─────────────────────┤
│ id (UUID, PK)   │     │ id (SERIAL, PK)     │──┬──────────────────┐
│ user_id (FK)    │     │ name                │  │                  │
│ name            │     │ description         │  │                  │
│ category        │     │ prep_time/cook_time │  ▼                  ▼
│ is_checked      │     │ difficulty          │ ingredients    instructions
└─────────────────┘     │ region              │   table           table
                        │ calories            │       ▲
                        └─────────────────────┘       │
                                                   tags table
```

### Edge Functions

The Supabase backend includes serverless functions:

| Function | Description |
|----------|-------------|
| `getRecipes` | Fetch paginated recipe list |
| `getRecipeById` | Get single recipe details |
| `getRecipeWithDetails` | Full recipe with ingredients/instructions |
| `getKenyanRecipes` | Kenyan cuisine specific queries |
| `createRecipe` | Add new user recipe |
| `updateRecipe` | Modify existing recipe |
| `deleteRecipe` | Remove recipe |

---

## Permissions

IngreDiet requires the following Android permissions:

| Permission | Reason |
|------------|--------|
| `INTERNET` | Network requests to Supabase API |
| `ACCESS_NETWORK_STATE` | Check connectivity status |
| `POST_NOTIFICATIONS` | Meal reminders (Android 13+) |
| `SCHEDULE_EXACT_ALARM` | Precise meal time notifications |
| `WAKE_LOCK` | Ensure alarms trigger reliably |
| `USE_BIOMETRIC` | Fingerprint/Face authentication |

---

## Testing

### Run Unit Tests

```bash
./gradlew test
```

### Run Instrumented Tests

```bash
./gradlew connectedAndroidTest
```

### Test Structure

```
app/src/
├── test/                    # Unit tests
│   └── java/
└── androidTest/             # Instrumented tests
    └── java/
```

---

## Build Variants

| Variant | Purpose |
|---------|---------|
| `debug` | Development build with debugging enabled |
| `release` | Production build with ProGuard/R8 optimization |

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK (requires signing config)
./gradlew assembleRelease
```

---

## Roadmap

### Current Focus (Alpha)
- [x] Recipe browser with search and filtering
- [x] Recipe detail view with ingredients and instructions
- [x] Favorites system
- [x] Basic shopping list management
- [x] User authentication (email + biometric)
- [x] Kenyan recipes collection
- [ ] Offline mode with local caching
- [ ] Meal planner calendar polish
- [ ] Shopping list generation from meal plans

### Planned Features
- Recipe recommendations based on ingredients
- Detailed nutritional breakdown visualization
- Serving size adjuster with ingredient scaling
- Rating and review system
- Cooking timers integrated with recipe steps
- "My Pantry" feature with expiration tracking
- Social sharing functionality
- Integration with health tracking apps

---

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## Contact

**Daniel Togey** — [danieltogey@proton.me](mailto:danieltogey@proton.me)
