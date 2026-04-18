package com.example.PaITS.project.dto;

import com.example.PaITS.issue.entity.IssuePriority;
import com.example.PaITS.issue.entity.IssueStatus;
import com.example.PaITS.issue.entity.IssueType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoadmapItemDTO {
    private UUID id;
    private String title;
    private String description;
    private IssueStatus status;
    private IssuePriority priority;
    private IssueType issueType;
}
