package com.jdbcpg.ireport.demo1;
/***
 * NOTE:
 *      To allow clients to connect to database:
 * 
 *      1. pg_hba.conf file: C:\Program Files\PostgreSQL\9.6\data\pg_hba.conf
 * 
 *      2. Set configuration
 *          local  all  postgres trust 
 *          host  all   postgres 0.0.0.0/0  trust 
 *          host  all   postgres ::/0 trust
 * 
 *      3. Once you have made these changes, you will need to restart the Postgres server:
 *          Start Menu > Services > Postgres > Restart
 */


public class PG {
    //Static Variables
    static java.sql.Connection conn  = null;
    static java.sql.Statement stmt = null;
    static java.sql.ResultSet rs = null;
    static java.sql.PreparedStatement pstmt = null;
    static String url = "";
    static String username = "";
    static String password = "";
    static String className = "";
    static String error = "";
    
    //Open DB Session Method
    public static boolean openDB(){
        boolean result = false;
        try{
            Class.forName(className);
            conn = java.sql.DriverManager.getConnection(url,username,password);
            
            //System.out.println("Open DB Success: DB session connected.");
            result = true;
        }
        catch(Exception e){
            error = e.getMessage();
            System.out.println("Open DB Failed: " + e.getMessage());
        } 
        return result;
    }

    //Close DB Session Method
    public static boolean closeDB(){
        boolean result = false;
        try{
            conn.close();

            System.out.println("Session has been executed successfully!");
            result = true;
        }
        catch(Exception e){
            error = e.getMessage();
            System.out.println("Close DB Failed: " + e.getMessage());
        }
        return result;
    }
    
    //Method for INSERT, UPDATE and DELETE statements
    public static boolean executeDML(String query){
        boolean result = false;
        try{
            stmt = conn.createStatement();
            stmt.executeUpdate(query);
            result = true;
        }
        catch(Exception e){
            error = e.getMessage();
            System.out.println("Execute DML Error: " + e.getMessage());
            System.out.println("Query: " + query);
        }
        return result;
    }
    
    //Overloaded Method for INSERT, UPDATE and DELETE statments
    public static boolean executeDML(String query, Object[] values){
        boolean result = false;
        if(openDB()){
            try{
                pstmt = null;
                pstmt = conn.prepareStatement(query);
                for(int i=1,j=0;i<=values.length;i++,j++){
                    pstmt.setObject(i, values[j]);
                }
                pstmt.executeUpdate();
                result = true;
            }
            catch(Exception e){
                error = e.getMessage();
                System.out.println("Execute DML Error: " + e.getMessage());
                System.out.println("Query: " + query);
            }
            closeDB();
        }
        return result;
    }    

    // Method for SELECT statements
    public static String[][] executeDQL(String table, String[] columns, String where){
        String[][] records = null;
        if(openDB()){
            try{
                String[] w = where.split(";");
                
                stmt = conn.createStatement();
                
                //Count total rows
                rs = stmt.executeQuery("SELECT count(*) FROM " + table + " WHERE " + w[0]);
                int totalRows = 0;
                if(rs.next())totalRows = rs.getInt(1);
                
                //Format columns
                String cols = "";
                for (int i=0;i<columns.length;i++) {
                    cols += columns[i];
                    if((i+1)!=columns.length)cols+=", ";
                }
                
                //Execute DML Query
                rs = stmt.executeQuery("SELECT "+ cols +" FROM " + table + " WHERE " + w[0]);
                java.sql.ResultSetMetaData rsmd = rs.getMetaData();
                
                //Count total columns
                int totalColumns = rsmd.getColumnCount();
                
                //Initialize 2D Array "records" with totalRows by totalColumns
                records = new String[totalRows][totalColumns];
                
                //Retrieve the record and store it to 2D Array "records"
                int row=0;
                while(rs.next()){                
                    for(int col=0,index=1;col<totalColumns;col++,index++){
                        records[row][col] = rs.getObject(index).toString();
                    }
                    row++;
                }
            }
            catch(Exception e){
                error = e.getMessage();
                System.out.println("Execute DQL Error: " + e.getMessage());
            }
            closeDB();
        }
        return records;
    } 
    
