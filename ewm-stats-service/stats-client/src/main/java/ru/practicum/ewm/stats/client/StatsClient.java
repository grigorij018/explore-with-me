package ru.practicum.ewm.stats.client;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.ewm.stats.dto.DateTimeFormat;
import ru.practicum.ewm.stats.dto.EndpointHitDto;
import ru.practicum.ewm.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

public class StatsClient {
    private final String baseUrl;
    private final RestTemplate restTemplate;

    public StatsClient(String baseUrl) {
        this(baseUrl, new RestTemplate());
    }

    public StatsClient(String baseUrl, RestTemplate restTemplate) {
        this.baseUrl = trimTrailingSlash(baseUrl);
        this.restTemplate = restTemplate;
    }

    public void hit(EndpointHitDto endpointHit) {
        restTemplate.postForEntity(baseUrl + "/hit", endpointHit, Void.class);
    }

    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl + "/stats")
                .queryParam("start", DateTimeFormat.FORMATTER.format(start))
                .queryParam("end", DateTimeFormat.FORMATTER.format(end))
                .queryParam("unique", unique);

        if (uris != null && !uris.isEmpty()) {
            builder.queryParam("uris", uris.toArray());
        }

        ResponseEntity<List<ViewStatsDto>> response = restTemplate.exchange(
                builder.encode().toUriString(),
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<>() {
                }
        );

        return response.getBody() == null ? List.of() : response.getBody();
    }

    private static String trimTrailingSlash(String url) {
        if (url.endsWith("/")) {
            return url.substring(0, url.length() - 1);
        }
        return url;
    }
}
