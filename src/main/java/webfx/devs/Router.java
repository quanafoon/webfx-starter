package webfx.devs;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Set;
import java.util.Stack;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;


/**
 * This is the Router class which can be used to navigate between different pages.
 */
public class Router {
    
    private Stack<HistoryData<String, String>> history;
    private java.net.URI currURI = null;
    private boolean backwards = false;
    private String packageName;
    HashMap<String, Method> methods;
    private WebView webView = null;

    /**
     * This constructor of the Router class which can be used to navigate between different pages.
     * It accepts the string of the first page to be rendered, and the current webView
     * Throws an IllegalArgumentException if the filename is not in the templates folder
     * @param filename , The name of the file
     * @param webView , WebView object
     */
    public Router(String filename, WebView webView) throws IllegalArgumentException {
        this.history = new Stack<>();
        history.push(new HistoryData<>(filename, ""));
        this.packageName = ResourceManager.evaluateClazz().getPackageName();
        this.webView = webView;
        currURI = ResourceManager.getRenderedPageUri();
        Reflections reflection = new Reflections(packageName, new MethodAnnotationsScanner());
        this.methods = mapMethods(reflection.getMethodsAnnotatedWith(Endpoint.class));
        if(ResourceManager.getTemplate(filename) == null)
            throw new IllegalArgumentException("Please pass a valid /template file");
        else
            render(filename);
    }
    

    /**
     * Accepts the name of a template and renders the page
     * @param filename , the name of the template
     */
    public void render(String filename){
        try{
            String template = ResourceManager.readFromTemplate(filename);
            if (currURI == null || template == null)
                return;
            Path newTemplate = Paths.get(currURI);
            if(!ResourceManager.writeToFile(newTemplate, template)){
                return;
            }
            TemplateEngine.clearPlaceholders(newTemplate, template);
            webView.getEngine().load(newTemplate.toUri().toString());

            if(!backwards)
                history.push(new HistoryData<>(filename, ""));
            else
                backwards = false;
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * Accepts the name of a template and the data to be injected, and renders the page
     * @param filename , the name of the template
     * @param data , the data to inject into the template
     */
    public void renderWithData(String filename, String data){
        try{
            String template = ResourceManager.readFromTemplate(filename);
            if (currURI == null || template == null)
                return;
            Path newTemplate = Paths.get(currURI);
            if(!ResourceManager.writeToFile(newTemplate, template)){
                return;
            }
            TemplateEngine.injectData(newTemplate, template, data);
            webView.getEngine().load(newTemplate.toUri().toString());
            if(!backwards)
                history.push(new HistoryData<>(filename, data));
            else
                backwards=false;
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * Go back to the last page in the Router's history, maintaining the data that was rendered to that page
     * 
     */
    public void goBack(){
        if (history.size() <= 1)
            return;
        backwards = true;
        history.pop();
        String data = history.peek().data;
        if(data.equals(""))
            render(history.peek().filename);
        else
            renderWithData(history.peek().filename, data);
    }


    /**
     * This is serves to be the method that javascript can call for form submission. 
     * <p>It reads through the input fields of the specified form id and carries the data to the specified endpoint. </p>
     * <p>Endpoints expecting data, must be have a single parameter of type {@code Data} </p>
     * <p>Empty form values will be interpreted as null </p>
     * @param id , indication the form id
     * @param endpoint , indicating the annotated endpoint name i.e "@Endpoint(name="")"
     */
    public void submission(String id, String endpoint){
        try{
            if(methods.containsKey(endpoint)){
                JSObject inputs = (JSObject) webView.getEngine().executeScript("document.querySelectorAll('#" + id + " input')");
                int length = (Integer) inputs.getMember("length");
                Data data = new Data();
                for(int i=0; i < length; i++){
                    JSObject input = (JSObject) inputs.getSlot(i);
                    Object value = input.getMember("value");
                    String property = (String) input.getMember("id");
                    if(value.equals(""))
                        value = null;
                    data.put(property, value);
                }
                try{
                    methods.get(endpoint).invoke(null, data);
                    return;
                } catch (IllegalArgumentException | NullPointerException e){
                    if(e instanceof IllegalArgumentException)
                        System.out.println("Error routing to endpoint: " + endpoint + ", Please ensure that it either has no parameters or it's only parameter is of type: Data");
                    if(e instanceof NullPointerException)
                        System.out.println("Error routing to endpoint: " + endpoint + ", Please ensure that the method that " + endpoint + " annotates is static");
                    return;
                }
            }
            else{
                System.out.println(endpoint + " does not exist or is not annotated properly");
            }
        } catch (Exception e){
            if (!(e instanceof InvocationTargetException))
                e.printStackTrace();
            return;
        }
    }


    /**
     * Returns a mapping of all endpoints with a given set of methods
     * @param methods , A {@code Set} of methods with {@code @Endpoint} Annotations
     * @return {@code HashMap<String, Method>} , of all endpoint names and methods
     */
    public HashMap<String, Method> mapMethods(Set<Method> methods){
        HashMap<String, Method> map = new HashMap<>();
        try{
            for(Method method : methods){
                method.setAccessible(true);
                String name = method.getAnnotation(Endpoint.class).name();
                if(!map.containsKey(name))
                    map.put(name, method);
            }
            return map;
        } catch (Exception e){
            e.printStackTrace();
            return map;
        }
    }

    
    /**
     * Routes to a specific endpoint given the endpoint's name. Data as a {@code String} can also be passed, insert {@code null} if no data is being passed
     * @param endpoint , indicating the annotated endpoint name i.e "@Endpoint(name="")"
     * @param jsonString , data being passed, insert {@code null} if no data is meant to be passed
     */
    public void route(String endpoint, String jsonString){
        try{
            if(!methods.containsKey(endpoint)){
                System.out.println(endpoint + " does not exist or is not annotated properly");
            }
            Method method = methods.get(endpoint);
            if(jsonString == null){
                method.invoke(null);
                return;
            }
            else{
                JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();
                Data data = DataManager.jsonToData(json);
                try{
                    method.invoke(null, data);
                    return;
                } catch (IllegalArgumentException | NullPointerException e){
                    if(e instanceof IllegalArgumentException)
                        System.out.println("Error routing to endpoint: " + endpoint + ", Please ensure that it either has no parameters or it's only parameter is of type: Data");
                    if(e instanceof NullPointerException)
                        System.out.println("Error routing to endpoint: " + endpoint + ", Please ensure that the method that " + endpoint + " annotates is static");
                    return;
                }
            }
        } catch (Exception e){
            System.out.println("Error during routing");
            e.printStackTrace();
        }
    }


    /**
     * Represents a key-value pair used to store the router's navigation history.
     *
     * @param <K> the type of the filename or key
     * @param <V> the type of the associated data
     */
    public class HistoryData<K, V> {

        /**
         * The name of the file or page associated with the history entry.
         */
        public K filename;

        /**
         * The data associated with the history entry.
         */
        public V data;

        /**
         * Constructs a new HistoryData entry with the specified filename and data.
         *
         * @param filename the file or page name
         * @param data the data to associate with the filename
         */
        public HistoryData(K filename, V data) {
            this.filename = filename;
            this.data = data;
        }
    }
}