    //Demo Method
    public static void main(String [] args){
        /*
        //Sample code snippet for INSERT DML Statement
        Object[] tasks = {"Task D", "NO"};
        String query = "INSERT INTO task (name, isdone) VALUES (?,?)";
        if(executeDML(query, tasks)) System.out.println("Query Ok!");
        else System.out.println("Query Not Ok!");
        */
        
        /*
        //Sample code snippet for UPDATE DML Statement
        Object[] values = {"Task B", "YES", 13};
        String query = "UPDATE task SET name=? ,isdone=? WHERE id=?";
        if(executeDML(query, values)) System.out.println("Query Ok!");
        else System.out.println("Query Not Ok!");        
        */
        
        /*
        //Sample code snippet for DELETE DML Statement
        Object[] values = {13};
        String query = "DELETE FROM task WHERE id=?";
        if(executeDML(query, values)) System.out.println("Query Ok!");
        else System.out.println("Query Not Ok!");        
        */
        
        /*
        //Sample code snippet for SELECT DQL Statement
        String[] columns = {"name","isdone"};
        String whereClause = "1=1";
        String[][] records = executeDQL("task", columns, whereClause);
        if(records==null || records.length==0)System.out.println("No record found!");
        else System.out.println(records.length + " record(s) found!");
        */
    }

    
    
    
    /* 
    #####################################################################
        List of JDBC Methods for Spcific DML, DQL and DDL statements 
    #####################################################################
    */
    
    //Create Record Method
    public static boolean create(String table, String values){
        boolean result = false;
        String query = null;
        try{
            stmt = conn.createStatement();
            query = "INSERT INTO "+ table +" VALUES(" + values + ")";
            stmt.executeUpdate(query);            
            //You can include exception handling here. (e.g. Duplicate Data, etc.)
            
            result = true;
        }
        catch(Exception e){
            System.out.println("Create Error: " + e.getMessage());
            System.out.println("Query: " + query);
        }
        return result;
    }    
    
    //Create Record Method
    public static boolean create(String table, String columns, String stmts, String[] values){
        boolean result = false;
        String query = null;
        try{
            //query = "INSERT INTO " + table +" ("+ columns +") VALUES("+ stmts +")"; 
            query = "INSERT INTO "+ table +" ("+columns+") VALUES("+stmts+")";
            java.sql.ResultSet rs = null;
            java.sql.PreparedStatement pstmt = null;            
            
            pstmt = conn.prepareStatement(query);
            for(int i=1,j=0;i<=values.length;i++,j++){
                pstmt.setString(i, values[j]);
            }
            //pstmt.setString(1, values[0]);
            //pstmt.setString(2, "Aug 4, 2017");
            pstmt.executeUpdate();
            
            result = true;
        }
        catch(Exception e){
            System.out.println("Create Error: " + e.getMessage());
            System.out.println("Query: " + query);
        }
        return result;
    }
 
    //Update Record Method
    public static boolean update(String table, String set, int id){
        boolean result = false;
        try{
            stmt = conn.createStatement();
            String query = "UPDATE "+ table +" SET " + set + " WHERE id=" + id;
            stmt.executeUpdate(query);
            //You can include exception handling here. (e.g. Duplicate Data, etc.)
            result = true;
        }
        catch(Exception e){
            System.out.println("Create Error: " + e.getMessage());
        }
        return result;
    }    

    //Delete Record Method
    public static boolean delete(String table, int id){
        boolean result = false;
        try{
            stmt = conn.createStatement();
            String query = "DELETE FROM "+ table + " WHERE id=" + id;
            stmt.executeUpdate(query);
            result = true;
        }
        catch(Exception e){
            System.out.println("Create Error: " + e.getMessage());
        }
        return result;
    }     

    //Read Record Method
    public static String[][] read(String table){
        String[][] records = null;
        try{
            stmt = conn.createStatement();

            //Count total rows
            java.sql.ResultSet rs = stmt.executeQuery("SELECT count(*) FROM " + table);
            int totalRows = rs.getInt(1);

            //Count total columns
            rs = stmt.executeQuery("SELECT * FROM " + table);
            java.sql.ResultSetMetaData rsmd = rs.getMetaData();
            int totalColumns = rsmd.getColumnCount();

            //Initialize 2D Array "records" with totalRows by totalColumns
            records = new String[totalRows][totalColumns];

            //Retrieve the record and store it to 2D Array "records"
            int row=0;
            while(rs.next()){                
                for(int col=0,index=1;col<totalColumns;col++,index++){
                    records[row][col] = rs.getObject(index).toString();
                }
                row++;
            }            
        }
        catch(Exception e){
            System.out.println("Read Error: " + e.getMessage());
        }
        return records;
    }
    
