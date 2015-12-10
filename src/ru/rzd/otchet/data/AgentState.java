package ru.rzd.otchet.data;

/**
 * Состояние оператора.
 *
 * @author ATonevitskiy
 */
public enum AgentState {

    WrongCode,
    LogIn,
    NotReady,
    Ready,
    Reserved,
    Talking,
    Work,
    LogOut,
    PayedNotReady;

    /**
     * Получить состояние агента по цифре(коду).
     *
     * @param code номер состояния, записанный в базе
     * @return состояние
     */
    public static AgentState getByCode(int code) {
        if (code > 0 && code < AgentState.values().length) {
            return AgentState.values()[code];
        }
        return WrongCode;
    }

}
