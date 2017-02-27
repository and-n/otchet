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
import java.util.LinkedHashMap;
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
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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

    public static final int[] PAY_REASON_CODE = new int[]{75, 76};

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
        File f = new File("folder" + File.separator + "ШАБЛОН.xlsx");
        if (f.exists()) {
            try {
                wb = new XSSFWorkbook(new FileInputStream(f));
                Sheet sheet = wb.getSheetAt(1);
                for (int i = 13; i < 37; i++) {
                    Row row = sheet.getRow(i);
                    addPeriodRow(row, periodList.get(i - 13), date, stats.get(i - 13));
                }
                createHead(date, periodList, sheet);
            } catch (IOException ex) {
                Logger.getLogger(OtchetLogic.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NullPointerException ex) {
                ex.printStackTrace();
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
        int ansCalls = period.getCalls() - period.getLostCalls() - period.getIvrCalls();
        c5.setCellValue(ansCalls);
        // два новых столбца 6 и 7
        Cell c6new = row.getCell(6);
        c6new.setCellValue(period.getLostCalls());

        Cell c7new = row.getCell(7);
        c7new.setCellValue(period.getAnswerIn20Sec());

        Cell c6 = row.getCell(8);
        int db6 = ansCalls == 0 ? 0 : new BigDecimal(period.getTalkTime()).divide(new BigDecimal(ansCalls), RoundingMode.HALF_EVEN).intValueExact();
        c6.setCellValue(db6);
        Cell c7 = row.getCell(9);
        int db7 = ansCalls == 0 ? 0 : new BigDecimal(period.getAnswerTime()).divide(new BigDecimal(ansCalls), RoundingMode.HALF_EVEN).intValueExact();
        c7.setCellValue(db7);

        Cell c8 = row.getCell(10);
        int db8 = period.getLostCalls() == 0 ? 0 : new BigDecimal(period.getQueueTime()).divide(new BigDecimal(period.getLostCalls()), RoundingMode.HALF_EVEN).intValueExact();
        c8.setCellValue(db8);
        Cell c9 = row.getCell(11);
        c9.setCellValue(period.getLostCallsIn5Sec());

        // новый 12 столбец IVR
        Cell c12new = row.getCell(12);
//        BigDecimal db12new = period.getCalls() == 0 ? BigDecimal.ZERO : new BigDecimal(period.getIvrCalls()).divide(new BigDecimal(period.getCalls()), 3, RoundingMode.HALF_EVEN);
        BigDecimal db12new = new BigDecimal(period.getIvrCalls());
        c12new.setCellValue(db12new.intValue());

        Cell c10 = row.getCell(13);
        BigDecimal db10 = period.getCalls() == 0 ? BigDecimal.ZERO : new BigDecimal(period.getLostCalls()).divide(new BigDecimal(period.getCalls()), 3, RoundingMode.HALF_EVEN);
        c10.setCellValue(db10.doubleValue());
        Cell c11 = row.getCell(14);
        BigDecimal db11 = period.getCalls() == 0 ? BigDecimal.ZERO : new BigDecimal(period.getLostCalls() - period.getLostCallsIn5Sec()).divide(new BigDecimal(period.getCalls()), 3, RoundingMode.HALF_EVEN);
        c11.setCellValue(db11.doubleValue());

        Cell c12 = row.getCell(15);
        BigDecimal db12 = period.getCalls() - period.getIvrCalls() == 0 ? BigDecimal.ZERO : new BigDecimal(period.getAnswerIn20Sec())
                .divide(new BigDecimal(period.getCalls() - period.getIvrCalls()), 3, RoundingMode.HALF_EVEN);
        c12.setCellValue(db12.doubleValue());
    }

    private void addPeriodRow(Row row, Period period, Calendar date, Statist60min stat) {
        addPeriodRow(row, period, date);
        BigDecimal ap = stat.getAgentWorkTime60min();
        int ansCalls = period.getCalls() - period.getLostCalls() - period.getIvrCalls();
        Cell c13 = row.getCell(16);
        c13.setCellValue(stat.getAgentPayedTime60min().doubleValue());

        Cell c14 = row.getCell(17);
        c14.setCellValue(ap.doubleValue());

        Cell c15 = row.getCell(18);

        BigDecimal db15 = ap.intValue() == 0 ? BigDecimal.ZERO : new BigDecimal(ansCalls).divide(ap, 3, RoundingMode.HALF_EVEN);
        c15.setCellValue(db15.doubleValue());
    }

    private void createHead(Calendar date, List<Period> periodList, Sheet sheet) {
        Cell c1 = sheet.getRow(0).getCell(2);
        c1.setCellValue(date);
        int all = 0;
        int ans20 = 0;
        int lost5 = 0;
        int lost = 0;
        int talk = 0;
        int ivr = 0;
        for (Period p : periodList) {
            all += p.getCalls();
            lost += p.getLostCalls();
            lost5 += p.getLostCallsIn5Sec();
            ans20 += p.getAnswerIn20Sec();
            talk += p.getTalkTime();
            ivr += p.getIvrCalls();
        }
        Cell c2 = sheet.getRow(1).getCell(2);
        c2.setCellValue(all);
        Cell c3 = sheet.getRow(2).getCell(2);
        c3.setCellValue(all - lost - ivr);
        Cell c4 = sheet.getRow(3).getCell(2);
        c4.setCellValue(ans20);

        Cell c5 = sheet.getRow(4).getCell(2);
        BigDecimal bd1 = (all - ivr) == 0 ? BigDecimal.ZERO : new BigDecimal(ans20).divide(new BigDecimal(all - ivr), 3, RoundingMode.HALF_EVEN);
        c5.setCellValue(bd1.floatValue());
        Cell c6 = sheet.getRow(5).getCell(2);
        c6.setCellValue(lost5);

        Cell c7 = sheet.getRow(6).getCell(2);
        BigDecimal bd2 = all == 0 ? BigDecimal.ZERO : new BigDecimal(lost).divide(new BigDecimal(all), 3, RoundingMode.HALF_EVEN);
        c7.setCellValue(bd2.floatValue());

        Cell c8 = sheet.getRow(7).getCell(2);
        BigDecimal bd3 = all == 0 ? BigDecimal.ZERO : new BigDecimal(lost - lost5).divide(new BigDecimal(all), 3, RoundingMode.HALF_EVEN);
        c8.setCellValue(bd3.floatValue());

        Cell c9 = sheet.getRow(8).getCell(2);
        BigDecimal bd4 = (all - lost) == 0 ? BigDecimal.ZERO : new BigDecimal(talk).divide(new BigDecimal(all - lost), 3, RoundingMode.HALF_EVEN);
        c9.setCellValue(bd4.floatValue());
    }

    private String getFileName(String ITOG_SUTOK, Calendar date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd_MM_yyyy");
        return ITOG_SUTOK + sdf.format(date.getTime());
    }

    public void createReport(Calendar date, Form f) throws SQLException, FileNotFoundException, IOException {
        List<Period> report = getReportByDay(date);
        List<Statist60min> stats = getStatsByDay(date);
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
            FormulaEvaluator formulaEvaluator = wb.getCreationHelper().createFormulaEvaluator();
            formulaEvaluator.evaluateAll();
            FileOutputStream fos = new FileOutputStream(folder + File.separator + fileName + ".xlsx", false);
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

    private List<Statist60min> getStatsByDay(Calendar cal) throws SQLException {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(cal.getTimeInMillis());
        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);

        List<Statist60min> stats = new ArrayList<>();
        if (spravka == null) {
            spravka = new DAOOtchet();
        }

        LinkedHashMap<Integer, Pair<AgentState, Long>> statesMap = getStartStats(date);

        Calendar endOfPeriod = Calendar.getInstance();
        for (int i = 0; i < 24; i++) {
            endOfPeriod.setTimeInMillis(date.getTimeInMillis() + 3600000L);
            ResultSet rs = spravka.getAgentStatePer60min(date);
            Statist60min  statist60min = new Statist60min();
            while (rs.next()) {
                AgentState type = AgentState.getByCode(rs.getInt(1));
                Integer id = new Integer(rs.getInt(3));
                Timestamp time = rs.getTimestamp(2);
                Pair<AgentState, Long> p = statesMap.get(id);

                if (type.equals(AgentState.LogIn)) {
                    statesMap.put(id, new Pair<>(type, time.getTime()));
                } else if (type.equals(AgentState.LogOut)) {
                    if (p != null) {
                        if (!p.getL().equals(AgentState.NotReady) || !p.getL().equals(AgentState.LogIn)) {
                            statist60min.addWorkTime(time.getTime() - p.getR());
                        }
                        statesMap.remove(id);
                    }
                } else if (type.equals(AgentState.NotReady)) {
                    if (p != null) {
                        // если время от логина не считать рабочим, то добавить логин.
                        if (!p.getL().equals(AgentState.NotReady) || !p.getL().equals(AgentState.LogIn)) {
                            statist60min.addWorkTime(time.getTime() - p.getR());
                        }
                        type = isPayedReason(rs.getInt(4)) ? AgentState.PayedNotReady : type;
                        statesMap.put(id, new Pair<AgentState, Long>(type, time.getTime()));
                    }
                } else {
                    if (statesMap.get(id) != null) {
                        // если время от логина не считать рабочим, то добавить логин.
                        if (p.getL().equals(AgentState.PayedNotReady)) {
                            statist60min.addPayedTime(time.getTime() - p.getR());
                        } else if (!p.getL().equals(AgentState.NotReady) || !p.getL().equals(AgentState.LogIn)
                                || !p.getL().equals(AgentState.PayedNotReady)) {
                            statist60min.addWorkTime(time.getTime() - p.getR());
                        }
                        statesMap.put(id, new Pair<AgentState, Long>(type, time.getTime()));
                    }
                }
                // removeLogOuts(statesMap);
            }
            for (int key : statesMap.keySet()) {
                if (!statesMap.get(key).equals(AgentState.NotReady) || !statesMap.get(key).equals(AgentState.LogIn)) {
                    statist60min.addWorkTime(endOfPeriod.getTimeInMillis() - statesMap.get(key).getR());
                }
            }
            setTimeToAll(statesMap, endOfPeriod);
            stats.add(statist60min);
            date.setTimeInMillis(date.getTimeInMillis() + 3600000L);

        }
        return stats;
    }

    // работает
    private LinkedHashMap<Integer, Pair<AgentState, Long>> getStartStats(Calendar date) throws SQLException {
        ResultSet res = spravka.getStartAgentState(date);
        LinkedHashMap<Integer, Pair<AgentState, Long>> states = new LinkedHashMap<>();
        while (res.next()) {
            Integer key = new Integer(res.getInt(3));
            AgentState as = AgentState.getByCode(res.getInt(1));
            Timestamp time = res.getTimestamp(2);
            Pair<AgentState, Long> p = new Pair<AgentState, Long>(as, new Long(time.getTime()));
            states.put(key, p);
        }
        setTimeToAll(states, date);
        return states;
    }

    private void setTimeToAll(Map<Integer, Pair<AgentState, Long>> states, Calendar date) {
        Set<Integer> s = new HashSet<Integer>(states.keySet());
        for (Integer i : s) {
            Pair p = states.get(i);
            p.setR(date.getTimeInMillis());
            states.put(i, p);
        }
    }

    private boolean isPayedReason(int code) {
        for (int i = 0; i < PAY_REASON_CODE.length; i++) {
            if (PAY_REASON_CODE[i] == code) {
                return true;
            }
        }
        return false;
    }

}
