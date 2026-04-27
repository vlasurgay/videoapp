package vlsurhai.web.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import vlsurhai.common.config.BootConfiguration;

@Import({ BootConfiguration.class })
@ComponentScan(basePackages = { "vlsurhai.web", "vlsurhai.storage" })
@SpringBootApplication
public class WebApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class);
    }
}
