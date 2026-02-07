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
public class GeneralToolSeeder {

    private final AiToolRepository aiToolRepository;

    public void seed() {
        log.info("Seeding GENERAL tools...");
        
        List<AiTool> tools = Arrays.asList(
            AiTool.builder()
                .id("claude-4-6-opus")
                .name("Claude 4.6 Opus")
                .specialty("Razonamiento autónomo y arquitectura de sistemas complejos")
                .description("El modelo más potente de Anthropic, diseñado para tareas complejas de razonamiento, codificación avanzada y arquitectura de sistemas. Ofrece capacidades superiores en análisis profundo y toma de decisiones autónomas.")
                .pricing("$20/mes")
                .url("https://claude.ai")
                .icon("ph-brain")
                .category(AiTool.Category.GENERAL)
                .tags(Arrays.asList("razonamiento", "código", "análisis", "autónomo"))
                .build(),
            
            AiTool.builder()
                .id("gpt-5-2")
                .name("GPT-5.2")
                .specialty("Procesamiento de lenguaje natural y generación de contenido")
                .description("La última versión del modelo de OpenAI con mejoras significativas en comprensión contextual, generación de código y razonamiento multimodal. Ideal para automatización de tareas complejas.")
                .pricing("$20/mes")
                .url("https://chat.openai.com")
                .icon("ph-open-ai-logo")
                .category(AiTool.Category.GENERAL)
                .tags(Arrays.asList("NLP", "contenido", "automatización", "multimodal"))
                .build(),
            
            AiTool.builder()
                .id("midjourney-v8")
                .name("Midjourney v8")
                .specialty("Generación de imágenes y arte digital")
                .description("La versión más reciente de Midjourney ofrece generación de imágenes fotorrealistas, arte conceptual y diseños creativos con un control sin precedentes sobre la composición y estilo.")
                .pricing("$10/mes")
                .url("https://midjourney.com")
                .icon("ph-image")
                .category(AiTool.Category.GENERAL)
                .tags(Arrays.asList("imágenes", "arte", "diseño", "creatividad"))
                .build(),
            
            AiTool.builder()
                .id("perplexity")
                .name("Perplexity AI")
                .specialty("Búsqueda y síntesis de información en tiempo real")
                .description("Motor de búsqueda conversacional que combina LLMs con acceso a internet en tiempo real. Proporciona respuestas con fuentes citadas y actualizadas al momento.")
                .pricing("Freemium / $20/mes")
                .url("https://perplexity.ai")
                .icon("ph-magnifying-glass")
                .category(AiTool.Category.GENERAL)
                .tags(Arrays.asList("búsqueda", "investigación", "fuentes", "tiempo real"))
                .build(),
            
            AiTool.builder()
                .id("github-copilot")
                .name("GitHub Copilot")
                .specialty("Asistente de código y pair programming")
                .description("Asistente de programación impulsado por IA que sugiere código en tiempo real, completa funciones enteras y ayuda en la resolución de problemas de desarrollo.")
                .pricing("$10/mes")
                .url("https://github.com/features/copilot")
                .icon("ph-github-logo")
                .category(AiTool.Category.GENERAL)
                .tags(Arrays.asList("código", "programación", "IDE", "autocompletado"))
                .build(),
            
            AiTool.builder()
                .id("suno-ai")
                .name("Suno AI")
                .specialty("Generación de música y audio")
                .description("Plataforma de IA que crea música original completa con letras, voz e instrumentación. Ideal para creadores de contenido y músicos que buscan inspiración rápida.")
                .pricing("Freemium / $10/mes")
                .url("https://suno.ai")
                .icon("ph-music-notes")
                .category(AiTool.Category.GENERAL)
                .tags(Arrays.asList("música", "audio", "creatividad", "producción"))
                .build(),
            
            AiTool.builder()
                .id("gemini-3-0-ultra")
                .name("Gemini 3.0 Ultra")
                .specialty("Multimodal avanzado y procesamiento de documentos")
                .description("El modelo más capaz de Google con ventana de contexto masiva de 2M tokens, excelente para análisis de documentos extensos, video y procesamiento multimodal.")
                .pricing("$20/mes")
                .url("https://gemini.google.com")
                .icon("ph-google-logo")
                .category(AiTool.Category.GENERAL)
                .tags(Arrays.asList("multimodal", "documentos", "Google", "contexto"))
                .build(),
            
            AiTool.builder()
                .id("grok-3")
                .name("Grok 3")
                .specialty("Acceso en tiempo real a X/Twitter y análisis de tendencias")
                .description("Desarrollado por xAI, Grok tiene acceso directo a datos de X (Twitter) en tiempo real, ideal para análisis de tendencias, noticias actualizadas y conversaciones sin filtros.")
                .pricing("$8/mes (X Premium)")
                .url("https://grok.x.ai")
                .icon("ph-x-logo")
                .category(AiTool.Category.GENERAL)
                .tags(Arrays.asList("X", "Twitter", "tiempo real", "tendencias"))
                .build(),
            
            AiTool.builder()
                .id("qwen-2-5")
                .name("Qwen 2.5")
                .specialty("Modelo multilingüe especializado en chino e inglés")
                .description("Modelo de Alibaba Cloud con excelente rendimiento en tareas multilingües, especialmente optimizado para chino e inglés. Incluye capacidades de código y razonamiento.")
                .pricing("Open Source / API pago")
                .url("https://qwen.ai")
                .icon("ph-translate")
                .category(AiTool.Category.GENERAL)
                .tags(Arrays.asList("multilingüe", "chino", "open source", "Alibaba"))
                .build(),
            
            AiTool.builder()
                .id("sora-2-turbo")
                .name("Sora 2.0 Turbo")
                .specialty("Generación de video cinematográfico instantáneo")
                .description("El motor de realidad de OpenAI. Capaz de generar escenas de video de hasta 5 minutos con consistencia física perfecta, audio integrado y renderizado en tiempo real a 4K.")
                .pricing("$30/mes")
                .url("https://openai.com/sora")
                .icon("ph-film-strip")
                .category(AiTool.Category.GENERAL)
                .tags(Arrays.asList("video", "cine", "3D", "simulación"))
                .build(),
            
            AiTool.builder()
                .id("ollama-3")
                .name("Ollama 3")
                .specialty("Ejecución de LLMs locales y privados")
                .description("La herramienta estándar para correr modelos como Llama 4 y Mistral localmente. Optimizado para funcionar en hardware de consumo (Mac M4/M5, NVIDIA RTX 50 series) con cero latencia de red.")
                .pricing("Gratis / Open Source")
                .url("https://ollama.com")
                .icon("ph-hard-drive")
                .category(AiTool.Category.GENERAL)
                .tags(Arrays.asList("local", "privacidad", "offline", "llama"))
                .build(),
            
            AiTool.builder()
                .id("elevenlabs-v4")
                .name("ElevenLabs v4")
                .specialty("Clonación de voz y doblaje universal")
                .description("Motor de síntesis de voz indistinguible de la humana. La versión v4 incluye traducción de doblaje en tiempo real manteniendo el tono de voz original y sincronización labial perfecta.")
                .pricing("Freemium / $20/mes")
                .url("https://elevenlabs.io")
                .icon("ph-microphone")
                .category(AiTool.Category.GENERAL)
                .tags(Arrays.asList("voz", "audio", "doblaje", "traducción"))
                .build(),
            
            AiTool.builder()
                .id("runway-gen-4")
                .name("Runway Gen-4")
                .specialty("Control de video avanzado para cineastas")
                .description("Herramienta de video generativo enfocada en control granular. Permite usar 'Motion Brushes' y directores virtuales para controlar la cámara, la iluminación y el movimiento de los personajes con precisión.")
                .pricing("$25/mes")
                .url("https://runwayml.com")
                .icon("ph-video-camera")
                .category(AiTool.Category.GENERAL)
                .tags(Arrays.asList("video", "edición", "control", "vfx"))
                .build(),

            AiTool.builder()
                .id("glm-4-7")
                .name("GLM-4.7")
                .specialty("Rendimiento nivel GPT-4 gratuito/Open Source")
                .description("Modelo de Zhipu AI que rivaliza con Claude 4.5 y GPT 5.2 en razonamiento y codificación. Es una de las mejores opciones Open Weights disponibles actualmente, con una ventana de contexto enorme.")
                .pricing("Gratis / Open Weights")
                .url("https://chat.z.ai/")
                .icon("ph-lightning")
                .category(AiTool.Category.GENERAL)
                .tags(Arrays.asList("open source", "gratis", "zhipu", "razonamiento"))
                .build()
        );
        
        aiToolRepository.saveAll(tools);
        log.info("Seeded {} GENERAL tools", tools.size());
    }
}
