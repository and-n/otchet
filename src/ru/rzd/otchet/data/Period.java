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

    private int calls, lostCalls, lostCallsIn5Sec, talkTime, answerTime, queueTime, answerIn20Sec = 0;

    public void addCall(boolean isLost, int talkTime, int answerTime, int outTime) {

    }

}
