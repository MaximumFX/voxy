package me.cortex.voxy.client.core.gpu;

import java.util.concurrent.atomic.AtomicInteger;

final class VulkanGpuTexture implements GpuTexture {
    private static final AtomicInteger NEXT_ID = new AtomicInteger(1);

    private final int id;
    private int width;
    private int height;
    private int levels;
    private int format;
    private String name;

    VulkanGpuTexture() {
        this.id = NEXT_ID.getAndIncrement();
    }

    @Override
    public int id() {
        return this.id;
    }

    @Override
    public GpuTexture store(int format, int levels, int width, int height) {
        this.format = format;
        this.levels = levels;
        this.width = width;
        this.height = height;
        return this;
    }

    @Override
    public GpuTexture createView() {
        VulkanGpuTexture view = new VulkanGpuTexture();
        view.store(this.format, this.levels, this.width, this.height);
        view.name(this.name);
        return view;
    }

    @Override
    public GpuTexture name(String name) {
        this.name = name;
        return this;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public int getLevels() {
        return this.levels;
    }

    @Override
    public int getFormat() {
        return this.format;
    }

    @Override
    public void close() {
    }

    @Override
    public String toString() {
        return "VulkanGpuTexture{id=" + this.id + ", name=" + this.name + "}";
    }
}
