package vlsurhai.worker.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import vlsurhai.common.config.BootConfiguration;

@Import({ BootConfiguration.class })
@ComponentScan(basePackages = { "vlsurhai.worker", "vlsurhai.storage", "vlsurhai.common" })
@ImportResource("classpath:camel-routes.xml")
@SpringBootApplication
public class WorkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorkerApplication.class);
    }
}
