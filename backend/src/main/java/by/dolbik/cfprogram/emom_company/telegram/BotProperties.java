package by.dolbik.cfprogram.emom_company.telegram;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "bot")
@Data
public class BotProperties {
    private String name;
    private String token;
}