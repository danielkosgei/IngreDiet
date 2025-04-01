import { serve } from "https://deno.land/std@0.177.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

serve(async (req) => {
  try {
    // Get recipe ID from URL
    const url = new URL(req.url);
    const id = url.searchParams.get('id');
    
    if (!id) {
      throw { code: 400, message: "Recipe ID is required" };
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

    // Get recipe by ID
    const { data, error } = await supabase
      .from("recipes")
      .select("*")
      .eq("id", id)
      .single();

    if (error) throw error;
    if (!data) throw { code: 404, message: "Recipe not found" };

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