package salex.messenger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import salex.messenger.config.JwtConfig;
import salex.messenger.config.LocalStorageConfig;

@SpringBootApplication
@EnableJdbcRepositories
@EnableJpaRepositories
@EnableConfigurationProperties({JwtConfig.class, LocalStorageConfig.class})
@EnableWebSecurity
public class MessengerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MessengerApplication.class, args);
    }
}
