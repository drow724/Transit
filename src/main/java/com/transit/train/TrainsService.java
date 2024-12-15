package com.transit.train;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class TrainsService {

    private final TrainsRepository trainsRepository;

    public Flux<String> getTrains() {
        return Flux.concat
    }
}
