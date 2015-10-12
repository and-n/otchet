/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.rzd.otchet;

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

/**
 *
 * @author ATonevitskiy
 */
public class DAOOtchet {

    private Connection connection;

    public DAOOtchet() {
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
            simpleReq = connection.prepareStatement("SELECT COUNT(*) FROM RtICDStatistics");
        } catch (SQLException ex) {
            Logger.getLogger(DAOOtchet.class.getName()).log(Level.SEVERE, null, ex);
            int i = JOptionPane.showConfirmDialog(null, "Нет соединения с базой. Переподключиться?", "Database error", JOptionPane.YES_NO_OPTION);
            if (i == JOptionPane.YES_OPTION) {
                connect();
            } else {
                System.exit(1);
            }

        }
    }

    private PreparedStatement simpleReq;

    public ResultSet getSimpleRequest() throws SQLException {
        ResultSet rs = simpleReq.executeQuery();
        return rs;
    }

    public ResultSet get30minPeriod(Calendar date) throws SQLException {

        PreparedStatement getPeriod = connection.prepareCall("Select q.queueTime, a.ringTime, a.talkTime from ContactCallDetail c"
                + " left join ContactQueueDetail q ON c.sessionID = q.sessionID "
                + " left join AgentConnectionDetail a ON q.sessionID =a.sessionID  where c.startDateTime > ? and c.startDateTime < ? "
                + "and c.applicationID=0");
        getPeriod.clearParameters();
        Timestamp tStart = new Timestamp(date.get(Calendar.YEAR) - 1900, date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH),
                date.get(Calendar.HOUR_OF_DAY), date.get(Calendar.MINUTE), date.get(Calendar.SECOND), 1);
        Timestamp end = new Timestamp(tStart.getTime());
        end.setMinutes(end.getMinutes() + 29);
        end.setSeconds(59);
        end.setNanos(999999);
        getPeriod.setTimestamp(1, tStart);
        getPeriod.setTimestamp(2, end);
        System.out.println("Date " + tStart + "  END " + end);
        ResultSet res = getPeriod.executeQuery();
        return res;
    }

}
