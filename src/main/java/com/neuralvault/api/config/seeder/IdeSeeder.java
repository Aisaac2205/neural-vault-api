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
public class IdeSeeder {

    private final AiToolRepository aiToolRepository;

    public void seed() {
        log.info("Seeding IDE tools...");
        
        List<AiTool> tools = Arrays.asList(
            AiTool.builder()
                .id("antigravity")
                .name("Antigravity")
                .specialty("IDE con foco en velocidad y minimalismo")
                .description("IDE ultraligero disenado para desarrollo rapido con interfaz minimalista. Incluye integracion nativa con LLMs y flujo de trabajo optimizado para productividad.")
                .pricing("Gratis")
                .url("https://antigravity.dev")
                .icon("ph-asterisk")
                .category(AiTool.Category.IDE)
                .tags(Arrays.asList("IDE", "rapido", "minimalista", "productividad"))
                .build(),
            
            AiTool.builder()
                .id("vscode")
                .name("VS Code")
                .specialty("Editor de codigo extensible y popular")
                .description("Editor de codigo ligero pero potente de Microsoft. Soporta miles de extensiones, integracion con Git, debugging avanzado y ahora incluye GitHub Copilot integrado.")
                .pricing("Gratis")
                .url("https://code.visualstudio.com")
                .icon("ph-code")
                .category(AiTool.Category.IDE)
                .tags(Arrays.asList("editor", "extensible", "Microsoft", "popular"))
                .build(),
            
            AiTool.builder()
                .id("open-code")
                .name("OpenCode")
                .specialty("IDE open source con integracion de IA nativa")
                .description("Entorno de desarrollo open source con integracion nativa de modelos de IA locales y en la nube. Enfocado en privacidad y control del usuario sobre sus datos.")
                .pricing("Gratis / Open Source")
                .url("https://opencode.ai")
                .icon("ph-git-fork")
                .category(AiTool.Category.IDE)
                .tags(Arrays.asList("open source", "IA nativa", "privacidad", "local"))
                .build(),
            
            AiTool.builder()
                .id("cursor")
                .name("Cursor")
                .specialty("IDE con IA integrada profundamente")
                .description("Fork de VS Code con integracion profunda de IA. Permite chat contextual, edicion multi-archivo, generacion de codigo y refactorizacion inteligente basada en el contexto del proyecto.")
                .pricing("Freemium / $20/mes")
                .url("https://cursor.sh")
                .icon("ph-cursor")
                .category(AiTool.Category.IDE)
                .tags(Arrays.asList("IDE", "IA integrada", "cursor", "edicion"))
                .build(),
            
            AiTool.builder()
                .id("windsurf-2")
                .name("Windsurf 2")
                .specialty("Editor colaborativo con IA en tiempo real")
                .description("Editor de codigo disenado para colaboracion en tiempo real con capacidades avanzadas de IA. Soporta pair programming remoto y sugerencias contextuales basadas en el equipo.")
                .pricing("$15/mes")
                .url("https://windsurf.io")
                .icon("ph-wind")
                .category(AiTool.Category.IDE)
                .tags(Arrays.asList("colaborativo", "pair programming", "tiempo real", "equipo"))
                .build(),
            
            AiTool.builder()
                .id("intellij-idea")
                .name("IntelliJ IDEA")
                .specialty("IDE profesional para Java y Kotlin")
                .description("IDE inteligente de JetBrains con refactorizacion avanzada, analisis de codigo estatico y soporte nativo para multiples frameworks. La version Ultimate incluye herramientas de IA para asistencia de codigo.")
                .pricing("Freemium / $169/año")
                .url("https://jetbrains.com/idea")
                .icon("ph-code")
                .category(AiTool.Category.IDE)
                .tags(Arrays.asList("Java", "Kotlin", "JetBrains", "profesional", "refactorizacion"))
                .build()
        );
        
        aiToolRepository.saveAll(tools);
        log.info("Seeded {} IDE tools", tools.size());
    }
}
