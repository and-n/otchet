/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package task;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import ru.rzd.otchet.DAOOtchet;
import ru.rzd.otchet.data.Period;

/**
 *
 * @author ATonevitskiy
 */
public class OtchetTask implements Callable<Void> {

    private List<Period> periods;
    private DAOOtchet dao;
    private HashMap<Calendar, List<Period>> map;
    private Calendar date;

    public OtchetTask(HashMap<Calendar, List<Period>> map, DAOOtchet dao, Calendar date) {
        this.map = map;
        this.dao = dao;
        this.date = date;
    }

    @Override
    public Void call() throws Exception {
        ResultSet res = dao.get30minPeriod(date);
        periods = new ArrayList<>();
        int all = 0;
        while (res.next()) {

            Period p = new Period();

        }
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
