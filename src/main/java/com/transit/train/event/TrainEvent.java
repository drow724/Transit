package com.transit.train.event;

import com.transit.train.dto.TrainDto;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

@Getter
public class TrainEvent extends ApplicationEvent {

    private final List<TrainDto> trainDto;

    public TrainEvent(Object source, List<TrainDto> trainDto) {
        super(source);
        this.trainDto = trainDto;
    }
}
