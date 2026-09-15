package br.com.fiap.inovagab.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Configuration
@EnableMongoAuditing(auditorAwareRef = "securityAuditorAware")
public class MongoAuditingConfig {
}

