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
	private Connection connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	public DBproject(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("Connecting to database...");
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");
			
			// obtain a physical connection
	        this.connection = DriverManager.getConnection(url, user, passwd);
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
		Statement stmt = this.connection.createStatement ();

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
		Statement stmt = this.connection.createStatement ();

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
		Statement stmt = this.connection.createStatement (); 
		
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
		Statement stmt = this.connection.createStatement ();

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
		Statement stmt = this.connection.createStatement ();
		
		ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
		if (rs.next()) return rs.getInt(1);
		return -1;
	}

	/**
	 * Method to close the physical connection if it is open.
	 */
	public void cleanup(){
		try{
			if (this.connection != null){
				this.connection.close ();
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
			   //Original Menu
				/*
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
				*/
				
				//Begin Custom Menu
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. Add a new entry.");
				System.out.println("2. Find an individual entry.");
				System.out.println("3. Search for information.");
				System.out.println("4. Edit an existing entry");
				System.out.println("5. Book a flight.");
				System.out.println("6. List number of available seats for a given flight.");
				System.out.println("7. List total number of repairs per plane in descending order.");
				System.out.println("8. List total number of repairs per year in ascending order.");
				System.out.println("9. Find total number of passengers with a given status.");
				System.out.println("10. < EXIT");
				
				switch (readChoice()){
					case 1: System.out.println("ADD NEW ENTRIES");
         				System.out.println("---------");
         				System.out.println("1. Add Plane");
         				System.out.println("2. Add Pilot");
         				System.out.println("3. Add Flight");
         				System.out.println("4. Add Technician");
         				System.out.println("5. Add Customer");
         				System.out.println("6. Add Repair");
         				System.out.println("7. Cancel");
         				
            			switch (readChoice()){
            			   case 1: AddPlane(esql); break;
         					case 2: AddPilot(esql); break;
         					case 3: AddFlight(esql); break;
         					case 4: AddTechnician(esql); break;
         					case 5: AddCustomer(esql); break;
         					case 6: AddRepair(esql); break;
         					case 7: break;
   					   }
   					   break;
					
					case 2: System.out.println("FIND INDIVIDUAL ENTRIES");
         				System.out.println("---------");
         				System.out.println("1. Find Plane");
         				System.out.println("2. Find Pilot");
         				System.out.println("3. Find Flight Details");
         				System.out.println("4. Find Flight Information (Pilot/Plane Assignments)");
         				System.out.println("5. Find Schedule");
         				System.out.println("6. Find Customer");
         				System.out.println("7. Find Reservation");
         				System.out.println("8. Find Technician");
         				System.out.println("9. Find Repair");
         				System.out.println("10. Cancel");
         				
            			switch (readChoice()){
            			   case 1: findPlane(esql); break;
         					case 2: findPilot(esql); break;
         					case 3: findFlight(esql); break;
         					case 4: findFlightInfo(esql); break;
         					case 5: findSchedule(esql); break;
         					case 6: findCustomer(esql); break;
         					case 7: findReservation(esql); break;
         					case 8: findTechnician(esql); break;
         					case 9: findRepair(esql); break;
         					case 10: break;
   					   }
   					   break;
					
					case 3: System.out.println("I am:");
         				System.out.println("---------");
         				System.out.println("1. Airline Management");
         				System.out.println("2. A Customer");
         				System.out.println("3. Maintenence Staff");
         				System.out.println("4. A Pilot");
         				System.out.println("5. Cancel Search");
         				
            			switch (readChoice()){
            			   case 1: System.out.println("AIRLINE MANAGEMENT MENU");
         				      System.out.println("---------");
               				System.out.println("1. Get a flight's schedule.");
               				System.out.println("2. Get a flight's seat availability.");
               				System.out.println("3. Check if a flight is on time.");
               				System.out.println("4. Get all flights on a certain date.");
               				System.out.println("5. List the passengers on a certain flight.");
               				System.out.println("6. Retrieve customer information with reservation number.");
               				System.out.println("7. Get plane information.");
               				System.out.println("8. List the repairs made by a certain technician.");
               				System.out.println("9. List a plane's repairs.");
               				System.out.println("10. Cancel");
               				
                  			switch (readChoice()){
                  			   case 1: getFlightSchedule(esql); break;
               					case 2: ListNumberOfAvailableSeats(esql); break;
               					case 3: getFlightActualTimes(esql); break;
               					case 4: getFlightsByDate(esql); break;
               					case 5: getPassengersOnFlight(esql); break;
               					case 6: getPassengerByReservation(esql); break;
               					case 7: getPlaneInfo(esql); break;
               					case 8: getRepairsByTechnician(esql); break;
               					case 9: getRepairsOnPlane(esql); break;
               					case 10: break;
         					   } break;
         					   
         					case 2: System.out.println("CUSTOMER MENU");
         				      System.out.println("---------");
               				System.out.println("1. Find flights by destination.");
               				System.out.println("2. Given a flight number, find the ticket cost.");
               				System.out.println("3. Given a flight number, find the airplane make and model.");
               				System.out.println("4. Make a reservation for a flight.");
               				System.out.println("5. Cancel");
               				
         					   switch (readChoice()){
                  			   case 1: getFlightsByDestination(esql); break;
               					case 2: break;
               					case 3: break;
               					case 4: BookFlight(); break;
               					case 5: break;
         					   } break;
         					   
         					case 3: System.out.println("MAINTENANCE STAFF MENU");
         				      System.out.println("---------");
               				System.out.println("1. Find all repairs made on a plane.");
               				System.out.println("2. Find all requests made by a pilot");
               				System.out.println("3. Enter a new repair.");
               				System.out.println("4. Find information of a previous repair.");
               				System.out.println("5. Cancel");
               				
         					   switch (readChoice()){
                  			   case 1: getRepairsOnPlane(esql); break;
               					case 2: break;
               					case 3: break;
               					case 4: AddRepair(esql); break;
               					case 5: FindRepair(esql); break;
         					   } break;
         					   
         					case 4: System.out.println("PILOT MENU");
         				      System.out.println("---------");
               				System.out.println("1. Make a repair request.");
               				System.out.println("2. Cancel");
               				
         					   switch (readChoice()){
                  			   case 1: AddRepair(); break;
               					case 2: break;
         					   } break;
         					   
         					case 5: break;
   					   } break;
					
					case 4: System.out.println("FIXME"); break;
					
					case 5: BookFlight(esql); break;
					case 6: ListNumberOfAvailableSeats(esql); break;
					case 7: ListsTotalNumberOfRepairsPerPlane(esql); break;
					case 8: ListTotalNumberOfRepairsPerYear(esql); break;
					case 9: FindPassengersCountWithStatus(esql); break;
					case 10: keepon = false; break;
				}
				//End Custom Menu
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
	private static String get Date() {
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
	 * Method to get FlightInfo ID from user and print the entry information if it exists.
	 * 
	 * @param DBproject
	 * @return FlightInfo ID
	 * @throws java.sql.SQLException when failed to execute the query
	 * @throws Exception when user input is not an integer
	 */
	public static int findFlightInfo(DBproject esql) {
	   int fiid;
	   do {
			System.out.print("Please enter the FlightInfo ID: ");
			try { // read the integer, parse it and break.
				fiid = Integer.parseInt(in.readLine());
			} catch (Exception e) {
				System.out.println("Please re-enter. The FlightInfo ID is an integer.");
				continue;
			}//end try
			
			try {
			   if(esql.executeQueryAndPrintResult("SELECT * FROM FlightInfo WHERE fiid = " + String.valueOf(fiid) + ";") < 1){
   			   System.out.println("Please re-enter. There is no FlightInfo with ID " + String.valueOf(fiid) + ".");
   			}
   			else{
   			   break;
   			}
         } catch (SQLException e) {
   			System.err.println (e.getMessage());
   		}
		}while (true);
		
		return fiid;
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
	   String fname;
	   
	   System.out.println("Lookup the pilot by: \n1. ID \n2. Full Name");
	   
	   switch(readChoice()){
	      case 1:
   	         do {
         			System.out.print("Please enter the pilot ID: ");
         			try { // read the integer, parse it and break.
         				pid = Integer.parseInt(in.readLine());
         			} catch (Exception e) {
         				System.out.println("Please re-enter. The pilot ID is an integer.");
         				continue;
         			}//end try
         			
         			try {
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
         		break;
      		
	      case 2:
   	         do{
         			System.out.print("Please enter the pilot's full name: ");
         			try {
            			fname = in.readLine();
            			break;
         			} catch (Exception e) {
         				System.out.println("Please re-enter. There was an error in reading the line.");
         				continue;
         			}
         			
         			if(fname.equals("lookup by ID")){
         			   goto case 1;
         			}
         			
         			try {
         			   int found = esql.executeQueryAndPrintResult("SELECT * FROM Pilot WHERE fullname = " + fname + ";");
         			   if(found < 1){
            			   System.out.println("There is no pilot named " + fname + ". Please re-enter or enter \"lookup by ID\" to lookup by ID.");
            			}
            			else if(found > 1){
            			   System.out.println("There are multiple pilots with this name. Please select by ID.");
            			   goto case 1;
            			}
            			else{
            			   List<List<String>> result = esql.executeQueryAndReturnResult("SELECT id FROM Pilot WHERE fullname = " + fname + ";");
            			   try {
               				pid = Integer.parseInt(result.get(1).get(1));
               			} catch (Exception e) {
               				System.out.println("Internal error in findPilot().");
               				continue;
               			}
            			   break;
            			}
                  } catch (SQLException e) {
            			System.err.println (e.getMessage());
            		}
         		}while (true);
         			
         		return pid;
   	         break;
	   }
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
	   String make;
	   String model;
	   
	   System.out.println("Look up the plane by: \n1. ID \n2. Make and Model");
	   
	   switch(readChoice()){
	      case 1:
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
	         break;
	      case 2:
	         do{
      			System.out.print("Please enter the plane's make: ");
      			try {
         			make = in.readLine();
         			break;
      			} catch (Exception e) {
      				System.out.println("Please re-enter. There was an error in reading the line.");
      				continue;
      			}
      		}while (true);
      		
      		if(make.equals("lookup by ID")){
   			   goto case 1;
   			}
      		
      		do{
      			System.out.print("Please enter the plane's model: ");
      			try {
         			model = in.readLine();
         			break;
      			} catch (Exception e) {
      				System.out.println("Please re-enter. There was an error in reading the line.");
      				continue;
      			}
      		}while (true);
      		
   			try {
      			   int found = esql.executeQueryAndPrintResult("SELECT * FROM Plane WHERE make = " + make + " AND model = " + model + ";");
      			   if(found < 1){
         			   System.out.println("There is no pilot with make and model " + make + " " + model + ". Please re-enter or enter \"lookup by ID\" to lookup by ID.");
         			}
         			else if(found > 1){
         			   System.out.println("There are multiple planes with this make and model. Please select by ID.");
         			   goto case 1;
         			}
         			else{
         			   List<List<String>> result = esql.executeQueryAndReturnResult("SELECT id FROM Plane WHERE make = " + make + " AND model = " + model + ";");
         			   try {
            				pid = Integer.parseInt(result.get(1).get(1));
            			} catch (Exception e) {
            				System.out.println("Internal error in findPlane().");
            				continue;
            			}
         			   break;
         			}
               } catch (SQLException e) {
         			System.err.println (e.getMessage());
         		}
      		
	         break;
	   }
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
	   String fname;
	   
	   System.out.println("Lookup the technician by: \n1. ID \n2. Full Name");
	   
	   switch(readChoice()){
	      case 1:
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
         	break;
      		
	      case 2:
   	         do{
         			System.out.print("Please enter the technician's full name: ");
         			try {
            			fname = in.readLine();
            			break;
         			} catch (Exception e) {
         				System.out.println("Please re-enter. There was an error in reading the line.");
         				continue;
         			}
         			
         			if(fname.equals("lookup by ID")){
         			   goto case 1;
         			}
         			
         			try {
         			   int found = esql.executeQueryAndPrintResult("SELECT * FROM Technician WHERE full_name = " + fname + ";");
         			   if(found < 1){
            			   System.out.println("There is no technician named " + fname + ". Please re-enter or enter \"lookup by ID\" to lookup by ID.");
            			}
            			else if(found > 1){
            			   System.out.println("There are multiple technicians with this name. Please select by ID.");
            			   goto case 1;
            			}
            			else{
            			   List<List<String>> result = esql.executeQueryAndReturnResult("SELECT id FROM Technician WHERE full_name = " + fname + ";");
            			   try {
               				techid = Integer.parseInt(result.get(1).get(1));
               			} catch (Exception e) {
               				System.out.println("Internal error in findTechnician().");
               				continue;
               			}
            			   break;
            			}
                  } catch (SQLException e) {
            			System.err.println (e.getMessage());
            		}
         		}while (true);
         			
         		return techid;
   	         break;
	   }
	}
	
	/**
	 * Method to get Schedule ID from user and print the entry information if it exists.
	 * 
	 * @param DBproject
	 * @return Schedule ID
	 * @throws java.sql.SQLException when failed to execute the query
	 * @throws Exception when user input is not an integer
	 */
	public static int findSchedule(DBproject esql) {
	   int sid;

	   do {
			System.out.print("Please enter the Schedule ID: ");
			try { // read the integer, parse it and break.
				sid = Integer.parseInt(in.readLine());
			} catch (Exception e) {
				System.out.println("Please re-enter. The Schedule ID is an integer.");
				continue;
			}//end try
			
			try {
			   if(esql.executeQueryAndPrintResult("SELECT * FROM Schedule WHERE id = " + String.valueOf(sid) + ";") < 1){
   			   System.out.println("Please re-enter. There is no schedule with ID " + String.valueOf(sid) + ".");
   			}
   			else{
   			   break;
   			}
         } catch (SQLException e) {
   			System.err.println (e.getMessage());
   		}
		}while (true);
		
		return sid;
	}
	
	/**
	 * Method to get Customer ID from user and print the entry information if it exists.
	 * 
	 * @param DBproject
	 * @return Customer ID
	 * @throws java.sql.SQLException when failed to execute the query
	 * @throws Exception when user input is not an integer
	 */
	public static int findCustomer(DBproject esql) {
	   int cid;
	   do {
			System.out.print("Please enter the Customer ID: ");
			try { // read the integer, parse it and break.
				cid = Integer.parseInt(in.readLine());
			} catch (Exception e) {
				System.out.println("Please re-enter. The Customer ID is an integer.");
				continue;
			}//end try
			
			try {
			   if(esql.executeQueryAndPrintResult("SELECT * FROM Customer WHERE id = " + String.valueOf(cid) + ";") < 1){
   			   System.out.println("Please re-enter. There is no customer with ID " + String.valueOf(cid) + ".");
   			}
   			else{
   			   break;
   			}
         } catch (SQLException e) {
   			System.err.println (e.getMessage());
   		}
		}while (true);
		
		return cid;
	}
	
	/**
	 * Method to get Repair ID from user and print the entry information if it exists.
	 * 
	 * @param DBproject
	 * @return Repair ID
	 * @throws java.sql.SQLException when failed to execute the query
	 * @throws Exception when user input is not an integer
	 */
	public static int findRepair(DBproject esql) {
	   int rid;
	   do {
			System.out.print("Please enter the Repair ID: ");
			try { // read the integer, parse it and break.
				rid = Integer.parseInt(in.readLine());
			} catch (Exception e) {
				System.out.println("Please re-enter. The Repair ID is an integer.");
				continue;
			}//end try
			
			try {
			   if(esql.executeQueryAndPrintResult("SELECT * FROM Repairs WHERE rid = " + String.valueOf(rid) + ";") < 1){
   			   System.out.println("Please re-enter. There is no Repair with ID " + String.valueOf(rid) + ".");
   			}
   			else{
   			   break;
   			}
         } catch (SQLException e) {
   			System.err.println (e.getMessage());
   		}
		}while (true);
		
		return rid;
	}
	
	/**
	 * Method to get Reservation ID from user and print the entry information if it exists.
	 * 
	 * @param DBproject
	 * @return Reservation ID
	 * @throws java.sql.SQLException when failed to execute the query
	 * @throws Exception when user input is not an integer
	 */
	public static int findReservation(DBproject esql) {
	   int rid;
	   do {
			System.out.print("Please enter the Reservation ID: ");
			try { // read the integer, parse it and break.
				rid = Integer.parseInt(in.readLine());
			} catch (Exception e) {
				System.out.println("Please re-enter. The Reservation ID is an integer.");
				continue;
			}//end try
			
			try {
			   if(esql.executeQueryAndPrintResult("SELECT * FROM Reservation WHERE rnum = " + String.valueOf(rid) + ";") < 1){
   			   System.out.println("Please re-enter. There is no Reservation with ID " + String.valueOf(rid) + ".");
   			}
   			else{
   			   break;
   			}
         } catch (SQLException e) {
   			System.err.println (e.getMessage());
   		}
		}while (true);
		
		return rid;
	}
	
	/**
	 * Method to get list all the repairs made on a plane given the ID.
	 * 
	 * @param DBproject
	 * @return void
	 * @throws java.sql.SQLException when failed to execute the query
	 * @throws Exception when user input is not an integer
	 */
	public static void getRepairsOnPlane(DBproject esql) {
	   int pid = findPlane(esql);
	   
	   try {
	      esql.executeQueryAndPrintResult("SELECT * FROM Repairs WHERE plane_id = " + pid + ";");
      } catch (SQLException e) {
			System.err.println (e.getMessage());
		}
	}
	
	/**
	 * Method to get a schedule of a flight given the ID.
	 * 
	 * @param DBproject
	 * @return void
	 * @throws java.sql.SQLException when failed to execute the query
	 * @throws Exception when user input is not an integer
	 */
	public static void getFlightSchedule(DBproject esql) {
	   int fid = findFlight(esql);
	   
	   try {
	      esql.executeQueryAndReturnResult("SELECT * FROM Schedule WHERE flightNum = " + fid + ";");
      } catch (SQLException e) {
			System.err.println (e.getMessage());
		}
	}
	
	/**
	 * Method to print the whether a plane's actual arrival and departure match with the schedule.
	 * 
	 * @param DBproject
	 * @return void
	 * @throws java.sql.SQLException when failed to execute the query
	 * @throws Exception when user input is not an integer
	 */
	public static void getFlightActualTimes(DBproject esql) {
	   int sid = findSchedule(esql);
	   List<List<String>> result;
	   
	   try {
	      result = esql.executeQueryAndReturnResult(
	         "SELECT actual_departure_date, actual_arrival_date, departure_time, arrival_time"
	         + " FROM Schedule S, Flight F WHERE"
	         + " F.fnum = S.flightNum"
	         + " S.id = " + String.valueOf(sid)
	         );
      } catch (SQLException e) {
			System.err.println (e.getMessage());
		}
		
		String arrivalSched = result.get(1).get(4);
		String arrivalActual = result.get(1).get(2);
		String departSched = result.get(1).get(3);
		String departActual = result.get(1).get(1);
		
		System.out.print("The flight was scheduled to arrive at " + departSched);
		if(arrivalSched.equals(arrivalActual)){
		   System.out.println(" and came on time.");
		}
		else{
		   System.out.println(" but came at " + arrivalActual + ".");
		}
		
		System.out.print("The flight was scheduled to depart at " + departSched);
		if(departSched.equals(departActual)){
		   System.out.println(" and left on time.");
		}
		else{
		   System.out.println(" but left at " + departActual + ".");
		}
	}
	
	/*
	 * Method to find all flights departing on a certain date.
	 * 
	 * @param DBproject
	 * @return void
	 * @throws java.sql.SQLException when failed to execute the query
	 * @throws Exception when user input is not an integer
	 */
	public static void getFlightsByDate(DBproject esql) {
	   System.out.println("Please enter the departure date of the flight you're looking for.");
	   String date = getDate();
	   
	   try {
	      esql.executeQueryAndPrintResult("SELECT * FROM Flight F, Schedule S WHERE F.fnum = S.flightNum AND S.departure_time = " date + ";");
      } catch (SQLException e) {
			System.err.println (e.getMessage());
		}
	}
	
	/*
	 * Method to find all flights connecting to specific airports.
	 * 
	 * @param DBproject
	 * @return void
	 * @throws java.sql.SQLException when failed to execute the query
	 * @throws Exception when user input is not an integer
	 */
	public static void getFlightsByDestination(DBproject esql) {
	   String arrivalairport;
	   String departureairport;
	   
      do{
			System.out.print("Please enter the code of the arrival airport: ");
			try {
   			arrivalairport = in.readLine();
   			if(arrivalairport.length() > 5){
   			   System.out.println("Please re-enter. The arrival airport code must be 5 characters or less.");
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
   			else{
   			   break;
   			}
			} catch (Exception e) {
				System.out.println("Please re-enter. There was an error in reading the line.");
				continue;
			}
		}while (true);
	   
	   try {
	      esql.executeQueryAndPrintResult("SELECT * FROM Flight WHERE"
	         + " arrival_airport = " + arrivalairport
	         + " AND departure_airport = " + departureairport
	         + " ORDER BY actual_departure_date DESC"
	         + ";");
      } catch (SQLException e) {
			System.err.println (e.getMessage());
		}
	}
	
	/*
	 * Method to list the passengers on a certain flight.
	 * 
	 * @param DBproject
	 * @return void
	 * @throws java.sql.SQLException when failed to execute the query
	 * @throws Exception when user input is not an integer
	 */
	public static void getPassengersOnFlight(DBproject esql) {
	   int fid = findFlight(esql);
	   
	   try {
	      esql.executeQueryAndPrintResult("SELECT * FROM Reservation WHERE fid = " fid + " GROUP BY status;");
      } catch (SQLException e) {
			System.err.println (e.getMessage());
		}
	}
	
	/*
	 * Method to list passenger information based on reservation number.
	 * 
	 * @param DBproject
	 * @return void
	 * @throws java.sql.SQLException when failed to execute the query
	 * @throws Exception when user input is not an integer
	 */
	public static void getPassengerByReservation(DBproject esql) {
	   int rid = findReservation(esql);
	   
	   try {
	      esql.executeQueryAndPrintResult("SELECT * FROM Customer C, Reservation R WHERE id = cid;");
      } catch (SQLException e) {
			System.err.println (e.getMessage());
		}
	}
	
	/*
	 * Method to list plane information including repair information
	 * 
	 * @param DBproject
	 * @return void
	 * @throws java.sql.SQLException when failed to execute the query
	 * @throws Exception when user input is not an integer
	 */
	public static void getPlaneInfo(DBproject esql) {
	   int pid = findPlane();
	   
	   try {
	      esql.executeQueryAndPrintResult("SELECT * FROM Plane P, Repairs R WHERE plane_id = id ORDER BY repair_date DESC;");
      } catch (SQLException e) {
			System.err.println (e.getMessage());
		}
	}
	
	/*
	 * Method to list repairs made by a specific technician.
	 * 
	 * @param DBproject
	 * @return void
	 * @throws java.sql.SQLException when failed to execute the query
	 * @throws Exception when user input is not an integer
	 */
	public static void getRepairsByTechnician(DBproject esql) {
	   int techid = findTechnician();
	   
	   try {
	      esql.executeQueryAndPrintResult("SELECT * FROM Repairs WHERE technician_id = " + techid + " ORDER BY repair_date DESC");
      } catch (SQLException e) {
			System.err.println (e.getMessage());
		}
	}
	
	//BEGIN REQUIRED FUNCTIONS
	
	/**
	 * Method to ask the user for details of a customer and add it to the database. This inserts an entry into Customer.
	 * 
	 * @param DBproject
	 * @return void
	 * @throws java.sql.SQLException when failed to execute the query
	 * @throws Exception when user input is not an integer
	 */
	public static void AddCustomer(DBproject esql) {
	   int id;
	   String fname; // must be 24 characters or less
	   String lname; // must be 24 characters or less
	   String gtype; // gender must be 'F' or 'M'
	   String dob;
	   String address; // must be 256 chars or less
	   String phone; // must be 10 chars
	   String zipcode; // must be 10 chars or less
	   
	   do{
			System.out.print("Please enter the customer's first name: ");
			try {
   			fname = in.readLine();
   			if(fname.length() > 24){
   			   System.out.println("Please re-enter. The customer's first name must be 24 characters or less.");
   			}
   			else if(fname.length() == 0){
   			   System.out.println("Please re-enter. The customer's first name cannot be null.");
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
			System.out.print("Please enter the customer's last name: ");
			try {
   			lname = in.readLine();
   			if(lname.length() > 24){
   			   System.out.println("Please re-enter. The customer's last name must be 24 characters or less.");
   			}
   			else if(lname.length() == 0){
   			   System.out.println("Please re-enter. The customer's last name cannot be null.");
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
			System.out.print("Please enter the customer's gender: ");
			try {
   			gtype = in.readLine();
   			if(!gtype.equals("F") && !gtype.equals("M"){
   			   System.out.println("Please re-enter. The customer's gender must be \'M\' or \'F\'.");
   			}
   			else{
   			   break;
   			}
			} catch (Exception e) {
				System.out.println("Please re-enter. There was an error in reading the line.");
				continue;
			}
		}while (true);
		
		System.out.println("Please enter the customer's date of birth.");
		dob = getDate();
		
		do{
			System.out.print("Please enter the customer's address: ");
			try {
   			address = in.readLine();
   			if(address.length() > 256){
   			   System.out.println("Please re-enter. The customer's address must be 256 characters or less.");
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
			System.out.print("Please enter the customer's phone number: ");
			try {
   			phone = in.readLine();
   			if(phone.length() != 0 && phone.length() != 10){
   			   System.out.println("Please re-enter. The customer's phone number must be 10 digits (area code included).");
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
			System.out.print("Please enter the customer's zipcode: ");
			try {
   			zipcode = in.readLine();
   			if(zipcode.length() > 10){
   			   System.out.println("Please re-enter. The customer's zipcode must be 10 digits or less.");
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
			esql.executeUpdate("INSERT INTO CUSTOMER (id, fname, lname, gtype, dob, address, phone, zipcode) VALUES (\'"
			                 + String.valueOf(id) + "\', \'"
			                 + fname + "\', \'"
			                 + lname + "\', \'"
			                 + gtype + "\', \'"
			                 + dob + "\', \'"
			                 + address + "\', \'"
			                 + phone + "\', \'"
			                 + zipcode
			                 + ");");
			
			System.out.println("Your entry has been added to the database.\n\n"
		                     + "New Customer with ID " + "???"
		                     + "\nFirst Name: " + fname
		                     + "\nLast Name: " + lname
		                     + "\nGender: " + gtype
		                     + "\nDate of Birth: " + dob
		                     + "\nAddress: " + address
		                     + "\nPhone Number: " + phone
		                     + "\nZipcode: " + zipcode
		                     );
		} catch (SQLException e) {
			System.err.println (e.getMessage());
		}
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
	   int fiid = 0; //flight info id
	   int id = 0; //schedule id
		
		//assign fnum, fiid, id
		
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
			esql.executeUpdate("INSERT INTO Flight(fnum, cost, numsold, numstops, actualarrivaldate, actualdeparturedate, arrivalairport, departureairport) VALUES (\'"
			                  + String.valueOf(fnum) + "\', \'"
			                  + String.valueOf(cost) + "\', \'"
			                  + String.valueOf(numsold) + "\', \'"
			                  + String.valueOf(numstops) + "\', \'"
			                  + arrivalDate + "\', \'"
			                  + departureDate + "\', \'"
			                  + String.valueOf(arrivalairport) + "\', \'"
			                  + String.valueOf(departureairport)
                           + "\');");
                              
         esql.executeUpdate("INSERT INTO FlightInfo(fiid, flightid, pilotid, planeid) VALUES (\'"
                           + String.valueOf(fiid) + "\', \'"
                           + String.valueOf(flightid) + "\', \'"'
                           + String.valueOf(pilotid) + "\', \'"
                           + String.valueOf(planeid)
                           + "\');");
                           
         esql.executeUpdate("INSERT INTO Schedule(id, flightnum, departuretime, arrivaltime) VALUES (\'"
                           + String.valueOf(id) + "\', \'"
                           + String.valueOf(flightnum) + "\', \'"
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
			esql.executeUpdate("INSERT INTO Technician(id, fullname) VALUES (\'" + String.valueOf(id) + "\', \'" + fullname + "\');");
               
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
		
		System.out.println("Please enture the departure date of the flight.");
		String date = getDate();
		
		try {
		   List<List<String>> result = esql.executeQueryAndReturnResult(
            "SELECT DIFFERENCE(seats, num_sold) FROM Plane P, FlightInfo FI, Schedule S, Flight F WHERE"
            + " FI.plane_id = P.id"
            + " AND FI.flight_id = " + String.valueOf(fid)
            + " AND S.departure_time = " + date
            + " AND S.flightNum = " + String.valueOf(fid)
            + " AND F.fnum = " + String.valueOf(fid)
            + " AND F.actual_departure_date = " + date
            + ";");
      } catch (SQLException e) {
			System.err.println (e.getMessage());
      }
		
		System.out.println("The number of avaiable seats for flight " + String.valueOf(fid) + " on " + date + " is:\n" + result.get(1).get(1));
		
		try {
		   available = Integer.parseInt(result.get(1).get(1));
		}catch (Exception e) {
			System.out.println("Internal error in ListNumberOfAvailableSeats().");
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
		   executeQueryAndPrintResult(
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
		   executeQueryAndPrintResult(
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
		   executeQueryAndPrintResult(
   		   "SELECT COUNT(rnum), cid"
   		   + " FROM Reservation"
   		   + " GROUP BY status"
   		   + ";");
      } catch (SQLException e) {
			System.err.println (e.getMessage());
      }
	}
}
