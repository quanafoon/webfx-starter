package webfx.devs;

import java.lang.reflect.Field;
import com.google.gson.JsonObject;



/**
 * A utility class for working with {@link Data} Objects.
 * <p>
 * This class is responsible for validating and transforming JSON input into usable Java structures,
 * particularly the custom {@code Data} object. It is especially useful in cases where there is a need
 * to bind frontend form data to backend Java objects.</p>
 * <p>It utilizes {@code com.google.gson}'s JsonParser to efficiently serialize data, and uses {@link Data} objects for efficient retrieval and error handling.</p>
 */
public class DataManager {
    

    private DataManager(){}



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
     * Asseses a {@code Data}  object against a {@code Class} and determines if the fields of the object 
     * can match the types of the Class, for that attribute. For each attribute, we look for the corresponding key,
     * meaning that if there is a {@code Data} key that does not map to an attribute's name, it does not become part 
     * of the returned Data object
     * <p>
     * Meant to validate data, ensuring that it's values can fit to a class's types and handle situations where it cannot.
     * <p>
     * The keys of json data must be identical to the names class's attributes. Using any {@code null} parameter returns {@code null}
     * @param clazz The class that we are comparing attributes to
     * @param data , The data we are anyalyzing
     * @return A {@code Data} object if data is valid, {@code null} otherwise
     */
    public static Data validateData(Class<?> clazz, Data data){
        if (data == null || clazz == null) 
            return null;
        Field[] fields = clazz.getDeclaredFields();
        Data validated = new Data();
        for(Field field : fields){
            field.setAccessible(true);
            try{
                Class<?> type  = field.getType();
                String key = field.getName();
                Object inputField = data.get(key);
                if(inputField != null){
                    Object value = checkDataType(type, inputField);
                    if(value != null)
                        validated.put(key, value);
                    else
                        return null;
                }
            } catch (Exception e){
                return null;
            }
        }
        return validated;
    }


    /**
     * Checks the type that a java class expects against the provided {@code Object}
     *
     * @param type , the Java class type that we are checking for
     * @param data , the data that is meant to be checked
     * @return {@code Object} if the type can be converted, {@code null} otherwise
     */
    public static Object checkDataType(Class<?> type, Object data) {
        try {
            String value = data.toString().trim();
            if (type == String.class)
                return value;
            else if (type == int.class || type == Integer.class)
                return Integer.parseInt(value);
            else if (type == long.class || type == Long.class)
                return (long) Double.parseDouble(value);
            else if (type == float.class || type == Float.class)
                return Float.parseFloat(value);
            else if (type == double.class || type == Double.class)
                return Double.parseDouble(value);
            else if (type == boolean.class || type == Boolean.class)
                return Boolean.parseBoolean(value);
            return value;
        } catch (Exception e) {
            return null;
        }
    }

}
