package com.luna.lilitalk.persistence.config;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaAuditing // JPA에 대한 감사 기능 @CreatedDate
@EnableJpaRepositories(basePackages = {"com.luna.lilitalk.persistence.repository"})
@EntityScan(basePackages = {"com.luna.lilitalk.domain.model"})
public class JpaConfig {

}
