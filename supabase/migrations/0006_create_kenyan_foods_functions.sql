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
