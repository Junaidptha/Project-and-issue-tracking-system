package com.example.PaITS.issue.dto;

import com.example.PaITS.issue.entity.IssueStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IssueStatusUpdateDTO {
    private IssueStatus status;
}
