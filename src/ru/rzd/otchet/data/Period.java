/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.rzd.otchet.data;

import java.util.Date;

/**
 * Один период в отчете.
 *
 * @author ATonevitskiy
 */
public class Period {

    private Date startTime, stopTime;
    private Long calls, lostCalls, lostCallsIn5Sec;
    private int talkTime, answerTime, outTime, answerIn20Sec;

}
