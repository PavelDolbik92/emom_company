package by.dolbik.cfprogram.emom_company.config;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TranslateConfig {
    @Value("${google.translate.api.key:key}")
    private String apiKey;

    @Bean
    public Translate model(){
        return TranslateOptions
                .newBuilder()
                .setApiKey(apiKey)
                .build()
                .getService();
    }
}
