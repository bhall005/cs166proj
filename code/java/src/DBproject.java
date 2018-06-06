/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class DBproject{
	//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	public DBproject(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("Connecting to database...");
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");
			
			// obtain a physical connection
	        this._connection = DriverManager.getConnection(url, user, passwd);
	        System.out.println("Done");
		}catch(Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
	        System.out.println("Make sure you started postgres on this machine");
	        System.exit(-1);
		}
	}
	
	/**
	 * Method to execute an update SQL statement.  Update SQL instructions
	 * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	 * 
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 * */
	public void executeUpdate (String sql) throws SQLException { 
		// creates a statement object
		Statement stmt = this._connection.createStatement ();

		// issues the update instruction
		stmt.executeUpdate (sql);

		// close the instruction
	    stmt.close ();
	}//end executeUpdate

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and outputs the results to
	 * standard out.
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQueryAndPrintResult (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 *  obtains the metadata object for the returned result set.  The metadata
		 *  contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;
		
		//iterates through the result set and output them to standard out.
		boolean outputHeader = true;
		while (rs.next()){
			if(outputHeader){
				for(int i = 1; i <= numCol; i++){
					System.out.print(rsmd.getColumnName(i) + "\t");
			    }
			    System.out.println();
			    outputHeader = false;
			}
			for (int i=1; i<=numCol; ++i)
				System.out.print (rs.getString (i) + "\t");
			System.out.println ();
			++rowCount;
		}//end while
		stmt.close ();
		
		return rowCount;
	}
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the results as
	 * a list of records. Each record in turn is a list of attribute values
	 * 
	 * @param query the input query string
	 * @return the query result as a list of records
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
		//creates a statement object 
		Statement stmt = this._connection.createStatement (); 
		
		//issues the query instruction 
		ResultSet rs = stmt.executeQuery (query); 
	 
		/*
		 * obtains the metadata object for the returned result set.  The metadata 
		 * contains row and column info. 
		*/ 
		ResultSetMetaData rsmd = rs.getMetaData (); 
		int numCol = rsmd.getColumnCount (); 
		int rowCount = 0; 
	 
		//iterates through the result set and saves the data returned by the query. 
		boolean outputHeader = false;
		List<List<String>> result  = new ArrayList<List<String>>(); 
		while (rs.next()){
			List<String> record = new ArrayList<String>(); 
			for (int i=1; i<=numCol; ++i) 
				record.add(rs.getString (i)); 
			result.add(record); 
		}//end while 
		stmt.close (); 
		return result; 
	}//end executeQueryAndReturnResult
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the number of results
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQuery (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		int rowCount = 0;

		//iterates through the result set and count nuber of results.
		if(rs.next()){
			rowCount++;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to fetch the last value from sequence. This
	 * method issues the query to the DBMS and returns the current 
	 * value of sequence used for autogenerated keys
	 * 
	 * @param sequence name of the DB sequence
	 * @return current value of a sequence
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	
	public int getCurrSeqVal(String sequence) throws SQLException {
		Statement stmt = this._connection.createStatement ();
		
		ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
		if (rs.next()) return rs.getInt(1);
		return -1;
	}

	/**
	 * Method to close the physical connection if it is open.
	 */
	public void cleanup(){
		try{
			if (this._connection != null){
				this._connection.close ();
			}//end if
		}catch (SQLException e){
	         // ignored.
		}//end try
	}//end cleanup

	/**
	 * The main execution method
	 * 
	 * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
	 */
	public static void main (String[] args) {
		if (args.length != 3) {
			System.err.println (
				"Usage: " + "java [-classpath <classpath>] " + DBproject.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if
		
		DBproject esql = null;
		
		try{
			System.out.println("(1)");
			
			try {
				Class.forName("org.postgresql.Driver");
			}catch(Exception e){

				System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
				e.printStackTrace();
				return;
			}
			
			System.out.println("(2)");
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];
			
			esql = new DBproject (dbname, dbport, user, "");
			
			boolean keepon = true;
			while(keepon){
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. Add Plane");
				System.out.println("2. Add Pilot");
				System.out.println("3. Add Flight");
				System.out.println("4. Add Technician");
				System.out.println("5. Book Flight");
				System.out.println("6. List number of available seats for a given flight.");
				System.out.println("7. List total number of repairs per plane in descending order");
				System.out.println("8. List total number of repairs per year in ascending order");
				System.out.println("9. Find total number of passengers with a given status");
				System.out.println("10. < EXIT");
				
				switch (readChoice()){
					case 1: AddPlane(esql); break;
					case 2: AddPilot(esql); break;
					case 3: AddFlight(esql); break;
					case 4: AddTechnician(esql); break;
					case 5: BookFlight(esql); break;
					case 6: ListNumberOfAvailableSeats(esql); break;
					case 7: ListsTotalNumberOfRepairsPerPlane(esql); break;
					case 8: ListTotalNumberOfRepairsPerYear(esql); break;
					case 9: FindPassengersCountWithStatus(esql); break;
					case 10: keepon = false; break;
				}
			}
		}catch(Exception e){
			System.err.println (e.getMessage ());
		}finally{
			try{
				if(esql != null) {
					System.out.print("Disconnecting from database...");
					esql.cleanup ();
					System.out.println("Done\n\nBye !");
				}//end if				
			}catch(Exception e){
				// ignored.
			}
		}
	}

	public static int readChoice() {
		int input;
		// returns only if a correct value is given.
		do {
			System.out.print("Please make your choice: ");
			try { // read the integer, parse it and break.
				input = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}//end try
		}while (true);
		return input;
	}//end readChoice
	
	/**
	 * Method to prompt the user to input a date (year, month, day).
	 * 
	 * @return date in the form YYYY-MM-DD
	 * @throws Exception when the input is not an integer
	 */
	private static String getDate() {
	   int year;
	   int month;
	   int day;
	   
	   do {
			System.out.print("Year: ");
			try { // read the integer, parse it and break.
			   year = Integer.parseInt(in.readLine());
				if(year < 1000 || year > 9999){
   			   System.out.println("Please re-enter. " + String.valueOf(year) + " is not a year.");
   			}
   			else{
   			   break;
   			}
			}catch (Exception e) {
				System.out.println("Please re-enter. The year must be an integer.");
				continue;
			}//end try
		}while (true);
		
		do {
			System.out.print("Month: ");
			try { // read the integer, parse it and break.
				month = Integer.parseInt(in.readLine());
				if(month < 1 || month > 12){
   			   System.out.println("Please re-enter. " + String.valueOf(month) + " is not a month.");
   			}
   			else{
   			   break;
   			}
			}catch (Exception e) {
				System.out.println("Please re-enter. The month must be an integer.");
				continue;
			}//end try
		}while (true);
		
		do {
			System.out.print(" Day: ");
			try { // read the integer, parse it and break.
				day = Integer.parseInt(in.readLine());
				if(day < 1 || day > 31
				      || (day > 30 && (month == 4 || month == 6 || month == 9 || month == 11))
				      || (day > 29 && month == 2)){
   			   System.out.println("Please re-enter. " + String.valueOf(month) + "/" + String.valueOf(day) + " is not valid day.");
   			}
   			else{
   			   break;
   			}
			}catch (Exception e) {
				System.out.println("Please re-enter. The day must be an integer.");
				continue;
			}//end try
		}while (true);
		
		if(month < 11){
		   if(day < 10){
		      return String.valueOf(year) + "-0" + String.valueOf(month) + "-0" + String.valueOf(day);
		   }
		   else{
		      return String.valueOf(year) + "-0" + String.valueOf(month) + "-" + String.valueOf(day);
		   }
		}
		else if(day < 10){
	      return String.valueOf(year) + "-" + String.valueOf(month) + "-0" + String.valueOf(day);
	   }
	   else{
	      return String.valueOf(year) + "-" + String.valueOf(month) + "-" + String.valueOf(day);
	   }
	}
	
	/**
	 * Method to get flight ID from user and print the entry information if it exists.
	 * 
	 * @param DBproject
	 * @return flight ID
	 * @throws java.sql.SQLException when failed to execute the query
	 * @throws Exception when user input is not an integer
	 */
	public static int findFlight(DBproject esql) {
	   int fid;
	   do {
			System.out.print("Please enter the flight ID: ");
			try { // read the integer, parse it and break.
				fid = Integer.parseInt(in.readLine());
			} catch (Exception e) {
				System.out.println("Please re-enter. The flight ID is an integer.");
				continue;
			}//end try
			
			try {
			   if(esql.executeQueryAndPrintResult("SELECT * FROM Flight WHERE fnum = " + String.valueOf(fid) + ";") < 1){
   			   System.out.println("Please re-enter. There is no flight with ID " + String.valueOf(fid) + ".");
   			}
   			else{
   			   break;
   			}
         } catch (SQLException e) {
   			System.err.println (e.getMessage());
   		}
		}while (true);
		
		return fid;
	}

	/**
	 * Method to get pilot ID from user and print the entry information if it exists.
	 * 
	 * @param DBproject
	 * @return pilot ID
	 * @throws java.sql.SQLException when failed to execute the query
	 * @throws Exception when user input is not an integer
	 */
	public static int findPilot(DBproject esql) {
		int pid;
		 do {
				System.out.print("Please enter the pilot ID: ");
				try { // read the integer, parse it and break.
					pid = Integer.parseInt(in.readLine());
				} catch (Exception e) {
					System.out.println("Please re-enter. The pilot ID is an integer.");
					continue;
				}//end try
				
				
				try {
					
				System.out.println("SELECT * FROM Pilot WHERE id = " + String.valueOf(pid) + ";");
				System.out.println(String.valueOf(esql.executeQueryAndPrintResult("SELECT * FROM Pilot WHERE id = " + String.valueOf(pid) + ";")));
					
				   if(esql.executeQueryAndPrintResult("SELECT * FROM Pilot WHERE id = " + String.valueOf(pid) + ";") < 1){
					   System.out.println("Please re-enter. There is no pilot with ID " + String.valueOf(pid) + ".");
					}
					else{
					   break;
					}
			  } catch (SQLException e) {
					System.err.println (e.getMessage());
				}
		}while (true);
			
		return pid;
	}
	
	/**
	 * Method to get plane ID from user and print the entry information if it exists.
	 * 
	 * @param DBproject
	 * @return plane ID
	 * @throws java.sql.SQLException when failed to execute the query
	 * @throws Exception when user input is not an integer
	 */
	public static int findPlane(DBproject esql) {
	   int pid;
		 do {
			System.out.print("Please enter the plane ID: ");
			try { // read the integer, parse it and break.
				pid = Integer.parseInt(in.readLine());
			} catch (Exception e) {
				System.out.println("Please re-enter. The plane ID is an integer.");
				continue;
			}//end try
			
			try {
			   if(esql.executeQueryAndPrintResult("SELECT * FROM Plane WHERE id = " + String.valueOf(pid) + ";") < 1){
				   System.out.println("Please re-enter. There is no plane with ID " + String.valueOf(pid) + ".");
				}
				else{
				   break;
				}
		   } catch (SQLException e) {
				System.err.println (e.getMessage());
			}
		}while (true);
		
		return pid;
    }
    
    /**
	 * Method to get technician ID from user and print the entry information if it exists.
	 * 
	 * @param DBproject
	 * @return technician ID
	 * @throws java.sql.SQLException when failed to execute the query
	 * @throws Exception when user input is not an integer
	 */
	public static int findTechnician(DBproject esql) {
	   int techid;
		 do {
			System.out.print("Please enter the technician ID: ");
			try { // read the integer, parse it and break.
				techid = Integer.parseInt(in.readLine());
			} catch (Exception e) {
				System.out.println("Please re-enter. The technician ID is an integer.");
				continue;
			}//end try
			
			try {
			   if(esql.executeQueryAndPrintResult("SELECT * FROM Technician WHERE id = " + String.valueOf(techid) + ";") < 1){
				   System.out.println("Please re-enter. There is no technician with ID " + String.valueOf(techid) + ".");
				}
				else{
				   break;
				}
		   } catch (SQLException e) {
				System.err.println (e.getMessage());
			}
		}while (true);
		
		return techid;
    }
    
	/**
	 * Method to ask the user for details of a repair and add it to the database. This inserts an entry into Repairs.
	 * 
	 * @param DBproject
	 * @return void
	 * @throws java.sql.SQLException when failed to execute the query
	 * @throws Exception when user input is not an integer
	 */
	public static void AddRepair(DBproject esql) {
	   int rid;
	   String repair_date;
	   String repair_code; // must be 'MJ', 'MN', or 'SV'
	   int pilot_id;
	   int plane_id;
	   int technician_id;
	   
	   //REMOVE ME
	   rid = 999;
	   
	   System.out.println("Please enter the date of repair.");
	   repair_date = getDate();
	   
   	do{
   		System.out.print("Please enter the repair code: ");
   		try {
   			repair_code = in.readLine();
   			if(!repair_code.equals("MJ") && !repair_code.equals("MN") && !repair_code.equals("SV")){
   			   System.out.println("Please re-enter. The valid codes are \"MN,\" \"MJ,\" and \"SV.\"");
   			}
   			else{
   			   break;
   			}
   		} catch (Exception e) {
   			System.out.println("Please re-enter. There was an error in reading the line.");
   			continue;
   		}
		}while (true);
		
		pilot_id = findPilot(esql);
		plane_id = findPlane(esql);
		technician_id = findTechnician(esql);
		
		try {
			esql.executeUpdate("INSERT INTO Repairs (rid, repair_date, repair_code, pilot_id, plane_id, technician_id) VALUES (\'"
			                 + String.valueOf(rid) + "\', \'"
			                 + repair_date + "\', \'"
			                 + repair_code + "\', \'"
			                 + pilot_id + "\', \'"
			                 + plane_id + "\', \'"
			                 + technician_id
			                 + "\');");
			
			System.out.println("Your entry has been added to the database.\n\n"
		                     + "New Repair with ID " + "???"
		                     + "\nDate of Repair: " + repair_date
		                     + "\nRepair Code: " + repair_code
		                     + "\nPilot ID: " + pilot_id
		                     + "\nPlane ID: " + plane_id
		                     + "\nTechnician ID: " + technician_id
		                     );
		} catch (SQLException e) {
			System.err.println (e.getMessage());
		}
	}

   /**
	 * Method to ask the user for details of a plane and add it to the database. This inserts an entry into Plane.
	 * 
	 * @param DBproject
	 * @return void
	 * @throws java.sql.SQLException when failed to execute the query
	 * @throws Exception when user input is not an integer
	 */
	public static void AddPlane(DBproject esql) {//1
	   int id; // must be unique
	   String make; // must be 32 chars or less
	   String model; // must be 64 chars or less
	   int age; // must be a year value
	   int seats; // must be between 0 and 500
	   
	   //assign id
	   id = 67;
		
		do{
			System.out.print("Please enter the plane's make: ");
			try {
   			make = in.readLine();
   			if(make.length() > 32){
   			   System.out.println("Please re-enter. The plane's make must be 32 characters or less.");
   			}
   			else if(make.length() == 0){
   			   System.out.println("Please re-enter. The plane's make cannot be null.");
   			}
   			else{
   			   break;
   			}
			} catch (Exception e) {
				System.out.println("Please re-enter. There was an error in reading the line.");
				continue;
			}
		}while (true);
		
		do{
			System.out.print("Please enter the plane's model: ");
			try {
			   model = in.readLine();
   			if(model.length() > 64){
   			   System.out.println("Please re-enter. The plane's model must be 64 characters or less.");
   			}
   			else if(model.length() == 0){
   			   System.out.println("Please re-enter. The plane's model cannot be null.");
   			}
   			else{
   			   break;
   			}
			} catch (Exception e) {
				System.out.println("Please re-enter. There was an error in reading the line.");
				continue;
			}
		}while (true);
		
		do {
			System.out.print("Please enter the plane's age: ");
			try { // read the integer, parse it and break.
				age = Integer.parseInt(in.readLine());
				if(age < 0){
   			   System.out.println("Please re-enter. The plane's age cannot be negative.");
   			}
   			else if(age > 9999){
   			   System.out.println("Please re-enter. The plane's age must be a year.");
   			}
   			else{
   			   break;
   			}
			}catch (Exception e) {
				System.out.println("Please re-enter. The age must be an integer.");
				continue;
			}//end try
		}while (true);
		
		do {
			System.out.print("Please enter the plane's seat capacity: ");
			try { // read the integer, parse it and break.
				seats = Integer.parseInt(in.readLine());
				if(seats <= 0 || seats >= 500){
   			   System.out.println("Please re-enter. The plane's seating must be between 0 and 500 (noninclusive).");
   			}
   			else{
   			   break;
   			}
			}catch (Exception e) {
				System.out.println("Please re-enter. The seat capacity must be an integer.");
				continue;
			}//end try
		}while (true);
		
		try {
			esql.executeUpdate("INSERT INTO Plane (id, make, model, age, seats) VALUES (\'"
			                 + String.valueOf(age) + "\', \'"
			                 + make + "\', \'"
			                 + model + "\', \'"
			                 + String.valueOf(age) + "\', \'"
			                 + String.valueOf(seats)
			                 + "\');");
			
			System.out.println("Your entry has been added to the database.\n\n"
		                     + "New Plane with ID " + "???"
		                     + "\nMake: " + make
		                     + "\nModel: " + model
		                     + "\nAge: " + String.valueOf(age)
		                     + "\nNumber of Seats: " + String.valueOf(seats)
		                     );
		} catch (SQLException e) {
			System.err.println (e.getMessage());
		}
		
		//what if they need to edit?
		// FIXME : increment ID
	}
	
   /**
	 * Method to ask the user for details of a pilot and add it to the database. This inserts an entry into Pilot.
	 * 
	 * @param DBproject
	 * @return void
	 * @throws java.sql.SQLException when failed to execute the query
	 * @throws Exception when user input is not an integer
	 */
	public static void AddPilot(DBproject esql) {//2
	   int id; // must be unique
	   String fullname; // must be 128 chars or less
	   String nationality; // must be 24 chars or less
	   
      //assign id
		id = 250;

		do{
			System.out.print("Please enter the pilot's full name: ");
			try {
   			fullname = in.readLine();
   			if(fullname.length() > 128){
   			   System.out.println("Please re-enter. The pilot's name must be entered in 128 characters or less.");
   			}
   			else{
   			   break;
   			}
			} catch (Exception e) {
				System.out.println("Please re-enter. There was an error in reading the line.");
				continue;
			}
		}while (true);
		
		do{
			System.out.print("Please enter the pilot's nationality: ");
			try {
   			nationality = in.readLine();
   			if(nationality.length() > 24){
   			   System.out.println("Please re-enter. The pilot's nationality must be 24 characters or less.");
      		}
   			else{
   			   break;
   			}
			} catch (Exception e) {
				System.out.println("Please re-enter. There was an error in reading the line.");
				continue;
			}
		}while (true);
		
		try {
			esql.executeUpdate("INSERT INTO Pilot(id, fullname, nationality) VALUES (\'"
			               + String.valueOf(id) + "\', \'"
			               + fullname + "\', \'"
			               + nationality
			               + "\');");
               
         System.out.println("Your entry has been added to the database.\n\n"
               + "New Pilot with ID " + "???"
               + "\nFull Name: " + fullname
               + "\nNationality: " + nationality
               );
      } catch (SQLException e) {
			System.err.println (e.getMessage());
		}
	}

   /**
	 * Method to ask the user for details of a flight and add it to the database. This inserts entries into Flight, FlightInfo, and Schedule.
	 * 
	 * @param DBproject
	 * @return void
	 * @throws java.sql.SQLException when failed to execute the query
	 * @throws Exception when user input is not an integer
	 */
	public static void AddFlight(DBproject esql) {//3
		// Given a pilot, plane and flight, adds a flight in the DB
		int fnum;
		int cost; // must be >= 0
		int numsold; // must be > 0
		int numstops; // must be > 0
		String departureDate;
		String arrivalDate;
		String arrivalairport; // must be 5 chars or less
	   String departureairport; // must be 5 chars or less
	   int pilotid;
	   int planeid;
	   int fiid = 999; //flight info id
	   int id = 999; //schedule id
		
		//assign fnum, fiid, id
		//REMOVE ME
	   fnum = 999;
		
		do {
			System.out.print("Please enter the cost of the flight: ");
			try { // read the integer, parse it and break.
				cost = Integer.parseInt(in.readLine());
				if(cost <= 0){
   			   System.out.println("Please re-enter. The flight cost cannot be less than or equal to 0.");
   			}
   			else{
   			   break;
   			}
			}catch (Exception e) {
				System.out.println("Please re-enter. The flight cost must be an integer.");
				continue;
			}//end try
		}while (true);
		
		do {
			System.out.print("Please enter the number of tickets sold for this flight: ");
			try { // read the integer, parse it and break.
				numsold = Integer.parseInt(in.readLine());
				if(numsold < 0){
   			   System.out.println("Please re-enter. The number of tickets sold cannot be less than 0.");
   			}
   			else{
   			   break;
   			}
			}catch (Exception e) {
				System.out.println("Please re-enter. The number of tickets sold must be an integer.");
				continue;
			}//end try
		}while (true);
		
		do {
			System.out.print("Please enter the number of stops for this flight: ");
			try { // read the integer, parse it and break.
				numstops = Integer.parseInt(in.readLine());
				if(numstops < 0){
   			   System.out.println("Please re-enter. The number of stops cannot be less than 0.");
   			}
   			else{
   			   break;
   			}
			}catch (Exception e) {
				System.out.println("Please re-enter. The number of stops must be an integer.");
				continue;
			}//end try
		}while (true);
		
		System.out.println("Please enter the departure date of this flight.");
		departureDate = getDate();
		
		System.out.println("Please enter the arrival date of this flight.");
		arrivalDate = getDate();
		
		do{
			System.out.print("Please enter the code of the arrival airport: ");
			try {
   			arrivalairport = in.readLine();
   			if(arrivalairport.length() > 5){
   			   System.out.println("Please re-enter. The arrival airport code must be 5 characters or less.");
   			}
   			else if(arrivalairport.length() == 0){
   			   System.out.println("Please re-enter. The arrival airport code cannot be null.");
   			}
   			else{
   			   break;
   			}
			} catch (Exception e) {
				System.out.println("Please re-enter. There was an error in reading the line.");
				continue;
			}
		}while (true);
		
		do{
			System.out.print("Please enter the code of the departure airport: ");
			try {
   			departureairport = in.readLine();
   			if(departureairport.length() > 5){
   			   System.out.println("Please re-enter. The departure airport code must be characters or less.");
   			}
   			
   			else if(departureairport.length() == 0){
   			   System.out.println("Please re-enter. The departure airport code cannot be null.");
   			}
   			else{
   			   break;
   			}
			} catch (Exception e) {
				System.out.println("Please re-enter. There was an error in reading the line.");
				continue;
			}
		}while (true);
		
		pilotid = findPilot(esql);
	   planeid = findPlane(esql);
		
		//no statement to get fiid
		try {
		   //Insert values into tables
			esql.executeUpdate("INSERT INTO Flight(fnum, cost, num_sold, num_stops, actual_arrival_date, actual_departure_date, arrival_airport, departure_airport) VALUES (\'"
			                  + String.valueOf(fnum) + "\', \'"
			                  + String.valueOf(cost) + "\', \'"
			                  + String.valueOf(numsold) + "\', \'"
			                  + String.valueOf(numstops) + "\', \'"
			                  + arrivalDate + "\', \'"
			                  + departureDate + "\', \'"
			                  + String.valueOf(arrivalairport) + "\', \'"
			                  + String.valueOf(departureairport)
                           + "\');");
                              
         esql.executeUpdate("INSERT INTO FlightInfo(fiid, flight_id, pilot_id, plane_id) VALUES (\'"
                           + String.valueOf(fiid) + "\', \'"
                           + String.valueOf(fnum) + "\', \'"
                           + String.valueOf(pilotid) + "\', \'"
                           + String.valueOf(planeid)
                           + "\');");
                           
         esql.executeUpdate("INSERT INTO Schedule(id, flightNum, departure_time, arrival_time) VALUES (\'"
                           + String.valueOf(id) + "\', \'"
                           + String.valueOf(fnum) + "\', \'"
                           + departureDate + "\', \'"
			               + arrivalDate + "\', \'"
                           + "\');");
         
         //Print information for user
         System.out.println("The following entries been added to the database.\n\n"
		                     + "New Flight with ID " + "???"
		                     + "\nCost: " +String.valueOf(cost)
                           + "\nNumber of Tickets Sold: " + String.valueOf(numsold)
                           + "\nNumber of Stops " + String.valueOf(numstops)
                           + "\nDate of Arrival: " + arrivalDate
                           + "\nDate of Departure: " + departureDate
                           + "\nArrival Airport Code: " + String.valueOf(arrivalairport)
                           + "\nDeparture Airport Code: " + String.valueOf(departureairport)
                           + "\n\n"
                           + "New FlightInfo entry with ID " + "???"
                           + "\nFlight ID: " + "???"
                           + "\nPilot ID: " + String.valueOf(pilotid)
                           + "\nPlane ID: " + String.valueOf(planeid)
                           + "\n\n"
                           + "New Schedule entry with ID " + "???"
                           + "\nFlight ID: " + "???"
                           + "\nTime of Departure: " + departureDate
                           + "\nTime of Arrival: " + arrivalDate
		                     );
      } catch (SQLException e) {
			System.err.println (e.getMessage());
		}
	}
	
   /**
	 * Method to ask the user for details of a technician and add it to the database. This inserts an entry into Technician.
	 * 
	 * @param DBproject
	 * @return void
	 * @throws java.sql.SQLException when failed to execute the query
	 * @throws Exception when user input is not an integer
	 */
	public static void AddTechnician(DBproject esql) {//4
	   int id; // must be unique
	   String fullname; // must be 128 chars or less
	   String nationality; // must be 24 chars or less
	   
	   //REMOVE ME
	   id = 999;
	   
	   //assign id
		
		do{
			System.out.print("Please enter the technician's full name: ");
			try {
   			fullname = in.readLine();
   			if(fullname.length() > 128){
   			   System.out.println("Please re-enter. The technician's name must be entered in 128 characters or less.");
   			}
   			else if(fullname.length() == 0){
   			   System.out.println("Please re-enter. The technician's name cannot be null.");
   
   			}
   			else{
   			   break;
			   }
			} catch (Exception e) {
				System.out.println("Please re-enter. There was an error in reading the line.");
				continue;
			}
		}while (true);
		
		try {
			esql.executeUpdate("INSERT INTO Technician(id, full_name) VALUES (\'" + String.valueOf(id) + "\', \'" + fullname + "\');");
               
         System.out.println("Your entry has been added to the database.\n\n"
            + "New Technician with ID " + "???"
            + "\nFull Name: " + fullname
            );
      } catch (SQLException e) {
			System.err.println (e.getMessage());
		}
	}
	
   /**
	 * Method to ask the user for details of a customer and flight and add it to the database. This inserts an entry into Reservation and updates the entry in Flight.
	 * 
	 * @param DBproject
	 * @return void
	 * @throws java.sql.SQLException when failed to execute the query
	 * @throws Exception when user input is not an integer
	 */
	public static void BookFlight(DBproject esql) {//5
		// Given a customer and a flight that he/she wants to book, add a reservation to the DB
		int rnum; // reservation number
		int cid; // customer id
		int fid; // flight id
		String status; // 'W', 'C', or 'R'
		
		//we assign the rnum
		//REMOVE ME
	   rnum = 999;
		
		do {
			System.out.print("Please enter the customer ID: ");
			try { // read the integer, parse it and break.
				cid = Integer.parseInt(in.readLine());
	
				if(esql.executeQueryAndPrintResult("SELECT id FROM Customer WHERE id = " + String.valueOf(cid) + ";") < 1){
				   System.out.println("Please re-enter. There is no customer with ID " + String.valueOf(cid) + ".");
				}
				else{
				   break;
				}
			}catch (Exception e) {
				System.out.println("Please re-enter. The customer ID is an integer.");
				continue;
			}//end try
		}while (true);
		
		fid = findFlight(esql);
		
		boolean flightisfull = (ListNumberOfAvailableSeats(esql) <= 0);
		
		if(flightisfull){
		   System.out.println("Flight " + String.valueOf(fid) + " is full. The reservation will be placed on the waitlist.");
		   status = "W";
		}
		else{
		   do {
		      String input;
   			System.out.print("Has the customer confirmed their reservation? (y/n):");
   			
   			try {
      			input = in.readLine();
      			if(input.equals("y")){
      			   status = "C";
      			   break;
      			}
      			else if(input.equals("n")){
      			   status = "R";
      			   break;
      			}
      			else{
      			   System.out.println("Please try again. Enter y for yes and n for no.");
      			}
   			} catch (Exception e) {
   				System.out.println("Please re-enter. There was an error in reading the line.");
   				continue;
   			}
   		}while (true);
   		
   		try {
   			esql.executeUpdate("UPDATE Flight SET numsold = numsold + 1 WHERE fnum = " + String.valueOf(fid) + ";");
   			System.out.println("The ticket has been sold to customer " + String.valueOf(cid));
         } catch (SQLException e) {
   			System.err.println (e.getMessage());
         }
		}
		
		try {
			esql.executeUpdate("INSERT INTO Reservation(rnum, cid, fid, status) VALUES (\'"
			      + String.valueOf(rnum) + "\', \'"
			      + String.valueOf(cid)+ "\', \'"
			      + String.valueOf(fid) + "\', \'"
			      + status
			      + "\');");
            
			System.out.println("Your entry has been added to the database.\n\n"
               + "New Reservation with ID " + "???"
               + "\nCustomer ID: " + cid
               + "\nFlight ID: " + fid
               + "\nReservation Status: " + status
               );
      } catch (SQLException e) {
			System.err.println (e.getMessage());
      }
	}

   /**
	 * Method to ask the user for a flight number and date and prints the number of available seats.
	 * 
	 * @param DBproject
	 * @return seats available
	 * @throws java.sql.SQLException when failed to execute the query
	 * @throws Exception when user input is not an integer
	 */
	public static int ListNumberOfAvailableSeats(DBproject esql) {//6
		// For flight number and date, find the number of available seats (i.e. total plane capacity minus booked seats )
		
		int fid = findFlight(esql);
		int available = -1;
		List<List<String>> result;
		
		System.out.println("Please enture the departure date of the flight.");
		String date = getDate();
		
		try {
		   result = esql.executeQueryAndReturnResult(
            "SELECT DIFFERENCE(seats, num_sold) FROM Plane P, FlightInfo FI, Schedule S, Flight F WHERE"
            + " FI.plane_id = P.id"
            + " AND FI.flight_id = " + String.valueOf(fid)
            + " AND S.departure_time = " + date
            + " AND S.flightNum = " + String.valueOf(fid)
            + " AND F.fnum = " + String.valueOf(fid)
            + " AND F.actual_departure_date = " + date
            + ";");
            
            System.out.println("The number of avaiable seats for flight " + String.valueOf(fid) + " on " + date + " is:\n" + result.get(0).get(0));
		
			try {
			   available = Integer.parseInt(result.get(0).get(0));
			}catch (Exception e) {
				System.out.println("Internal error in ListNumberOfAvailableSeats().");
			}
		
		  } catch (SQLException e) {
				System.err.println (e.getMessage());
		  }
		
		
		return available;
	}

   /**
	 * Method to count the number of repairs per plane and list them in descending order.
	 * 
	 * @param DBproject
	 * @return void
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public static void ListsTotalNumberOfRepairsPerPlane(DBproject esql) {//7
      try {
		   esql.executeQueryAndPrintResult(
   		   "SELECT P.id, COUNT(R.rid), R.rid"
   		   + " FROM Plane P, Repairs R"
   		   + " WHERE P.id = R.plane_id"
   		   + " GROUP BY P.id"
   		   + " ORDER BY R.rid DESC"
   		   + ";");
      } catch (SQLException e) {
			System.err.println (e.getMessage());
      }
	}

   /**
	 * Method to count repairs per year and list them in ascending order.
	 * 
	 * @param DBproject
	 * @return void
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public static void ListTotalNumberOfRepairsPerYear(DBproject esql) {//8
      try {
		   esql.executeQueryAndPrintResult(
   		   "SELECT COUNT(rid), rid"
   		   + " FROM Repairs"
   		   + " GROUP BY YEAR(repair_date)"
   		   + " ORDER BY rid ASC"
   		   + ";");
      } catch (SQLException e) {
			System.err.println (e.getMessage());
      }
	}
	
	/**
	 * Method to find how many passengers there are with a status (i.e. W,C,R) and list that number.
	 * 
	 * @param DBproject
	 * @return void
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public static void FindPassengersCountWithStatus(DBproject esql) {//9
		try {
		   esql.executeQueryAndPrintResult(
   		   "SELECT COUNT(rnum), cid"
   		   + " FROM Reservation"
   		   + " GROUP BY status"
   		   + ";");
      } catch (SQLException e) {
			System.err.println (e.getMessage());
      }
	}
}
