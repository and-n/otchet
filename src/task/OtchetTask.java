/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package task;

import java.sql.ResultSet;
import java.util.Calendar;
import java.util.concurrent.Callable;
import ru.rzd.otchet.DAOOtchet;
import ru.rzd.otchet.data.Period;

/**
 *
 * @author ATonevitskiy
 */
public class OtchetTask implements Callable<Period> {

    private DAOOtchet dao;
    private Calendar date;

    public OtchetTask(DAOOtchet dao, Calendar date) {
        this.dao = dao;
        this.date = date;
    }

    @Override
    public Period call() throws Exception {
        ResultSet res = dao.get30minPeriod(date);
        Period p = new Period();
        while (res.next()) {
            int qt = res.getInt(1);
            int at = res.getInt(2);
            int tt = res.getInt(3);
            p.addCall(at == 0 && tt == 0, qt, tt, at);
        }
        return p;
    }

}
