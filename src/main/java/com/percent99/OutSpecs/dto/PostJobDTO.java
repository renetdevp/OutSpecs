package com.percent99.OutSpecs.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PostJobDTO {

    private Integer career;
    private List<String> techniques;
}