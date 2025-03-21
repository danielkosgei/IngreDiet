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