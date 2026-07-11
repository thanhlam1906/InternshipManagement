package com.example.internshipmanagement.config;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties(prefix = "external-api")
@Getter
@Setter
@Validated
public class ExternalApiProperties {

    private JSearch jsearch = new JSearch();
    private Gemini gemini = new Gemini();
    private Groq groq = new Groq();

    @Getter
    @Setter
    public static class JSearch {
        private String baseUrl = "https://api.openwebninja.com";

        @NotEmpty
        private String apiKey;
    }

    @Getter
    @Setter
    public static class Gemini {
        private String baseUrl = "https://generativelanguage.googleapis.com/v1beta";
        private String model = "gemini-2.0-flash";

        @NotEmpty
        private String apiKey;
    }

    @Getter
    @Setter
    public static class Groq {
        private String baseUrl = "https://api.groq.com/openai";
        private String model = "llama-3.3-70b-versatile";
    }
}
