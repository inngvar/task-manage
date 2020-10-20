package com.task.manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import java.util.HashMap;
import java.util.Map;

public class ApplicationWebXml extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        // set a default to use when no profile is configured.
        DefaultProfileUtil.addDefaultProfile(application.application());
        return application.sources(TaskManagerApplication.class);
    }

    public static class DefaultProfileUtil {

        private static final String SPRING_PROFILE_DEFAULT = "spring.profiles.default";

        private DefaultProfileUtil() {
        }

        /**
         * Set a default to use when no profile is configured.
         *
         * @param app the Spring application.
         */
        public static void addDefaultProfile(SpringApplication app) {
            Map<String, Object> defProperties = new HashMap<>();
            /*
             * The default profile to use when no other profiles are defined
             * This cannot be set in the application.yml file.
             * See https://github.com/spring-projects/spring-boot/issues/1219
             */
            defProperties.put(SPRING_PROFILE_DEFAULT, "dev");
            app.setDefaultProperties(defProperties);
        }
    }
}
