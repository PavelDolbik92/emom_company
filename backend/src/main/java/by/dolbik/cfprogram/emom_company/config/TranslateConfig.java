package by.dolbik.cfprogram.emom_company.config;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TranslateConfig {

    @Bean
    public Translate model(){
        return TranslateOptions
                .newBuilder()
                .setApiKey("AIzaSyB8y1dUci_E6lm6stlskoRsAUXhp76iPlY")
                .build()
                .getService();
    }
}
