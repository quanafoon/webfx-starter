package webfx.devs;

import java.lang.reflect.Field;
import java.util.HashMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;



/**
 * A utility class for working with {@code JsonObject} and {@link Data} Objects.
 * <p>
 * This class is responsible for validating and transforming JSON input into usable Java structures,
 * particularly the custom {@code Data} object. It is especially useful in cases where there is a need
 * to bind frontend form data to backend Java objects without manually parsing each field.</p>
 * <p>To use this class, ensure that {@code com.google.gson} is available as a project dependency.</p>
 */
public class DataManager {
    

    private DataManager(){}

    /**
     * Asseses a {@code JsonObject} against a {@code Class} and determines if the fields of the object 
     * can match the types of the Class, for that attribute. For each attribute, we look for the corresponding key,
     * meaning that if there is a json key that does not map to an attribute's name, it does not become part 
     * of the returned Data object
     * <p>
     * Meant to validate json data, ensuring that it's values can fit to a class's types and handle situations where it cannot.
     * <p>
     * The keys of json data must be identical to the names class's attributes. Using any {@code null} parameter returns {@code null}
     * @param clazz The class that we are comparing attributes to
     * @param json , The json data we are anyalyzing
     * @return A {@code Data} object if data is valid, {@code null} otherwise
     */
    public static Data validateJson(Class<?> clazz, JsonObject json){
        if (json == null || clazz == null) 
            return null;
        Field[] fields = clazz.getDeclaredFields();
        Data data = new Data();
        for(Field field : fields){
            field.setAccessible(true);
            try{
                Class<?> type  = field.getType();
                String key = field.getName();
                JsonElement inputField = json.get(key);
                if(inputField != null){
                    Object value = checkDataType(type, inputField);
                    if(value != null)
                        data.put(key, value);
                    else
                        return null;
                }
            } catch (Exception e){
                return null;
            }
        }
        return data;
    }

    
    /**
     * Converts a {@code JsonObject} to a {@code HashMap<String, JsonElements>}.
     * Each key-value pair in the JSON object is added to the map.
     *
     * @param json the {@code JsonObject} to be converted
     * @return a {@code HashMap} containing the JSON key-value pairs, or {@code null} if an exception occurs
     */
    public static HashMap<String, JsonElement> jsonToMap(JsonObject json){
        try{
            HashMap<String, JsonElement> map = new HashMap<>();
            for(String key : json.keySet()){
                map.put(key, json.get(key));
            }
            return map;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Converts a {@code JsonObject} to a {@code Data} object.
     * Each key-value pair in the JSON object is added to the map.
     *
     * @param json the {@code JsonObject} to be converted
     * @return a {@code HashMap} containing the JSON key-value pairs, or {@code null} if an exception occurs
     */
    public static Data jsonToData(JsonObject json){
        try{
            Data map = new Data();
            for(String key : json.keySet()){
                map.put(key, json.get(key));
            }
            return map;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Checks the type that what a java class expects and returns
     *
     * @param type , the Java class type that we are checking for
     * @param data , the data that is meant to be checked
     * @return {@code String} the equivalent SQLite type as a String
     */
    private static Object checkDataType(Class<?> type, JsonElement data) {
        try{
            if (type == String.class)
                return data.getAsString();
            else if (type == int.class || type == Integer.class)
                return data.getAsInt();
            else if (type == long.class || type == Long.class)
                return data.getAsLong();
            else if (type == float.class || type == Float.class)
                return data.getAsFloat();
            else if (type == double.class || type == Double.class)
                return data.getAsDouble();
            else if (type == boolean.class || type == Boolean.class)
                return data.getAsBoolean();
            return data.getAsString();
        } catch (Exception e){
            return null;
        }
    }
}
