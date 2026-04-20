package com.example.PaITS.project.service;

import com.example.PaITS.project.dto.RoadmapItemDTO;
import com.example.PaITS.issue.entity.IssuePriority;
import com.example.PaITS.issue.entity.IssueStatus;
import com.example.PaITS.issue.entity.IssueType;
import com.example.PaITS.project.entity.Project;
import com.example.PaITS.project.entity.RoadmapItem;
import com.example.PaITS.project.repository.ProjectRepository;
import com.example.PaITS.project.repository.RoadmapItemRepository;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RoadmapService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private RoadmapItemRepository roadmapItemRepository;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=";

    public void generateRoadmapForProject(UUID projectId, UUID creatorId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        String userTemplate = "⚡ Phase 1: Project Setup (Day 1–2)\n" +
            "🗄️ Phase 2: Database Schema (Day 3–4)\n" +
            "🔐 Phase 3: Authentication (Day 5–6)\n" +
            "⚙️ Phase 4: Core Backend APIs (Week 2)\n" +
            "🎨 Phase 5: Frontend Core (Week 3)\n" +
            "🔗 Phase 6: Integration (Week 3 End)\n" +
            "⏱️ Phase 7: App Engine / Triggers (Week 4)\n" +
            "🧪 Phase 8: Testing (3–5 Days)\n" +
            "🚀 Phase 9: Deployment (2–3 Days)";

        String prompt = String.format(
            "You are an expert technical project manager. Create a highly detailed and comprehensive roadmap for the following project. " +
            "Project Name: %s\nProject Description: %s\n\n" +
            "You MUST strictly structure the roadmap into exactly 9 phases mirroring this exact focused template timeline and style:\n%s\n\n" +
            "RETURN ONLY RAW VALID JSON representing a List of exactly 9 objects. DO NOT wrap the output in ```json markdown blocks. " +
            "Each object MUST have these exactly named keys:\n" +
            "1. 'title' (string, MUST include the Phase number and an appropriate title with an emoji, matching the template style).\n" +
            "2. 'description' (string, provide an extremely detailed breakdown mapping to the project's exact needs. Use bullet points like '* Feature' and clear subheadings. Include technical stack suggestions like React/Node/Postgres. Format with newlines).\n" +
            "3. 'issueType' (string, exactly one of: TASK, BUG, IMPROVEMENT, STORY).\n" +
            "4. 'priority' (string, exactly one of: LOW, MEDIUM, HIGH, CRITICAL).",
            project.getName(), 
            project.getDescription() == null || project.getDescription().isEmpty() ? "Standard software project" : project.getDescription(),
            userTemplate
        );

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        List<Map<String, Object>> contents = new ArrayList<>();
        Map<String, Object> partsObj = new HashMap<>();
        List<Map<String, String>> parts = new ArrayList<>();
        Map<String, String> textObj = new HashMap<>();
        textObj.put("text", prompt);
        parts.add(textObj);
        partsObj.put("parts", parts);
        contents.add(partsObj);
        requestBody.put("contents", contents);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(GEMINI_API_URL + geminiApiKey, entity, String.class);
            
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response.getBody());
            String rawJsonResponse = rootNode.path("candidates").get(0).path("content").path("parts").get(0).path("text").textValue();
            if (rawJsonResponse == null) rawJsonResponse = "";
             
            rawJsonResponse = rawJsonResponse.replaceAll("^```json\\s*", "");
            rawJsonResponse = rawJsonResponse.replaceAll("\\s*```$", "");
            rawJsonResponse = rawJsonResponse.trim();

            List<RoadmapItemDTO> mappedItems = mapper.readValue(rawJsonResponse, new TypeReference<List<RoadmapItemDTO>>() {});
            
            // Delete old roadmap items for this project before saving new ones
            roadmapItemRepository.deleteByProjectId(projectId);
            
            for (RoadmapItemDTO dto : mappedItems) {
                if (dto.getIssueType() == null) dto.setIssueType(IssueType.TASK);
                if (dto.getPriority() == null) dto.setPriority(IssuePriority.MEDIUM);
                
                RoadmapItem item = RoadmapItem.builder()
                        .project(project)
                        .title(dto.getTitle())
                        .description(dto.getDescription())
                        .status(IssueStatus.OPEN)
                        .priority(dto.getPriority())
                        .issueType(dto.getIssueType())
                        .build();
                        
                roadmapItemRepository.save(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to generate AI roadmap: " + e.getMessage());
        }
    }

    public List<RoadmapItemDTO> getRoadmapForProject(UUID projectId) {
        return roadmapItemRepository.findByProjectIdOrderByCreatedAtAsc(projectId).stream()
            .map(item -> RoadmapItemDTO.builder()
                .id(item.getId())
                .title(item.getTitle())
                .description(item.getDescription())
                .status(item.getStatus())
                .priority(item.getPriority())
                .issueType(item.getIssueType())
                .build())
            .collect(Collectors.toList());
    }

    public RoadmapItemDTO updateRoadmapItemStatus(UUID itemId, IssueStatus newStatus) {
        RoadmapItem item = roadmapItemRepository.findById(itemId)
            .orElseThrow(() -> new RuntimeException("Roadmap item not found"));
        item.setStatus(newStatus);
        RoadmapItem saved = roadmapItemRepository.save(item);
        
        return RoadmapItemDTO.builder()
            .id(saved.getId())
            .title(saved.getTitle())
            .description(saved.getDescription())
            .status(saved.getStatus())
            .priority(saved.getPriority())
            .issueType(saved.getIssueType())
            .build();
    }
}
