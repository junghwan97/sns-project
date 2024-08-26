package sns.snsproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SnsProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(SnsProjectApplication.class, args);
    }

}
