package me.ikevoodoo.helix.reporting;

import me.ikevoodoo.helix.api.reporting.HelixErrorReporter;

import java.util.ArrayDeque;
import java.util.Deque;

public class BukkitHelixErrorReporter implements HelixErrorReporter {

    private final Deque<HelixErrorSession> sessions = new ArrayDeque<>();

    @Override
    public void beginSession() {
        this.sessions.push(new HelixErrorSession());
    }

    @Override
    public HelixErrorSession getSession() {
        return this.sessions.peek();
    }

    @Override
    public void endSession() {
        this.sessions.pop();
    }
}
