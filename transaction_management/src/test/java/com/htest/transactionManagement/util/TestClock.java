package com.htest.transactionManagement.util;

import java.time.LocalDateTime;

public class TestClock implements Clock {
    private LocalDateTime now;

    public TestClock(LocalDateTime initial) {
        this.now = initial;
    }

    @Override
    public LocalDateTime now() {
        return now;
    }

    public void advanceSeconds(long seconds) {
        now = now.plusSeconds(seconds);
    }
}
