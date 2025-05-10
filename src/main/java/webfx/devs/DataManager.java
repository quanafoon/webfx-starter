package webfx.devs;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.lang.Character;

import com.google.gson.JsonArray;
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
     * Accepts a key from the {@code Data} object being used to call this method and returns an {@code ArrayList} if applicable.
     * <p>The ArrayList will contain type: {@code String} </p>
     * @param key The key corresponding the desired data
     * @return An {@code ArrayList<String>} of the Data's content for the specified key, or {@code null}
     */

    /**
     * Accepts a {@code String} and a returns an {@code ArrayList} of the String's contents if applicable.
     * <p>Accepts a {@code Class<T>} and attempts to cast/convert it to the specified type </p>
     * <p>This method expects the String to be of the form: "[val, val, val]" </p>
     * @param <T> the generic type
     * @param data the String to be converted to an ArrayList
     * @param type the type of the ArrayList
     * @return An {@code ArrayList} of the specified type if the String can be converted and is of the correct structure, {@code null} otherwise
     */
    @SuppressWarnings("unchecked")
    public static <T> ArrayList<T> parseList(String data, Class<T> type){
        try{
            String str = data.toString();
            Character curr = str.charAt(0);
            
            if(!curr.equals('[')){
                System.out.println("Conversion Error: Data did not start with a '['");
                return null;
            }
            curr = str.charAt(str.length()-1);
            if(!curr.equals(']')){
                System.out.println("Conversion Error: Data did not end in ']'");
                return null;
            }
            String word = "";
            ArrayList<T> list = new ArrayList<>();
            Object checkedWord;
            for(int i = 1; i < str.length(); i++){
                curr = str.charAt(i);
                if(curr.equals(']')){
                    checkedWord = checkDataType(type, word);
                    list.add((T) checkedWord);
                    break;
                }
                if(curr.equals('"'))
                    continue;
                if(curr.equals(',')){
                    checkedWord = checkDataType(type, word);
                    list.add((T) checkedWord);
                    word = "";  
                    continue;
                }
                word += curr;
            }
            return list;
        }catch(Exception e){
            return null;
        }
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
