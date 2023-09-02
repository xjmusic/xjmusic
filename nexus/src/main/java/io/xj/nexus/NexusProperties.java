package io.xj.nexus;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:application-nexus.properties")
public class NexusProperties {
  // Configuration goes here
}

