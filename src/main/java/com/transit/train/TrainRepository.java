package com.transit.train;

import com.transit.train.entity.Train;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface TrainRepository extends R2dbcRepository<Train, Long> {
}
