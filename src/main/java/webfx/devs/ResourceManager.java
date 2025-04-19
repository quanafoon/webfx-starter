package webfx.devs;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;


/**
 * A utility class that is used to retrieve and manipulate resources.
 * <p>Also performs operations on the External Temp File. The External Temp File is the html file where
 * html content are rendered to.
 * The default location is {@code webfx-external\current.html} in the user home directory, but
 * this can be altered using {@code setTempFileURI()}.</p>
 */
public class ResourceManager {

    // The class within a package that contains all resources
    public static Class<?> clazz = evaluateClazz();

    //The directory and filename for the external Temp File where html content is rendered to
    private static String directoryName = "webfx-external";
    private static String filename = "current.html";

    /**
     * Retrieves an image from a given filename.
     * @param filename , the name of the image file
     * @return an {@code Image} object
     */
    public static Image get_image(String filename) {
        java.net.URL URL = clazz.getResource("/images/" + filename);
        
        if (URL != null) {
            return new Image(URL.toExternalForm());
        }
        System.out.println("The file: " + filename + " could not be found");
        return null;
    }


    /**
     * Retrieves the URL of the given file (as a string) in external form
     * <p>File structure must be {@code Resources/templates/filename}</p>
     * @param filename , The name of the template
     * @return {@code String} The filename's resource path (External Form)
     */
    public static String getTemplate(String filename){
        java.net.URL URL = null;
        if(filename.endsWith(".html")){
            URL = clazz.getResource("/templates/" + filename);
        }
        else{
            URL = clazz.getResource("/templates/" + filename + ".html");
        }
        if(URL != null){
            return URL.toExternalForm();
        }
        System.out.println("The file: " + filename + " could not be found");
        return null;
    }


    /**
     * Retrieves the URI of the given file (as a string) in external form
     * <p>File structure must be {@code Resources/style/filename}</p>
     * @param filename , The name of the stylesheet
     * @return {@code String} The filename's resource path (External Form)
     */
    public static String getStylesheet(String filename){
        java.net.URL URL = null;
        try{
            if(filename.endsWith(".css")){
                URL = clazz.getResource("/style/" + filename);
            }
            else{
                URL = clazz.getResource("/style/" + filename + ".css");
            }
            if(URL != null){
                return URL.toExternalForm();
            }
            return null;
        } catch (Exception e){
            System.out.println("The file: " + filename + " could not be found");
            return null;
        }
    }


