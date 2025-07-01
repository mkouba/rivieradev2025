package org.acme;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import io.quarkiverse.mcp.server.Progress;
import io.quarkiverse.mcp.server.ProgressTracker;
import io.quarkiverse.mcp.server.Tool;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;

public class LongRunningTools {

    @Inject
    ExecutorService executor;

    @Tool
    Uni<String> longRunning(Progress progress) {
        if (progress.token().isEmpty()) {
            return Uni.createFrom().item("Client does not support progress notifications!");
        }
        ProgressTracker tracker = progress.trackerBuilder()
                .setDefaultStep(1)
                .setTotal(10)
                .setMessageBuilder(i -> "Long running progress: " + i)
                .build();

        CompletableFuture<String> ret = new CompletableFuture<String>();
        executor.execute(() -> {
            for (int i = 0; i < 10; i++) {
                try {
                    // Do something that takes time...
                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                tracker.advanceAndForget();
            }
            ret.complete("ok");
        });
        return Uni.createFrom().completionStage(ret);
    }

}
