package org.apache.coheigea.cxf.sts;

import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
public class Application {

  @Value("${keycloak.address}")
  String address;

  @Value("${keycloak.realm}")
  String realm;

  /*@Bean
	public ServletRegistrationBean dispatcherServlet() {
		return new ServletRegistrationBean(new CXFServlet(), "/*");
  }*/
  
  public static void main(String[] args) throws Exception {
    SpringApplication.run(Application.class, args);
  }

  @Bean
  public SpringBus springBus() {
      return new SpringBus();
  }

  @Bean
  public KeycloakAuthValidator keycloakAuthValidator() {
    KeycloakAuthValidator keycloakAuthValidator = new KeycloakAuthValidator();
    keycloakAuthValidator.setAddress(address);
    keycloakAuthValidator.setRealm(realm);
    return keycloakAuthValidator;
  }

}