   /**
     * Returns the URI of the HTML file where rendered pages are written. Creates it if it doesn't exist.
     * <p>The file and its directory are saved to the user's home directory</p>
     * <p>The default directory is {@code "webfx-external"} and filename is {@code "current.html"}.</p>
     * <p>Use {@code setTempFileURI()} to modify the default directory and filename </p>
     * @return The {@code java.net.URI} of the html file
     */
    public static URI getRenderedPageUri(){
        try {
            Path baseDir = Paths.get(System.getProperty("user.home"));
            if(directoryName != null){
                baseDir = Paths.get(System.getProperty("user.home"), directoryName);
            }
            Files.createDirectories(baseDir);
            Path filePath = baseDir.resolve(filename);
            return filePath.toUri();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Returns a writable URI inside a given directory. The file and its directory are saved to the user's home directory
     * <p>This is the function that is used to create the html file where rendered pages are written.</p>
     * <p>Leaving a paramter null results in the use of the already existing directory and file names,
     * where the default {@code directoryName} is {@code "webfx-external"} and the default {@code filename} is {@code "current.html"} </p> 
     * @param directoryName Name of the subdirectory (e.g., "temp", "rendered", etc.)
     * @param filename Name of the file (e.g., "current.html")
     * @return The URI to a file inside the given directory, or null if an error occurs
     */
    public static URI setTempFileURI(String directoryName, String filename) {
        if(directoryName == null) 
            directoryName = ResourceManager.directoryName;
        if(filename == null)
            filename = ResourceManager.filename;
        try {
            ResourceManager.directoryName = directoryName;
            ResourceManager.filename = filename;
            Path baseDir = Paths.get(System.getProperty("user.home"), directoryName);
            Files.createDirectories(baseDir);
            Path filePath = null;
            if(filename.endsWith(".html")){
                filePath = baseDir.resolve(filename);
            }
            else{
                filePath = baseDir.resolve(filename + ".html");
            }

            return filePath.toUri();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Reads the contents within a template
     * <p> Template must be in {@code resources/template/}</p>
     * @param filename , The name of the template
     * @return {@code String} of the file's contents
     */
    public static String readFromTemplate(String filename){
        InputStream in = null;
        if(filename.endsWith(".html")){
            in = clazz.getResourceAsStream("/templates/" + filename);
        }
        else{
            in = clazz.getResourceAsStream("/templates/" + filename + ".html");
        }
        if(in == null){
            System.out.println("The file: " + filename + " could not be found");
            return null;
        }
        try{
            String content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            return content;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Reads the contents within a stylesheet
     * <p> Stylesheet must be in {@code resources/style/}</p>
     * @param filename , The name of the stylesheet
     * @return {@code String} of the file's contents
     */
    public static String readFromStylesheet(String filename){
        InputStream in = null;
        if(filename.endsWith(".css")){
            in = clazz.getResourceAsStream("/style/" + filename);
        }
        else{
            in = clazz.getResourceAsStream("/style/" + filename + ".css");
        }
        if(in == null){
            System.out.println("The file: " + filename + " could not be found");
            return null;
        }
        try{
            String content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            return content;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Reads the contents within a file in the resources directory
     * <p>If it's not in the root of resources, must specify the path e.g. {@code Path/to/route}
     * <p>Extension must be included in filename e.g. .html, .css</p>
     * @param filename , The name of the file
     * @return {@code String} of the file's contents
     */
    public static String readResource(String filename){
        InputStream in = null;
        in = clazz.getResourceAsStream(filename);
        if(in == null){
            System.out.println("The file: " + filename + " could not be found");
            return null;
        }
        try{
            String content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            return content;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    
    /**
     * Writes to a specified path given a string and returns the success status as a boolean variable
     * @param output , The path of the file to which we are writing to
     * @param data , The data that we are writing to the Path
     * @return {@code boolean} status
     */
    public static boolean writeToFile(Path output, String data){
        try{
            Files.writeString(output, data, StandardCharsets.UTF_8);
            return true;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Loads a style sheet in into the external directory
     * @param filename , The name of the stylesheet to be loaded
     */
    public static void loadStylesheet(String filename){
        if(filename == null){
            System.out.println("Enter a valid filename, null is not valid");
            return;
        }
        try{
            Path baseDir = Paths.get(System.getProperty("user.home"), directoryName, "style");
            Files.createDirectories(baseDir);            
            Path filepath = baseDir.resolve(filename);
            String content = readFromStylesheet(filename);
            writeToFile(filepath, content);
        }catch (Exception e){
            System.out.println("Error loading stylesheets");
            return;
        }
    }

    
    /**
     * Returns the name of the external directory
     * @return {@code String} the directory name
     */
    public static String getDirectoryName() {
        return directoryName;
    }

    /**
     * Sets the name of the external directory
     * @param directoryName
     */
    public static void setDirectoryName(String directoryName) {
        ResourceManager.directoryName = directoryName;
    }

    /**
     * Returns the name of the external file where html content is rendered
     * @return {@code String} the filename
     */
    public static String getFilename() {
        return filename;
    }

    /**
     * Sets the name of the external file where html content is rendered
     */
    public static void setFilename(String filename) {
        ResourceManager.filename = filename;
    }


    /**
     * Returns the package name of of the Resource Manager, useful when working with classes of the same package
     * @return {@code String}
     */
    public static String getPackage(){
        String canonical = clazz.getCanonicalName();
        String className = "." + clazz.getSimpleName();
        return canonical.split(className)[0];
    }


    /**
     * Returns the package of a given class
     * @param clazz , The class that belongs to the package we are looking for
     * @return {@code String}
     */
    public static String getPackage(Class<?> clazz){
        String canonical = clazz.getCanonicalName();
        String className = "." + clazz.getSimpleName();
        return canonical.split(className)[0];
    }


    /**
     * Attempts to evaluate and return the class that initiated the application by invoking a method named {@code start()}.
     * <p>
     * This method inspects the current thread's stack trace to find a class with a {@code void start()} method.
     * This can be useful for context-aware operations during application startup.
     * <p>
     * The class can also be manually set. e.g. ResourceManager.clazz = SomeClass.class
     * @return the {@code Class<?>} object representing the class that called the {@code start()} method,
     *         or {@code null} if it could not be determined
     */
    public static Class<?> evaluateClazz(){
        for(StackTraceElement element : Thread.currentThread().getStackTrace()){
            if(element.getMethodName().equals("start")){
                try {
                    Class<?> clazz = Class.forName(element.getClassName());
                    if(clazz.getMethod(element.getMethodName(), Stage.class).getReturnType() == void.class && clazz.getGenericSuperclass() == Application.class)
                        return clazz;
                } catch (ClassNotFoundException | NoSuchMethodException e ) {
                    e.printStackTrace();
                    return null;
                }
            }
        }
        return null;
    }
}