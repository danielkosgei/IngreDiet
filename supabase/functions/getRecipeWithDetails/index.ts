import { serve } from "https://deno.land/std@0.177.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

serve(async (req) => {
  try {
    const url = new URL(req.url);
    const id = url.searchParams.get('id');
    
    if (!id) {
      throw { code: 400, message: "Recipe ID is required" };
    }

    const supabaseUrl = Deno.env.get("SUPABASE_URL") ?? "";
    const supabaseKey = Deno.env.get("SUPABASE_ANON_KEY") ?? "";
    
    const supabase = createClient(supabaseUrl, supabaseKey, {
      global: {
        headers: {
          Authorization: req.headers.get("Authorization") || "",
        },
      },
    });

    // Get recipe with all related data
    const { data: recipe, error: recipeError } = await supabase
      .from("recipes")
      .select("*")
      .eq("id", id)
      .single();

    if (recipeError) throw recipeError;
    if (!recipe) throw { code: 404, message: "Recipe not found" };

    // Get ingredients
    const { data: ingredients, error: ingredientsError } = await supabase
      .from("recipe_ingredients")
      .select("*, ingredients(*)")
      .eq("recipe_id", id);

    if (ingredientsError) throw ingredientsError;

    // Get instructions
    const { data: instructions, error: instructionsError } = await supabase
      .from("recipe_instructions")
      .select("*")
      .eq("recipe_id", id)
      .order("step_number", { ascending: true });

    if (instructionsError) throw instructionsError;

    // Get nutrition facts
    const { data: nutrition, error: nutritionError } = await supabase
      .from("recipe_nutrition")
      .select("*")
      .eq("recipe_id", id)
      .single();

    // Combine all data
    const fullRecipe = {
      ...recipe,
      ingredients: ingredients,
      instructions: instructions,
      nutrition: nutrition || null
    };

    return new Response(JSON.stringify(fullRecipe), {
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