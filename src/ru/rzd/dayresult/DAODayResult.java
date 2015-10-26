package ru.rzd.dayresult;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
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

    public int getID(String surname, String initials) throws SQLException {
        PreparedStatement getID = connection.prepareStatement("select resourceID from Resource where resourceName = ? "
                + " and active=1");
        int id = -1;
        getID.setString(1, initials + " " + surname);
        ResultSet res = getID.executeQuery();
        if (res.next()) {
            id = res.getInt(1);
        }
        if (res.next()) {
            id = -1;
        }
        return id;
    }

    public ResultSet getAgentStates(String name, Calendar date) throws SQLException {
        PreparedStatement getAgentState = connection.prepareStatement("Select  a.eventType, a.eventDateTime from AgentStateDetail a "
                + "inner join Resource r on r.resourceID=a.agentID "
                + "where a.eventDateTime > ? and a.eventDateTime < ? "
                + "and r.resourceName = ? order by a.eventDateTime");
        Timestamp tStart = new Timestamp(date.get(Calendar.YEAR) - 1900, date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH),
                19, 0, 0, 1);
        Timestamp end = new Timestamp(tStart.getTime());
        end.setHours(23);
        end.setMinutes(59);
        end.setSeconds(59);
        end.setNanos(999999);

        tStart.setTime(tStart.getTime() - 86400000L);

        getAgentState.setTimestamp(1, tStart);
        getAgentState.setTimestamp(2, end);
        getAgentState.setString(3, name);
        return getAgentState.executeQuery();
    }

    public ResultSet getCallDetail(String name, Timestamp startTime) throws SQLException {
        PreparedStatement getAgentState = connection.prepareStatement("Select  a.ringTime, a.talkTime, a.holdTime, a.workTime "
                + "from AgentConnectionDetail a "
                + "inner join Resource r on r.resourceID=a.resourceID "
                + "where a.startDateTime > ? and a.startDateTime < ? and "
                + "r.resourceName=?");
        Timestamp end = new Timestamp(startTime.getTime() + 50400000L);

        getAgentState.setTimestamp(1, startTime);
        getAgentState.setTimestamp(2, end);
        getAgentState.setString(3, name);
        return getAgentState.executeQuery();
    }

}
