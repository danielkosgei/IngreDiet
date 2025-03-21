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