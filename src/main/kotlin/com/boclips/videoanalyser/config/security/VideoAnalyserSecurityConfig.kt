package com.boclips.videoanalyser.config.security

import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod.POST
import org.springframework.http.HttpMethod.GET
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter


@Configuration
class VideoAnalyserSecurityConfig : WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity) {
        http
                .httpBasic()
                .and()
                .csrf().disable()
                .authorizeRequests()
                .antMatchers(POST, "/v1/videos/*/publish_analysed_video").permitAll()
                .antMatchers(GET, "/actuator/health").permitAll()
                .anyRequest().authenticated()
    }
}
