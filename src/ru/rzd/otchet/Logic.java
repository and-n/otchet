package ru.rzd.otchet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
import static ru.rzd.otchet.Form.ISCONSOLE;
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

    public static final String ITOG_SUTOK = "Справка по итогам суток ";

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
                System.out.println("per " + p);
                periods.add(p);
            }
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(Logic.class.getName()).log(Level.SEVERE, null, ex);
        }
        executor.shutdown();
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
            try {
                wb = new HSSFWorkbook(new FileInputStream(f));
                Sheet sheet = wb.getSheetAt(0);
                for (int i = 13; i < 61; i++) {
                    Row row = sheet.getRow(i);
                    addPeriodRow(row, periodList.get(i - 13), date);
                }
                createHead(date, periodList, sheet);
            } catch (IOException ex) {
                Logger.getLogger(Logic.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NullPointerException ex) {
                JOptionPane.showMessageDialog(null, "Неверный файл шаблона!!!");
                System.exit(1);
            }
            return wb;
        } else {
            JOptionPane.showMessageDialog(null, "Не найден файл шаблона справки\n"
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
        c2.setCellValue(dateNames[date.get(Calendar.DAY_OF_WEEK) - 1]);
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
        Cell c9 = row.getCell(9);
        c9.setCellValue(period.getLostCallsIn5Sec());
        Cell c10 = row.getCell(10);
        BigDecimal db10 = ansCalls == 0 ? BigDecimal.ZERO : new BigDecimal(period.getLostCalls()).divide(new BigDecimal(period.getCalls()), 3, RoundingMode.HALF_EVEN);
        c10.setCellValue(db10.doubleValue());
        Cell c11 = row.getCell(11);
        BigDecimal db11 = ansCalls == 0 ? BigDecimal.ZERO : new BigDecimal(period.getLostCalls() - period.getLostCallsIn5Sec()).divide(new BigDecimal(period.getCalls()), 3, RoundingMode.HALF_EVEN);
        c11.setCellValue(db11.doubleValue());

        Cell c12 = row.getCell(12);
        BigDecimal db12 = ansCalls == 0 ? BigDecimal.ZERO : new BigDecimal(period.getAnswerIn20Sec()).divide(new BigDecimal(ansCalls), 3, RoundingMode.HALF_EVEN);
        c12.setCellValue(db12.doubleValue());
    }

    private void createHead(Calendar date, List<Period> periodList, Sheet sheet) {
        Cell c1 = sheet.getRow(0).getCell(2);
        c1.setCellValue(date);
        int all = 0;
        int ans20 = 0;
        int lost5 = 0;
        int lost = 0;
        for (Period p : periodList) {
            all += p.getCalls();
            lost += p.getLostCalls();
            lost5 += p.getLostCallsIn5Sec();
            ans20 += p.getAnswerIn20Sec();
        }
        Cell c2 = sheet.getRow(1).getCell(2);
        c2.setCellValue(all);
        Cell c3 = sheet.getRow(2).getCell(2);
        c3.setCellValue(all - lost);
        Cell c4 = sheet.getRow(3).getCell(2);
        c4.setCellValue(ans20);

        Cell c5 = sheet.getRow(4).getCell(2);
        BigDecimal bd1 = new BigDecimal(ans20).divide(new BigDecimal(all - lost), 3, RoundingMode.HALF_EVEN);
        c5.setCellValue(bd1.floatValue());
        Cell c6 = sheet.getRow(5).getCell(2);
        c6.setCellValue(lost5);

        Cell c7 = sheet.getRow(6).getCell(2);
        BigDecimal bd2 = new BigDecimal(lost).divide(new BigDecimal(all), 3, RoundingMode.HALF_EVEN);
        c7.setCellValue(bd2.floatValue());

        Cell c8 = sheet.getRow(7).getCell(2);
        BigDecimal bd3 = new BigDecimal(lost - lost5).divide(new BigDecimal(all), 3, RoundingMode.HALF_EVEN);
        c8.setCellValue(bd3.floatValue());
    }

    public String getFileName(String ITOG_SUTOK, Calendar date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd_MM_yyyy");
        return ITOG_SUTOK + sdf.format(date.getTime());
    }

    public void createReport(Calendar date, Form f) throws SQLException, FileNotFoundException, IOException {
        List<Period> report = getReportByDay(date);
        Workbook wb = createPeriodInSpravka(date, report);
        String fileName = getFileName(Logic.ITOG_SUTOK, date);
        String folder = "";
        if (ISCONSOLE) {
            folder = "DaylyReports";
            new File(folder).mkdir();
        } else {
            folder = f.selectSaveFile();
        }
        if (!folder.isEmpty()) {
            FileOutputStream fos = new FileOutputStream(folder + File.separator + fileName + ".xls", false);
            wb.write(fos);
            wb.close();
            fos.close();
        }
    }

}
