package ru.rzd.dayresult;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private final String surname;
    /**
     * Имя отчество оператора.
     */
    private final String name;
//    private int id;
    private long staffTime, talkTime, maxTalkTime, waitTime, changeCalls, workTime,
            missCalls, allCals, holdTime, ringTime, unpaidTime;

    public Operator(String name, String surname) {
        this.name = name.trim();
        this.surname = surname.trim();
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
    private Timestamp loginWorkTime;

    public void addState(AgentState state, Timestamp time) {
        long t = lastTime != null ? time.getTime() - lastTime.getTime() : 0;
        if (loginTime != null || state.equals(AgentState.LogIn)) {
            switch (state) {
                case LogIn: {
                    if (loginTime == null) {
                        loginTime = time;
                    } else if (time.getTime() - lastTime.getTime() > 28800000L) {
                        loginTime = time;
                        reset();
                    }
                    loginWorkTime = time;
                    break;
                }
                case LogOut: {
                    staffTime = staffTime + time.getTime() - loginWorkTime.getTime();
                    setTime(t);
                    break;
                }
                default:
                    setTime(t);
                    break;
            }
            lastTime = time;
            lastState = state;
        }

    }

    private void setTime(long time) {
        if (lastState.equals(AgentState.LogIn) || lastState.equals(AgentState.NotReady)
                || lastState.equals(AgentState.LogOut)) {
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
        if (isMissed) {
            missCalls++;
        }
    }

    public void addChangedCall() {
        changeCalls++;
    }

//    public void setId(int id) {
//        this.id = id;
//    }
//
//    public int getId() {
//        return id;
//    }
    public long getStaffTime() {
        BigDecimal st = new BigDecimal(staffTime).divide(new BigDecimal(1000), RoundingMode.HALF_EVEN);
        return st.longValue();
    }

    public long getTalkTime() {
        return talkTime;
    }

    public long getMaxTalkTime() {
        return maxTalkTime;
    }

    public long getWaitTime() {
        BigDecimal st = new BigDecimal(waitTime).divide(new BigDecimal(1000), RoundingMode.HALF_EVEN);
        return st.longValue();
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

    public long getAllCalls() {
        return allCals;
    }

    public long getHoldTime() {
        return holdTime;
    }

    public long getRingTime() {
        return ringTime;
    }

    public long getUnpaidTime() {
        BigDecimal st = new BigDecimal(unpaidTime).divide(new BigDecimal(1000), RoundingMode.HALF_EVEN);
        return st.longValue();
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

    public void addTimes(int ring, int talk, int hold, int work) {
        addRingTime(ring);
        addTalkTime(talk);
        addHoldTime(hold);
        addWorkTime(work);
        if (talk == 0) {
            addCall(true);
        } else {
            addCall(false);
        }
    }

    private void reset() {
        unpaidTime = 0;
        staffTime = 0;
        waitTime = 0;
    }

    void sumOperator(Operator operator) {
        allCals += operator.allCals;
        changeCalls += operator.changeCalls;
        holdTime += operator.holdTime;
        lastState = operator.lastState;
        lastTime = operator.lastTime;
        loginTime = operator.loginTime;
        maxTalkTime = Math.max(maxTalkTime, operator.getMaxTalkTime());
        missCalls += operator.missCalls;
        ringTime += operator.ringTime;
        staffTime += operator.staffTime;
        talkTime += operator.talkTime;
        unpaidTime += operator.unpaidTime;
        waitTime += operator.waitTime;
        workTime += operator.workTime;
    }

}
