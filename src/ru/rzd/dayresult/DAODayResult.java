package ru.rzd.dayresult;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import ru.rzd.otchet.Form;
import ru.rzd.otchet.data.DAOOtchet;

/**
 *
 * @author ATonevitskiy
 */
public class DAODayResult {

    private Connection connection;

    public DAODayResult() {
        if (connection == null) {
            connect();
        }
    }

    private void connect() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException ex) {
            JOptionPane.showMessageDialog(null, "Нет драйвера базы!!!", null, JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(DAOOtchet.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(0);
        }
        // 10.58.1.96
        String connectionUrl1 = "jdbc:sqlserver://10.58.50.6\\CRSSQL;"
                + "databaseName=db_cra;user=SQLview;password=QwErFdSa1234;";
        try {
            connection = DriverManager.getConnection(connectionUrl1);
//            getAgentStatePer30min = connection.prepareCall("Select  eventType, eventDateTime, agentID from AgentStateDetail "
//                    + "where eventDateTime > ? and eventDateTime < ? and "
//                    + "(eventType=2 or eventType=3 or eventType=7) order by eventDateTime");
        } catch (SQLException ex) {
            if (!Form.ISCONSOLE) {
                Logger.getLogger(DAOOtchet.class.getName()).log(Level.SEVERE, null, ex);
                int i = JOptionPane.showConfirmDialog(null, "Нет соединения с базой. Переподключиться?", "Database error", JOptionPane.YES_NO_OPTION);
                if (i == JOptionPane.YES_OPTION) {
                    connect();
                } else {
                    System.exit(1);
                }
            } else {
                System.out.println("Нет коннекта к базе! ");
                connect();
            }
        }
    }

    public int getID(String surname, String name) throws SQLException {
        PreparedStatement getID = connection.prepareStatement("select resourceID from Resource where resourceName like ? "
                + " and active=1");
        int id = -1;
        getID.setString(1, name + " " + surname);
        ResultSet res = getID.executeQuery();
        if (res.next()) {
            id = res.getInt(1);
        }
        if (res.next()) {
            id = -1;
        }
        return id;
    }

}
