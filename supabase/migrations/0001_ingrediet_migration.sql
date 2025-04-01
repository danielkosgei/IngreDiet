-- Combined migration from all previous migrations
-- Created at: Tue  1 Apr 02:49:23 EAT 2025

------------------------------------------------------------------------------
-- Originally from: 0001_create_profiles_table.sql
------------------------------------------------------------------------------

-- Create profiles table
CREATE TABLE IF NOT EXISTS profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    email TEXT,
    first_name TEXT,
    last_name TEXT,
    dietary_preferences TEXT[] DEFAULT '{}',
    allergies TEXT[] DEFAULT '{}',
    weight_goal TEXT,
    calorie_target INTEGER,
    profile_image_url TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Set up Row Level Security (RLS)
ALTER TABLE profiles ENABLE ROW LEVEL SECURITY;

-- Create policies
-- Allow users to view their own profile
CREATE POLICY "Users can view their own profile"
ON profiles
FOR SELECT
USING (auth.uid() = id);

-- Allow users to update their own profile
CREATE POLICY "Users can update their own profile"
ON profiles
FOR UPDATE
USING (auth.uid() = id);

-- Create a trigger to create a profile when a new user signs up
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.profiles (id, email)
    VALUES (new.id, new.email);
    RETURN new;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Set up the trigger on auth.users
CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

------------------------------------------------------------------------------
-- Originally from: 0002_create_recipes_tables.sql
------------------------------------------------------------------------------

-- Recipes table
CREATE TABLE recipes (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name TEXT NOT NULL,
  description TEXT,
  ingredients JSONB NOT NULL,
  instructions TEXT NOT NULL,
  image_url TEXT,
  category TEXT
);

