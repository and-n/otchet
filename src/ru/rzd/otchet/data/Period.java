/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.rzd.otchet.data;

/**
 * Один период в отчете.
 *
 * @author ATonevitskiy
 */
public class Period {

    private int calls, lostCalls, lostCallsIn5Sec, talkTime, answerTime, queueTime, answerIn20Sec;

    public void addCall(boolean isLost, int outTime, int talkTime, int ansTime) {
        ++calls;
        if (!isLost) {
            if (outTime <= 20) {
                answerIn20Sec++;
            }
            if (ansTime == 10 && talkTime == 0) {
                calls--;
                answerIn20Sec--;
            }
            answerTime += outTime;
            this.talkTime += talkTime + ansTime;
        } else {
            lostCalls++;
            queueTime += outTime;
            if (outTime <= 5) {
                lostCallsIn5Sec++;
            }
        }
    }

    public int getCalls() {
        return calls;
    }

    public int getLostCalls() {
        return lostCalls;
    }

    public int getLostCallsIn5Sec() {
        return lostCallsIn5Sec;
    }

    public int getTalkTime() {
        return talkTime;
    }

    public int getAnswerTime() {
        return answerTime;
    }

    public int getQueueTime() {
        return queueTime;
    }

    public int getAnswerIn20Sec() {
        return answerIn20Sec;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + this.calls;
        hash = 29 * hash + this.lostCalls;
        hash = 29 * hash + this.lostCallsIn5Sec;
        hash = 29 * hash + this.talkTime;
        hash = 29 * hash + this.answerTime;
        hash = 29 * hash + this.queueTime;
        hash = 29 * hash + this.answerIn20Sec;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Period other = (Period) obj;
        if (this.calls != other.calls) {
            return false;
        }
        if (this.lostCalls != other.lostCalls) {
            return false;
        }
        if (this.lostCallsIn5Sec != other.lostCallsIn5Sec) {
            return false;
        }
        if (this.talkTime != other.talkTime) {
            return false;
        }
        if (this.answerTime != other.answerTime) {
            return false;
        }
        if (this.queueTime != other.queueTime) {
            return false;
        }
        if (this.answerIn20Sec != other.answerIn20Sec) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Period{" + "calls=" + calls + ", lostCalls=" + lostCalls
                + ", lostCallsIn5Sec=" + lostCallsIn5Sec + ", talkTime=" + talkTime
                + ", answerTime=" + answerTime + ", queueTime=" + queueTime
                + ", answerIn20Sec=" + answerIn20Sec + '}';
    }

}
