package ru.rzd.dayresult;

import java.util.concurrent.Callable;
import org.apache.poi.ss.usermodel.Row;

/**
 *
 * @author ATonevitskiy
 */
public class DayResultTask implements Callable<Void> {

    Operator operator;
    DAODayResult dao;
    private Row row;

    public DayResultTask(Operator operator, DAODayResult dao, Row row) {
        this.dao = dao;
        this.operator = operator;
        this.row = row;
    }

    @Override
    public Void call() throws Exception {
        int id = dao.getID(operator.getSurname(), operator.getInitials());
        System.out.println("OPERATOR " + operator.surname + " " + operator.getInitials() + "  " + id);
        operator.setId(id);

        return null;
    }

}
