package com.transit.train.event;

import com.transit.train.dto.TrainDto;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainEventPublisherService {

    private final ApplicationEventPublisher eventPublisher;

    public void publishEvent(List<TrainDto> trainDto) {
        eventPublisher.publishEvent(new TrainEvent(this, trainDto));
    }
}
