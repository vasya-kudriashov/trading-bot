package bux.bot;

import bux.bot.service.positiion.ClosePositionService;
import bux.bot.service.positiion.OpenPositionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({"bux.bot.*"})
public class AppConfig {
    @Bean
    public OpenPositionService openPositionService() {
        return new OpenPositionService();
    }

    @Bean
    public ClosePositionService closePositionService() {
        return new ClosePositionService();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
