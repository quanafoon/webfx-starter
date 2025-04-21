package webfx.devs;

import java.util.HashMap;


/**
 * The Data class is an extension of a {@code HashMap<String, Object>} and so all HashMap operations
 * can be performed on it.
 * It can work alongside the DataManager to easily store data and efficiently reteieve it.
 */
public class Data extends HashMap<String, Object>{


    /**
     * Initializes a {@code Data} Object
     */
    public Data(){
        super();
    }

    /**
     * Initializes a {@code Data} Object with an existing {@code HashMap<String, Object>}
     * @param data The map used to initalize the object 
     */
    public Data(HashMap<String, Object> data){
        super(data);
    }


    /**
     * Retrieves a value from the Data object and attempts to cast/convert it to the specified type.
     * This can be used as a safe an alternative to casting the desired type on data. 
     * <p>The safest ways of accessing data in a {@code Data} object for types other than string, are through this method and typecasting after ensuring DataManager's {@link DataManager#validateData(Class, Data) validateData()}  is not null.</p>
     * @param <T> the generic type
     * @param key The key in the Data object.
     * @param type The Class object of the type to convert to.
     * @return The converted value of type T, {@code null} if conversion fails.
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        try {
            Object value = this.get(key);
            Object converted = DataManager.checkDataType(type, value);
            return (T) converted;
        } catch (Exception e) {
            return null;
        }
    }
}
