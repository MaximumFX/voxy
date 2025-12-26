package me.cortex.voxy.client.core.gl;

import me.cortex.voxy.client.core.gpu.GpuBuffer;
import me.cortex.voxy.common.util.TrackedObject;

import static org.lwjgl.opengl.ARBMapBufferRange.GL_MAP_WRITE_BIT;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL45C.*;

public class GlPersistentMappedBuffer extends TrackedObject implements GpuBuffer {
    public final int id;
    private final long size;
    private final long addr;
    public GlPersistentMappedBuffer(long size, int flags) {
        this.id = glCreateBuffers();
        this.size = size;
        glNamedBufferStorage(this.id, size, GL_MAP_PERSISTENT_BIT|(flags&(GL_MAP_COHERENT_BIT|GL_MAP_WRITE_BIT|GL_MAP_READ_BIT|GL_CLIENT_STORAGE_BIT)));
        this.addr = nglMapNamedBufferRange(this.id, 0, size, (flags&(GL_MAP_WRITE_BIT|GL_MAP_READ_BIT|GL_MAP_UNSYNCHRONIZED_BIT|GL_MAP_FLUSH_EXPLICIT_BIT))|GL_MAP_PERSISTENT_BIT);
    }

    @Override
    public void free() {
        this.free0();
        glUnmapNamedBuffer(this.id);
        glDeleteBuffers(this.id);
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
        return false;
    }

    @Override
    public GlPersistentMappedBuffer zero() {
        throw new UnsupportedOperationException("Persistent mapped buffers do not support zero()");
    }

    @Override
    public GlPersistentMappedBuffer zeroRange(long offset, long size) {
        throw new UnsupportedOperationException("Persistent mapped buffers do not support zeroRange()");
    }

    @Override
    public GlPersistentMappedBuffer fill(int data) {
        throw new UnsupportedOperationException("Persistent mapped buffers do not support fill()");
    }

    public long addr() {
        return this.addr;
    }

    @Override
    public long mappedAddress() {
        return this.addr;
    }

    @Override
    public GlPersistentMappedBuffer name(String name) {
        return GlDebug.name(name, this);
    }

    @Override
    public void close() {
        this.free();
    }
}
