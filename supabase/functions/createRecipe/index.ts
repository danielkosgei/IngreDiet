import { serve } from "https://deno.land/std@0.177.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

serve(async (req) => {
  try {
    // Parse request body
    const recipeData = await req.json();
    
    // Validate required fields
    if (!recipeData.name || !recipeData.ingredients || !recipeData.instructions) {
      throw { code: 400, message: "Name, ingredients, and instructions are required" };
    }

    // Create Supabase client
    const supabaseUrl = Deno.env.get("SUPABASE_URL") ?? "";
    const supabaseKey = Deno.env.get("SUPABASE_ANON_KEY") ?? "";
    
    const supabase = createClient(supabaseUrl, supabaseKey, {
      global: {
        headers: {
          Authorization: req.headers.get("Authorization") || "",
        },
      },
    });

    // Insert recipe
    const { data, error } = await supabase
      .from("recipes")
      .insert(recipeData)
      .select()
      .single();

    if (error) throw error;

    return new Response(JSON.stringify(data), {
      headers: { "Content-Type": "application/json" },
    });
  } catch (error) {
    return new Response(
      JSON.stringify({ 
        code: error.code || 500, 
        message: error.message 
      }),
      { 
        status: error.code || 500,
        headers: { "Content-Type": "application/json" },
      }
    );
  }
}); 