    //Read Record Method
    public static String[][] read(String table, String[] columns){
        String[][] records = null;
        try{
            stmt = conn.createStatement();

            //Count total rows
            java.sql.ResultSet rs = stmt.executeQuery("SELECT count(*) FROM " + table);
            int totalRows = rs.getInt(1);

            //Count total columns
            int totalColumns = columns.length;
            String cols = "";
            for(int i=0;i<totalColumns;i++){
                cols += columns[i];
                if((i+1)!=totalColumns)cols+=", ";
            }
            rs = stmt.executeQuery("SELECT "+ cols +" FROM " + table);

            //Initialize 2D Array "records" with totalRows by totalColumns
            records = new String[totalRows][totalColumns];

            //Retrieve the record and store it to 2D Array "records"
            int row=0;
            while(rs.next()){                
                for(int col=0,index=1;col<totalColumns;col++,index++){
                    records[row][col] = rs.getObject(index).toString();
                }
                row++;
            }            
        }
        catch(Exception e){
            System.out.println("Read Error: " + e.getMessage());
        }
        return records;
    }  
    
    //Read Record Method with WHERE clause
    public static String[][] read(String table, String where){
        String[][] records = null;
        try{
            stmt = conn.createStatement();

            //Count total rows
            java.sql.ResultSet rs = stmt.executeQuery("SELECT count(*) FROM " + table + " WHERE " + where);
            int totalRows = rs.getInt(1);

            //Count total columns
            rs = stmt.executeQuery("SELECT * FROM " + table + " WHERE " + where);
            java.sql.ResultSetMetaData rsmd = rs.getMetaData();
            int totalColumns = rsmd.getColumnCount();

            //Initialize 2D Array "records" with totalRows by totalColumns
            records = new String[totalRows][totalColumns];

            //Retrieve the record and store it to 2D Array "records"
            int row=0;
            while(rs.next()){                
                for(int col=0,index=1;col<totalColumns;col++,index++){
                    records[row][col] = rs.getObject(index).toString();
                }
                row++;
            }            
        }
        catch(Exception e){
            System.out.println("Read Error: " + e.getMessage());
        }
        return records;
    }       
    
    //Read Image Method
    public static byte[] read(String table, String column, int id){
        byte[] buffer = null;
        try{
            String query = "SELECT " + column +" FROM " + table + " WHERE id=?"; 
            java.sql.ResultSet rs = null;
            java.sql.PreparedStatement pstmt = null;            
             
            pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, id);
            
            rs = pstmt.executeQuery();

            while (rs.next()) {
                buffer = rs.getBytes("image");
            }
        }
        catch(Exception e){
            System.out.println("Read Error: " + e.getMessage());
        }
        return buffer;
    }
    
    //Modify record
    public static boolean update(String table, byte[] image, String column, int id){
        //byte[] buffer = null;
        boolean result = false;
        try{
            String query = "UPDATE " + table +" SET "+ column +"=? WHERE id=" + id; 
            java.sql.ResultSet rs = null;
            java.sql.PreparedStatement pstmt = null;            
            
            pstmt = conn.prepareStatement(query);
            pstmt.setBytes(1, image);
            
            pstmt.executeUpdate();
            System.out.println("Image saved to database successfully!");
            result = true;
            /*
            while (rs.next()) {
                buffer = rs.getBytes("image");
            }
            */
        }
        catch(Exception e){
            System.out.println("Read Error: " + e.getMessage());
        }
        return result;
    }    
    
    //Read File
    private static byte[] readFile(String file) {
        java.io.ByteArrayOutputStream bos = null;
        try {
            java.io.File f = new java.io.File(file);
            java.io.FileInputStream fis = new java.io.FileInputStream(f);
            byte[] buffer = new byte[1024];
            bos = new java.io.ByteArrayOutputStream();
            for (int len; (len = fis.read(buffer)) != -1;) {
                bos.write(buffer, 0, len);
            }
        } catch (java.io.FileNotFoundException e) {
            System.err.println(e.getMessage());
        } catch (java.io.IOException e2) {
            System.err.println(e2.getMessage());
        }
        return bos != null ? bos.toByteArray() : null;
    }

    //Create image as blob
    public static void insertBlob(String table, String filename, String desc) {
        // insert sql
        String query = "INSERT INTO " + table
                + " VALUES('', ? , ? "
                + ")";
 
        try (
            java.sql.PreparedStatement pstmt = conn.prepareStatement(query)) {
 
            // set parameters
            pstmt.setBytes(1, readFile(filename));
            pstmt.setString(2, desc);
 
            pstmt.executeUpdate();
            System.out.println("Stored the file in the BLOB column.");
 
        } catch (java.sql.SQLException e) {
            System.out.println(e.getMessage());
        }
    }     

}

