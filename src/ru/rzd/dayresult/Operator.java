package ru.rzd.dayresult;

/**
 *
 * @author ATonevitskiy
 */
public class Operator {

    String surname;
    String name;
    int id;
    long staffTime, talkTime, maxTalkTime, waitTime, changeCalls, workTime, missCalls, allCals, holdTime, ringTime;

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

}
