package com.boclips.videoanalyser.config.security

import com.boclips.videoanalyser.presentation.VideosController.Companion.INDEXING_PROGRESS_PATH_TEMPLATE
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod.POST
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.crypto.factory.PasswordEncoderFactories


@Configuration
class VideoAnalyserSecurityConfig : WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity) {
        http
                .httpBasic()
                .and()
                .csrf().disable()
                .authorizeRequests()
                .antMatchers(POST, INDEXING_PROGRESS_PATH_TEMPLATE.replace("{videoId}", "*")).permitAll()
                .anyRequest().authenticated()
    }
}