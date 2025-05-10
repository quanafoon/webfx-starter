package webfx.devs;

import java.io.File;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;


/**
 * A simple SQLite-based database utility class designed to work with POJOs for schema generation and data insertion.
 * It supports basic operations such as connecting to the database, creating tables, inserting records, and managing tables.
 */
public class Database {

    
    private Database(){};

    //The connection object that is used for interacting with the database
    private static Connection connection;


     /**
     * Establishes a connection to an SQLite database located in the user's home directory.
     * It accepts a directory and filename and creates it if it does not exist.
     * <p>Directory can be the same the default ResourceManager directory, "webfx-external" </p>
     * <p>
     * Use of this class without calling this method requires updating the {@code Connection} Database.connection attribute
     * @param directory , the name of the folder that contains the database file
     * @param filename , the name of the database file
     * @return {@code true} if connection is successful, {@code false} otherwise
     */
    public static boolean connect(String directory, String filename) {
        if(!filename.endsWith(".db"))
            filename += ".db";
        try {
            String dbPath = System.getProperty("user.home") + File.separator + directory + File.separator + filename;
            File dbDirectory = new File(System.getProperty("user.home"));
            if (!dbDirectory.exists()) {
                dbDirectory.mkdirs();
            }
    
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            connection.setAutoCommit(false);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * Prints all records in the specified table to the console.
     *
     * @param tableName , the name of the table to print
     */
    public static void printTable(String tableName) {
        String query = "SELECT * FROM " + tableName;
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                System.out.print(meta.getColumnName(i) + "\t");
            }
            System.out.println();
            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    System.out.print(rs.getString(i) + "\t");
                }
                System.out.println();
            }
        } catch (SQLException e) {
            System.out.println("Error querying table: " + e.getMessage());
        }
    }


    /**
     * Creates a table based on fields in a given class, with the given table name
     * <p> Fields declared as {@code id} is not permitted as there is an already existing Auto-Incrememented Primary Key Field, id.
     * <p> It is not recommended to annotate primitive fields with {@code @Nullable}. Only reference types (e.g., Integer, Double, String) should be marked as nullable, since primitives cannot represent null values. </p>  
     * @param table , The name of the table
     * @param clazz , The class that we are mapping the table to
     * @return {@code true} if creation is successful, {@code false} otherwise
     */
    public static boolean createTable(String table, Class<?> clazz){
        try{
            Field[] fields = clazz.getDeclaredFields();
            StringBuilder columns = new StringBuilder();
            for(Field field : fields){
                if(!field.isAnnotationPresent(Ignore.class)){
                    if(field.isAnnotationPresent(Nullable.class))
                        columns.append(field.getName() + " " + mapJavaTypeToSQL(field.getType()) + ",");
                    else
                        columns.append(field.getName() + " " + mapJavaTypeToSQL(field.getType()) + " NOT NULL,");
                }
            }
            if (columns.length() > 0)
                columns.deleteCharAt(columns.length()-1);
            String sql = "CREATE TABLE IF NOT EXISTS " + table + " (id INTEGER PRIMARY KEY AUTOINCREMENT, " + columns + ")";
            Statement statement = connection.createStatement();
            statement.execute(sql);
            connection.commit();
            return true;
        }catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }


    /**
     * Drops the table with the specified name from the database.
     *
     * @param table , the name of the table to drop
     * @return {@code true} if dropped successfully, {@code false} otherwise
     */
    public static boolean dropTable(String table){
        try{
            String sql = "DROP TABLE IF EXISTS " + table;
            Statement statement = connection.createStatement();
            statement.execute(sql);
            connection.commit();
            return true;
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }


    /**
     * Deletes all rows from the specified table.
     *
     * @param table , the name of the table to clear
     * @return {@code true} if deletion is successful, {@code false} otherwise
     */
    public static boolean deleteAll(String table){
        try{
            String sql = "DELETE FROM " + table;
            Statement statement = connection.createStatement();
            statement.execute(sql);
            connection.commit();
            return true;
        }catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    
    /**
     * Inserts a new record into the specified table using the fields and values of the given object.
     * Only the non-null fields with {@code @Nullable} annotation will be included in the insert query.
     * <p>It is not recommended to annotate primitive fields with {@code @Nullable}. Only reference types (e.g., Integer, Double, String) should be marked as nullable, since primitives cannot represent null values.</p>  
     * @param table , the name of the table
     * @param record , the object containing the data to insert
     * @return {@code true} if insertion is successful, {@code false} otherwise
     */
    public static boolean insert(String table, Object record){
        try{
            if (record == null) {
                System.out.println("Insertion error: record is null.");
                return false;
            }
            Class<?> clazz = record.getClass();
            Field[] fields = clazz.getDeclaredFields();
            StringBuilder names = new StringBuilder();
            LinkedList<Object> values = new LinkedList<>();
            StringBuilder placeholders = new StringBuilder();
            for (Field field : fields){
                if(!field.isAnnotationPresent(Ignore.class)){
                    field.setAccessible(true);
                    Object value = field.get(record);
                    if(value == null && !field.isAnnotationPresent(Nullable.class)){
                        System.out.println("Passing a null value for a non-nullable field: " + field.getName());
                        return false;
                    }
                    if(value != null){
                        names.append(field.getName() + ",");
                        values.add(value);
                        placeholders.append("?,");
                    }  
                }
            }
            if (placeholders.length() > 0)
                placeholders.deleteCharAt(placeholders.length()-1);
            if (names.length() > 0)
                names.deleteCharAt(names.length()-1);
            
            String sql = "INSERT INTO " + table + " (" + names +  ") VALUES (" + placeholders + ")";
            PreparedStatement statement = connection.prepareStatement(sql);
            for(int i=0; i < values.size(); i++){
                statement.setObject(i+1, values.get(i));
            }
            statement.executeUpdate();
            connection.commit();
            return true;
        }catch (SQLException | IllegalAccessException e){
            e.printStackTrace();
            return false;
        }
    }


    /**
     * Maps Java field types to their corresponding SQLite data types.
     *
     * @param type , the Java class type
     * @return {@code String} the equivalent SQLite type as a String
     */
    private static String mapJavaTypeToSQL(Class<?> type) {
        if (type == String.class)
            return "TEXT";
        else if (type == int.class || type == Integer.class)
            return "INTEGER";
        else if (type == long.class || type == Long.class)
            return "BIGINT";
        else if (type == float.class || type == Float.class)
            return "REAL";
        else if (type == double.class || type == Double.class)
            return "DOUBLE";
        else if (type == boolean.class || type == Boolean.class)
            return "BOOLEAN";
        return "TEXT";
    }
}
