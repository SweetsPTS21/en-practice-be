package com.swpts.enpracticebe.service;

import java.util.UUID;

public interface SmartReminderService {

    /**
     * Pre-computes and persists an AI-generated smart reminder for the given user.
     * Called by SmartReminderScheduler every 6 hours.
     */
    void computeSmartReminder(UUID userId);
}
