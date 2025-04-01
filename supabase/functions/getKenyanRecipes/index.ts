import { serve } from "https://deno.land/std@0.177.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

serve(async (req) => {
  try {
    // Create a Supabase client with the Auth context of the logged in user
    const supabaseUrl = Deno.env.get("SUPABASE_URL") ?? "";
    const supabaseKey = Deno.env.get("SUPABASE_ANON_KEY") ?? "";
    
    // Create a Supabase client with the Authorization header from the request
    const supabase = createClient(supabaseUrl, supabaseKey, {
      global: {
        headers: {
          Authorization: req.headers.get("Authorization") || "",
        },
      },
    });

    // Parse the request parameters
    const params = await req.json().catch(() => ({}));
    console.log("Request parameters:", params);

    // Determine which operation to perform based on parameters
    let data;
    
    // Get recipe by ID
    if (params.id) {
      try {
        // Use the get_complete_kenyan_recipe function we created in the database
        const { data: result, error } = await supabase.rpc(
          "get_complete_kenyan_recipe", 
          { recipe_id: params.id }
        );
        
        if (error) throw error;
        
        data = result;
      } catch (error) {
        console.error("Error getting Kenyan recipe by ID:", error);
        throw error;
      }
    }
    // Search recipes by query
    else if (params.query) {
      try {
        // Use the search_kenyan_recipes function we created in the database
        const { data: result, error } = await supabase.rpc(
          "search_kenyan_recipes", 
          { search_query: params.query }
        );
        
        if (error) throw error;
        
        // Get complete data for each recipe
        const recipesWithDetails = await Promise.all(
          result.map(async (recipe) => {
            const { data: details, error } = await supabase.rpc(
              "get_complete_kenyan_recipe", 
              { recipe_id: recipe.id }
            );
            
            if (error) {
              console.error("Error getting recipe details:", error);
              return null;
            }
            
            return details;
          })
        );
        
        data = recipesWithDetails
          .filter(r => r !== null)
          .slice(0, params.limit || 10);
      } catch (error) {
        console.error("Error searching Kenyan recipes:", error);
        throw error;
      }
    }
    // Get recipes by region
    else if (params.region) {
      try {
        // Use the get_kenyan_recipes_by_region function we created in the database
        const { data: result, error } = await supabase.rpc(
          "get_kenyan_recipes_by_region", 
          { region_name: params.region }
        );
        
        if (error) throw error;
        
        // Get complete data for each recipe
        const recipesWithDetails = await Promise.all(
          result.map(async (recipe) => {
            const { data: details, error } = await supabase.rpc(
              "get_complete_kenyan_recipe", 
              { recipe_id: recipe.id }
            );
            
            if (error) {
              console.error("Error getting recipe details:", error);
              return null;
            }
            
            return details;
          })
        );
        
        data = recipesWithDetails
          .filter(r => r !== null)
          .slice(0, params.limit || 10);
      } catch (error) {
        console.error("Error getting Kenyan recipes by region:", error);
        throw error;
      }
    }
    // Default - get all Kenyan recipes with pagination
    else {
      try {
        // Get basic recipe data
        const { data: result, error } = await supabase
          .from("kenyan_recipes")
          .select("*")
          .limit(params.limit || 10);
        
        if (error) throw error;
        
        // Get complete data for each recipe
        const recipesWithDetails = await Promise.all(
          result.map(async (recipe) => {
            const { data: details, error } = await supabase.rpc(
              "get_complete_kenyan_recipe", 
              { recipe_id: recipe.id }
            );
            
            if (error) {
              console.error("Error getting recipe details:", error);
              return null;
            }
            
            return details;
          })
        );
        
        data = recipesWithDetails
          .filter(r => r !== null);
      } catch (error) {
        console.error("Error getting all Kenyan recipes:", error);
        throw error;
      }
    }

    return new Response(JSON.stringify(data), {
      headers: { "Content-Type": "application/json" },
    });
  } catch (error) {
    console.error("Error processing request:", error);
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