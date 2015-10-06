/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.rzd.otchet;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import ru.rzd.otchet.data.Period;
import task.OtchetTask;

/**
 *
 * @author Andrey
 */
public class Logic {

    public static final int REQUEST_TIMEOUT = 25;

    /**
     * Запрос данных из базы и формирование справки.
     *
     */
    public void getReportByDay(Calendar calendar) throws SQLException {
        DAOOtchet spravka = new DAOOtchet();
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 001);
        ExecutorService executor = Executors.newFixedThreadPool(5);
        Collection<Callable<Period>> taskList = new ArrayList<>();
        for (int i = 0; i < 48; i++) {
            Calendar newCal = Calendar.getInstance();
            newCal.setTimeInMillis(
                    calendar.getTimeInMillis() + (i * 1800000));
            OtchetTask task = new OtchetTask(spravka, newCal);
            taskList.add(task);
        }
        try {
            List<Future<Period>> flist = executor.invokeAll(taskList, REQUEST_TIMEOUT, TimeUnit.SECONDS);
            for (Future f : flist) {
                Period res = (Period) f.get();
                System.out.println("RESULT " + res);
            }
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(Logic.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
