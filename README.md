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

---

*IngreDiet: Your meal planning companion.*
