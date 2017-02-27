package ru.rzd.dayresult;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.concurrent.Callable;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import ru.rzd.otchet.data.AgentState;

/**
 *
 * @author ATonevitskiy
 */
public class DayResultTask implements Callable<Void> {

    private Operator operator;
    private DAODayResult dao;
    private Row row;
    private Calendar date;

    public DayResultTask(Operator operator, DAODayResult dao, Row row, Calendar date) {
        super();
        this.dao = dao;
        this.operator = operator;
        this.row = row;
        this.date = date;
    }

    @Override
    public Void call() throws Exception {
        String iname = operator.getInitials() + " " + operator.getSurname();
        String login = dao.findLogin(iname);
//        ResultSet rs = dao.getAgentStates(iname, date);
                ResultSet rs = dao.getAgentStatesWithLogin(login, date);
        while (rs.next()) {
            AgentState state = AgentState.getByCode(rs.getInt(1));
            Timestamp time = rs.getTimestamp(2);
            operator.addState(state, time);
        }
        System.out.println("HI " + iname + " " + operator.getLoginTime());
        if (operator.getLoginTime() != null) {
            ResultSet rset = dao.getCallDetail(iname, operator.getLoginTime());
            Timestamp last = new Timestamp(1);
            while (rset.next()) {
                int ring = rset.getInt(1);
                int talk = rset.getInt(2);
                int hold = rset.getInt(3);
                int work = rset.getInt(4);
                last = rset.getTimestamp(5);
                operator.addTimes(ring, talk, hold, work);
            }
            if (operator.getLastState().equals(AgentState.LogOut) && operator.getLastTime().after(last)) {
                addRows();
            }
        }
        return null;
    }

    private Operator task(Calendar start) {

        return null;
    }

    private void addRows() {
        System.out.println("addrow " + operator.getSurname()
                + " ca" + operator.getAllCalls() + operator.getStaffTime());
        if (row != null && operator.getAllCalls() > 0 && operator.getStaffTime() > 0) {
            Cell cell;
            BigDecimal staffTime = new BigDecimal(operator.getStaffTime());
            BigDecimal allCalls = new BigDecimal(operator.getAllCalls());

            // stafftime
            cell = row.getCell(1);
            cell.setCellValue(staffTime.divide(new BigDecimal(3600), 2, RoundingMode.HALF_EVEN).doubleValue());
            // время диалога %
            cell = row.getCell(2);
            cell.setCellValue(new BigDecimal(operator.getTalkTime()).divide(staffTime, 3, RoundingMode.HALF_EVEN).doubleValue());
            // время ожидания звонка.
            cell = row.getCell(3);
            cell.setCellValue(new BigDecimal(operator.getWaitTime()).divide(staffTime, 3, RoundingMode.HALF_EVEN).doubleValue());
            // переведенные звонки
            // cell = row.getCell(4);
//            cell.setCellValue(operator.getWaitTime() / operator.getStaffTime()*100);
            // % UTZ
            cell = row.getCell(5);
            if (staffTime.compareTo(BigDecimal.ZERO) == 1) {
                cell.setCellValue(new BigDecimal(operator.getStaffTime() - operator.getUnpaidTime())
                        .divide(staffTime, 3, RoundingMode.HALF_EVEN).doubleValue());
            } else {
                cell.setCellValue(0);
            }
            cell = row.getCell(6);
            if (staffTime.compareTo(BigDecimal.ZERO) == 1) {
                // Поствызовная обработка, %
                cell.setCellValue(new BigDecimal(operator.getWorkTime()).divide(staffTime, 3, RoundingMode.HALF_EVEN).doubleValue());
            } else {
                cell.setCellValue(0);
            }
            cell = row.getCell(7);
            if (staffTime.compareTo(BigDecimal.ZERO) == 1) {
                // Ring Time (среднее время на 1 звонок), сек
                cell.setCellValue(new BigDecimal(operator.getHoldTime()).divide(staffTime, 3, RoundingMode.HALF_EVEN).doubleValue());
            } else {
                cell.setCellValue(0);
            }
            cell = row.getCell(8);
            if (allCalls.compareTo(BigDecimal.ZERO) == 1) {
                // Ring Time (среднее время на 1 звонок), сек
                cell.setCellValue(new BigDecimal(operator.getRingTime()).divide(allCalls, 2, RoundingMode.HALF_EVEN).doubleValue());
            } else {
                cell.setCellValue(0);
            }
            cell = row.getCell(9);
            if (staffTime.compareTo(BigDecimal.ZERO) == 1) {
                // Состояние "недоступен для приема входящих звонков" (Обед+Перерыв).
                cell.setCellValue(new BigDecimal(operator.getUnpaidTime()).divide(staffTime, 3, RoundingMode.HALF_EVEN).doubleValue());
            } else {
                cell.setCellValue(0);
            }
            // Всего звонков, распределенных на диспетчера, шт
            cell = row.getCell(10);
            cell.setCellValue(operator.getAllCalls());
            // Принятых звонков
            cell = row.getCell(11);
            cell.setCellValue(operator.getAllCalls() - operator.getMissCalls());
            // Средняя продолжительность диалога (секунд)
            cell = row.getCell(12);
            if (operator.getAllCalls() - operator.getMissCalls() != 0) {
                BigDecimal db = new BigDecimal(operator.getTalkTime())
                        .divide(new BigDecimal(operator.getAllCalls() - operator.getMissCalls()), 2, RoundingMode.HALF_EVEN);
                cell.setCellValue(db.doubleValue());
            } else {
                cell.setCellValue(0D);
            }
            //максимальное время разговора.
            cell = row.getCell(13);
            cell.setCellValue(operator.getMaxTalkTime());
            // пропущенных вызовов
            cell = row.getCell(14);
            cell.setCellValue(operator.getMissCalls());
            // пропущенные вызовы
            cell = row.getCell(15);
            if (allCalls.compareTo(BigDecimal.ZERO) == 1) {
                cell.setCellValue(new BigDecimal(operator.getMissCalls()).divide(allCalls, 3, RoundingMode.HALF_EVEN).doubleValue());
            } else {
                cell.setCellValue(0);
            }
        }
    }

}
