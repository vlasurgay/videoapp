package videoapp.worker.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.annotation.EnableScheduling;
import videoapp.common.config.BootConfiguration;

@EnableScheduling
@SpringBootApplication
@Import({ BootConfiguration.class,  })
@ComponentScan(basePackages = { "videoapp.worker", "videoapp.storage", "videoapp.common", "videoapp.core" })
public class WorkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorkerApplication.class);
    }
}
