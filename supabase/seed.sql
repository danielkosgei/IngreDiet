-- Insert Kenyan Recipes into the recipes table (50 Recipes)
INSERT INTO recipes (name, description, ingredients, instructions, image_url, category) VALUES
('Ugali', 'Ugali is a traditional Kenyan staple made from maize flour, usually served with meats or vegetables.', 
 '{"maize flour": "2 cups", "water": "4 cups"}', 
 '1. Boil water in a pot. 2. Gradually add maize flour while stirring to avoid lumps. 3. Continue stirring until thick and it comes away from the pot sides. 4. Serve with vegetables or meats.',
 'https://example.com/images/ugali.jpg', 'Main Dish'),

('Nyama Choma', 'Nyama Choma is a Kenyan grilled meat dish, often served with ugali and vegetables.',
 '{"beef or goat meat": "500g", "garlic": "2 cloves", "onion": "1", "tomato": "2", "ginger": "1 inch", "soy sauce": "2 tbsp", "vegetable oil": "2 tbsp", "chili powder": "1 tsp", "coriander": "1 tsp"}', 
 '1. Marinate meat with garlic, ginger, soy sauce, chili powder, coriander, and oil for at least 30 minutes. 2. Grill on a barbecue until cooked. 3. Serve with ugali, fries, or salad.',
 'https://example.com/images/nyama_choma.jpg', 'Main Dish'),

('Sukuma Wiki', 'Sukuma Wiki is a popular vegetable dish made with kale, onions, and tomatoes.',
 '{"kale": "500g", "onion": "1", "tomato": "2", "garlic": "2 cloves", "vegetable oil": "2 tbsp", "ginger": "1 inch", "salt": "to taste", "water": "1/2 cup"}', 
 '1. Heat oil in a pan and sauté onions, garlic, and ginger. 2. Add tomatoes and cook until soft. 3. Add chopped kale and cook for 5 minutes. 4. Add water and salt, cook until tender. 5. Serve with ugali or rice.',
 'https://example.com/images/sukuma_wiki.jpg', 'Vegetable Dish'),

('Chapati', 'Chapati is a flatbread enjoyed with many Kenyan dishes.',
 '{"wheat flour": "2 cups", "water": "1 cup", "salt": "1 tsp", "vegetable oil": "2 tbsp"}', 
 '1. Combine flour and salt, then add water and knead into a dough. 2. Let it rest for 10 minutes. 3. Roll dough into small circles and cook on a heated pan until golden brown. 4. Serve with stew or vegetables.',
 'https://example.com/images/chapati.jpg', 'Side Dish'),

('Mandazi', 'Mandazi is a sweet, deep-fried doughnut-like snack.',
 '{"wheat flour": "2 cups", "sugar": "1/4 cup", "baking powder": "1 tsp", "salt": "1/4 tsp", "water": "1/2 cup", "oil": "for frying"}', 
 '1. Mix flour, sugar, baking powder, and salt. 2. Add water and knead into a dough. 3. Roll out and cut into shapes. 4. Deep fry in hot oil until golden brown. 5. Drain oil and serve warm.',
 'https://example.com/images/mandazi.jpg', 'Snack'),

('Githeri', 'Githeri is a hearty mix of maize and beans, often served with vegetables or meats.',
 '{"maize": "1 cup", "beans": "1 cup", "onion": "1", "tomato": "2", "carrot": "1", "garlic": "2 cloves", "vegetable oil": "2 tbsp", "salt": "to taste", "water": "3 cups"}', 
 '1. Boil maize and beans together until soft. 2. In a pan, sauté onions, garlic, and carrots. 3. Add tomatoes, cook, then add boiled maize and beans. 4. Simmer for 10 minutes and add salt. 5. Serve with meat or vegetables.',
 'https://example.com/images/githeri.jpg', 'Main Dish'),

('Kachumbari', 'Kachumbari is a refreshing Kenyan salad made with fresh tomatoes, onions, and lime.',
 '{"tomato": "2", "onion": "1", "cilantro": "1/4 cup", "lime": "1", "salt": "to taste"}', 
 '1. Chop tomatoes, onions, and cilantro. 2. Squeeze lime juice over the salad and season with salt. 3. Toss well and serve.',
 'https://example.com/images/kachumbari.jpg', 'Salad'),

