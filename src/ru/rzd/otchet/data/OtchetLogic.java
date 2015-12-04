package ru.rzd.otchet.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import ru.rzd.otchet.Form;
import static ru.rzd.otchet.Form.ISCONSOLE;
import ru.rzd.otchet.Pair;

/**
 *
 * @author Andrey
 */
public class OtchetLogic {

    /**
     * Таймаут запроса данных из базы за пол часа.
     */
    public static final int REQUEST_TIMEOUT = 25;

    public static final String ITOG_SUTOK = "Справка по итогам суток ";
    private DAOOtchet spravka;

    /**
     * Запрос из базы данных за сутки с разбивкой по пол часа.
     *
     * @param calendar
     * @return
     * @throws SQLException
     */
    public List<Period> getReportByDay(Calendar calendar) throws SQLException {
        if (spravka == null) {
            spravka = new DAOOtchet();
        }
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 001);
        ExecutorService executor = Executors.newFixedThreadPool(4);
        List<Future<Period>> flist = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            Calendar newCal = Calendar.getInstance();
            newCal.setTimeInMillis(
                    calendar.getTimeInMillis() + (i * 3600000));
            OtchetTask task = new OtchetTask(spravka, newCal);
            flist.add(executor.submit(task));
        }
        List<Period> periods = new ArrayList<>();
        try {
//            List<Future<Period>> flist = executor.invokeAll(taskList, REQUEST_TIMEOUT, TimeUnit.SECONDS);
            for (Future f : flist) {
                Period p = (Period) f.get();
                System.out.println("per " + p);
                periods.add(p);
            }
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(OtchetLogic.class.getName()).log(Level.SEVERE, null, ex);
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
    public Workbook createPeriodInSpravka(Calendar date, List<Period> periodList, List<Statist60min> stats) {
        Workbook wb = null;
        File f = new File("folder" + File.separator + "ШАБЛОН.xls");
        if (f.exists()) {
            try {
                wb = new HSSFWorkbook(new FileInputStream(f));
                Sheet sheet = wb.getSheetAt(1);
                for (int i = 13; i < 37; i++) {
                    Row row = sheet.getRow(i);
                    addPeriodRow(row, periodList.get(i - 13), date, stats.get(i - 13));
                }
                createHead(date, periodList, sheet);
            } catch (IOException ex) {
                Logger.getLogger(OtchetLogic.class.getName()).log(Level.SEVERE, null, ex);
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

    private void addPeriodRow(Row row, Period period, Calendar date, Statist60min stat) {
        addPeriodRow(row, period, date);
        BigDecimal ap = stat.getAgentPer60min();
        int ansCalls = period.getCalls() - period.getLostCalls();
        Cell c13 = row.getCell(13);
        c13.setCellValue(ap.doubleValue());
        Cell c14 = row.getCell(14);
        c14.setCellValue(ap.doubleValue());

        Cell c15 = row.getCell(15);
        BigDecimal db15 = ansCalls == 0 ? BigDecimal.ZERO : new BigDecimal(ansCalls).divide(ap, 3, RoundingMode.HALF_EVEN)
                .divide(new BigDecimal(2));
        c15.setCellValue(db15.doubleValue());
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
        BigDecimal bd1 = new BigDecimal(ans20).divide(new BigDecimal(all), 3, RoundingMode.HALF_EVEN);
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

    private String getFileName(String ITOG_SUTOK, Calendar date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd_MM_yyyy");
        return ITOG_SUTOK + sdf.format(date.getTime());
    }

    public void createReport(Calendar date, Form f) throws SQLException, FileNotFoundException, IOException {
        List<Period> report = getReportByDay(date);
        List<Statist60min> stats = getStatsByDay(date, null);
        Workbook wb = createPeriodInSpravka(date, report, stats);
        String fileName = getFileName(OtchetLogic.ITOG_SUTOK, date);
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

    public void createReport(Calendar date, Calendar end, Form f) throws SQLException, FileNotFoundException, IOException {
        if (date != null && end != null) {
            boolean b = ISCONSOLE;
            ISCONSOLE = true;
            for (; date.before(end) || date.equals(end);
                    date.setTimeInMillis(date.getTimeInMillis() + 86400000L)) {
                createReport(date, f);
            }
            ISCONSOLE = b;
        }

    }

    private List<Statist60min> getStatsByDay(Calendar cal, Map<Integer, Pair<AgentState, Long>> statesMap) throws SQLException {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(cal.getTimeInMillis());
        List<Statist60min> stats = new ArrayList<>();
        if (spravka == null) {
            spravka = new DAOOtchet();
        }
        if (statesMap == null) {
            statesMap = getStartStats(date);
        }

        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);
        Calendar endOfPeriod = Calendar.getInstance();
        for (int i = 0; i < 24; i++) {
            endOfPeriod.setTimeInMillis(date.getTimeInMillis() + 3600000L);
            ResultSet rs = spravka.getAgentStatePer60min(date);
            int worked = getWorked(statesMap);
            Statist60min sm = new Statist60min(worked);
            while (rs.next()) {
                AgentState type = AgentState.getByCode(rs.getInt(1));
                Integer id = rs.getInt(3);
                Timestamp time = rs.getTimestamp(2);
                Pair<AgentState, Long> pair = new Pair(type, time.getTime());
                if (type.equals(AgentState.LogIn)) {
//                    sm.addWorkAgent();
                    if (statesMap.get(id) == null) {
                        sm.addWorkTime(date.getTimeInMillis() - time.getTime());
                    }
                    statesMap.put(id, pair);
                } else if (type.equals(AgentState.LogOut)) {
                    if (statesMap.get(id) != null && !statesMap.get(id).equals(AgentState.NotReady)) {
                        sm.addWorkTime(time.getTime() - endOfPeriod.getTimeInMillis());
                    }
                    statesMap.remove(id);
                } else if (type.equals(AgentState.Ready)) {
                    if (statesMap.get(id) == null || (!statesMap.get(id).equals(AgentState.Ready) && !statesMap.get(id).equals(AgentState.LogIn))) {
                        sm.addWorkTime(endOfPeriod.getTimeInMillis() - time.getTime());
                    }
                    statesMap.put(id, pair);
                } else if (type.equals(AgentState.NotReady)) {
//                    sm.removeWorkAgent();
                    sm.addWorkTime(time.getTime() - endOfPeriod.getTimeInMillis());
                    statesMap.put(id, pair);
                }
                // removeLogOuts(statesMap);
            }
            stats.add(sm);
            date.setTimeInMillis(date.getTimeInMillis() + 3600000L);
        }

        return stats;
    }

    // работает
    private Map<Integer, Pair<AgentState, Long>> getStartStats(Calendar date) throws SQLException {
        ResultSet res = spravka.getStartAgentState(date);
        Map<Integer, Pair<AgentState, Long>> states = new HashMap<>();
        while (res.next()) {
            Integer key = new Integer(res.getInt(1));
            Timestamp time = res.getTimestamp(2);
            int as = res.getInt(3);
            if (as != AgentState.LogOut.ordinal() || as != AgentState.NotReady.ordinal() // || states.containsKey(key)
                    ) {
                states.put(key, new Pair<AgentState, Long>(AgentState.getByCode(as), time.getTime()));
            }
        }
//        AgentState as = states.get(i).getL();

        Set<Integer> s = new HashSet<Integer>(states.keySet());
        for (Integer i : s) {
            AgentState as = states.get(i).getL();
            if (as.equals(AgentState.LogOut)) {
                states.remove(i);
            }
        }
        return states;
    }

//    @Deprecated
    private int getWorked(Map<Integer, Pair<AgentState, Long>> statesMap) {
        int l = 0;
        for (Integer i : statesMap.keySet()) {
            if (!statesMap.get(i).getL().equals(AgentState.NotReady)) {
                ++l;
            }
        }
        System.out.println("WORKED " + l);
        return l;
    }

}
