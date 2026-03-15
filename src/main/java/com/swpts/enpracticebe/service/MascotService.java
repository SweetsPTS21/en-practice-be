package com.swpts.enpracticebe.service;

import com.swpts.enpracticebe.dto.response.mascot.MascotResponse;

import java.util.UUID;

public interface MascotService {

    /**
     * Returns pre-computed mascot data for the currently authenticated user.
     * Falls back to default messages if none are pre-computed yet.
     */
    MascotResponse getMascotData();

    /**
     * Compute and persist AI-generated mascot messages for the given user.
     * Called by MascotScheduler every 6 hours.
     */
    void computeMascotMessages(UUID userId);
}
