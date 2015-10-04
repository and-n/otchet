/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.rzd.otchet;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Andrey
 */
public class Logic {

    /**
     * Запрос данных из базы и формирование справки.
     *
     */
    public void getReportByDay(Calendar calendar) {
        DAOOtchet spravka = new DAOOtchet();
        ResultSet res = spravka.getSimpleRequest();
        if (res != null) {
            try {
                res.next();
                System.out.println("Есть контакт " + res.getInt(1));
            } catch (SQLException ex) {
                Logger.getLogger(Form.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            
            
            
            
        } else {
            System.err.println("res=null");
        }
    }

}
