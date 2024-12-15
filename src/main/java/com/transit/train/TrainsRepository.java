package com.transit.train;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface TrainsRepository extends R2dbcRepository<Trains, Long> {
}
