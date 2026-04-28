package videoapp.worker.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
public class ProcessExecutor {

    public String executeWithOutput(List<String> command) {
        try {
            Process process = new ProcessBuilder(command)
                    .redirectErrorStream(true)
                    .start();

            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            if (process.waitFor() != 0) {
                throw new RuntimeException("Process execution failed: " + output);
            }
            return output;

        } catch (IOException e) {
            throw new RuntimeException("Process execution start failed", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Process execution interrupted", e);
        }
    }


    public String executePipedProcessesWithOutput(List<String> producerCmd, List<String> consumerCmd) {
        Process producer = null;
        Process consumer = null;

        try {
            producer = new ProcessBuilder(producerCmd).start();
            consumer = new ProcessBuilder(consumerCmd).start();

            Thread pipeThread = startPipeThread(producer, consumer);

            String result;
            try (InputStream consumerStdout = consumer.getInputStream()) {
                result = new String(consumerStdout.readAllBytes(), StandardCharsets.UTF_8);
            }

            int producerExit = producer.waitFor();
            int consumerExit = consumer.waitFor();

            pipeThread.join();

            if (consumerExit != 0 || producerExit != 0) {
                throw new RuntimeException("Process piping failed, consumer exit code: " + consumerExit + ", producer exit code: " + producerExit);
            }

            return result;

        } catch (Exception e) {
            if (producer != null) {
                producer.destroyForcibly();
            }
            if (consumer != null) {
                consumer.destroyForcibly();
            }

            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new RuntimeException("Process execution piping failed", e);
        }
    }

    private Thread startPipeThread(Process producer, Process consumer) {
        Thread thread = new Thread(() -> {

            try (InputStream in = producer.getInputStream(); OutputStream out = consumer.getOutputStream()) {
                in.transferTo(out);
            } catch (IOException e) {
                log.error("Failed to start a pipe thread for process", e);
            }
        });
        thread.setDaemon(true);
        thread.start();

        return thread;
    }
}
