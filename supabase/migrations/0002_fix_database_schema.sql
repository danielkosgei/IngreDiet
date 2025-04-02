-- Fix database schema to ensure compatibility with the app

-- Create tables if they don't exist
-- 1. Create shopping_items table
CREATE TABLE IF NOT EXISTS public.shopping_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    name TEXT NOT NULL,
    category TEXT DEFAULT '',
    is_checked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 2. Create ingredients table if it doesn't exist
CREATE TABLE IF NOT EXISTS public.ingredients (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    image_url TEXT,
    calories INT,
    carbs FLOAT,
    protein FLOAT,
    fat FLOAT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 3. Create recipe_ingredients table if it doesn't exist
CREATE TABLE IF NOT EXISTS public.recipe_ingredients (
    id SERIAL PRIMARY KEY,
    recipe_id UUID REFERENCES public.recipes(id),
    ingredient_id UUID REFERENCES public.ingredients(id),
    quantity FLOAT,
    unit TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 4. Create recipe_instructions table if it doesn't exist
CREATE TABLE IF NOT EXISTS public.recipe_instructions (
    id SERIAL PRIMARY KEY,
    recipe_id UUID REFERENCES public.recipes(id),
    step_number INT,
    instruction TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 5. Create recipe_nutrition table if it doesn't exist
CREATE TABLE IF NOT EXISTS public.recipe_nutrition (
    id SERIAL PRIMARY KEY,
    recipe_id UUID REFERENCES public.recipes(id),
    calories INT,
    protein FLOAT,
    carbs FLOAT,
    fat FLOAT,
    fiber FLOAT,
    sugar FLOAT,
    sodium FLOAT,
    cholesterol FLOAT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 6. Create user_favorites table if it doesn't exist
CREATE TABLE IF NOT EXISTS public.user_favorites (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    recipe_id UUID REFERENCES public.recipes(id),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(user_id, recipe_id)
);

-- 7. Create user_meal_plans table if it doesn't exist
CREATE TABLE IF NOT EXISTS public.user_meal_plans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    day_of_week TEXT NOT NULL,
    meal_type TEXT NOT NULL,
    recipe_id TEXT,
    meal_name TEXT NOT NULL,
    meal_description TEXT,
    calories INT DEFAULT 0,
    time TEXT,
    image_url TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 8. Create profiles table if it doesn't exist
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY,
    email TEXT,
    first_name TEXT,
    last_name TEXT,
    dietary_preferences TEXT[] DEFAULT '{}',
    allergies TEXT[] DEFAULT '{}',
    weight_goal TEXT,
    calorie_target INT,
    profile_image_url TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 9. Create kenyan_recipe_ingredients table if it doesn't exist
CREATE TABLE IF NOT EXISTS public.kenyan_recipe_ingredients (
    id SERIAL PRIMARY KEY,
    recipe_id INT REFERENCES public.kenyan_recipes(id),
    recipe_text_id TEXT REFERENCES public.kenyan_recipes(text_id),
    name TEXT NOT NULL,
    quantity TEXT,
    unit TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 10. Create kenyan_recipe_instructions table if it doesn't exist
CREATE TABLE IF NOT EXISTS public.kenyan_recipe_instructions (
    id SERIAL PRIMARY KEY,
    recipe_id INT REFERENCES public.kenyan_recipes(id),
    recipe_text_id TEXT REFERENCES public.kenyan_recipes(text_id),
    step_number INT,
    instruction TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 11. Create kenyan_recipe_tags table if it doesn't exist
CREATE TABLE IF NOT EXISTS public.kenyan_recipe_tags (
    id SERIAL PRIMARY KEY,
    recipe_id INT REFERENCES public.kenyan_recipes(id),
    recipe_text_id TEXT REFERENCES public.kenyan_recipes(text_id),
    tag TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_shopping_items_user_id ON public.shopping_items(user_id);
CREATE INDEX IF NOT EXISTS idx_recipes_name ON public.recipes(name);
CREATE INDEX IF NOT EXISTS idx_kenyan_recipes_region ON public.kenyan_recipes(region);
CREATE INDEX IF NOT EXISTS idx_user_favorites_user_id ON public.user_favorites(user_id);
CREATE INDEX IF NOT EXISTS idx_user_meal_plans_user_id ON public.user_meal_plans(user_id);
CREATE INDEX IF NOT EXISTS idx_recipe_ingredients_recipe_id ON public.recipe_ingredients(recipe_id);

-- Create RLS (Row Level Security) policies
-- This ensures users can only access their own data

-- First, check if RLS is already enabled for each table
DO $$
BEGIN
    -- RLS for shopping_items
    IF NOT EXISTS (
        SELECT 1 FROM pg_tables 
        WHERE tablename = 'shopping_items' 
        AND rowsecurity = true
    ) THEN
        ALTER TABLE public.shopping_items ENABLE ROW LEVEL SECURITY;
    END IF;
    
    -- RLS for user_favorites
    IF NOT EXISTS (
        SELECT 1 FROM pg_tables 
        WHERE tablename = 'user_favorites' 
        AND rowsecurity = true
    ) THEN
        ALTER TABLE public.user_favorites ENABLE ROW LEVEL SECURITY;
    END IF;
    
    -- RLS for user_meal_plans
    IF NOT EXISTS (
        SELECT 1 FROM pg_tables 
        WHERE tablename = 'user_meal_plans' 
        AND rowsecurity = true
    ) THEN
        ALTER TABLE public.user_meal_plans ENABLE ROW LEVEL SECURITY;
    END IF;
    
    -- RLS for profiles
    IF NOT EXISTS (
        SELECT 1 FROM pg_tables 
        WHERE tablename = 'profiles' 
        AND rowsecurity = true
    ) THEN
        ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
    END IF;
END
$$;

-- Now drop any existing policies to avoid errors when recreating
DO $$
BEGIN
    -- Drop shopping_items policies
    DROP POLICY IF EXISTS shopping_items_select_policy ON public.shopping_items;
    DROP POLICY IF EXISTS shopping_items_insert_policy ON public.shopping_items;
    DROP POLICY IF EXISTS shopping_items_update_policy ON public.shopping_items;
    DROP POLICY IF EXISTS shopping_items_delete_policy ON public.shopping_items;
    
    -- Drop user_favorites policies
    DROP POLICY IF EXISTS user_favorites_select_policy ON public.user_favorites;
    DROP POLICY IF EXISTS user_favorites_insert_policy ON public.user_favorites;
    DROP POLICY IF EXISTS user_favorites_update_policy ON public.user_favorites;
    DROP POLICY IF EXISTS user_favorites_delete_policy ON public.user_favorites;
    
    -- Drop user_meal_plans policies
    DROP POLICY IF EXISTS user_meal_plans_select_policy ON public.user_meal_plans;
    DROP POLICY IF EXISTS user_meal_plans_insert_policy ON public.user_meal_plans;
    DROP POLICY IF EXISTS user_meal_plans_update_policy ON public.user_meal_plans;
    DROP POLICY IF EXISTS user_meal_plans_delete_policy ON public.user_meal_plans;
    
    -- Drop profiles policies
    DROP POLICY IF EXISTS profiles_select_policy ON public.profiles;
    DROP POLICY IF EXISTS profiles_insert_policy ON public.profiles;
    DROP POLICY IF EXISTS profiles_update_policy ON public.profiles;
EXCEPTION
    WHEN undefined_object THEN
        -- Ignore errors about non-existent policies
END
$$;

-- Create new policies

-- Policies for shopping_items
CREATE POLICY shopping_items_select_policy
  ON public.shopping_items FOR SELECT
  USING (auth.uid() = user_id);

CREATE POLICY shopping_items_insert_policy
  ON public.shopping_items FOR INSERT
  WITH CHECK (auth.uid() = user_id);

CREATE POLICY shopping_items_update_policy
  ON public.shopping_items FOR UPDATE
  USING (auth.uid() = user_id);

CREATE POLICY shopping_items_delete_policy
  ON public.shopping_items FOR DELETE
  USING (auth.uid() = user_id);

-- Policies for user_favorites
CREATE POLICY user_favorites_select_policy
  ON public.user_favorites FOR SELECT
  USING (auth.uid() = user_id);

CREATE POLICY user_favorites_insert_policy
  ON public.user_favorites FOR INSERT
  WITH CHECK (auth.uid() = user_id);

CREATE POLICY user_favorites_update_policy
  ON public.user_favorites FOR UPDATE
  USING (auth.uid() = user_id);

CREATE POLICY user_favorites_delete_policy
  ON public.user_favorites FOR DELETE
  USING (auth.uid() = user_id);

-- Policies for user_meal_plans
CREATE POLICY user_meal_plans_select_policy
  ON public.user_meal_plans FOR SELECT
  USING (auth.uid() = user_id);

CREATE POLICY user_meal_plans_insert_policy
  ON public.user_meal_plans FOR INSERT
  WITH CHECK (auth.uid() = user_id);

CREATE POLICY user_meal_plans_update_policy
  ON public.user_meal_plans FOR UPDATE
  USING (auth.uid() = user_id);

CREATE POLICY user_meal_plans_delete_policy
  ON public.user_meal_plans FOR DELETE
  USING (auth.uid() = user_id);

-- Policies for profiles
CREATE POLICY profiles_select_policy
  ON public.profiles FOR SELECT
  USING (auth.uid() = id);

CREATE POLICY profiles_insert_policy
  ON public.profiles FOR INSERT
  WITH CHECK (auth.uid() = id);

CREATE POLICY profiles_update_policy
  ON public.profiles FOR UPDATE
  USING (auth.uid() = id);

-- Create functions and triggers to automatically update updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
   NEW.updated_at = NOW();
   RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply trigger to tables
DO $$
BEGIN
    -- triggers for shopping_items
    DROP TRIGGER IF EXISTS update_shopping_items_updated_at ON public.shopping_items;
    CREATE TRIGGER update_shopping_items_updated_at
    BEFORE UPDATE ON public.shopping_items
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
    
    -- triggers for user_favorites
    DROP TRIGGER IF EXISTS update_user_favorites_updated_at ON public.user_favorites;
    CREATE TRIGGER update_user_favorites_updated_at
    BEFORE UPDATE ON public.user_favorites
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
    
    -- triggers for user_meal_plans
    DROP TRIGGER IF EXISTS update_user_meal_plans_updated_at ON public.user_meal_plans;
    CREATE TRIGGER update_user_meal_plans_updated_at
    BEFORE UPDATE ON public.user_meal_plans
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
    
    -- triggers for profiles
    DROP TRIGGER IF EXISTS update_profiles_updated_at ON public.profiles;
    CREATE TRIGGER update_profiles_updated_at
    BEFORE UPDATE ON public.profiles
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
END
$$;

-- Create trigger to handle profile creation on user signup
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
  INSERT INTO public.profiles (id, email)
  VALUES (NEW.id, NEW.email);
  RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Check if the trigger already exists, if not create it
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'on_auth_user_created') THEN
    CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();
  END IF;
END
$$; 