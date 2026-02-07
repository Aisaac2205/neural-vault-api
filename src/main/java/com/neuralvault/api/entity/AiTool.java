package com.neuralvault.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "ai_tool")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiTool {
    
    @Id
    private String id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String specialty;
    
    @Lob
    @Column(nullable = false, length = 2000)
    private String description;
    
    @Column(nullable = false)
    private String pricing;
    
    @Column(nullable = false)
    private String url;
    
    @Column(nullable = false)
    private String icon;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Category category;
    
    @ElementCollection
    @CollectionTable(name = "ai_tool_tags", joinColumns = @JoinColumn(name = "tool_id"))
    @Column(name = "tag")
    private List<String> tags;
    
    public enum Category {
        GENERAL,
        AGENT,
        IDE
    }
}
