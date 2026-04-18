package ru.practicum.ewm.main.event;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationEmbeddable {
    @Column(nullable = false)
    private Float lat;

    @Column(nullable = false)
    private Float lon;
}
