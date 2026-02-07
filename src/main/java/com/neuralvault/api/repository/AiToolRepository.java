package com.neuralvault.api.repository;

import com.neuralvault.api.entity.AiTool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiToolRepository extends JpaRepository<AiTool, String> {
    List<AiTool> findByCategory(AiTool.Category category);
}
