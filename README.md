# IngreDiet

A modern recipe management and meal planning application built with Jetpack Compose for Android.

![Project Status](https://img.shields.io/badge/Status-Alpha%20Development-orange)
![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)
![License](https://img.shields.io/badge/License-MIT-blue)

## 🚧 Project Status: Alpha Development

IngreDiet is currently in active alpha development. Core features are being implemented and stabilized.

## 📱 Current Features

- **Recipe Browser**: Browse through curated recipes with images and basic details
- **Recipe Detail View**: View detailed recipe information including ingredients, instructions, and nutritional facts
- **Basic Search**: Search for recipes by name
- **Favorites**: Save and view your favorite recipes
- **User Profiles**: Basic user authentication and profile management
- **Recipe Categories**: Browse recipes by category and cuisine type
- **Shopping List**: Create and manage shopping lists for your recipes

## 🔜 Roadmap to Alpha Release

### Core Features (Current Focus)
- [x] Recipe browser with search and filtering
- [x] Recipe detail view with ingredients and instructions
- [x] Favorites system
- [x] Basic shopping list management
- [ ] User dietary preferences and restrictions
- [ ] Meal planner calendar
- [ ] Shopping list generation based on meal plans
- [ ] Recipe recommendations based on ingredients

### Post-Alpha Features
- Detailed nutritional breakdown visualization
- Serving size adjuster
- Rating and review system for recipes
- Cooking timers integrated with recipe steps
- "My Pantry" feature to track available ingredients
- Expiration date tracking for pantry items
- User-created recipes
- Social sharing functionality
- Integration with health tracking apps

## 📚 Tech Stack

- Kotlin
- Jetpack Compose (UI)
- Material 3 Design
- MVVM Architecture
- Coroutines & Flow
- Supabase (Backend)
- Coil (Image loading)

## 🛠️ Development Setup

1. Clone the repository
```
git clone https://github.com/danielkosgei/IngreDiet.git
```

2. Open the project in Android Studio

3. Set up your properties file with necessary API keys:
```
# Supabase Configuration
SUPABASE_URL=your_supabase_url
SUPABASE_KEY=your_supabase_key
```

4. Build and run the application

## 💻 Contributing

IngreDiet is currently in early development. If you'd like to contribute, please reach out to me.

## 📝 Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/thenewkenya/ingrediet/
│   │   │   ├── data/                 # Data layer: models, repositories, services
│   │   │   ├── di/                   # Dependency injection
│   │   │   ├── feature/              # Feature modules (recipe, search, etc.)
│   │   │   ├── ui/                   # UI components and themes
│   │   │   └── util/                 # Utilities and extensions
│   │   └── res/                      # Resources (layouts, drawables, etc.)
```

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🧪 Known Issues

- Limited recipe database during development
- Some UI components may not be fully responsive on all screen sizes
- Network operations need optimization for better performance
- Error handling needs improvement in some areas

## 📞 Contact

For questions or feedback about IngreDiet, please contact [danieltogey@proton.me](mailto:danieltogey@proton.me)

## Database Setup

IngreDiet uses Supabase as its backend database. To ensure the application works properly with the database, follow these steps:

1. Make sure you have the correct Supabase credentials in your `apikeys.properties` file:
   ```
   SUPABASE_URL=your_supabase_url
   SUPABASE_ANON_KEY=your_supabase_anon_key
   ```

2. Run the database migrations to create all required tables:
   - Navigate to the `supabase` directory
   - Run `supabase db reset` or manually apply the migrations in `supabase/migrations/`
   - The migrations will create all required tables and relationships

3. If you encounter "relation does not exist" errors, manually apply the SQL from `supabase/migrations/fix_database_schema.sql`

## Database Schema

IngreDiet uses the following main tables:

1. **recipes**: Main recipes table with UUIDs as primary keys
2. **kenyan_recipes**: Specialized table for Kenyan recipes with more regional information
3. **ingredients**: Ingredients table with nutrition data
4. **recipe_ingredients**: Junction table linking recipes and ingredients
5. **recipe_instructions**: Table storing recipe instructions in steps
6. **recipe_nutrition**: Table for detailed nutrition information
7. **shopping_items**: User shopping lists
8. **user_favorites**: User favorite recipes
9. **user_meal_plans**: User meal planning data
10. **profiles**: User profile information

## Error Handling

The application includes built-in error handling for common database issues:

- Missing tables (returns empty results or appropriate defaults)
- Authentication failures (user-friendly error messages)
- Network connectivity issues
- Query errors

These are handled through the `DatabaseErrorHandler` utility class.

---

*IngreDiet: Your meal planning companion.*
