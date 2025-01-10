package com.transit.train.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.transit.train.dto.TrainDto;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class TrainResponse {

    @JsonProperty("realtimeArrivalList")
    private List<TrainDto> realtimeArrivalList = new ArrayList<>();
}
