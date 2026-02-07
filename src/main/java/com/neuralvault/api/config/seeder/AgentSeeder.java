package com.neuralvault.api.config.seeder;

import com.neuralvault.api.entity.AiTool;
import com.neuralvault.api.repository.AiToolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AgentSeeder {

    private final AiToolRepository aiToolRepository;

    public void seed() {
        log.info("Seeding AGENT tools...");
        
        List<AiTool> tools = Arrays.asList(
            AiTool.builder()
                .id("devin-3-pro")
                .name("Devin 3.0 Pro")
                .specialty("Ingeniero de Software Autonomo")
                .description("El primer ingeniero de IA totalmente autonomo. Devin 3 puede tomar un ticket de Jira, planificar la solucion, escribir el codigo, arreglar sus propios bugs y hacer el deploy a produccion sin supervision.")
                .pricing("$50/mes")
                .url("https://cognition.ai")
                .icon("ph-robot")
                .category(AiTool.Category.AGENT)
                .tags(Arrays.asList("agente", "autonomo", "ingenieria", "full-stack", "devin"))
                .build(),
            
            AiTool.builder()
                .id("claude-code-agent")
                .name("Claude Code")
                .specialty("Agente de codificacion autonomo en terminal")
                .description("Herramienta de terminal que permite a Claude interactuar directamente con tu codebase. Puede leer archivos, ejecutar comandos, hacer commits y refactorizar codigo autonomamente. Es un agente que trabaja por ti.")
                .pricing("Incluido con Claude Pro")
                .url("https://claude.ai/code")
                .icon("ph-terminal")
                .category(AiTool.Category.AGENT)
                .tags(Arrays.asList("terminal", "agente", "autonomo", "refactorizacion", "claude"))
                .build(),
            
            AiTool.builder()
                .id("auto-gpt-5")
                .name("AutoGPT 5.0")
                .specialty("Agente autonomo de proposito general")
                .description("Agente de IA que puede ejecutar tareas complejas de manera autonoma, descomponiendo objetivos en subtareas y ejecutandolas sin intervencion humana. Ideal para automatizacion de workflows.")
                .pricing("Open Source / API costs")
                .url("https://agpt.co")
                .icon("ph-infinity")
                .category(AiTool.Category.AGENT)
                .tags(Arrays.asList("agente", "autonomo", "open-source", "automatizacion", "GPT"))
                .build(),
            
            AiTool.builder()
                .id("babyagi-3")
                .name("BabyAGI 3.0")
                .specialty("Gestion de tareas y priorizacion autonoma")
                .description("Sistema de gestion de tareas impulsado por IA que crea, prioriza y ejecuta tareas de forma autonoma. Mantiene un loop continuo de ejecucion hasta completar el objetivo principal.")
                .pricing("Open Source")
                .url("https://github.com/yoheinakajima/babyagi")
                .icon("ph-baby")
                .category(AiTool.Category.AGENT)
                .tags(Arrays.asList("agente", "autonomo", "tareas", "priorizacion", "workflow"))
                .build()
        );
        
        aiToolRepository.saveAll(tools);
        log.info("Seeded {} AGENT tools", tools.size());
    }
}
