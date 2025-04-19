package webfx.devs;

import java.util.HashMap;


/**
 * The Data class is an extension of a {@code HashMap<String, Object>} and so all HashMap operations
 * can be performed on it.
 * It can work alongside the DataManager to easily and efficiently reteieve data from json format (com.google.gson.JsonObject)
 */
public class Data extends HashMap<String, Object>{
    
    public Data(){
        super();
    }

    public Data(HashMap<String, Object> data){
        super(data);
    }

}
