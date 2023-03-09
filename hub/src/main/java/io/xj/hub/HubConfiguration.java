package io.xj.hub;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import io.xj.lib.app.AppConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.ServletRequestPathFilter;

@Configuration
public class HubConfiguration {

  @Bean
  public AppConfiguration appConfiguration() {
    return new AppConfiguration("hub");
  }

  @Bean
  public HttpTransport httpTransport() {
    return new NetHttpTransport();
  }

  @Bean
  public FilterRegistrationBean<ServletRequestPathFilter> servletRequestPathFilter() {
    FilterRegistrationBean<ServletRequestPathFilter> registrationBean = new FilterRegistrationBean<>();
    registrationBean.setFilter(new ServletRequestPathFilter());
    registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
    return registrationBean;
  }

}
