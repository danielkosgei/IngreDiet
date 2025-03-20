-- Create extension for text search if not already exists
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Create table for Kenyan recipes
CREATE TABLE kenyan_recipes (
  id SERIAL PRIMARY KEY,
  name TEXT NOT NULL,
  description TEXT NOT NULL,
  image_url TEXT DEFAULT '',
  preparation_time INTEGER NOT NULL,
  cooking_time INTEGER NOT NULL,
  servings INTEGER DEFAULT 4,
  difficulty TEXT DEFAULT 'Medium',
  region TEXT DEFAULT 'Traditional',
  calories INTEGER DEFAULT 0,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Add indexes for faster searching
CREATE INDEX idx_kenyan_recipes_name ON kenyan_recipes USING GIN (name gin_trgm_ops);
CREATE INDEX idx_kenyan_recipes_region ON kenyan_recipes (region);

-- Create table for ingredients
CREATE TABLE kenyan_recipe_ingredients (
  id SERIAL PRIMARY KEY,
  recipe_id INTEGER REFERENCES kenyan_recipes(id) ON DELETE CASCADE,
  name TEXT NOT NULL,
  quantity FLOAT DEFAULT 1,
  unit TEXT DEFAULT 'unit',
  order_index INTEGER NOT NULL
);

CREATE INDEX idx_kenyan_recipe_ingredients_recipe_id ON kenyan_recipe_ingredients (recipe_id);

-- Create table for instructions
CREATE TABLE kenyan_recipe_instructions (
  id SERIAL PRIMARY KEY,
  recipe_id INTEGER REFERENCES kenyan_recipes(id) ON DELETE CASCADE,
  instruction_text TEXT NOT NULL,
  step_number INTEGER NOT NULL
);

CREATE INDEX idx_kenyan_recipe_instructions_recipe_id ON kenyan_recipe_instructions (recipe_id);

-- Create table for tags
CREATE TABLE kenyan_recipe_tags (
  id SERIAL PRIMARY KEY,
  recipe_id INTEGER REFERENCES kenyan_recipes(id) ON DELETE CASCADE,
  tag_name TEXT NOT NULL
);

CREATE INDEX idx_kenyan_recipe_tags_recipe_id ON kenyan_recipe_tags (recipe_id);
CREATE INDEX idx_kenyan_recipe_tags_tag_name ON kenyan_recipe_tags (tag_name);

-- Add RLS policies
ALTER TABLE kenyan_recipes ENABLE ROW LEVEL SECURITY;
ALTER TABLE kenyan_recipe_ingredients ENABLE ROW LEVEL SECURITY;
ALTER TABLE kenyan_recipe_instructions ENABLE ROW LEVEL SECURITY;
ALTER TABLE kenyan_recipe_tags ENABLE ROW LEVEL SECURITY;

-- Create policies for public read access
CREATE POLICY "Public read access for kenyan_recipes" 
ON kenyan_recipes FOR SELECT USING (true);

CREATE POLICY "Public read access for kenyan_recipe_ingredients" 
ON kenyan_recipe_ingredients FOR SELECT USING (true);

CREATE POLICY "Public read access for kenyan_recipe_instructions" 
ON kenyan_recipe_instructions FOR SELECT USING (true);

CREATE POLICY "Public read access for kenyan_recipe_tags" 
ON kenyan_recipe_tags FOR SELECT USING (true);

-- Create policies for authenticated users to insert/update/delete
CREATE POLICY "Authenticated users can insert kenyan_recipes" 
ON kenyan_recipes FOR INSERT WITH CHECK (auth.role() = 'authenticated');

CREATE POLICY "Authenticated users can update kenyan_recipes" 
ON kenyan_recipes FOR UPDATE USING (auth.role() = 'authenticated');

CREATE POLICY "Authenticated users can delete kenyan_recipes" 
ON kenyan_recipes FOR DELETE USING (auth.role() = 'authenticated');

-- Similar policies for related tables
CREATE POLICY "Authenticated users can insert kenyan_recipe_ingredients" 
ON kenyan_recipe_ingredients FOR INSERT WITH CHECK (auth.role() = 'authenticated');

CREATE POLICY "Authenticated users can update kenyan_recipe_ingredients" 
ON kenyan_recipe_ingredients FOR UPDATE USING (auth.role() = 'authenticated');

CREATE POLICY "Authenticated users can delete kenyan_recipe_ingredients" 
ON kenyan_recipe_ingredients FOR DELETE USING (auth.role() = 'authenticated');

CREATE POLICY "Authenticated users can insert kenyan_recipe_instructions" 
ON kenyan_recipe_instructions FOR INSERT WITH CHECK (auth.role() = 'authenticated');

CREATE POLICY "Authenticated users can update kenyan_recipe_instructions" 
ON kenyan_recipe_instructions FOR UPDATE USING (auth.role() = 'authenticated');

CREATE POLICY "Authenticated users can delete kenyan_recipe_instructions" 
ON kenyan_recipe_instructions FOR DELETE USING (auth.role() = 'authenticated');

CREATE POLICY "Authenticated users can insert kenyan_recipe_tags" 
ON kenyan_recipe_tags FOR INSERT WITH CHECK (auth.role() = 'authenticated');

CREATE POLICY "Authenticated users can update kenyan_recipe_tags" 
ON kenyan_recipe_tags FOR UPDATE USING (auth.role() = 'authenticated');

CREATE POLICY "Authenticated users can delete kenyan_recipe_tags" 
ON kenyan_recipe_tags FOR DELETE USING (auth.role() = 'authenticated');
