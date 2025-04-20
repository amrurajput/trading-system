package com.universalbank.trading_system.constant;

public class TradingAppConstant {
    public static Long inProgressStatus = 1l;
    public static Long draftStatus = 100l;
    public static Long traderToPickState = 1l;
    public static Long tradePickedByTraderState = 2l;
    public static Integer clientNoOfRequestThreshold = 5;

    /** Feature flag: if true, scheduler will auto‑assign unclaimed orders */
    public static boolean autoAssignEnabled     = true;
    /** How long (in minutes) before an un‑assigned order is eligible for auto‑assignment */
    public static long    autoAssignDelayMinutes = 15;
}
