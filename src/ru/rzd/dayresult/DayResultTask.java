package ru.rzd.dayresult;

import java.util.concurrent.Callable;

/**
 *
 * @author ATonevitskiy
 */
public class DayResultTask implements Callable<Operator> {

    Operator operator;
    DAODayResult dao;

    public DayResultTask(Operator operator, DAODayResult dao) {
        this.dao = dao;
        this.operator = operator;
    }

    @Override
    public Operator call() throws Exception {
        int id = dao.getID(operator.getSurname(), operator.getInitials());
        System.out.println("OPERATOR " + operator.surname + " " + operator.getInitials() + "  " + id);
        operator.setId(id);

        return operator;
    }

}
