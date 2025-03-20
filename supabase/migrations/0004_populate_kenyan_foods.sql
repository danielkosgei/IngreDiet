-- Populate kenyan_recipes table with initial data

-- Ugali
INSERT INTO kenyan_recipes (id, name, description, preparation_time, cooking_time, region, calories)
VALUES (
  1001,
  'Ugali',
  'A staple food in Kenya made from maize flour and water, similar to polenta but firmer.',
  5,
  15,
  'National',
  150
);

INSERT INTO kenyan_recipe_ingredients (recipe_id, name, quantity, unit, order_index)
VALUES
  (1001, 'Maize flour', 2, 'cups', 1),
  (1001, 'Water', 4, 'cups', 2),
  (1001, 'Salt', 1, 'pinch', 3);

INSERT INTO kenyan_recipe_instructions (recipe_id, instruction_text, step_number)
VALUES
  (1001, 'Boil water in a pot', 1),
  (1001, 'Gradually add maize flour while stirring', 2),
  (1001, 'Continue stirring until it forms a firm dough', 3),
  (1001, 'Cover and let it cook for 5 minutes', 4),
  (1001, 'Serve hot with stew or vegetables', 5);

INSERT INTO kenyan_recipe_tags (recipe_id, tag_name)
VALUES
  (1001, 'Kenyan'),
  (1001, 'Traditional'),
  (1001, 'Staple');

-- Nyama Choma
INSERT INTO kenyan_recipes (id, name, description, preparation_time, cooking_time, region, calories)
VALUES (
  1002,
  'Nyama Choma',
  'Grilled meat, usually goat or beef, seasoned with salt and sometimes spices.',
  20,
  40,
  'National',
  300
);

INSERT INTO kenyan_recipe_ingredients (recipe_id, name, quantity, unit, order_index)
VALUES
  (1002, 'Goat meat or beef', 500, 'grams', 1),
  (1002, 'Salt', 1, 'teaspoon', 2),
  (1002, 'Black pepper', 0.5, 'teaspoon', 3),
  (1002, 'Optional spices', 1, 'tablespoon', 4);

INSERT INTO kenyan_recipe_instructions (recipe_id, instruction_text, step_number)
VALUES
  (1002, 'Cut meat into pieces', 1),
  (1002, 'Season with salt and pepper', 2),
  (1002, 'Grill over open fire or charcoal until cooked through', 3),
  (1002, 'Serve hot with kachumbari', 4);

INSERT INTO kenyan_recipe_tags (recipe_id, tag_name)
VALUES
  (1002, 'Kenyan'),
  (1002, 'Meat'),
  (1002, 'Grilled');

-- Sukuma Wiki
INSERT INTO kenyan_recipes (id, name, description, preparation_time, cooking_time, region, calories)
VALUES (
  1003,
  'Sukuma Wiki',
  'A simple dish made with collard greens, onions, and tomatoes.',
  10,
  15,
  'National',
  80
);

INSERT INTO kenyan_recipe_ingredients (recipe_id, name, quantity, unit, order_index)
VALUES
  (1003, 'Collard greens (kale)', 1, 'bunch', 1),
  (1003, 'Onions', 1, 'medium', 2),
  (1003, 'Tomatoes', 2, 'medium', 3),
  (1003, 'Oil', 2, 'tablespoons', 4),
  (1003, 'Salt', 1, 'teaspoon', 5);

INSERT INTO kenyan_recipe_instructions (recipe_id, instruction_text, step_number)
VALUES
  (1003, 'Chop collard greens into small pieces', 1),
  (1003, 'Dice onions and tomatoes', 2),
  (1003, 'Sauté onions in oil until translucent', 3),
  (1003, 'Add tomatoes and cook until soft', 4),
  (1003, 'Add collard greens and salt', 5),
  (1003, 'Cook until greens are tender but still bright green', 6);

INSERT INTO kenyan_recipe_tags (recipe_id, tag_name)
VALUES
  (1003, 'Kenyan'),
  (1003, 'Vegetable'),
  (1003, 'Healthy');

