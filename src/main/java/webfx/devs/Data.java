package webfx.devs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


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
     * This can be used as a safe alternative to casting the desired type on data. 
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


    /**
    * Retrieves the value associated with the given key if it is a {@code List}.
    *
    * @param key The key whose associated value is expected to be an {@code ArrayList}.
    * @return The {@code ArrayList<Object>} if the value exists and is a list, {@code null} otherwise.
    */
    @SuppressWarnings("unchecked")
    public ArrayList<Object> getList(String key){
        try{
            if(this.get(key) instanceof ArrayList)
                return (ArrayList<Object>) this.get(key);
            return null;
        }catch(Exception e){
            return null;
        }
    }


    /**
     * Retrieves the value associated with the given key if it is a {@link Data} object.
     *
     * @param key The key whose associated value is expected to be a {@code Data} object.
     * @return The {@code Data} object if present and valid, or {@code null} if not.
     */
    public Data getData(String key){
        try{
            if(this.get(key) instanceof Data)
                return (Data) this.get(key);
            return null;
        } catch (Exception e){
            return null;
        }
    }


    /**
     * Finds a specific value within a {@link Data} object, given a key which represents a path to that value
     * <p>Example Key: "object1.object2.list[0].value" </p>
     * <p>Only objects and lists should be followed by ".", with the desired value following the final "." </p>
     * <p>When looking for specific values within lists, the location of that value should be enclosed {@code []} and follow the list's name, e.g. myList[loc] </p>
     * @param key The location of the value within the {@code Data} object.
     * @return The desired value as an {@code Object} 
     */
    public Object find(String key){
        try{

            if(!key.contains(".")){
                return this.get(key);
            }

            List<String> keys = new ArrayList<>(List.of(key.split("\\.")));
            Data curr = this;
            key = keys.removeLast();

            for(String k : keys){
                if(isFindList(k)){
                    int loc = getListLoc(k);
                    curr = DataManager.toData(curr.getList(getListName(k)).get(loc));
                }
                else
                    curr = curr.getData(k);
                if(curr == null)
                    return null;
            }
            if(isFindList(key))
                return curr.getList(getListName(key)).get(getListLoc(key));
            else
                return curr.get(key);
        }catch(Exception e){
            return null;
        }
    }


    /**
     * Extracts the index value from a key string in the format "listName[index]".
     * <p>Serves as a helper for {@link Data#find(String) find()}</p>
     *
     * @param list The string containing a list name with an index.
     * @return The index as an integer, or -1 if invalid.
     */
    private int getListLoc(String list){
        try{
            if(!isFindList(list))
                return -1;
    
            int start = list.indexOf("[") + 1;
            int end = list.indexOf("]");

            String num = list.substring(start, end);

            return Integer.parseInt(num);
        } catch (Exception e){
            return -1;
        }
    }


    /**
     * Extracts the list name from a key string in the format "listName[index]".
     * <p>Serves as a helper for {@link Data#find(String) find()}</p>
     *
     * @param list The string containing a list name with an index.
     * @return The name of the list, or {@code null} if the format is invalid.
     */
    private String getListName(String list){
        try{
            if(!isFindList(list))
                return null;

            int end = list.indexOf("[");
            String name = list.substring(0, end);

            return name;
        }catch(Exception e){
            return null;
        }
    }


    /**
     * Used to determine if a specified String is a list in a find search String.
     * <p>Serves as a helper for {@link Data#find(String) find()}</p>
     * 
     * @param list The string containing a list name with an index.
     * @return {@code True} if it is a list, {@code False} otherwise
     */
    private boolean isFindList(String list){
        if(!list.contains("[") || !list.endsWith("]") || list.startsWith("["))
            return false;
        int start = list.indexOf("[") + 1;
        int end = list.indexOf("]");
        if(end - start < 1)
            return false;
        return true;
    }


    /**
     * Accepts a key from the {@code Data} object being used to call this method and returns an {@code ArrayList} if applicable.
     * <p>The ArrayList will contain type: {@code String} </p>
     * <p>This method expects the key's value to be of the form: "[val, val, val]" </p>
     * @param key The key corresponding the desired data
     * @return An {@code ArrayList<String>} of the Data's content for the specified key, or {@code null}
     */
    public ArrayList<String> getListAsString(String key){
        try{
            Object data = this.get(key);
            String str = data.toString();
            ArrayList<String> list = DataManager.parseList(str, String.class);
            return list;
        }catch(Exception e){
            return null;
        }
    }


    /**
     * Create an Iterator from {@code Data}. The Iterator accesses all the immediate values to this {@code Data} object.
     * @return A {@code Iterator<Object>} of each value in the data object
     */
    public Iterator<Object> getIterator(){
        try{
            ArrayList<Object> values = new ArrayList<>();
            for(String key : this.keySet()){
                values.add(this.get(key));
            }
            Iterator<Object> iterator = values.iterator();
            return iterator;
        }catch(Exception e){
            return null;
        }
    }


}