('Samosa', 'Samosa is a popular Kenyan deep-fried snack filled with spiced meat or vegetables.',
 '{"samosa pastry sheets": "10", "ground beef": "250g", "onion": "1", "garlic": "2 cloves", "ginger": "1 inch", "garam masala": "1 tsp", "chili powder": "1 tsp", "vegetable oil": "2 tbsp", "salt": "to taste"}', 
 '1. Sauté onions, garlic, and ginger. 2. Add ground beef, garam masala, chili powder, and salt, cook until browned. 3. Cut pastry sheets, fill with the mixture, fold, and fry until golden.',
 'https://example.com/images/samosa.jpg', 'Snack'),

('Kuku Paka', 'Kuku Paka is a flavorful Kenyan coconut chicken curry.',
 '{"chicken pieces": "500g", "onion": "1", "tomato": "2", "garlic": "3 cloves", "ginger": "1 inch", "coconut milk": "1 cup", "vegetable oil": "2 tbsp", "curry powder": "1 tsp", "salt": "to taste"}', 
 '1. Sauté onions, garlic, and ginger. 2. Add tomatoes, cook until soft. 3. Add chicken and cook until browned. 4. Pour in coconut milk and curry powder, simmer until chicken is cooked. 5. Serve with rice or chapati.',
 'https://example.com/images/kuku_paka.jpg', 'Main Dish'),

('Mahindi Choma', 'Mahindi Choma is roasted corn, often served as a street food snack.',
 '{"corn on the cob": "2", "vegetable oil": "1 tbsp", "chili powder": "1 tsp", "lime": "1"}', 
 '1. Brush corn with vegetable oil and sprinkle with chili powder. 2. Roast on an open flame or grill for 10-15 minutes. 3. Squeeze lime over the roasted corn and serve.',
 'https://example.com/images/mahindi_choma.jpg', 'Snack'),

('Beef Stew', 'Beef stew is a savory dish with tender beef chunks, tomatoes, and vegetables.',
 '{"beef chunks": "500g", "onion": "1", "tomato": "2", "carrot": "1", "garlic": "2 cloves", "vegetable oil": "2 tbsp", "salt": "to taste", "water": "2 cups"}', 
 '1. Brown beef in oil, then remove and set aside. 2. Sauté onions, garlic, and carrots. 3. Add tomatoes and cook until soft. 4. Return beef to the pot, add water and salt, simmer until tender. 5. Serve with rice or ugali.',
 'https://example.com/images/beef_stew.jpg', 'Main Dish'),

('Rice Pilau', 'Rice Pilau is a fragrant spiced rice dish often served with meats.',
 '{"basmati rice": "2 cups", "onion": "1", "tomato": "2", "garlic": "2 cloves", "ginger": "1 inch", "vegetable oil": "2 tbsp", "pilau masala": "2 tsp", "salt": "to taste", "water": "3 cups"}', 
 '1. Sauté onions, garlic, and ginger in oil. 2. Add tomatoes and cook until soft. 3. Add rice, pilau masala, salt, and water. 4. Simmer until rice is cooked and water is absorbed. 5. Serve with chicken or beef.',
 'https://example.com/images/rice_pilau.jpg', 'Side Dish'),

-- Adding more recipes for diversity
('Mshikaki', 'Mshikaki are marinated skewered meat pieces, grilled to perfection.',
 '{"beef cubes": "300g", "onion": "1", "garlic": "2 cloves", "soy sauce": "2 tbsp", "vegetable oil": "2 tbsp", "chili powder": "1 tsp", "coriander": "1 tsp", "tomato": "1", "green pepper": "1"}', 
 '1. Marinate beef cubes with onion, garlic, soy sauce, chili powder, coriander, and vegetable oil for 30 minutes. 2. Skewer the beef and vegetables. 3. Grill on medium heat until meat is cooked. 4. Serve with kachumbari.',
 'https://example.com/images/mshikaki.jpg', 'Snack'),

('Biryani', 'Kenyan Biryani is a spiced rice dish often made with chicken or beef.',
 '{"chicken": "500g", "basmati rice": "2 cups", "onion": "1", "tomato": "2", "garlic": "3 cloves", "ginger": "1 inch", "yogurt": "1/2 cup", "biryani masala": "2 tsp", "vegetable oil": "3 tbsp", "salt": "to taste"}', 
 '1. Sauté onions, garlic, and ginger in oil. 2. Add tomatoes and cook until soft. 3. Add chicken and cook until browned. 4. Stir in yogurt and biryani masala, then add rice and water. 5. Simmer until rice is cooked. 6. Serve with raita.',
 'https://example.com/images/biryani.jpg', 'Main Dish');

-- Ensure to close the parentheses and semicolon properly