-- Githeri
INSERT INTO kenyan_recipes (id, name, description, preparation_time, cooking_time, region, calories)
VALUES (
  1004,
  'Githeri',
  'A traditional Kenyan dish made with maize and beans, sometimes with vegetables added.',
  30,
  60,
  'Central',
  250
);

INSERT INTO kenyan_recipe_ingredients (recipe_id, name, quantity, unit, order_index)
VALUES
  (1004, 'Maize kernels', 2, 'cups', 1),
  (1004, 'Beans', 1, 'cup', 2),
  (1004, 'Onions', 1, 'medium', 3),
  (1004, 'Tomatoes', 2, 'medium', 4),
  (1004, 'Salt', 1, 'teaspoon', 5),
  (1004, 'Oil', 2, 'tablespoons', 6);

INSERT INTO kenyan_recipe_instructions (recipe_id, instruction_text, step_number)
VALUES
  (1004, 'Soak maize and beans overnight', 1),
  (1004, 'Boil until soft', 2),
  (1004, 'In a separate pan, sauté onions and tomatoes', 3),
  (1004, 'Add the cooked maize and beans', 4),
  (1004, 'Season with salt and simmer for 10 minutes', 5);

INSERT INTO kenyan_recipe_tags (recipe_id, tag_name)
VALUES
  (1004, 'Kenyan'),
  (1004, 'Central'),
  (1004, 'Legumes');

-- Pilau
INSERT INTO kenyan_recipes (id, name, description, preparation_time, cooking_time, region, calories)
VALUES (
  1005,
  'Pilau',
  'Spiced rice dish with meat, popular in coastal Kenya.',
  20,
  40,
  'Coastal',
  400
);

INSERT INTO kenyan_recipe_ingredients (recipe_id, name, quantity, unit, order_index)
VALUES
  (1005, 'Rice', 2, 'cups', 1),
  (1005, 'Meat (beef or chicken)', 500, 'grams', 2),
  (1005, 'Onions', 2, 'large', 3),
  (1005, 'Pilau masala', 2, 'tablespoons', 4),
  (1005, 'Garlic', 3, 'cloves', 5),
  (1005, 'Ginger', 1, 'tablespoon', 6),
  (1005, 'Oil', 3, 'tablespoons', 7);

INSERT INTO kenyan_recipe_instructions (recipe_id, instruction_text, step_number)
VALUES
  (1005, 'Brown meat with onions, garlic, and ginger', 1),
  (1005, 'Add pilau masala and stir', 2),
  (1005, 'Add rice and water', 3),
  (1005, 'Cook until rice is tender and water is absorbed', 4);

INSERT INTO kenyan_recipe_tags (recipe_id, tag_name)
VALUES
  (1005, 'Kenyan'),
  (1005, 'Coastal'),
  (1005, 'Rice');

-- Add more recipes as needed...

-- Nyoyo (the recipe we added earlier)
INSERT INTO kenyan_recipes (id, name, description, preparation_time, cooking_time, region, calories)
VALUES (
  1021,
  'Nyoyo',
  'A traditional Luo dish made with beans and maize, similar to githeri but with specific preparation methods.',
  30,
  60,
  'Nyanza',
  280
);

INSERT INTO kenyan_recipe_ingredients (recipe_id, name, quantity, unit, order_index)
VALUES
  (1021, 'Beans', 1, 'cup', 1),
  (1021, 'Maize', 1, 'cup', 2),
  (1021, 'Onions', 1, 'medium', 3),
  (1021, 'Tomatoes', 2, 'medium', 4),
  (1021, 'Salt', 1, 'teaspoon', 5),
  (1021, 'Oil', 2, 'tablespoons', 6);

INSERT INTO kenyan_recipe_instructions (recipe_id, instruction_text, step_number)
VALUES
  (1021, 'Soak beans and maize overnight', 1),
  (1021, 'Boil together until soft', 2),
  (1021, 'In a separate pan, sauté onions and tomatoes', 3),
  (1021, 'Mix the beans and maize with the sautéed vegetables', 4),
  (1021, 'Season with salt and simmer for 10 minutes', 5);

INSERT INTO kenyan_recipe_tags (recipe_id, tag_name)
VALUES
  (1021, 'Kenyan'),
  (1021, 'Nyanza'),
  (1021, 'Luo'),
  (1021, 'Legumes');
