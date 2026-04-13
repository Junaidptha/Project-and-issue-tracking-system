package com.example.PaITS.issue.dto;

import com.example.PaITS.issue.entity.IssuePriority;
import com.example.PaITS.issue.entity.IssueType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssueRequestDTO {
    private String title;
    private String description;
    private IssuePriority priority;
    private IssueType issueType;
    private UUID assigneeId;
}
