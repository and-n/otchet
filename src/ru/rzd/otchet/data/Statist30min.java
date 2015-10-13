package ru.rzd.otchet.data;

import java.math.BigDecimal;

/**
 *
 * @author ATonevitskiy
 */
public class Statist30min {

    private final long min30 = 1800000L;

    private int startState;
    private long workTime = 0;
    private int changeIn30Min = 0;

    public Statist30min(int startState) {
        this.startState = startState;
    }

    public int getStartState() {
        return startState;
    }

    public int getNEWStartState() {
        return startState + changeIn30Min;
    }

    public void addWorkAgent() {
        changeIn30Min++;
    }

    public void removeWorkAgent() {
        changeIn30Min--;
    }

    public void addWorkTime(long time) {
        workTime += time;
    }

    public long getAllWorkTime() {
        return workTime + (startState * min30);
    }

    public BigDecimal getAgentPer30min() {
        BigDecimal mh = new BigDecimal(getAllWorkTime()).divide(new BigDecimal(getNEWStartState()));
        return mh;
    }

}
