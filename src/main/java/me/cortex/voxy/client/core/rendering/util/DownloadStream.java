package me.cortex.voxy.client.core.rendering.util;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import me.cortex.voxy.client.VoxyClient;
import me.cortex.voxy.client.core.gpu.GpuBackend;
import me.cortex.voxy.client.core.gpu.GpuBuffer;
import me.cortex.voxy.client.core.gpu.GpuDevice;
import me.cortex.voxy.client.core.gpu.GpuFence;
import me.cortex.voxy.common.Logger;
import me.cortex.voxy.common.util.AllocationArena;
import me.cortex.voxy.common.util.MemoryBuffer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.function.Consumer;

import static me.cortex.voxy.common.util.AllocationArena.SIZE_LIMIT;
import static org.lwjgl.opengl.GL30C.GL_MAP_READ_BIT;
import static org.lwjgl.opengl.GL42.GL_BUFFER_UPDATE_BARRIER_BIT;
import static org.lwjgl.opengl.GL44.GL_CLIENT_MAPPED_BUFFER_BARRIER_BIT;

public class DownloadStream {
    public interface DownloadResultConsumer {
        void consume(long ptr, long size);
    }

    private final AllocationArena allocationArena = new AllocationArena();
    private final GpuBuffer downloadBuffer;
    private final GpuDevice device;
    private final boolean immediateFenceSignal;

    private final Deque<DownloadFrame> frames = new ArrayDeque<>();
    private final LongArrayList thisFrameAllocations = new LongArrayList();
    private final Deque<DownloadData> downloadList = new ArrayDeque<>();
    private final ArrayList<DownloadData> thisFrameDownloadList = new ArrayList<>();

    public DownloadStream(long size) {
        this.device = VoxyClient.getBackend().device();
        this.immediateFenceSignal = VoxyClient.getBackend().backendType() == GpuBackend.BackendType.VULKAN;
        this.downloadBuffer = this.device.createMappedBuffer(new GpuDevice.MappedBufferDescriptor(size, GL_MAP_READ_BIT, "DownloadStream"));
        this.allocationArena.setLimit(size);
    }

    private long caddr = -1;
    private long offset = 0;

    //Pulls the entire buffer from the gpu
    public void download(GpuBuffer buffer, DownloadResultConsumer resultConsumer) {
        this.download(buffer, 0, buffer.size(), resultConsumer);
    }

    public void download(GpuBuffer buffer, Consumer<MemoryBuffer> resultConsumer) {
        this.download(buffer, 0, buffer.size(), resultConsumer);
    }

    public void download(GpuBuffer buffer, long downloadOffset, long size, Consumer<MemoryBuffer> consumer) {
        this.download(buffer, downloadOffset, size, (ptr,size2)-> {
            consumer.accept(MemoryBuffer.createUntrackedUnfreeableRawFrom(ptr, size));
        });
    }

    public void download(GpuBuffer buffer, long downloadOffset, long size, DownloadResultConsumer resultConsumer) {
        if (size > Integer.MAX_VALUE) {
            throw new IllegalArgumentException();
        }
        if (size <= 0) {
            throw new IllegalArgumentException();
        }
        if (downloadOffset+size > buffer.size()) {
            throw new IllegalArgumentException();
        }

        long addr;
        if (this.caddr == -1 || !this.allocationArena.expand(this.caddr, (int) size)) {
            this.caddr = this.allocationArena.alloc((int) size);//TODO: replace with allocFromLargest
            if (this.caddr == SIZE_LIMIT) {
                Logger.warn("Download stream full, preemptively committing, this could cause bad things to happen");
                this.commit();
                int attempts = 10;
                while (--attempts != 0 && this.caddr == SIZE_LIMIT) {
                    this.device.waitForIdle();
                    this.tick();
                    this.caddr = this.allocationArena.alloc((int) size);
                }
                if (this.caddr == SIZE_LIMIT) {
                    throw new IllegalStateException("Could not allocate memory segment big enough for upload even after force flush");
                }
            }
            this.thisFrameAllocations.add(this.caddr);
            this.offset = size;
            addr = this.caddr;
        } else {//Could expand the allocation so just update it
            addr = this.caddr + this.offset;
            this.offset += size;
        }

        if (this.caddr + size > this.downloadBuffer.size()) {
            throw new IllegalStateException();
        }

        this.downloadList.add(new DownloadData(buffer, addr, downloadOffset, size, resultConsumer));

        //TODO: maybe not auto-commit
        this.commit();
    }


    public void commit() {
        if (this.downloadList.isEmpty()) {
            return;
        }
        this.device.bufferBarrier(GL_BUFFER_UPDATE_BARRIER_BIT);
        //Copies all the data from target buffers into the download stream
        for (var entry : this.downloadList) {
            this.device.copyBuffer(entry.target, this.downloadBuffer, entry.targetOffset, entry.downloadStreamOffset, entry.size);
        }
        this.device.bufferBarrier(GL_CLIENT_MAPPED_BUFFER_BARRIER_BIT | GL_BUFFER_UPDATE_BARRIER_BIT);
        this.thisFrameDownloadList.addAll(this.downloadList);
        this.downloadList.clear();

        this.caddr = -1;
        this.offset = 0;
    }

    public void tick() {
        this.commit();
        if (!this.thisFrameAllocations.isEmpty()) {
            GpuFence fence = this.device.createFence();
            if (this.immediateFenceSignal) {
                fence.signal();
            }
            this.frames.add(new DownloadFrame(fence, new LongArrayList(this.thisFrameAllocations), new ArrayList<>(this.thisFrameDownloadList)));
            this.thisFrameAllocations.clear();
            this.thisFrameDownloadList.clear();
        }

        while (!this.frames.isEmpty()) {
            //Since the ordering of frames is the ordering of the gl commands if we encounter an unsignaled fence
            // all the other fences should also be unsignaled
            if (!this.frames.peek().fence.signaled()) {
                break;
            }

            //Release all the allocations from the frame
            var frame = this.frames.pop();

            //Apply all the callbacks
            for (var data : frame.data) {
                data.resultConsumer.consume(this.downloadBuffer.mappedAddress() + data.downloadStreamOffset, data.size);
            }

            frame.allocations.forEach(this.allocationArena::free);
            frame.fence.free();
        }
    }

    //Synchonize force flushes everything
    public void waitDiscard() {
        this.device.waitForIdle();
        var fence = VoxyClient.getBackend().device().createFence();
        this.device.waitForIdle();
        while (!fence.signaled())
            Thread.onSpinWait();
        fence.free();
        while (!this.frames.isEmpty()) {
            var frame = this.frames.pop();
            while (!frame.fence.signaled()) Thread.onSpinWait();
            frame.allocations.forEach(this.allocationArena::free);
            frame.fence.free();
        }
    }

    public void flushWaitClear() {
        this.device.waitForIdle();
        this.tick();
        var fence = VoxyClient.getBackend().device().createFence();
        this.device.waitForIdle();
        while (!fence.signaled())
            Thread.onSpinWait();
        fence.free();
        this.tick();
        if (!this.frames.isEmpty()) {
            throw new IllegalStateException();
        }
    }

    private record DownloadFrame(GpuFence fence, LongArrayList allocations, ArrayList<DownloadData> data) {}
    private record DownloadData(GpuBuffer target, long downloadStreamOffset, long targetOffset, long size, DownloadResultConsumer resultConsumer) {}


    // Global download stream
    public static final DownloadStream INSTANCE = new DownloadStream(1<<25);//32 mb download buffer
}
