/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.rzd.otchet.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * Один период в отчете.
 *
 * @author ATonevitskiy
 */
public class Period {

    private int calls, lostCalls, lostCallsIn5Sec, talkTime, answerTime, queueTime, answerIn20Sec, ivrCalls;

    private Collection<Long> list = new ArrayList<>();

    public void addCall(boolean isLost, int outTime, int talkTime, int ansTime, long id) {
        ++calls;
        boolean dubl = list.contains(id); // повторяется ли id звонка 
        if (dubl) {
            calls--;
        } else {
            list.add(id);
        }
        if (!isLost) {
            if (outTime <= 20 && !dubl) {
                answerIn20Sec++;
            }
            if (ansTime == 10 && talkTime == 0 && !dubl) {
                // calls--;
                answerIn20Sec--;
            }
            if (outTime - ansTime >= 0) {
                answerTime += outTime - ansTime;
            } else {
                answerTime += outTime;
            }
            this.talkTime += (talkTime + ansTime);
        } else {
            if (outTime != 0) {
                lostCalls++;
                queueTime += outTime;
                if (outTime <= 5) {
                    lostCallsIn5Sec++;
                }
            } else {
                ivrCalls++;
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

    public int getIvrCalls() {
        return ivrCalls;
    }

    public void setIvrCalls(int ivrCalls) {
        this.ivrCalls = ivrCalls;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + this.calls;
        hash = 53 * hash + this.lostCalls;
        hash = 53 * hash + this.lostCallsIn5Sec;
        hash = 53 * hash + this.talkTime;
        hash = 53 * hash + this.answerTime;
        hash = 53 * hash + this.queueTime;
        hash = 53 * hash + this.answerIn20Sec;
        hash = 53 * hash + this.ivrCalls;
        hash = 53 * hash + Objects.hashCode(this.list);
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
        if (this.ivrCalls != other.ivrCalls) {
            return false;
        }
        if (!Objects.equals(this.list, other.list)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Period{" + "calls=" + calls + ", lostCalls=" + lostCalls
                + ", lostCallsIn5Sec=" + lostCallsIn5Sec + ", talkTime="
                + talkTime + ", answerTime=" + answerTime + ", queueTime="
                + queueTime + ", answerIn20Sec=" + answerIn20Sec + ", ivrCalls="
                + ivrCalls + ", list=" + list + '}';
    }

}
