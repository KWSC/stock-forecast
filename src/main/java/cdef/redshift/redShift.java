package cdef.redshift;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Properties;

public class redShift 
{
	static final String dbURL = "jdbc:redshift://priceforecast.cypkj43qbvcd.ap-northeast-2.redshift.amazonaws.com:5439/priceforecast";
	static final String MasterUserName = "cdefinition";
	static final String MasterUserPassword = "cdForecast1#";
	
    public static void delete()
    {
        Connection conn = null;
        Statement stmt = null;
        
        try {
        	Class.forName("com.amazon.redshift.jdbc42.Driver");
        	
        	//System.out.println("Connecting to database...");
        	Properties props = new Properties();
        	
        	props.setProperty("user", MasterUserName);
        	props.setProperty("password", MasterUserPassword);
        	conn = DriverManager.getConnection(dbURL, props);
        	
        	//System.out.println("Listing system tables...");
        	stmt = conn.createStatement();
            String sql;
            sql = "delete from predict_result";
            ResultSet rs = stmt.executeQuery(sql);
            //System.out.println(rs);
             
             rs.close();
             stmt.close();
             conn.close();
        } catch(Exception ex){
            //For convenience, handle all errors here.
           // ex.printStackTrace();
        } finally{
            //Finally block to close resources.
            try{
               if(stmt!=null)
                  stmt.close();
            } catch(Exception ex){
            }// nothing we can do
            try{
               if(conn!=null)
                  conn.close();
            } catch(Exception ex){
               ex.printStackTrace();
            }
         }
       // System.out.println("Finished connectivity test.");
    }
    
    public static void insert(HashMap<String, String> input)
    {
        Connection conn = null;
        Statement stmt = null;
        
        try {
        	Class.forName("com.amazon.redshift.jdbc42.Driver");
        	
        	//System.out.println("Connecting to database...");
        	Properties props = new Properties();
        	
        	props.setProperty("user", MasterUserName);
        	props.setProperty("password", MasterUserPassword);
        	conn = DriverManager.getConnection(dbURL, props);
        	
        	//System.out.println("Listing system tables...");
        	stmt = conn.createStatement();
            String sql = "insert into predict_result values(";
            sql += "\'" + input.get("NAME") + "\',\'" + input.get("TC") + "\',\'" + input.get("CO") + "\',\'" + input.get("RE") + "\')";
            ResultSet rs = stmt.executeQuery(sql);
            //System.out.println(rs);
             
             rs.close();
             stmt.close();
             conn.close();
        } catch(Exception ex){
            //For convenience, handle all errors here.
            //ex.printStackTrace();
        } finally{
            //Finally block to close resources.
            try{
               if(stmt!=null)
                  stmt.close();
            } catch(Exception ex){
            }// nothing we can do
            try{
               if(conn!=null)
                  conn.close();
            } catch(Exception ex){
               ex.printStackTrace();
            }
         }
        //System.out.println("Finished connectivity test.");
    }
}
