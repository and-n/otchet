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
            try {
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            } catch (ClassNotFoundException ex) {
                JOptionPane.showMessageDialog(null, "Нет драйвера базы!!!", null, JOptionPane.ERROR_MESSAGE);
                Logger.getLogger(DAOOtchet.class.getName()).log(Level.SEVERE, null, ex);
            }
            String connectionUrl1 = "jdbc:sqlserver://10.58.50.6\\CRSSQL;"
                    + "databaseName=db_cra;user=SQLview;password=QwErFdSa1234;";
            try {
                //            String connectionUrl2 = "jdbc:sqlserver://10.58.3.168;"
//                    + "databaseName=CallCenter;user=Client;password=123456789;";
                connection = DriverManager.getConnection(connectionUrl1);
                simpleReq = connection.prepareStatement("SELECT COUNT(*) FROM RtICDStatistics");
            } catch (SQLException ex) {
                Logger.getLogger(DAOOtchet.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private PreparedStatement simpleReq;

    public ResultSet getSimpleRequest() {
        ResultSet rs = null;
        try {
            rs = simpleReq.executeQuery();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Проверка не пройдена.");
            Logger.getLogger(DAOOtchet.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rs;
    }

    public ResultSet getPeriod(Calendar date) {

        throw new NoSuchMethodError();
    }

}
