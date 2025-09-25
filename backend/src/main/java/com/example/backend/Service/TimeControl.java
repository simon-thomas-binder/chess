package com.example.backend.Service;

import com.example.backend.enums.Color;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Manages the timing control for a game session, tracking time for each player
 */
public class TimeControl {

    private final Map<Color, Long> times = new HashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final int increment;
    private final int delay;
    private final GameSession session;

    private ScheduledFuture<?> currentTask;
    private long lastStart;

    /**
     * Initializes the TimeControl with the given parameters
     *
     * @param session The current game session
     * @param initialTime The initial time for each player
     * @param increment The time increment after each move
     * @param delay The delay before the timer starts
     * @param colors List of player colors
     */
    public TimeControl(GameSession session, long initialTime, int increment, int delay, List<Color> colors) {
        this.session = session;
        this.increment = increment;
        this.delay = delay;
        for (Color color : colors) {
            times.put(color, initialTime);
        }
    }

    /**
     * Updates the time for the specified color after a move is played
     *
     * @param color The color of the player who made the move
     * @return The updated map of remaining times for each color
     */
    public synchronized Map<Color, Long> onMovePlayed(Color color) {
        if (lastStart <= 0) {
            return times;
        }
        long elapsed = System.currentTimeMillis() - lastStart;
        if (elapsed <= delay) {
            elapsed = 0;
        } else {
            elapsed = elapsed - delay;
        }
        long updated = times.get(color) - elapsed + increment;
        times.put(color, updated);

        if (currentTask != null) currentTask.cancel(false);
        return times;
    }

    /**
     * Starts the turn for the specified color, initializing the timer
     *
     * @param color The color of the player whose turn is starting
     */
    public synchronized void startTurn(Color color) {
        lastStart = System.currentTimeMillis();
        long timeLeft = times.get(color) + delay;

        if (currentTask != null) currentTask.cancel(false);
        currentTask = scheduler.schedule(
                () -> session.timeUp(color),
                timeLeft,
                TimeUnit.MILLISECONDS
        );
    }

    /**
     * Stops the current timer task if it exists
     */
    public synchronized void stop() {
        if (currentTask != null) currentTask.cancel(false);
    }
}
