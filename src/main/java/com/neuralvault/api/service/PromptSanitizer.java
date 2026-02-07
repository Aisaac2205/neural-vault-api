package com.neuralvault.api.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Slf4j
@Component
public class PromptSanitizer {

    // Patrones comunes de prompt injection
    private static final Pattern[] INJECTION_PATTERNS = {
        Pattern.compile("(?i)ignore.*previous.*instructions"),
        Pattern.compile("(?i)disregard.*above"),
        Pattern.compile("(?i)forget.*everything"),
        Pattern.compile("(?i)you are now.*assistant"),
        Pattern.compile("(?i)system\\s*:"),
        Pattern.compile("(?i)developer\\s*:"),
        Pattern.compile("(?i)user\\s*:"),
        Pattern.compile("(?i)assistant\\s*:"),
        Pattern.compile("<\\s*script[^>]*>", Pattern.CASE_INSENSITIVE),
        Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
        Pattern.compile("on\\w+\\s*=", Pattern.CASE_INSENSITIVE),
    };

    // Caracteres peligrosos que podrían romper la estructura del prompt
    private static final String DANGEROUS_CHARS = "<>\"'`;{}[]|\\";

    /**
     * Sanitiza el input del usuario para prevenir prompt injection
     */
    public String sanitize(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        String sanitized = input;

        // Detect and block injection patterns
        for (Pattern pattern : INJECTION_PATTERNS) {
            if (pattern.matcher(sanitized).find()) {
                log.warn("Possible prompt injection attempt detected: {}", input);
                // Replace pattern with empty string
                sanitized = pattern.matcher(sanitized).replaceAll("");
            }
        }

        // Eliminar caracteres peligrosos
        StringBuilder clean = new StringBuilder();
        for (char c : sanitized.toCharArray()) {
            if (DANGEROUS_CHARS.indexOf(c) == -1) {
                clean.append(c);
            }
        }
        sanitized = clean.toString();

        // Limit maximum length
        if (sanitized.length() > 500) {
            sanitized = sanitized.substring(0, 500);
            log.debug("Input truncated to 500 characters");
        }

        // Normalizar espacios múltiples
        sanitized = sanitized.trim().replaceAll("\\s+", " ");

        return sanitized;
    }

    /**
     * Verifica si el input es sospechoso de injection
     */
    public boolean isSuspicious(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }

        for (Pattern pattern : INJECTION_PATTERNS) {
            if (pattern.matcher(input).find()) {
                return true;
            }
        }

        // Verificar caracteres peligrosos
        for (char c : input.toCharArray()) {
            if (DANGEROUS_CHARS.indexOf(c) != -1) {
                return true;
            }
        }

        return false;
    }
}
