package hive;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/*
ssh cloudera@192.168.31.132
password: cloudera

[cloudera@quickstart ~]$ hive
hive> show tables;
OK

    employee
    Time taken: 0.671 seconds, Fetched: 1 row(s)


hive> describe default.employee;
OK

    eid                 	int
    name                	string
    salary              	string
    destignation        	string
    Time taken: 0.162 seconds, Fetched: 4 row(s)

hive> select * from employee;
OK
    Time taken: 0.729 seconds
*/

public class CreateTableUsingJdbc {

/*
        <dependency>
            <groupId>org.apache.hive</groupId>
            <artifactId>hive-jdbc</artifactId>
            <version>1.1.0</version>
        </dependency>

        This dependency provides HiveDriver.
        Different version of dependency has different package name for HiveDriver class. So, make sure that path of HiveDriver in the code is correct.
*/
    private static String driverName = "org.apache.hive.jdbc.HiveDriver";

    public static void main(String[] args) throws SQLException, ClassNotFoundException {

        // Register driver and create driver instance
        Class.forName(driverName);

        // get connection
        String ipAddressOfHadoopVM = "192.168.31.132";  // go to cloudera vm and verify the ip address using 'ifconfig' command.
        Connection con = DriverManager.getConnection("jdbc:hive2://"+ipAddressOfHadoopVM+":10000"+"/default", "", "");

        // create statement
        Statement stmt = con.createStatement();

        // execute statement
        stmt.executeQuery("CREATE TABLE IF NOT EXISTS "
                +" employee ( eid int, name String, "
                +" salary String, destignation String)"
                +" COMMENT 'Employee details'"
                +" ROW FORMAT DELIMITED"
                +" FIELDS TERMINATED BY '\t'"
                +" LINES TERMINATED BY '\n'"
                +" STORED AS TEXTFILE");

        System.out.println("Table employee created.");
        con.close();
    }

}
