package ru.rzd.dayresult;

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
        int id = dao.getID(operator.getSurname(), operator.getInitials());
        operator.setId(id);

        ResultSet rs = dao.getAgentStates(id, date);
        while (rs.next()) {
            AgentState state = AgentState.getByCode(rs.getInt(1));
            Timestamp time = rs.getTimestamp(2);
            operator.addState(state, time);
        }
        if (operator.getLoginTime() != null) {
            rs = dao.getCallDetail(id, operator.getLoginTime());
            while (rs.next()) {
                int ring = rs.getInt(1);
                int talk = rs.getInt(2);
                int hold = rs.getInt(3);
                int work = rs.getInt(4);
                operator.addTimes(ring, talk, hold, work);
            }
            addRows();
        }
        return null;
    }

    private void addRows() {
        if (operator.getId() > 0 || row != null) {
            Cell cell;
            // stafftime
            cell = row.getCell(1);
            cell.setCellValue(operator.getStaffTime());
            // время диалога %
            cell = row.getCell(2);
            cell.setCellValue(operator.getTalkTime() / operator.getStaffTime());
            // время ожидания звонка.
            cell = row.getCell(3);
            cell.setCellValue(operator.getWaitTime() / operator.getStaffTime());

        }
    }

}
