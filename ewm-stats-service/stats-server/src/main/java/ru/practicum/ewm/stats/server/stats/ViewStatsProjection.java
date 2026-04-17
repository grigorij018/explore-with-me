package ru.practicum.ewm.stats.server.stats;

public interface ViewStatsProjection {
    String getApp();

    String getUri();

    Long getHits();
}
