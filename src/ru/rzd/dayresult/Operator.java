package ru.rzd.dayresult;

import java.sql.Timestamp;
import ru.rzd.otchet.data.AgentState;

/**
 * Данные для одной строки оператора в отчете.
 *
 * @author ATonevitskiy
 */
public class Operator {

    /**
     * Фамилия оператора.
     */
    String surname;
    /**
     * Имя отчество оператора.
     */
    String name;
    int id;
    long staffTime, talkTime, maxTalkTime, waitTime, changeCalls, workTime,
            missCalls, allCals, holdTime, ringTime, unpaidTime;

    public Operator(String name, String surname) {
        this.name = name;
        this.surname = surname;
    }

    public String getSurname() {
        return surname;
    }

    public String getName() {
        return name;
    }

    public String getInitials() {
        String[] splits = name.split("\\s+");
        String init = splits[0].substring(0, 1) + "." + splits[1].substring(0, 1) + ".";
        return init;
    }

    private AgentState lastState;
    private Timestamp lastTime;
    private Timestamp loginTime;

    public void addState(AgentState state, Timestamp time) {
        long t = lastTime != null ? time.getTime() - lastTime.getTime() : 0;
        switch (state) {
            case LogIn: {
                if (lastState == null) {
                    loginTime = time;
                } else {
                    System.out.println("WTF?");
                }
            }
            case LogOut: {
                staffTime = time.getTime() - loginTime.getTime();
                setTime(t);
            }
            default:
                setTime(t);
        }
        lastTime = time;
        lastState = state;
    }

    private void setTime(long time) {
        if (lastState.equals(AgentState.LogIn)) {

        } else if (lastState.equals(AgentState.Ready)) {

        }
    }

}
