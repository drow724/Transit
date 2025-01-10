package com.transit.train.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("trains")
public class Train {

    @Id
    private Long id;
}
