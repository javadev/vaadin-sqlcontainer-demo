package com.vaadin.data.util.sqlcontainer.demo;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.vaadin.Application;
import com.vaadin.data.util.sqlcontainer.SQLContainer;
import com.vaadin.data.util.sqlcontainer.connection.JDBCConnectionPool;
import com.vaadin.data.util.sqlcontainer.connection.SimpleJDBCConnectionPool;
import com.vaadin.data.util.sqlcontainer.query.FreeformQuery;
import com.vaadin.data.Container;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;

public class ComplexQueryDemo extends Application {

    @Override
    public void init() {
        Window mainWindow = new Window("Complex query demo");
        mainWindow.getContent().setSizeFull();
        try {
            mainWindow.addComponent(buildTableWithContainer());
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        setMainWindow(mainWindow);
    }

    private Table buildTableWithContainer() throws SQLException {
        Table table = new Table();
        table.setContainerDataSource(buildContainer());
        table.setSizeFull();
        return table;
    }

    private Container buildContainer() throws SQLException {
        SimpleJDBCConnectionPool connectionPool = new SimpleJDBCConnectionPool(
                "org.hsqldb.jdbc.JDBCDriver", "jdbc:hsqldb:mem:sqlcontainer",
                "SA", "", 2, 5);
        // SimpleJDBCConnectionPool connectionPool = new
        // SimpleJDBCConnectionPool(
        // "com.mysql.jdbc.Driver", "jdbc:mysql://localhost/sqlcontainer",
        // "sqlcontainer", "sqlcontainer", 2, 2);

        initDatabase(connectionPool);
        fillDatabase(connectionPool);
        SQLContainer container = new SQLContainer(new FreeformQuery(
                "SELECT * FROM PEOPLE P INNER JOIN ADDRESS A ON P.ADDRESS_ID = A.ID "
                        + "INNER JOIN EMPLOYEES E ON E.PERSON_ID = P.ID "
                        + "INNER JOIN COMPANIES C ON E.COMPANY_ID = C.ID",
                connectionPool));
        return container;
    }

    private void fillDatabase(JDBCConnectionPool connectionPool) {
        Random rnd = new Random(System.currentTimeMillis());
        Connection conn = null;
        try {
            conn = connectionPool.reserveConnection();
            Statement statement = conn.createStatement();
            for (int i = 0; i < 10; i++) {
                String name = "company " + i;
                statement
                        .executeUpdate("INSERT INTO COMPANIES VALUES(DEFAULT, '"
                                + name + "')");
            }
            for (int i = 0; i < 10; i++) {
                String street = "Hameenkatu " + i;
                statement.executeUpdate("INSERT INTO ADDRESS VALUES(DEFAULT, '"
                        + street + "', '20500', 'Turku', 'na', 'Finland')");
            }
            List<String> firstnames = Arrays.asList("Bengt", "Börje", "Ritva",
                    "Anu", "Allan", "Bill", "Leffe", "Ville", "Walter");
            List<String> lastnames = Arrays.asList("Gates", "Meikäläinen",
                    "Svensson", "Hermans", "Poirot");
            for (int i = 0; i < 10; i++) {
                String firstname = firstnames
                        .get(rnd.nextInt(firstnames.size()));
                String lastname = lastnames.get(rnd.nextInt(lastnames.size()));
                String email = firstname + "." + lastname + "@gmail.com";
                int addressId = rnd.nextInt(10);
                statement.executeUpdate("INSERT INTO PEOPLE VALUES(DEFAULT, '"
                        + firstname + "', '" + lastname + "', '040-86766" + i
                        + "', '02-53433" + i + "', '" + email + "', "
                        + addressId + ")");
            }

            for (int i = 0; i < 10; i++) {
                String email = "employee" + i + "@foocompany.com";
                int personId = rnd.nextInt(10);
                int companyId = rnd.nextInt(10);
                String phone = "02-1234" + i;
                statement
                        .executeUpdate("INSERT INTO EMPLOYEES VALUES(DEFAULT, "
                                + personId + ", " + companyId + ", '" + phone
                                + "', '" + email + "')");
            }
            statement.close();
            conn.commit();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            connectionPool.releaseConnection(conn);
        }
    }

    private void initDatabase(JDBCConnectionPool connectionPool) {
        Connection conn = null;
        try {
            conn = connectionPool.reserveConnection();
            Statement statement = conn.createStatement();
            try {
                statement.executeUpdate("DROP TABLE PEOPLE");
                statement.executeUpdate("DROP TABLE ADDRESS");
                statement.executeUpdate("DROP TABLE EMPLOYEES");
                statement.executeUpdate("DROP TABLE COMPANIES");
            } catch (SQLException e) {
            }
            statement
                    .execute("CREATE TABLE PEOPLE "
                            + "(ID INTEGER GENERATED ALWAYS AS IDENTITY, "
                            // + "(ID INTEGER auto_increment, "
                            + "FIRSTNAME VARCHAR(32), LASTNAME VARCHAR(32), "
                            + "MOBILE VARCHAR(20), "
                            + "HOMEPHONE VARCHAR(20), HOMEEMAIL VARCHAR(128), ADDRESS_ID INTEGER, "
                            + "PRIMARY KEY(ID))");
            statement
                    .execute("CREATE TABLE ADDRESS"
                            + "(ID INTEGER GENERATED ALWAYS AS IDENTITY, "
                            // + "(ID INTEGER auto_increment, "
                            + "STREET VARCHAR(32), ZIP VARCHAR(16), CITY VARCHAR(32), STATE VARCHAR(2), "
                            + "COUNTRY VARCHAR(32), PRIMARY KEY(ID))");

            statement.execute("CREATE TABLE EMPLOYEES "
                    + "(ID INTEGER GENERATED ALWAYS AS IDENTITY, "
                    // + "(ID INTEGER auto_increment, "
                    + "PERSON_ID INTEGER, COMPANY_ID INTEGER, "
                    + "WORKPHONE VARCHAR(20), WORKEMAIL VARCHAR(128), "
                    + "PRIMARY KEY(ID))");

            statement.execute("CREATE TABLE COMPANIES"
                    + "(ID INTEGER GENERATED ALWAYS AS IDENTITY, "
                    // + "(ID INTEGER auto_increment, "
                    + "NAME VARCHAR(20), PRIMARY KEY(ID))");
            statement.close();
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            connectionPool.releaseConnection(conn);
        }
    }
}
