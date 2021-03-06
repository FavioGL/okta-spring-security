package com.okta.okta.spring.example;

/*
Copyright 2017 Okta, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/


import com.okta.okta.spring.example.controller.CustomAccessDeniedHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.authentication.TokenExtractor;
import org.springframework.security.oauth2.provider.expression.OAuth2MethodSecurityExpressionHandler;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

@SpringBootApplication
public class OktaSpringSecurityRolesExampleApplication {

    private final String ACCESS_TOKEN_COOKIE_NAME = "access_token";

    private CustomAccessDeniedHandler customAccessDeniedHandler;

    @Autowired
    public OktaSpringSecurityRolesExampleApplication(CustomAccessDeniedHandler customAccessDeniedHandler) {
        this.customAccessDeniedHandler = customAccessDeniedHandler;
    }

    @EnableGlobalMethodSecurity(prePostEnabled = true)
    protected static class GlobalSecurityConfiguration extends GlobalMethodSecurityConfiguration {
        @Override
        protected MethodSecurityExpressionHandler createExpressionHandler() {
            return new OAuth2MethodSecurityExpressionHandler();
        }
    }

    @Bean
    protected ResourceServerConfigurerAdapter resourceServerConfigurerAdapter() {
        return new ResourceServerConfigurerAdapter() {

            @Override
            public void configure(HttpSecurity http) throws Exception {
                http.authorizeRequests()
                    .antMatchers("/", "/login", "/images/**").permitAll()
                    .and()
                    .exceptionHandling().accessDeniedHandler(customAccessDeniedHandler);
            }

            @Override
            public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
                resources.tokenExtractor(new TokenExtractor() {

                    @Override
                    public Authentication extract(HttpServletRequest request) {
                        String tokenValue = findCookie(ACCESS_TOKEN_COOKIE_NAME, request.getCookies());
                        if (tokenValue == null) { return null; }

                        return new PreAuthenticatedAuthenticationToken(tokenValue, "");
                    }

                    private String findCookie(String name, Cookie[] cookies) {
                        if (name == null || cookies == null) { return null; }
                        for (Cookie cookie : cookies) {
                            if (name.equals(cookie.getName())) {
                                return cookie.getValue();
                            }
                        }
                        return null;
                    }
                });
            }
        };
    }

    public static void main(String[] args) {
		SpringApplication.run(OktaSpringSecurityRolesExampleApplication.class, args);
	}
}
