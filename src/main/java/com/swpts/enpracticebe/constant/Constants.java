package com.swpts.enpracticebe.constant;

public class Constants {
    public static final String AI_NAME = "Nino - Trợ lý học tiếng anh";

    // File service
    public static final Integer SIGNATURE_SKEW_SECONDS = 300;
    public static final Integer PRESIGN_URL_TIME = 30 * 60; // 30 phút

    // ─── Band score mapping (IELTS standard for Listening/Reading out of 40) ───
    public static final int[][] BAND_TABLE = {
            {39, 90}, // 39-40 → 9.0
            {37, 85}, // 37-38 → 8.5
            {35, 80}, // 35-36 → 8.0
            {33, 75}, // 33-34 → 7.5
            {30, 70}, // 30-32 → 7.0
            {27, 65}, // 27-29 → 6.5
            {23, 60}, // 23-26 → 6.0
            {20, 55}, // 20-22 → 5.5
            {16, 50}, // 16-19 → 5.0
            {13, 45}, // 13-15 → 4.5
            {10, 40}, // 10-12 → 4.0
            {6, 35}, // 6-9 → 3.5
            {4, 30}, // 4-5 → 3.0
            {0, 25}, // 0-3 → 2.5
    };

    // Leaderboard & XP
    public static final int MAX_DAILY_XP = 300;
    public static final int REPEAT_XP_PENALTY_PERCENT = 50;
}
