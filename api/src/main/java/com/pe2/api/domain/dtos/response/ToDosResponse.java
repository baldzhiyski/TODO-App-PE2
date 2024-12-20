package com.pe2.api.domain.dtos.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ToDosResponse {

    private Long id;

    private String title;

    private boolean finished;

    private String description;

    private List<AssigneeResponse> assigneeList;

    private Long createdDate;
    private Long dueDate;
    private Long finishedDate;

    private String category;

}
