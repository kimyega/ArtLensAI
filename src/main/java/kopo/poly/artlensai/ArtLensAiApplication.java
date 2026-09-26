package kopo.poly.artlensai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableJpaRepositories
@EnableScheduling
@SpringBootApplication
public class ArtLensAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ArtLensAiApplication.class, args);
    }

}
