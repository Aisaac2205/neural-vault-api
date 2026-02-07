package com.neuralvault.api.config;

import com.neuralvault.api.config.seeder.AgentSeeder;
import com.neuralvault.api.config.seeder.GeneralToolSeeder;
import com.neuralvault.api.config.seeder.IdeSeeder;
import com.neuralvault.api.repository.AiToolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final AiToolRepository aiToolRepository;
    private final GeneralToolSeeder generalToolSeeder;
    private final AgentSeeder agentSeeder;
    private final IdeSeeder ideSeeder;

    @Override
    public void run(String... args) {
        if (aiToolRepository.count() == 0) {
            log.info("Starting database seeding process...");
            
            // Seed each category separately (SRP - Single Responsibility Principle)
            generalToolSeeder.seed();
            agentSeeder.seed();
            ideSeeder.seed();
            
            long totalTools = aiToolRepository.count();
            log.info("Successfully seeded {} AI tools into the database", totalTools);
        } else {
            log.info("Database already contains {} tools, skipping seeding", aiToolRepository.count());
        }
    }
}
