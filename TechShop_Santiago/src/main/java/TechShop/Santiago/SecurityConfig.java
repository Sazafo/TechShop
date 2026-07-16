/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package TechShop.Santiago;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    // Rutas públicas: no necesitan iniciar sesión
    private static final String[] PUBLIC_URLS = {
        "/",
        "/index",
        "/login",
        "/acceso_denegado",
        "/css/**",
        "/js/**",
        "/fav/**",
        "/webjars/**"
    };

    // Rutas exclusivas del usuario cliente
    private static final String[] USUARIO_URLS = {
        "/facturar/carrito/**"
    };

    // Rutas que pueden consultar ADMIN y VENDEDOR
    private static final String[] ADMIN_OR_VENDEDOR_URLS = {
        "/producto/listado",
        "/categoria/listado",
        "/usuario/listado",
        "/consultas/**"
    };

    // Rutas exclusivas del administrador
    private static final String[] ADMIN_URLS = {
        "/producto/guardar",
        "/producto/eliminar",
        "/producto/modificar/**",
        "/categoria/guardar",
        "/categoria/eliminar",
        "/categoria/modificar/**",
        "/usuario/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
            .authorizeHttpRequests(auth -> auth

                // Acceso público
                .requestMatchers(PUBLIC_URLS).permitAll()

                // Acceso para clientes
                .requestMatchers(USUARIO_URLS)
                    .hasRole("USUARIO")

                // Acceso para vendedores y administradores
                .requestMatchers(ADMIN_OR_VENDEDOR_URLS)
                    .hasAnyRole("ADMIN", "VENDEDOR")

                // Acceso exclusivo del administrador
                .requestMatchers(ADMIN_URLS)
                    .hasRole("ADMIN")

                // Cualquier otra ruta requiere autenticación
                .anyRequest().authenticated()
            )

            .formLogin(login -> login
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )

            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )

            .exceptionHandling(exception -> exception
                .accessDeniedPage("/acceso_denegado")
            )

            .sessionManagement(session -> session
                .maximumSessions(1)
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(
            PasswordEncoder passwordEncoder) {

        var juan = User.builder()
                .username("juan")
                .password(passwordEncoder.encode("123"))
                .roles("ADMIN")
                .build();

        var rebeca = User.builder()
                .username("rebeca")
                .password(passwordEncoder.encode("123"))
                .roles("VENDEDOR")
                .build();

        var pedro = User.builder()
                .username("pedro")
                .password(passwordEncoder.encode("123"))
                .roles("USUARIO")
                .build();

        return new InMemoryUserDetailsManager(
                juan,
                rebeca,
                pedro
        );
    }
}