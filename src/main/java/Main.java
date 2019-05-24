import util.Printer;

import java.io.FileNotFoundException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
  private static final String DB_DRIVER = "org.postgresql.Driver";
  private static final String DB_CONNECTION_FILE = "connection.txt";
  public static final Logger LOG = Logger.getLogger("PROJECT 3");

  public static void main(String args[]) throws ClassNotFoundException {
    Class.forName(DB_DRIVER);

    // Create PostgreSQL Connection
    PsqlConnection conn;
    try {
      FileParser txtParser = new FileParser();
      try {
        txtParser.open(DB_CONNECTION_FILE);
      } catch (FileNotFoundException e) {
        Main.LOG.log(Level.SEVERE, String.format("%s is not exist", DB_CONNECTION_FILE));
        throw new RuntimeException(e);
      }
      Map<String, String> connectionInfo = txtParser.parseTxt();
      txtParser.close();
      Printer.printMap(connectionInfo);

      String connectionUrl = String.format(
          "jdbc:postgresql://%s/%s", connectionInfo.get("IP"), connectionInfo.get("DB_NAME"));

      Properties props = new Properties();
      props.setProperty("user", connectionInfo.get("ID"));
      props.setProperty("password", connectionInfo.get("PW"));
      props.setProperty("currentSchema", connectionInfo.get("SCHEMA_NAME"));

      conn = PsqlConnection.create(DriverManager.getConnection(connectionUrl, props), connectionInfo);
    } catch (SQLException e) {
      LOG.log(Level.SEVERE, "Connection Failed");
      throw new RuntimeException(e);
    }

    while (true) {
      Scanner input = new Scanner(System.in);
      int code;

      Labeler.ConsoleLabel.INSTRUCTION_INIT.print();
      try {
        code = input.nextInt();
      } catch (InputMismatchException e) {
        Labeler.ConsoleLabel.INSTRUCTION_TRY_AGAIN.println();
        continue;
      }
      Instruction inst = Instruction.getInstruction(code);
      switch (inst) {
        case IMPORT_CSV: {
          Labeler.ConsoleLabel.INSTRUCTION_IMPORT_CSV.println();
          importCsv(conn);
          break;
        }
        case EXPORT_CSV: {
          Labeler.ConsoleLabel.INSTRUCTION_EXPORT_CSV.println();
          exportCsv(conn);
          break;
        }
        case MANIPULATE_DATA: {
          Labeler.ConsoleLabel.INSTRUCTION_MANIPULATE_DATA.println();
          manipulateData(conn);
          break;
        }
        case EXIT: {
          Labeler.ConsoleLabel.INSTRUCTION_EXIT.println();
          return;
        }
        case INVALID: {
          Labeler.ConsoleLabel.INSTRUCTION_TRY_AGAIN.println();
          continue;
        }
      }
      System.out.println();
    }
  }

  private static void importCsv(PsqlConnection conn) {
    Scanner input = new Scanner(System.in);
    Labeler.ConsoleLabel.IMPORT_CSV_TABLE_DESCRIPTION_SPECIFY_FILENAME.print();
    String tableDescriptionFileName = input.nextLine();

    FileParser txtParser = new FileParser();
    try {
      txtParser.open(tableDescriptionFileName);
    } catch (FileNotFoundException e) {
      LOG.log(Level.SEVERE, "Table Description File does not exist.");
      throw new RuntimeException(e);
    }
    Map<String, String> tableDescription = txtParser.parseTxt();
    Printer.printMap(tableDescription);
    txtParser.close();

    // TODO(totoro): Create Table
    // QueryGenerator.createTable(tableDescription);

    boolean isTableAlreadyExists = false;
    if (isTableAlreadyExists) {
      Labeler.ConsoleLabel.IMPORT_CSV_TABLE_DESCRIPTION_ALREADY_EXISTS.println();
    } else {
      Labeler.ConsoleLabel.IMPORT_CSV_TABLE_DESCRIPTION_NEW_CREATE.println();
    }

    Labeler.ConsoleLabel.IMPORT_CSV_INSERT_SPECIFY_CSV_FILE_NAME.print();
    String csvFileName = input.nextLine();

    FileParser csvParser = new FileParser();
    try {
      csvParser.open(csvFileName);
    } catch (FileNotFoundException e) {
      LOG.log(Level.SEVERE, "CSV File does not exist.");
      throw new RuntimeException(e);
    }
    List<Map<String, String>> csvRows = csvParser.parseCsv();
    for (Map<String, String> row : csvRows) {
      Printer.printMap(row);
    }
    csvParser.close();

    // TODO(totoro): Insert rows from CSV
    // QueryGenerator.insert(tableDescription, csvRows);

    int insertionSuccessCount = 0;
    int insertionFailureCount = 0;
    System.out.println(String.format(
        "%s (Insertion Success : %d, Insertion Failure : %d)",
        Labeler.ConsoleLabel.IMPORT_CSV_IMPORT_SUCCESS.get(), insertionSuccessCount, insertionFailureCount));
  }

  private static void exportCsv(PsqlConnection conn) {
    Scanner input = new Scanner(System.in);
    Labeler.ConsoleLabel.EXPORT_CSV_TABLE_NAME.print();
    String tableName = input.nextLine();

    // TODO(totoro): Get table rows
    // QueryGenerator.selectAll(tableName);

    boolean isTableExists = true;
    if (!isTableExists) {
      Labeler.ConsoleLabel.EXPORT_CSV_TABLE_NAME_NOT_EXISTS.println();
      Labeler.ConsoleLabel.EXPORT_CSV_EXPORT_FAILURE.println();
      return;
    }

    Labeler.ConsoleLabel.EXPORT_CSV_CSV_FILE_NAME.print();
    String csvFileName = input.nextLine();

    // TODO(totoro): Export rows to CSV

    Labeler.ConsoleLabel.EXPORT_CSV_EXPORT_SUCCESS.println();
  }

  private static void manipulateData(PsqlConnection conn) {
    // TODO(totoro): Implements manipulateData logics...
  }
}

