package com.iraqimilitant.dungeonslink;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.ResultSet;

/**
 * Worker class which handles all interaction with the database
 * 
 * @author IraqiMilitant
 *
 */
public class DatabaseWorker {
	static Connection con;


	/**
	 * gets the connection to the database
	 * 
	 * @return Connection
	 */
	public static Connection connect(){

		try {
			if(!(con instanceof Connection) || !con.isValid(5)){//attempt connection if there is none
				Class.forName("com.mysql.jdbc.Driver");//set the driver
				DungeonsLink.getPlugin(DungeonsLink.class).log("attempting connection to jdbc:mysql://"+Constants.host+":"+Constants.port+"/"+Constants.dbName+" With Username: "+Constants.user+" and Pass: "+Constants.pass);
				if (Constants.pass.equals("nopass")){//check if there is a password
					con=DriverManager.getConnection("jdbc:mysql://"+Constants.host+":"+Constants.port+"/"+Constants.dbName);

				}else{
					con=DriverManager.getConnection("jdbc:mysql://"+Constants.host+":"+Constants.port+"/"+Constants.dbName,Constants.user,Constants.pass);	
				}
				DungeonsLink.getPlugin(DungeonsLink.class).log("Connection to SQL database established");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return con;
	}

	/**
	 * Closes the connection to the database
	 */
	public static void disconnect(){

		try {
			if (con!=null && !con.isClosed()){//if there is a connection
				con.close();//close it
				DungeonsLink.getPlugin(DungeonsLink.class).log("SQL Database connection closed");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static HashMap<String,Integer> getPlayerRatings(){
		HashMap<String,Integer> ratings=new HashMap<String,Integer>();
		Statement stmt = null;
		ResultSet rs;
		String queryL=
				"SELECT *"
						+ " FROM " + Constants.serverName 
						+ "_USERS;";
		if(connect() instanceof Connection){
			try {
				stmt=con.createStatement();
				rs=stmt.executeQuery(queryL);//execute the Local query
				OfflinePlayer op;
				while (rs.next()){
					op=Bukkit.getOfflinePlayer(UUID.fromString(rs.getString("UUID")));
					ratings.put(op.getName(), rs.getInt("RATING"));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return ratings;
	}


	/**
	 * Read the player info from the database, and generate the corresponding Raider object
	 * 
	 * @param p Player
	 * @return Raider
	 */
	public static Raider readPlayerInfo(Player p){
		Raider r=new Raider(p);
		String UUID=p.getUniqueId().toString();//player UUID is primary key in DB
		String query;
		Statement stmt = null;
		ResultSet rs;
		Date lastSeen;
		int timeNow = Calendar.getInstance().DAY_OF_YEAR;
		int yearNow=Calendar.getInstance().YEAR;

		if(connect() instanceof Connection){//ensure there is a connection
			//global query
			query=
					"SELECT *"
							+ " FROM USERS"
							+ " WHERE UUID = '"
							+ UUID +"';";

			try {
				stmt=con.createStatement();

				rs=stmt.executeQuery(query);//excute global query
				if (rs.next()){
					lastSeen=rs.getDate(4);
					Calendar cal=Calendar.getInstance();
					cal.setTime(lastSeen);
					//int diff=timeNow-cal.DAY_OF_YEAR;
					if(timeNow>cal.DAY_OF_YEAR ||yearNow>cal.YEAR ){
						int mod;
						if(yearNow>cal.YEAR){
							mod=365-cal.DAY_OF_YEAR+timeNow;
						}else{
							mod=timeNow-cal.DAY_OF_YEAR;
						}
						r.setRating((int)(rs.getInt(2)-(mod)));//set rating
						//writePlayerInfo(r);
					}else{
						r.setRating(rs.getInt(2));//set rating
					}

				}else{
					r.setRating(0);
					registerPlayer(UUID);
					DungeonsLink.getPlugin(DungeonsLink.class).log("Player new to network");
				}
				stmt.close();

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return null;
	}

	/**
	 * Writes the given player info to the database
	 * 
	 * @param r Raider
	 * @return if write successful
	 */
	public static boolean writePlayerInfo(Raider r){
		Statement s1,s2;
		//set the global update query
		String global="UPDATE "
				+ "USERS"
				+ " SET RATING = " + r.getRating()
				+ " WHERE UUID = '" + r.getPlayer().getUniqueId().toString()
				+ "';";
		String date="UPDATE "
				+ "USERS"
				+ " SET LASTSEEN = " + new java.sql.Date(Calendar.getInstance().getTimeInMillis())
				+ " WHERE UUID = '" + r.getPlayer().getUniqueId().toString()
				+ "';";


		if(connect() instanceof Connection){//ensure there is a connection
			try {
				//execute the queries
				s1=con.createStatement();
				s1.executeUpdate(global);
				s1.close();
				s2=con.createStatement();
				s2.executeUpdate(date);
				s2.close();
				return true;

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}



		return false;
	}

	/**
	 * Creates new global and/or local entries for a new player
	 * 
	 * @param UUID
	 * @param global
	 */
	private static void registerPlayer(String UUID){
		String query;
		Statement stmt;

			query=
					"INSERT INTO "
					+ Constants.dbName + "."
					+ " USERS"
					+ " VALUES ('"
					+ UUID + "',0,0,'"+new java.sql.Date(Calendar.getInstance().getTimeInMillis())+"');";

		if (connect() instanceof Connection){//ensure connection

			try {
				DungeonsLink.getPlugin(DungeonsLink.class).log("Registering player");
				stmt=con.createStatement();
				stmt.executeUpdate(query);//execute query
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}


	}

	/**
	 *Ensures the required tables exist
	 *
	 */
	public static void createTables(){


		//global table query
		String userGlobalTable=
				"CREATE TABLE IF NOT EXISTS " + //Constants.dbName + "."
						"USERS " +
						"(UUID varchar(36) NOT NULL, " +
						"RATING int NOT NULL, " +
						"SIN int NOT NULL, " +
						"LASTSEEN DATE NOT NULL, " +
						"PRIMARY KEY (UUID))";


		Statement stmt=null;

		if (connect() instanceof Connection){//ensure connection

			try {//execute queries
				stmt = con.createStatement();
				stmt.executeUpdate(userGlobalTable);
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if (stmt != null) { try {
					stmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} }
			}
		}


	}

	/**
	 * wipes all data from local and global tables
	 */
	public static void wipeTables(){
		//set queries
		String userLocalTable=
				"TRUNCATE TABLE "
						+ Constants.serverName +
						"_USERS;";
		String userGlobalTable=
				"TRUNCATE TABLE " +
						"USERS;";


		Statement stmt=null;

		if (connect() instanceof Connection){//ensure connection

			try {//execute queries
				stmt = con.createStatement();
				stmt.executeUpdate(userLocalTable);
				stmt.close();
				stmt = con.createStatement();
				stmt.executeUpdate(userGlobalTable);
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if (stmt != null) { try {
					stmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} }
			}
		}

	}

}
