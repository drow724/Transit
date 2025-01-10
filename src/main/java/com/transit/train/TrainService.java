package com.transit.train;

import com.transit.train.dto.TrainDto;
import com.transit.train.event.TrainEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainService {

    private final Sinks.Many<List<TrainDto>> sink = Sinks.many().multicast().onBackpressureBuffer();

    public Flux<List<TrainDto>> getTrains() {
        return sink.asFlux();
    }

    @EventListener
    public void handleCustomEvent(TrainEvent event) {
        sink.tryEmitNext(event.getTrainDto());
    }
}
