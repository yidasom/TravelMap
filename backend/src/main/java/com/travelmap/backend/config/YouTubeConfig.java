package com.travelmap.backend.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Configuration
public class YouTubeConfig {
    
    @Value("${youtube.api.key}")
    private String apiKey;
    
    @Value("${youtube.api.application-name}")
    private String applicationName;
    
    @Bean
    public YouTube youTube() throws GeneralSecurityException, IOException {
        return new YouTube.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                null)
                .setApplicationName(applicationName)
                .build();
    }
    
    @Bean
    public String youTubeApiKey() {
        return apiKey;
    }
} 