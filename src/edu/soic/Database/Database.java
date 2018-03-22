package edu.soic.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
	public static String DB_type;
	public static final String H2 = "H2";
	public static final String hsqldb = "hsqldb";
	Connection conn;
	Statement stat;

	/** Sets up the embedded H2database */
	public void setup() {
		if(DB_type.equals(H2)){
			try {
				Class.forName("org.h2.Driver");
				conn = DriverManager.getConnection("jdbc:h2:mem:test;MULTI_THREADED=1;MAX_OPERATION_MEMORY=0");
//				conn = DriverManager.getConnection("jdbc:h2:mem:test;MAX_OPERATION_MEMORY=0");
				stat = conn.createStatement();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
		}
		else if(DB_type.equals(hsqldb)){
			try {
				Class.forName("org.hsqldb.jdbcDriver");
				conn = DriverManager.getConnection("jdbc:hsqldb:mem:mymemdb", "SA", "");
				stat = conn.createStatement();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/** Executes statement without return value - used for populating the db */
	public void execute(String s){
		try {
			stat.execute(s);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public ResultSet executSelectQuery(String s){
		try {
			return stat.executeQuery(s);
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/** Executes count query statement with int return value */
	public int executeCountQuery(String s){
		try {
			ResultSet rs = stat.executeQuery(s);
			rs.next();
			return rs.getInt(1);
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/** Terminates the embedded database */
	public void close(){
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}