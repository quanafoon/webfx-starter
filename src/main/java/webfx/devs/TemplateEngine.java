package webfx.devs;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.regex.Pattern;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;


/**
 * The main class that runs the lightweight {@link TemplateEngine}. It replaces values encapsulated with {{}}, 
 * with the appropriate injected data.
 * <p>The template engine runs by default, but setting the value of {@code running} to {@code false},
 * prevents the template from performing operations on rendered templates</p>
 * <p>To turn off the engine, call {@code setRunning(false)}</p>
 */
public class TemplateEngine {
    
    private static boolean running = true;


    private TemplateEngine(){}

    /**
     * Injects data into the template and writes it to the specified {@code Path}.
     * <p> This can be used whether or not the engine is running. {@code TemplateEngine.setRunning()} sets whether
     * or not the template runs</p>
     * @param output The destination path (Path can be obtained from Path.of(URI))
     * @param content The raw template string with placeholders
     * @param data JSON string representing key-value pairs
     */
    public static void injectData(Path output, String content, String data){
        if(!running)
            return;
        try{
            JsonElement jsonElement = JsonParser.parseString(data);
            if(jsonElement.isJsonObject()){
                JsonObject json = jsonElement.getAsJsonObject();
                for(Map.Entry<String, JsonElement> entry : json.entrySet()){
                    content = content.replaceAll("\\{\\{" + Pattern.quote(entry.getKey()) + "\\}\\}", entry.getValue().getAsString());
                }
                content = clearPlaceholders(content);
                ResourceManager.writeToFile(output, content);
            }
            else{
                return;
            }
        }catch (JsonSyntaxException e){
            e.printStackTrace();
            return;
        }
    }

    /**
     * Clears all the placeholders in a given {@code Path} and rewrites the template
     * <p> This can be used whether or not the engine is running. {@code TemplateEngine.setRunning()} sets whether
     * or not the template runs</p>
     * @param output , The {@code Path} of the template to write to
     * @param content , The contents of the template. Can call (ResourceManager.readFromTemplate()) to get this.
     */
    public static void clearPlaceholders(Path output, String content){
        if(!running)
            return;
        content = content.replaceAll("\\{\\{.*\\}\\}", "");
        ResourceManager.writeToFile(output, content);
    }

    /**
     * Clears all the placeholders in the template
     * <p> This can be used whether or not the engine is running. {@code TemplateEngine.setRunning()} sets whether
     * or not the template runs</p>
     * @param content , The contents of the template. Can call (ResourceManager.readFromTemplate()) to get this.
     * @return {@code String} of the new content
     */
    public static String clearPlaceholders(String content){
        if(!running)
            return null;
        return content.replaceAll("\\{\\{.*\\}\\}", "");
    }


    /**
     * Adds a stylesheet from it's given {@code filename} using the {@link ResourceManager}'s currently set external {@code Path} i.e. {@code directoryName} and {@code filename}.
     * <p>Stylesheet must be located in {@code resources/styles} </p>
     * <p>This is done by fetching the stylesheet with the given {@code filename} 
     * and adding a {@code <link>} to the html that the {@code Path} is represents. 
     * <p> This can be used whether or not the engine is running. {@code TemplateEngine.setRunning()} sets whether
     * or not the template runs</p>
     * @param filename , The name of the stylesheet that's being added
     * @return {@code boolean} true if successful, false otherwise
     */
    public static boolean addStylesheet(String filename){

        Path output = Paths.get(ResourceManager.getRenderedPageUri());
        if(output == null){
            System.out.println("Error adding stylesheet. Could not locate external html file");
            return false;
        }
        String stylesheet = ResourceManager.getStylesheet(filename);
        if(stylesheet == null){
            System.out.println("Error adding stylesheet. Could not locate the stylesheet: " + filename + " in the stylesheet folder");
            return false;
        }
        String style = "<link rel='stylesheet' type='text/css' href='" + stylesheet + "'>";
        try{
            String content = Files.readString(output);
            if(content.contains("</head>"))
                content = content.replace("</head>", style + "\n</head>");
            else
                content = style + content;
                ResourceManager.writeToFile(output, content);
            return true;
        } catch (Exception e){
            System.out.println("Error adding stylesheet");
            return false;
        }
    }



    /**
     * Adds a stylesheet to a given {@code Path}.
     * <p>Stylesheet must be located in {@code resources/styles}</p>
     * <p>This is done by fetching the stylesheet with the given {@code filename} 
     * and adding a {@code <link>} to the html that the {@code Path} is represents </p>
     * <p> This can be used whether or not the engine is running. {@code TemplateEngine.setRunning()} sets whether
     * or not the template runs</p>
     * @param output , The {@code Path} of the template to write to
     * @param filename , The name of the stylesheet that's being added
     * @return {@code boolean} true if successful, false otherwise
     */
    public static boolean addStylesheet(Path output, String filename){
        String stylesheet = ResourceManager.getStylesheet(filename);
        if(stylesheet == null){
            System.out.println("Error adding stylesheet. Could not locate the stylesheet: " + filename + " in the stylesheet folder");
            return false;
        }
        String style = "<link rel='stylesheet' type='text/css' href='" + stylesheet + "'>";
        try{
            String content = Files.readString(output);
            if(content.contains("</head>"))
                content = content.replace("</head>", style + "\n</head>");
            else
                content = style + content;
                ResourceManager.writeToFile(output, content);
            return true;
        } catch (Exception e){
            System.out.println("Error adding stylesheet");
            return false;
        }
    }


    /**
     * Get whether or not the engine runs when templates are rendered
     * @return {@code true} if the template engine is active; {@code false} otherwise
     */
    public static boolean isRunning() {
        return running;
    }
    
    /**
     * Set whether or not the engine runs when templates are rendered
     * @param running The {@code boolean} attribute that determines if the template engine runs or not
     */
    public static void setRunning(boolean running) {
        TemplateEngine.running = running;
    }

    
}
