package videoapp.web.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import videoapp.common.config.BootConfiguration;

@Import({ BootConfiguration.class })
@ComponentScan(basePackages = { "videoapp.web", "videoapp.storage", "videoapp.core", "videoapp.common" })
@SpringBootApplication
public class WebApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class);
    }
}
