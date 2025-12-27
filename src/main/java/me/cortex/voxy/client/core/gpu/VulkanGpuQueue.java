package me.cortex.voxy.client.core.gpu;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

final class VulkanGpuQueue {
    private final ExecutorService executor;
    private final VulkanFrameSync frameSync = new VulkanFrameSync(2);

    VulkanGpuQueue() {
        ThreadFactory factory = runnable -> {
            Thread thread = new Thread(runnable, "VulkanGpuQueue");
            thread.setDaemon(true);
            return thread;
        };
        this.executor = Executors.newSingleThreadExecutor(factory);
    }

    void submit(VulkanGpuCommandList commandList, VulkanGpuFence fence) {
        VulkanGpuFence frameFence = this.frameSync.acquire();
        this.executor.execute(() -> {
            commandList.awaitSemaphores();
            commandList.execute();
            commandList.signalSemaphores();
            frameFence.signal();
            if (fence != null) {
                fence.signal();
            }
        });
    }

    void waitForIdle() {
        var completion = new java.util.concurrent.CompletableFuture<Void>();
        this.executor.execute(() -> completion.complete(null));
        completion.join();
    }

    private static final class VulkanFrameSync {
        private final VulkanGpuFence[] inFlight;
        private int frameIndex;

        private VulkanFrameSync(int maxFrames) {
            this.inFlight = new VulkanGpuFence[maxFrames];
        }

        synchronized VulkanGpuFence acquire() {
            int index = this.frameIndex++ % this.inFlight.length;
            VulkanGpuFence fence = this.inFlight[index];
            if (fence != null) {
                fence.await();
            }
            VulkanGpuFence nextFence = new VulkanGpuFence();
            this.inFlight[index] = nextFence;
            return nextFence;
        }
    }
}
