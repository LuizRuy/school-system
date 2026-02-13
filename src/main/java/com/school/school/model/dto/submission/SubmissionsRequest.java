package com.school.school.model.dto.submission;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.HashMap;
import java.util.Set;

@Data
public class SubmissionsRequest {

    @NotNull
    private HashMap<Long, Boolean> submissions; // Key: Student ID, Value: Submitted or not

}
