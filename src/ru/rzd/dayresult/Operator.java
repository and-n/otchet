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
        if (lastState.equals(AgentState.LogIn) || lastState.equals(AgentState.NotReady)) {
            unpaidTime += time;
        } else if (lastState.equals(AgentState.Ready)) {
            waitTime += time;
        }
    }

    public void addTalkTime(int time) {
        talkTime += time;
        maxTalkTime = Math.max(maxTalkTime, time);
    }

    public void addHoldTime(int time) {
        holdTime += time;
    }

    public void addWorkTime(int time) {
        workTime += time;
    }

    public void addRingTime(int time) {
        ringTime += time;
    }

    public void addCall(boolean isMissed) {
        allCals++;
        missCalls = isMissed ? missCalls++ : missCalls;
    }

    public void addChangedCall() {
        changeCalls++;
    }

    public int getId() {
        return id;
    }

    public long getStaffTime() {
        return staffTime;
    }

    public long getTalkTime() {
        return talkTime;
    }

    public long getMaxTalkTime() {
        return maxTalkTime;
    }

    public long getWaitTime() {
        return waitTime;
    }

    public long getChangeCalls() {
        return changeCalls;
    }

    public long getWorkTime() {
        return workTime;
    }

    public long getMissCalls() {
        return missCalls;
    }

    public long getAllCals() {
        return allCals;
    }

    public long getHoldTime() {
        return holdTime;
    }

    public long getRingTime() {
        return ringTime;
    }

    public long getUnpaidTime() {
        return unpaidTime;
    }

    public AgentState getLastState() {
        return lastState;
    }

    public Timestamp getLastTime() {
        return lastTime;
    }

    public Timestamp getLoginTime() {
        return loginTime;
    }

}
