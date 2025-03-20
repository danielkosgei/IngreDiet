-- Add more Kenyan recipes

-- Chapati
INSERT INTO kenyan_recipes (id, name, description, preparation_time, cooking_time, region, calories)
VALUES (
  1006,
  'Chapati',
  'Flatbread common in Kenya, similar to Indian chapati.',
  30,
  20,
  'National',
  150
);

INSERT INTO kenyan_recipe_ingredients (recipe_id, name, quantity, unit, order_index)
VALUES
  (1006, 'Wheat flour', 2, 'cups', 1),
  (1006, 'Water', 0.75, 'cup', 2),
  (1006, 'Salt', 1, 'teaspoon', 3),
  (1006, 'Oil', 3, 'tablespoons', 4);

INSERT INTO kenyan_recipe_instructions (recipe_id, instruction_text, step_number)
VALUES
  (1006, 'Mix flour, salt, and water to form dough', 1),
  (1006, 'Knead until smooth', 2),
  (1006, 'Divide into balls and roll out into circles', 3),
  (1006, 'Cook on a hot pan with oil until golden brown on both sides', 4);

INSERT INTO kenyan_recipe_tags (recipe_id, tag_name)
VALUES
  (1006, 'Kenyan'),
  (1006, 'Bread'),
  (1006, 'Breakfast');

-- Mandazi
INSERT INTO kenyan_recipes (id, name, description, preparation_time, cooking_time, region, calories)
VALUES (
  1007,
  'Mandazi',
  'Sweet, triangular-shaped fried bread, similar to a doughnut.',
  40,
  20,
  'Coastal',
  200
);

INSERT INTO kenyan_recipe_ingredients (recipe_id, name, quantity, unit, order_index)
VALUES
  (1007, 'Flour', 3, 'cups', 1),
  (1007, 'Sugar', 0.5, 'cup', 2),
  (1007, 'Milk', 1, 'cup', 3),
  (1007, 'Eggs', 2, 'unit', 4),
  (1007, 'Baking powder', 2, 'teaspoons', 5),
  (1007, 'Cardamom', 0.5, 'teaspoon', 6),
  (1007, 'Oil for frying', 2, 'cups', 7);

INSERT INTO kenyan_recipe_instructions (recipe_id, instruction_text, step_number)
VALUES
  (1007, 'Mix dry ingredients', 1),
  (1007, 'Add wet ingredients to form dough', 2),
  (1007, 'Roll out and cut into triangles', 3),
  (1007, 'Deep fry until golden brown', 4);

INSERT INTO kenyan_recipe_tags (recipe_id, tag_name)
VALUES
  (1007, 'Kenyan'),
  (1007, 'Coastal'),
  (1007, 'Breakfast'),
  (1007, 'Snack');

-- Mukimo
INSERT INTO kenyan_recipes (id, name, description, preparation_time, cooking_time, region, calories)
VALUES (
  1008,
  'Mukimo',
  'Mashed potatoes mixed with peas, corn, and greens.',
  20,
  40,
  'Central',
  200
);

INSERT INTO kenyan_recipe_ingredients (recipe_id, name, quantity, unit, order_index)
VALUES
  (1008, 'Potatoes', 4, 'large', 1),
  (1008, 'Green peas', 1, 'cup', 2),
  (1008, 'Corn', 1, 'cup', 3),
  (1008, 'Spinach or pumpkin leaves', 2, 'cups', 4),
  (1008, 'Onions', 1, 'medium', 5),
  (1008, 'Salt', 1, 'teaspoon', 6);

INSERT INTO kenyan_recipe_instructions (recipe_id, instruction_text, step_number)
VALUES
  (1008, 'Boil potatoes until soft', 1),
  (1008, 'Cook peas, corn, and greens separately', 2),
  (1008, 'Mash potatoes and mix in the vegetables', 3),
  (1008, 'Season with sautéed onions and salt', 4);

INSERT INTO kenyan_recipe_tags (recipe_id, tag_name)
VALUES
  (1008, 'Kenyan'),
  (1008, 'Central'),
  (1008, 'Vegetarian');

-- Kachumbari
INSERT INTO kenyan_recipes (id, name, description, preparation_time, cooking_time, region, calories)
VALUES (
  1012,
  'Kachumbari',
  'Fresh tomato and onion salad, often served with nyama choma.',
  15,
  0,
  'National',
  50
);

INSERT INTO kenyan_recipe_ingredients (recipe_id, name, quantity, unit, order_index)
VALUES
  (1012, 'Tomatoes', 3, 'medium', 1),
  (1012, 'Onions', 1, 'large', 2),
  (1012, 'Cilantro', 0.25, 'cup', 3),
  (1012, 'Lemon juice', 2, 'tablespoons', 4),
  (1012, 'Salt', 0.5, 'teaspoon', 5);

INSERT INTO kenyan_recipe_instructions (recipe_id, instruction_text, step_number)
VALUES
  (1012, 'Dice tomatoes and onions finely', 1),
  (1012, 'Chop cilantro', 2),
  (1012, 'Mix all ingredients together', 3),
  (1012, 'Season with lemon juice and salt', 4);

INSERT INTO kenyan_recipe_tags (recipe_id, tag_name)
VALUES
  (1012, 'Kenyan'),
  (1012, 'Salad'),
  (1012, 'Side Dish'),
  (1012, 'Raw');

-- Mahindi Choma
INSERT INTO kenyan_recipes (id, name, description, preparation_time, cooking_time, region, calories)
VALUES (
  1014,
  'Mahindi Choma',
  'Roasted corn on the cob, a popular street food.',
  5,
  15,
  'Urban',
  120
);

INSERT INTO kenyan_recipe_ingredients (recipe_id, name, quantity, unit, order_index)
VALUES
  (1014, 'Corn on the cob', 4, 'pieces', 1),
  (1014, 'Lime or lemon', 2, 'pieces', 2),
  (1014, 'Salt', 1, 'teaspoon', 3),
  (1014, 'Chili powder', 0.5, 'teaspoon', 4);

INSERT INTO kenyan_recipe_instructions (recipe_id, instruction_text, step_number)
VALUES
  (1014, 'Remove corn husks and silk', 1),
  (1014, 'Roast corn over open fire or grill until charred', 2),
  (1014, 'Rub with lime/lemon', 3),
  (1014, 'Sprinkle with salt and chili powder if desired', 4);

INSERT INTO kenyan_recipe_tags (recipe_id, tag_name)
VALUES
  (1014, 'Kenyan'),
  (1014, 'Street Food'),
  (1014, 'Snack'),
  (1014, 'Vegetarian');

-- Create a function to update the updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create a trigger to automatically update the updated_at column
CREATE TRIGGER update_kenyan_recipes_updated_at
    BEFORE UPDATE ON kenyan_recipes
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
