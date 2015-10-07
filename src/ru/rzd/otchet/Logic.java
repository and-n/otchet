/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.rzd.otchet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
import javax.swing.JOptionPane;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import ru.rzd.otchet.data.Period;
import task.OtchetTask;

/**
 *
 * @author Andrey
 */
public class Logic {

    /**
     * Таймаут запроса данных из базы за пол часа.
     */
    public static final int REQUEST_TIMEOUT = 25;

    /**
     * Запрос из базы данных за сутки с разбивкой по пол часа.
     *
     * @param calendar
     * @return
     * @throws SQLException
     */
    public List<Period> getReportByDay(Calendar calendar) throws SQLException {
        DAOOtchet spravka = new DAOOtchet();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
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
        List<Period> periods = new ArrayList<>();
        try {
            List<Future<Period>> flist = executor.invokeAll(taskList, REQUEST_TIMEOUT, TimeUnit.SECONDS);
            for (Future f : flist) {
                Period p = (Period) f.get();
                periods.add(p);
                System.out.println("RESULT " + p);
            }
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(Logic.class.getName()).log(Level.SEVERE, null, ex);
        }
        return periods;
    }

    /**
     * Кладем данные в шаблонную xls- справку
     *
     * @param date дата данных в справке.
     * @param periodList Лист получасовок.
     * @return
     */
    public Workbook createPeriodInSpravka(Calendar date, List<Period> periodList) {
        Workbook wb = null;
        File f = new File("folder" + File.separator + "ШАБЛОН.xls");

        if (f.exists()) {
            System.out.println("НАШЕЛ ШАБЛОН");
            try {
                wb = new HSSFWorkbook(new FileInputStream(f));
                Sheet sheet = wb.getSheetAt(0);
                System.out.println("ROWWW " + sheet.getSheetName());
                for (int i = 13; i < 61; i++) {
                    Row row = sheet.getRow(i);
                    addPeriodRow(row, periodList.get(i - 13), date);
                }

            } catch (IOException ex) {
                Logger.getLogger(Logic.class.getName()).log(Level.SEVERE, null, ex);
            }
            return wb;
        } else {
            JOptionPane.showMessageDialog(null, "не найден файл шаблона справки\n"
                    + "Файл \"ШАБЛОН.xls\" должен лежать в папке folder");
            System.exit(1);
        }

        return wb;
    }
    private final DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
    private final String[] dateNames = {"Воскресенье", "Понедельник", "Вторник",
        "Среда", "Четверг", "Пятница", "Суббота",};

    private void addPeriodRow(Row row, Period period, Calendar date) {
        Cell c2 = row.getCell(0);
        c2.setCellValue(dateNames[Calendar.DAY_OF_WEEK - 1]);
        Cell c1 = row.getCell(1);
        c1.setCellValue(df.format(date.getTime()));
        Cell c4 = row.getCell(4);
        c4.setCellValue(period.getCalls());
        Cell c5 = row.getCell(5);
        int ansCalls = period.getCalls() - period.getLostCalls();
        c5.setCellValue(ansCalls);
        Cell c6 = row.getCell(6);
        int db6 = ansCalls == 0 ? 0 : new BigDecimal(period.getTalkTime()).divide(new BigDecimal(ansCalls), RoundingMode.HALF_EVEN).intValueExact();
        c6.setCellValue(db6);
        Cell c7 = row.getCell(7);
        int db7 = ansCalls == 0 ? 0 : new BigDecimal(period.getAnswerTime()).divide(new BigDecimal(ansCalls), RoundingMode.HALF_EVEN).intValueExact();
        c7.setCellValue(db7);

        Cell c8 = row.getCell(8);
        int db8 = ansCalls == 0 ? 0 : new BigDecimal(period.getQueueTime()).divide(new BigDecimal(period.getCalls()), RoundingMode.HALF_EVEN).intValueExact();
        c8.setCellValue(db8);

//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
