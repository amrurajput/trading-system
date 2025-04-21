package com.universalbank.trading_system.constant;

public class TradingAppConstant {
    public static Long inProgressStatus = 1l;
    public static Long completedStatus = 2l;
    public static Long pendingAssignmentStatus =3l;
    public static Long draftStatus = 4l;
    public static Long draftState = 4l;
    public static Long toBeAssignedTOTraderState = 1l;
    public static Long tradePickedByTraderState = 7l;
    public static Long partialExcecutedState = 2l;
    public static Long fullyExcecutedState = 3l;
    public static Long SalesPersonNotAvailableState = 5l;

  // if trader or sales person is with 5 request he will be marked as occupied in system , we can set system flag
    public static Integer NofOrderRequestThreshold = 5;

    /** Feature flag: if true, scheduler will auto‑assign unclaimed orders */
    public static boolean autoAssignEnabled  = false;
    /** How long (in minutes) before an un‑assigned order is eligible for auto‑assignment */
    public static long  autoAssignDelayMinutes = 1;
}
