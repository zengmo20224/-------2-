package com.petcare.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Maps uploaded files directory to a static URL path.
 * Files saved to local disk are served via /uploads/**.
 */
@Configuration
public class FileUploadConfig implements WebMvcConfigurer {

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + UPLOAD_DIR);
    }

    public static String getUploadDir() {
        return UPLOAD_DIR;
    }
}
