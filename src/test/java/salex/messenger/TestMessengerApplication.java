package salex.messenger;

import org.springframework.boot.SpringApplication;

public class TestMessengerApplication {

    public static void main(String[] args) {
        SpringApplication.from(MessengerApplication::main)
                .with(TestcontainersConfiguration.class)
                .run(args);
    }
}