//        Properties props = new Properties();
//
//        /* Setting Connection Info */
//        props.setProperty("user", 		DB_USER);
//        props.setProperty("password", 	DB_PASSWORD);
//
//        /* Connect! */
//        Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, props);
//
//        Statement st = conn.createStatement();
//
//        /* Create Table SQL */
//        String CreateTableSQL = "CREATE TABLE student_table " +
//                "(ID int, " +
//                "name varchar(20) not null, " +
//                "address varchar(50) not null," +
//                "department_ID int," +
//                "primary key (ID))";
//
//        st.executeUpdate(CreateTableSQL);
//
//        /* Insert Row using Statement */
//        String InsertSQL_1 = "INSERT INTO student_table values(1, 'Brandt', 'addr1', 1)";
//        st.executeUpdate(InsertSQL_1);
//
//        /* Insert Row using PreparedStatement */
//        String InsertSQL_2 = "INSERT INTO student_table (ID, name, address, department_ID) values(?, ?, ?, ?)";
//
//        PreparedStatement preparedStmt = conn.prepareStatement(InsertSQL_2);
//        preparedStmt.setInt(1, 2);
//        preparedStmt.setString(2, "Chavez");
//        preparedStmt.setString(3, "addr2");
//        preparedStmt.setInt(4, 2);
//
//        preparedStmt.execute();
//        ResultSet rs = st.executeQuery("SELECT ID, name, address, department_ID FROM student_table");
//
//        System.out.println("============ RESULT ============");
//        while (rs.next()) {
//            System.out.print("ID : " + rs.getString(1) + ", ");
//            System.out.print("Name : " + rs.getString(2) + ", ");
//            System.out.print("Address : " + rs.getString(3) + ", ");
//            System.out.print("Department_ID : " + rs.getString(4));
//            System.out.println();
//        }
//
//        /* Update Row */
//        String UpdateSQL = "UPDATE student_table SET address = ? where ID = ?";
//
//        preparedStmt = conn.prepareStatement(UpdateSQL);
//        preparedStmt.setString(1, "addr3");
//        preparedStmt.setInt(2, 2);
//        preparedStmt.executeUpdate();
//
//        rs = st.executeQuery("SELECT ID, name, address, department_ID FROM student_table");
//
//        System.out.println("============ RESULT ============");
//        while (rs.next()) {
//            System.out.print("ID : " + rs.getString(1) + ", ");
//            System.out.print("Name : " + rs.getString(2) + ", ");
//            System.out.print("Address : " + rs.getString(3) + ", ");
//            System.out.print("Department_ID : " + rs.getString(4));
//            System.out.println();
//        }
//        /* Delete Row */
//        String DeleteSQL = "DELETE FROM student_table where ID = 2";
//        st.executeUpdate(DeleteSQL);
//
//        rs = st.executeQuery("SELECT ID, name, address, department_ID FROM student_table");
//
//        System.out.println("============ RESULT ============");
//        while (rs.next()) {
//            System.out.print("ID : " + rs.getString(1) + ", ");
//            System.out.print("Name : " + rs.getString(2) + ", ");
//            System.out.print("Address : " + rs.getString(3) + ", ");
//            System.out.print("Department_ID : " + rs.getString(4));
//            System.out.println();
//        }
//
//        /* Drop table */
//        String DropTableSQL = "DROP TABLE student_table";
//        st.executeUpdate(DropTableSQL);
//
//        preparedStmt.close();
//        st.close();
//        rs.close();