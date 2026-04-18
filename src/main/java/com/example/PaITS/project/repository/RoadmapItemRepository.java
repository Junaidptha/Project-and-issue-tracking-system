package com.example.PaITS.project.repository;

import com.example.PaITS.project.entity.RoadmapItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RoadmapItemRepository extends JpaRepository<RoadmapItem, UUID> {
    List<RoadmapItem> findByProjectIdOrderByCreatedAtAsc(UUID projectId);
    
    @org.springframework.transaction.annotation.Transactional
    void deleteByProjectId(UUID projectId);
}
