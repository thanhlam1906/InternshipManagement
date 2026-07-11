package com.example.internshipmanagement.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "external-api")
@Getter
@Setter
public class ExternalApiProperties {

    private JSearch jsearch = new JSearch();
    private Gemini gemini = new Gemini();
    private OpenAi openai = new OpenAi();

    @Getter
    @Setter
    public static class JSearch {
        private String baseUrl = "https://api.openwebninja.com";
        private String apiKey;
    }

    @Getter
    @Setter
    public static class Gemini {
        private String baseUrl = "https://generativelanguage.googleapis.com/v1beta";
        private String model = "gemini-2.0-flash";
    }

    @Getter
    @Setter
    public static class OpenAi {
        private String baseUrl = "https://api.groq.com/openai";
        private String model = "llama-3.3-70b-versatile";
    }
}
