package sns.snsproject.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import sns.snsproject.configuration.filter.JwtTokenFilter;
import sns.snsproject.exception.CustomAuthenticationEntryPoint;
import sns.snsproject.service.UserService;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class AuthenticationConfig {

    private final UserService userService;

    @Value("${jwt.secret-key}")
    private String key;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        //csrf disable
        http
                .csrf((auth) -> auth.disable());

        //경로별 인가 작업
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorizeHttpRequestsCustomizer -> authorizeHttpRequestsCustomizer
                        .requestMatchers("/api/**").permitAll()
                        .anyRequest().authenticated());

        //세션 설정 : STATELESS
        http
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http
                .addFilterBefore(new JwtTokenFilter(key, userService), UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling()
                .authenticationEntryPoint(new CustomAuthenticationEntryPoint());
        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers("/api/")
                .requestMatchers(HttpMethod.POST, "/api/*/users/join", "/api/*/users/login");
    }
}