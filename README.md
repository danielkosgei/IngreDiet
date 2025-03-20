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

## 🔜 Roadmap to Beta

### Phase 1: Core Functionality Stabilization (Current)
- [x] Complete recipe data model implementation
- [x] Implement favorites functionality
- [x] Create basic shopping list feature
- [x] Add search functionality
- [ ] Add proper error handling and loading states throughout the app
- [ ] Implement offline caching for recipes
- [ ] Add user dietary preferences

### Phase 2: Enhanced Recipe Experience
- [ ] Implement detailed nutritional breakdown visualization
- [ ] Add serving size adjuster
- [ ] Enhance recipe filtering by ingredients and nutrition values
- [ ] Implement rating and review system for recipes
- [ ] Add cooking timers integrated with recipe steps

### Phase 3: Meal Planning & Pantry
- [ ] Weekly meal planner calendar
- [ ] "My Pantry" feature to track available ingredients
- [ ] Shopping list generation based on meal plans and pantry
- [ ] Recipe recommendations based on pantry ingredients
- [ ] Expiration date tracking for pantry items

### Phase 4: Social & Extended Features
- [ ] User-created recipes
- [ ] Social sharing functionality
- [ ] Collaborative meal planning
- [ ] Integration with health tracking apps
- [ ] Advanced image recognition for ingredients

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
git clone https://github.com/username/IngreDiet.git
```

2. Open the project in Android Studio

3. Set up your local.properties file with necessary API keys:
```
# Supabase Configuration
SUPABASE_URL=your_supabase_url
SUPABASE_KEY=your_supabase_key
```

4. Build and run the application

## 💻 Contributing

IngreDiet is currently in early development. If you'd like to contribute, please reach out to the repository owner.

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
