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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.PatternSyntaxException;


/**
 * A simple SQLite-based database utility class designed to work with POJOs for schema generation and data insertion.
 * It supports basic operations such as connecting to the database, creating tables, inserting records, and managing tables.
 */
public class Database {

    
    private Database(){};

    //The connection object that is used for interacting with the database
    private static Connection connection = null;


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
        if(connection == null){
            System.out.println("Connection is null, use the connect() function or set the connection's value through Database.connection = ...");
            return;
        }
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
     * @param tableName , The name of the table
     * @param clazz , The class that we are mapping the table to
     * @return {@code true} if creation is successful, {@code false} otherwise
     */
    public static boolean createTable(String tableName, Class<?> clazz){
        if(connection == null){
            System.out.println("Connection is null, use the connect() function or set the connection's value through Database.connection = ...");
            return false;
        }
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
            String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (id INTEGER PRIMARY KEY AUTOINCREMENT, " + columns + ")";
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
        if(connection == null){
            System.out.println("Connection is null, use the connect() function or set the connection's value through Database.connection = ...");
            return false;
        }
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
        if(connection == null){
            System.out.println("Connection is null, use the connect() function or set the connection's value through Database.connection = ...");
            return false;
        }
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
        if(connection == null){
            System.out.println("Connection is null, use the connect() function or set the connection's value through Database.connection = ...");
            return false;
        }
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
     * Accepts an id and returns a {@link Data} representing that record in the database
     * @param table The name of the table
     * @param id The id of the desired record
     * @return A {@code Data} object containing the content of the retrieved record
     */
    public static Data get(String table, String id) {
        if(connection == null){
            System.out.println("Connection is null, use the connect() function or set the connection's value through Database.connection = ...");
            return null;
        }
        if (id == null || id.trim().isEmpty()) {
            System.out.println("The id: " + id + " is invalid");
            return null;
        }
    
        String sql = "SELECT * FROM " + table + " WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Data record = new Data();
                    ResultSetMetaData metaData = resultSet.getMetaData();
                    for (int i = 1; i <= metaData.getColumnCount(); i++)
                        record.put(metaData.getColumnName(i), resultSet.getObject(i));
                    return record;
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error while retrieving from " + table + ": " + e.getMessage());
        }
    
        return null;
    }


    /**
     * Retrieves all records from the specified table that match the provided WHERE clause and values.
     * <p>This method supports dynamic SQL filtering with parameterized inputs to ensure security and flexibility.</p>
     * <p><b>Example Usage:</b></p>
     * <pre>{@code
     * // 1. Retrieve all users with the username "bob"
     * List<Data> results1 = getAll("User", "username = ?", "bob");
     *
     * // 2. Retrieve all orders from a user with ID "42" and status "delivered"
     * List<Data> results2 = getAll("Orders", "user_id = ? AND status = ?", "42", "delivered");
     *
     * // 3. Retrieve all employees with a salary greater than 50000
     * List<Data> results3 = getAll("Employees", "salary > ?", "50000");
     * }</pre>    
     * @param tableName the name of the table to query from
     * @param whereClause the WHERE clause of the SQL statement (e.g. "username = ? AND age > ?")
     * @param values the values to substitute into the WHERE clause placeholders
     * @return {@code List<Data>} object of each record in the returned from the query
     */
    public static List<Data> getAll(String tableName, String whereClause, String... values){
        if(connection == null){
            System.out.println("Connection is null, use the connect() function or set the connection's value through Database.connection = ...");
            return null;
        }
        if(whereClause == null || whereClause.trim().equals("")){
            System.err.println("An invalid 'Where Clause' was passed to getAll()");
            return null;
        }
        for(String value : values){
            if(value == null || value.trim().equals("")){
                System.err.println("An invalid value was passed to getAll()");
                return null;
            }
        }

        if(whereClause.chars().filter(ch -> ch == '?').count() != values.length){
            System.err.println("Error in paramters. The number of values does not match the number of '?'");
            return null;
        }
        List<Data> results = new ArrayList<>();
        String sql = "SELECT * FROM " + tableName + " WHERE " + whereClause;
        try(PreparedStatement statement = connection.prepareStatement(sql)){

            for(int i=0; i < values.length; i++)
                statement.setString(i+1, values[i]);
            
            ResultSet resultSet = statement.executeQuery();
            ResultSetMetaData metaData = resultSet.getMetaData();
            while(resultSet.next()){
                Data record = new Data();
                for(int i=1; i <= metaData.getColumnCount(); i++)
                    record.put(metaData.getColumnName(i), resultSet.getObject(i));
                results.add(record);
            }

            return results;
        }catch(SQLException | PatternSyntaxException e){
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Accepts the name of a table and returns all records in that table as {@code List<Data>}
     * @param tableName the name of the table to query from
     * @return {@code List<Data>} of all records
     */
    public List<Data> getAll(String tableName){
        if(connection == null){
            System.out.println("Connection is null, use the connect() function or set the connection's value through Database.connection = ...");
            return null;
        }
        if(tableName == null || tableName.trim().equals("")){
            System.err.println("Invalid Table name");
            return null;
        }
        List<Data> results = new ArrayList<>();
        try{
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery( "SELECT * FROM " + tableName);
            while(resultSet.next()){
                ResultSetMetaData metaData = resultSet.getMetaData();
                Data record = new Data();
                for(int i=1; i <= metaData.getColumnCount(); i++)
                    record.put(metaData.getColumnName(i), resultSet.getObject(i));
                results.add(record);
            }
            return results;
        }catch (SQLException e){
            e.printStackTrace();
            return null;
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
