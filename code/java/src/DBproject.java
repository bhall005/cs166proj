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

	public static void AddPlane(DBproject esql) {//1
	   int id; // must be unique
	   String make; // must be 32 chars or less
	   String model; // must be 64 chars or less
	   int age; // must be a year value
	   int seats; // must be between 0 and 500
	   
		/*
		do {
			System.out.print("Please enter the Plane ID: ");
			try { // read the integer, parse it and break.
				id = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Please re-enter. The ID must be an integer.");
				continue;
			}//end try
		}while (true);
		*/
		
		do{
			System.out.print("Please enter the plane's make: ");
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
		}while (true);
		
		do{
			System.out.print("Please enter the plane's model: ");
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
		
		/*executeUpdate("INSERT INTO Plane(id, make, model, age, seats) " +
		               "VALUES (" + String.valueOf(id) + ", " + make + ", " + model + ", " + String.valueOf(age) + ", " + String.valueOf(seats) + ");");
		               */
		System.out.println("Your entry has been added to the database.\n\n"
		                     + "New Plane with ID " + "???"
		                     + "\nMake: " + make
		                     + "\nModel: " + model
		                     + "\nAge: " + String.valueOf(age)
		                     + "\nNumber of Seats: " + String.valueOf(seats)
		                     );
		                     
		//what if they need to edit?
	}

	public static void AddPilot(DBproject esql) {//2
	   int id; // must be unique
	   String fullname; // must be 128 chars or less
	   String nationality; // must be 24 chars or less
	   
	   /*
		do {
			System.out.print("Please enter the Pilot ID: ");
			try { // read the integer, parse it and break.
				id = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Please re-enter. The ID must be an integer.");
				continue;
			}//end try
		}while (true);
		*/
		
		do{
			System.out.print("Please enter the pilot's full name: ");
			fullname = in.readLine();
			if(fullname.length() > 128){
			   System.out.println("Please re-enter. The pilot's name must be entered in 128 characters or less.");
			}
			else{
			   break;
			}
		}while (true);
		
		do{
			System.out.print("Please enter the pilot's nationality: ");
			nationality = in.readLine();
			if(nationality.length() > 24){
			   System.out.println("Please re-enter. The pilot's nationality must be 24 characters or less.");
			}
			else{
			   break;
			}
		}while (true);
		
		/*executeUpdate("INSERT INTO Pilot(id, fullname, nationality) " +
               "VALUES (" + String.valueOf(id) + ", " + fullname + ", " + nationality + ");");
               */
      
      System.out.println("Your entry has been added to the database.\n\n"
		                     + "New Pilot with ID " + "???"
		                     + "\nFull Name: " + fullname
		                     + "\nNationality: " + nationality
		                     );
	}

	public static void AddFlight(DBproject esql) {//3
		// Given a pilot, plane and flight, adds a flight in the DB
		int fnum;
		int cost; // must be >= 0
		int num_sold; // must be > 0
		int num_stops; // must be > 0
		int departure_year;
		int departure_month;
		int departure_day;
		int arrival_year;
		int arrival_month;
		int arrival_day;
		String arrival_airport; // must be 5 chars or less
	   String departure_airport; // must be 5 chars or less
	   int pilot_id;
	   int plane_id;
	   int fiid = 0; //flight info id
	   int id = 0; //schedule id
		
		/*
		do {
			System.out.print("Please enter the flight number: ");
			try { // read the integer, parse it and break.
				fnum = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Please re-enter. The seat capacity must be an integer.");
				continue;
			}//end try
		}while (true);
		*/
		
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
				num_sold = Integer.parseInt(in.readLine());
				if(num_sold < 0){
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
				num_stops = Integer.parseInt(in.readLine());
				if(num_stops < 0){
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
		
		do {
			System.out.print("Departure Year: ");
			try { // read the integer, parse it and break.
				departure_year = Integer.parseInt(in.readLine());
				if(departure_year < 0 || departure_year > 9999){
   			   System.out.println("Please re-enter. " + String.valueOf(departure_year) + " is not a year.");
   			}
   			else{
   			   break;
   			}
			}catch (Exception e) {
				System.out.println("Please re-enter. The departure year must be an integer.");
				continue;
			}//end try
		}while (true);
		
		do {
			System.out.print("Departure Month: ");
			try { // read the integer, parse it and break.
				departure_month = Integer.parseInt(in.readLine());
				if(departure_month < 0 || departure_month > 12){
   			   System.out.println("Please re-enter. " + String.valueOf(departure_month) + " is not a month.");
   			}
   			else{
   			   break;
   			}
			}catch (Exception e) {
				System.out.println("Please re-enter. The departure month must be an integer.");
				continue;
			}//end try
		}while (true);
		
		do {
			System.out.print("Departure Day: ");
			try { // read the integer, parse it and break.
				departure_day = Integer.parseInt(in.readLine());
				if(departure_day < 0 || departure_day > 31
				      || (departure_day > 30 && (departure_month == 4 || departure_month == 6 || departure_month == 9 || departure_month == 11))
				      || (departure_day > 29 && departure_month == 2)){
   			   System.out.println("Please re-enter. " + String.valueOf(departure_month) + "/" + String.valueOf(departure_day) + " is not valid day.");
   			}
   			else{
   			   break;
   			}
			}catch (Exception e) {
				System.out.println("Please re-enter. The departure day must be an integer.");
				continue;
			}//end try
		}while (true);
		
		System.out.println("Please enter the arrival date of this flight.");
		
		do {
			System.out.print("Arrival Year: ");
			try { // read the integer, parse it and break.
				arrival_year = Integer.parseInt(in.readLine());
				if(arrival_year < 0 || arrival_year > 9999){
   			   System.out.println("Please re-enter. " + String.valueOf(arrival_year) + " is not a year.");
   			}
   			else{
   			   break;
   			}
			}catch (Exception e) {
				System.out.println("Please re-enter. The arrival year must be an integer.");
				continue;
			}//end try
		}while (true);
		
		do {
			System.out.print("Arrival Month: ");
			try { // read the integer, parse it and break.
				arrival_month = Integer.parseInt(in.readLine());
				if(arrival_month < 0 || arrival_month > 12){
   			   System.out.println("Please re-enter. " + String.valueOf(arrival_month) + " is not a month.");
   			}
   			else{
   			   break;
   			}
			}catch (Exception e) {
				System.out.println("Please re-enter. The arrival month must be an integer.");
				continue;
			}//end try
		}while (true);
		
		do {
			System.out.print("Arrival Day: ");
			try { // read the integer, parse it and break.
				arrival_day = Integer.parseInt(in.readLine());
				if(arrival_day < 0 || arrival_day > 31
				      || (arrival_day > 30 && (arrival_month == 4 || arrival_month == 6 || arrival_month == 9 || arrival_month == 11))
				      || (arrival_day > 29 && arrival_month == 2)){
   			   System.out.println("Please re-enter. " + String.valueOf(arrival_month) + "/" + String.valueOf(arrival_day) + " is not valid day.");
   			}
   			else{
   			   break;
   			}
			}catch (Exception e) {
				System.out.println("Please re-enter. The arrival day must be an integer.");
				continue;
			}//end try
		}while (true);
		
		do{
			System.out.print("Please enter the code of the arrival airport: ");
			arrival_airport = in.readLine();
			if(arrival_airport.length() > 5){
			   System.out.println("Please re-enter. The arrival airport code must be 5 characters or less.");
			}
			else if(arrival_airport.length() == 0){
			   System.out.println("Please re-enter. The arrival airport code cannot be null.");
			}
			else{
			   break;
			}
		}while (true);
		
		do{
			System.out.print("Please enter the code of the departure airport: ");
			departure_airport = in.readLine();
			if(departure_airport.length() > 5){
			   System.out.println("Please re-enter. The departure airport code must be characters or less.");
			}
			
			else if(departure_airport.length() == 0){
			   System.out.println("Please re-enter. The departure airport code cannot be null.");
			}
			else{
			   break;
			}
		}while (true);
		
		//lookup by full name?
		do {
			System.out.print("Please enter the ID of the pilot for this flight: ");
			try { // read the integer, parse it and break.
				pilot_id = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Please re-enter. The pilot ID must be an integer.");
				continue;
			}//end try
		}while (true);
		
		//check if pilot id is valid
		
		do {
			System.out.print("Please enter the ID of the plane for this flight: ");
			try { // read the integer, parse it and break.
				plane_id = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Please re-enter. The plane ID must be an integer.");
				continue;
			}//end try
		}while (true);
		
		//check if plane id is valid
		
		//no statement to get fiid
		
		/*
		executeUpdate("INSERT INTO Flight(fnum, cost, num_sold, num_stops, actual_arrival_date, actual_departure_date, arrival_airport, departure_airport) " +
                  "VALUES (" + String.valueOf(fnum) + ", " + String.valueOf(cost)
                           + ", " + String.valueOf(num_sold) + ", " + String.valueOf(num_stops)
                           + ", " + String.valueOf(arrival_year) + "-" + String.valueOf(arrival_month) + "-" + String.valueOf(arrival_day)
                           + ", " + String.valueOf(departure_year) + "-" + String.valueOf(departure_month) + "-" + String.valueOf(departure_day)
                           + ", " + String.valueOf(arrival_airport) + ", " + String.valueOf(departure_airport)
                           + ");");
                           
      executeUpdate("INSERT INTO FlightInfo(fiid, flight_id, pilot_id, plane_id) " +
                  "VALUES (" + String.valueOf(fiid) + ", " + String.valueOf(flight_id)
                           + ", " + String.valueOf(pilot_id) + ", " + String.valueOf(plane_id)
                           + ");");
                        
      executeUpdate("INSERT INTO Schedule(id, flightnum, departure_time, arrival_time) " +
                  "VALUES (" + String.valueOf(id) + ", " + String.valueOf(flightnum)
                           + ", " + String.valueOf(departure_year) + "-" + String.valueOf(departure_month) + "-" + String.valueOf(departure_day)
                           + ", " + String.valueOf(arrival_year) + "-" + String.valueOf(arrival_month) + "-" + String.valueOf(arrival_day)
                           + ");");
      */
      
      System.out.println("The following entries been added to the database.\n\n"
		                     + "New Flight with ID " + "???"
		                     + "\nCost: " +String.valueOf(cost)
                           + "\nNumber of Tickets Sold: " + String.valueOf(num_sold)
                           + "\nNumber of Stops " + String.valueOf(num_stops)
                           + "\nDate of Arrival: " + String.valueOf(arrival_month) + "/" + String.valueOf(arrival_day) + "/" + String.valueOf(arrival_year)
                           + "\nDate of Departure: " + String.valueOf(departure_month) + "/" + String.valueOf(departure_day) + "/" + String.valueOf(departure_year)
                           + "\nArrival Airport Code: " + String.valueOf(arrival_airport)
                           + "\nDeparture Airport Code: " + String.valueOf(departure_airport)
                           + "\n\n"
                           + "New FlightInfo entry with ID " + "???"
                           + "\nFlight ID: " + "???"
                           + "\nPilot ID: " + String.valueOf(pilot_id)
                           + "\nPlane ID: " + String.valueOf(plane_id)
                           + "\n\n"
                           + "New Schedule entry with ID " + "???"
                           + "\nFlight ID: " + "???"
                           + "\nTime of Departure: " + String.valueOf(departure_month) + "/" + String.valueOf(departure_day) + "/" + String.valueOf(departure_year)
                           + "\nTime of Arrival: " + String.valueOf(arrival_month) + "/" + String.valueOf(arrival_day) + "/" + String.valueOf(arrival_year)
		                     );
	}

	public static void AddTechnician(DBproject esql) {//4
	   int id; // must be unique
	   String full_name; // must be 128 chars or less
	   String nationality; // must be 24 chars or less
	   
	   /*
		do {
			System.out.print("Please enter the Technician ID: ");
			try { // read the integer, parse it and break.
				id = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Please re-enter. The ID must be an integer.");
				continue;
			}//end try
		}while (true);
		*/
		
		do{
			System.out.print("Please enter the technician's full name: ");
			full_name = in.readLine();
			if(full_name.length() > 128){
			   System.out.println("Please re-enter. The technician's name must be entered in 128 characters or less.");
			}
			else if(full_name.length() == 0){
			   System.out.println("Please re-enter. The technician's name cannot be null.");

			}
			else{
			   break;
			}
		}while (true);
		
		/*executeUpdate("INSERT INTO Technician(id, full_name) " +
               "VALUES (" + String.valueOf(id) + ", " + full_name + ");");
               */
               
      System.out.println("Your entry has been added to the database.\n\n"
                     + "New Technician with ID " + "???"
                     + "\nFull Name: " + full_name
                     );
	}

	public static void BookFlight(DBproject esql) {//5
		// Given a customer and a flight that he/she wants to book, add a reservation to the DB
		int rnum;
		int cid;
		int fid;
		char status; //'w', 'c', or 'r'
		do{
			System.out.print("Please enter the technician's full name: ");
			full_name = in.readLine();
			if(full_name.length() > 128){
			   System.out.println("Please re-enter. The technician's name must be entered in 128 characters or less.");
			}
			else if(full_name.length() == 0){
			   System.out.println("Please re-enter. The technician's name cannot be null.");

			}
			else{
			   break;
			}
		}while (true);
	}

	public static void ListNumberOfAvailableSeats(DBproject esql) {//6
		// For flight number and date, find the number of available seats (i.e. total plane capacity minus booked seats )
	}

	public static void ListsTotalNumberOfRepairsPerPlane(DBproject esql) {//7
		// Count number of repairs per planes and list them in descending order
	}

	public static void ListTotalNumberOfRepairsPerYear(DBproject esql) {//8
		// Count repairs per year and list them in ascending order
	}
	
	public static void FindPassengersCountWithStatus(DBproject esql) {//9
		// Find how many passengers there are with a status (i.e. W,C,R) and list that number.
	}
}