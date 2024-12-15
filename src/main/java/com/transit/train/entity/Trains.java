package com.transit.train.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("trains")
public class Trains {

    @Id
    private Long id;
}
