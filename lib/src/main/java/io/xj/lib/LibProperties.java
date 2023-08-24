package io.xj.lib;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:application-lib.properties")
public class LibProperties {
  // Configuration goes here
}

