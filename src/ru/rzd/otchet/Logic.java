/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.rzd.otchet;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import ru.rzd.otchet.data.Period;

/**
 *
 * @author Andrey
 */
public class Logic {

    /**
     * Запрос данных из базы и формирование справки.
     *
     */
    public void getReportByDay(Calendar calendar) throws SQLException {
        DAOOtchet spravka = new DAOOtchet();

//        ResultSet res = spravka.getSimpleRequest();
        ResultSet res = spravka.get30minPeriod(calendar);
        if (res != null) {
            try {
                System.out.println("next " + res.next());
                System.out.println("Есть контакт " + res.getInt(1));
                int i = 1;
                while (res.next()) {
                    i++;
                }
                System.out.println("RES " + i);
            } catch (SQLException ex) {
                Logger.getLogger(Form.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else {
            System.err.println("res=null");
        }
    }

}
