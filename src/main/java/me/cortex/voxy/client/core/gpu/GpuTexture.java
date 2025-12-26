package me.cortex.voxy.client.core.gpu;

public interface GpuTexture extends AutoCloseable {
    int id();

    GpuTexture store(int format, int levels, int width, int height);

    GpuTexture createView();

    GpuTexture name(String name);

    int getWidth();

    int getHeight();

    int getLevels();

    int getFormat();

    default void free() {
        close();
    }

    @Override
    void close();
}