-- Ingredients table
CREATE TABLE ingredients (
  id SERIAL PRIMARY KEY,
  name TEXT NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Recipe ingredients junction table
CREATE TABLE recipe_ingredients (
  id SERIAL PRIMARY KEY,
  recipe_id UUID REFERENCES recipes(id) ON DELETE CASCADE,
  ingredient_id INTEGER REFERENCES ingredients(id) ON DELETE CASCADE,
  quantity REAL NOT NULL,
  unit TEXT NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Instructions table
CREATE TABLE recipe_instructions (
  id SERIAL PRIMARY KEY,
  recipe_id UUID REFERENCES recipes(id) ON DELETE CASCADE,
  step_number INTEGER NOT NULL,
  instruction TEXT NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Nutrition facts table
CREATE TABLE recipe_nutrition (
  id SERIAL PRIMARY KEY,
  recipe_id UUID REFERENCES recipes(id) ON DELETE CASCADE,
  calories INTEGER,
  protein REAL,
  carbs REAL,
  fat REAL,
  fiber REAL,
  sugar REAL,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- User favorites table
CREATE TABLE user_favorites (
  id SERIAL PRIMARY KEY,
  user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
  recipe_id UUID REFERENCES recipes(id) ON DELETE CASCADE,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  UNIQUE(user_id, recipe_id)
);

-- Enable RLS on all tables
ALTER TABLE recipes ENABLE ROW LEVEL SECURITY;
ALTER TABLE ingredients ENABLE ROW LEVEL SECURITY;
ALTER TABLE recipe_ingredients ENABLE ROW LEVEL SECURITY;
ALTER TABLE recipe_instructions ENABLE ROW LEVEL SECURITY;
ALTER TABLE recipe_nutrition ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_favorites ENABLE ROW LEVEL SECURITY;

-- Create policies for recipes (allow anyone to read, only authenticated users to modify)
CREATE POLICY "Allow public read access" ON recipes FOR SELECT USING (true);
CREATE POLICY "Allow authenticated users to create" ON recipes FOR INSERT WITH CHECK (auth.role() = 'authenticated');
--CREATE POLICY "Allow owners to update" ON recipes FOR UPDATE USING (auth.uid() IN (SELECT user_id FROM recipe_owners WHERE recipe_id = id));

-- Policy for user_favorites (users can only see and modify their own favorites)
CREATE POLICY "Users can view their own favorites" ON user_favorites FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Users can add their own favorites" ON user_favorites FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Users can delete their own favorites" ON user_favorites FOR DELETE USING (auth.uid() = user_id);

------------------------------------------------------------------------------
-- Originally from: 0003_create_kenyan_foods_tables.sql, Part 1
------------------------------------------------------------------------------

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

------------------------------------------------------------------------------
-- Originally from: 0007_add_ingredients_policy.sql
------------------------------------------------------------------------------

-- Add missing RLS policy for the ingredients table
-- This policy allows authenticated users to insert into the ingredients table
CREATE POLICY "Allow authenticated users to insert ingredients" 
ON ingredients FOR INSERT 
WITH CHECK (auth.role() = 'authenticated');

-- Also add a select policy to allow public read access
CREATE POLICY "Allow public read access for ingredients" 
ON ingredients FOR SELECT 
USING (true);

------------------------------------------------------------------------------
-- Originally from: 0008_create_user_meal_plans.sql
------------------------------------------------------------------------------

-- Create user_meal_plans table
CREATE TABLE IF NOT EXISTS user_meal_plans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
    day_of_week TEXT NOT NULL, -- e.g., 'MONDAY', 'TUESDAY', etc.
    meal_type TEXT NOT NULL, -- e.g., 'Breakfast', 'Lunch', 'Dinner', 'Snack'
    recipe_id INTEGER,
    meal_name TEXT NOT NULL,
    meal_description TEXT,
    calories INTEGER,
    time TEXT, -- Time of day for the meal (e.g., '08:00')
    image_url TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Create index for faster queries
CREATE INDEX user_meal_plans_user_id_idx ON user_meal_plans(user_id);
CREATE INDEX user_meal_plans_day_of_week_idx ON user_meal_plans(day_of_week);

-- Set up Row Level Security (RLS)
ALTER TABLE user_meal_plans ENABLE ROW LEVEL SECURITY;

-- Create policies
-- Allow users to view their own meal plans
CREATE POLICY "Users can view their own meal plans"
ON user_meal_plans
FOR SELECT
USING (auth.uid() = user_id);

-- Allow users to insert their own meal plans
CREATE POLICY "Users can insert their own meal plans"
ON user_meal_plans
FOR INSERT
WITH CHECK (auth.uid() = user_id);

-- Allow users to update their own meal plans
CREATE POLICY "Users can update their own meal plans"
ON user_meal_plans
FOR UPDATE
USING (auth.uid() = user_id);

-- Allow users to delete their own meal plans
CREATE POLICY "Users can delete their own meal plans"
ON user_meal_plans
FOR DELETE
USING (auth.uid() = user_id);

------------------------------------------------------------------------------
-- Originally from: 0009_create_shopping_items.sql
------------------------------------------------------------------------------

CREATE TABLE public.shopping_items (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
  name TEXT NOT NULL,
  category TEXT DEFAULT '',
  is_checked BOOLEAN DEFAULT false,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Enable RLS
ALTER TABLE public.shopping_items ENABLE ROW LEVEL SECURITY;

-- Create policy for users to see only their own items
CREATE POLICY "Users can view their own shopping items"
ON public.shopping_items FOR SELECT
USING (auth.uid() = user_id);

-- Create policy for users to insert their own items
CREATE POLICY "Users can insert their own shopping items"
ON public.shopping_items FOR INSERT
WITH CHECK (auth.uid() = user_id);

-- Create policy for users to update their own items
CREATE POLICY "Users can update their own shopping items"
ON public.shopping_items FOR UPDATE
USING (auth.uid() = user_id);

-- Create policy for users to delete their own items
CREATE POLICY "Users can delete their own shopping items"
ON public.shopping_items FOR DELETE
USING (auth.uid() = user_id);

------------------------------------------------------------------------------
-- Originally from: 0005_add_more_kenyan_foods.sql (just the function and trigger)
------------------------------------------------------------------------------

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

------------------------------------------------------------------------------
-- Originally from: 0006_create_kenyan_foods_functions.sql
------------------------------------------------------------------------------

-- Function to get a complete Kenyan recipe with all its related data
CREATE OR REPLACE FUNCTION get_complete_kenyan_recipe(recipe_id INTEGER)
RETURNS JSON AS $$
DECLARE
    recipe_json JSON;
BEGIN
    SELECT json_build_object(
        'recipe', r,
        'ingredients', (
            SELECT json_agg(i ORDER BY i.order_index)
            FROM kenyan_recipe_ingredients i
            WHERE i.recipe_id = r.id
        ),
        'instructions', (
            SELECT json_agg(ins ORDER BY ins.step_number)
            FROM kenyan_recipe_instructions ins
            WHERE ins.recipe_id = r.id
        ),
        'tags', (
            SELECT json_agg(t.tag_name)
            FROM kenyan_recipe_tags t
            WHERE t.recipe_id = r.id
        )
    )
    INTO recipe_json
    FROM kenyan_recipes r
    WHERE r.id = recipe_id;
    
    RETURN recipe_json;
END;
$$ LANGUAGE plpgsql;

-- Function to search Kenyan recipes with fuzzy matching
CREATE OR REPLACE FUNCTION search_kenyan_recipes(search_query TEXT)
RETURNS TABLE (
    id INTEGER,
    name TEXT,
    description TEXT,
    region TEXT,
    similarity FLOAT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        r.id,
        r.name,
        r.description,
        r.region,
        similarity(r.name, search_query) as similarity
    FROM kenyan_recipes r
    WHERE 
        r.name ILIKE '%' || search_query || '%'
        OR r.description ILIKE '%' || search_query || '%'
        OR EXISTS (
            SELECT 1 
            FROM kenyan_recipe_tags t 
            WHERE t.recipe_id = r.id 
            AND t.tag_name ILIKE '%' || search_query || '%'
        )
    ORDER BY similarity DESC
    LIMIT 10;
END;
$$ LANGUAGE plpgsql;

-- Function to get recipes by region
CREATE OR REPLACE FUNCTION get_kenyan_recipes_by_region(region_name TEXT)
RETURNS TABLE (
    id INTEGER,
    name TEXT,
    description TEXT,
    preparation_time INTEGER,
    cooking_time INTEGER,
    calories INTEGER
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        r.id,
        r.name,
        r.description,
        r.preparation_time,
        r.cooking_time,
        r.calories
    FROM kenyan_recipes r
    WHERE r.region = region_name
    ORDER BY r.name;
END;
$$ LANGUAGE plpgsql;

-- Function to get popular tags
CREATE OR REPLACE FUNCTION get_popular_kenyan_recipe_tags(limit_count INTEGER DEFAULT 10)
RETURNS TABLE (
    tag_name TEXT,
    count BIGINT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        t.tag_name,
        COUNT(*) as count
    FROM kenyan_recipe_tags t
    GROUP BY t.tag_name
    ORDER BY count DESC
    LIMIT limit_count;
END;
$$ LANGUAGE plpgsql;

-- Function to get recipe suggestions based on ingredients
CREATE OR REPLACE FUNCTION get_kenyan_recipe_suggestions(ingredient_list TEXT[])
RETURNS TABLE (
    id INTEGER,
    name TEXT,
    description TEXT,
    matching_ingredients INTEGER
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        r.id,
        r.name,
        r.description,
        COUNT(DISTINCT i.name) as matching_ingredients
    FROM kenyan_recipes r
    JOIN kenyan_recipe_ingredients i ON i.recipe_id = r.id
    WHERE i.name = ANY(ingredient_list)
    GROUP BY r.id, r.name, r.description
    ORDER BY matching_ingredients DESC, r.name
    LIMIT 5;
END;
$$ LANGUAGE plpgsql;

-- Create a view for recipe statistics
CREATE OR REPLACE VIEW kenyan_recipe_stats AS
SELECT
    r.region,
    COUNT(DISTINCT r.id) as recipe_count,
    AVG(r.preparation_time + r.cooking_time) as avg_total_time,
    AVG(r.calories) as avg_calories,
    array_agg(DISTINCT t.tag_name) as common_tags
FROM kenyan_recipes r
LEFT JOIN kenyan_recipe_tags t ON t.recipe_id = r.id
GROUP BY r.region;

-- Grant access to the functions and view
GRANT EXECUTE ON FUNCTION get_complete_kenyan_recipe(INTEGER) TO authenticated, anon;
GRANT EXECUTE ON FUNCTION search_kenyan_recipes(TEXT) TO authenticated, anon;
GRANT EXECUTE ON FUNCTION get_kenyan_recipes_by_region(TEXT) TO authenticated, anon;
GRANT EXECUTE ON FUNCTION get_popular_kenyan_recipe_tags(INTEGER) TO authenticated, anon;
GRANT EXECUTE ON FUNCTION get_kenyan_recipe_suggestions(TEXT[]) TO authenticated, anon;
GRANT SELECT ON kenyan_recipe_stats TO authenticated, anon;

------------------------------------------------------------------------------
-- Note: Data population migrations (0004_populate_kenyan_foods.sql and 
-- 0005_add_more_kenyan_foods.sql) are not included in this combined file
-- to keep it focused on schema changes. Data can be added separately.
------------------------------------------------------------------------------ 