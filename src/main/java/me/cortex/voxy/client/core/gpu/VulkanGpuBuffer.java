package me.cortex.voxy.client.core.gpu;

import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

final class VulkanGpuBuffer implements GpuBuffer {
    private static final AtomicInteger NEXT_ID = new AtomicInteger(1);

    private final int id;
    private final long size;
    private final boolean sparse;
    private final ByteBuffer deviceMemory;
    private final ByteBuffer mappedBuffer;
    private final boolean ownsDeviceMemory;
    private String name;

    private VulkanGpuBuffer(long size, boolean sparse, ByteBuffer mappedBuffer, String name) {
        this.id = NEXT_ID.getAndIncrement();
        this.size = size;
        this.sparse = sparse;
        this.mappedBuffer = mappedBuffer;
        if (mappedBuffer == null) {
            this.deviceMemory = MemoryUtil.memAlloc((int) size);
        } else {
            this.deviceMemory = mappedBuffer;
        }
        this.ownsDeviceMemory = true;
        this.name = name;
        MemoryUtil.memSet(MemoryUtil.memAddress(this.deviceMemory), 0, this.deviceMemory.remaining());
    }

    static VulkanGpuBuffer unmapped(GpuDevice.BufferDescriptor descriptor) {
        return new VulkanGpuBuffer(descriptor.sizeBytes(), descriptor.flags() != 0, null, descriptor.label());
    }

    static VulkanGpuBuffer mapped(GpuDevice.MappedBufferDescriptor descriptor) {
        ByteBuffer buffer = MemoryUtil.memAlloc((int) descriptor.sizeBytes());
        return new VulkanGpuBuffer(descriptor.sizeBytes(), false, buffer, descriptor.label());
    }

    @Override
    public int id() {
        return this.id;
    }

    @Override
    public long size() {
        return this.size;
    }

    @Override
    public boolean isSparse() {
        return this.sparse;
    }

    @Override
    public GpuBuffer zero() {
        if (this.mappedBuffer != null) {
            MemoryUtil.memSet(MemoryUtil.memAddress(this.mappedBuffer), 0, this.mappedBuffer.remaining());
        }
        return this;
    }

    @Override
    public GpuBuffer zeroRange(long offset, long size) {
        if (this.mappedBuffer != null) {
            long start = Math.min(offset, this.mappedBuffer.remaining());
            long length = Math.min(size, this.mappedBuffer.remaining() - start);
            MemoryUtil.memSet(MemoryUtil.memAddress(this.mappedBuffer) + start, 0, length);
        }
        return this;
    }

    @Override
    public GpuBuffer fill(int data) {
        if (this.mappedBuffer != null) {
            for (int i = 0; i + 4 <= this.mappedBuffer.capacity(); i += 4) {
                this.mappedBuffer.putInt(i, data);
            }
        }
        return this;
    }

    @Override
    public GpuBuffer name(String name) {
        this.name = name;
        return this;
    }

    @Override
    public long mappedAddress() {
        if (this.mappedBuffer == null) {
            return GpuBuffer.super.mappedAddress();
        }
        return MemoryUtil.memAddress(this.mappedBuffer);
    }

    long deviceAddress() {
        return MemoryUtil.memAddress(this.deviceMemory);
    }

    @Override
    public void close() {
        if (this.ownsDeviceMemory) {
            MemoryUtil.memFree(this.deviceMemory);
        }
    }

    @Override
    public String toString() {
        return "VulkanGpuBuffer{id=" + this.id + ", name=" + this.name + "}";
    }
}
