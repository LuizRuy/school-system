package com.school.school.model.dto.task;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TaskResponse {

    private Long id;
    private String name;
}
