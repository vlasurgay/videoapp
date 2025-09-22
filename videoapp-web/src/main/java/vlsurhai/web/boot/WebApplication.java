package vlsurhai.web.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = "vlsurhai")
@Import(vlsurhai.storage.s3.config.S3Config.class)
@EntityScan(basePackages = "vlsurhai.common.model")
@EnableJpaRepositories(basePackages = "vlsurhai.storage.jpa")
public class WebApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class);
    }
}
