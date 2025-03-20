-- Add missing RLS policy for the ingredients table
-- This policy allows authenticated users to insert into the ingredients table
CREATE POLICY "Allow authenticated users to insert ingredients" 
ON ingredients FOR INSERT 
WITH CHECK (auth.role() = 'authenticated');

-- Also add a select policy to allow public read access
CREATE POLICY "Allow public read access for ingredients" 
ON ingredients FOR SELECT 
USING (true); 