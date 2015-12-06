package ru.rzd.otchet.data;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 *
 * @author ATonevitskiy
 */
public class Statist60min {

    private long workTime = 0;

    public Statist60min() {
    }

    public void addWorkTime(long time) {
        workTime = workTime + time;
    }

    public long getAllWorkTime() {
        return workTime;
    }

    public BigDecimal getAgentPer60min() {
        BigDecimal mh = new BigDecimal(getAllWorkTime())
                .divide(new BigDecimal(1000), 1, RoundingMode.HALF_EVEN)
                .divide(new BigDecimal(3600), 3, RoundingMode.HALF_EVEN);
        System.out.println("AP " + getAllWorkTime());
        return mh;
    }

}
