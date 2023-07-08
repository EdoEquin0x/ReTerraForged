/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.tile;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import com.terraforged.engine.cell.Cell;
import com.terraforged.engine.concurrent.Disposable;
import com.terraforged.engine.concurrent.Resource;
import com.terraforged.engine.concurrent.batch.Batcher;
import com.terraforged.engine.concurrent.cache.SafeCloseable;
import com.terraforged.engine.filter.Filterable;
import com.terraforged.engine.tile.chunk.ChunkBatchTask;
import com.terraforged.engine.tile.chunk.ChunkGenTask;
import com.terraforged.engine.tile.chunk.ChunkReader;
import com.terraforged.engine.tile.chunk.ChunkStripeBatchTask;
import com.terraforged.engine.tile.chunk.ChunkWriter;
import com.terraforged.engine.tile.gen.TileResources;
import com.terraforged.engine.util.pos.PosUtil;
import com.terraforged.engine.world.heightmap.Heightmap;
import com.terraforged.engine.world.heightmap.HeightmapCache;
import com.terraforged.engine.world.rivermap.Rivermap;
import com.terraforged.noise.util.NoiseUtil;

public class Tile
implements Disposable,
SafeCloseable {
    protected final int regionX;
    protected final int regionZ;
    protected final int chunkX;
    protected final int chunkZ;
    protected final int blockX;
    protected final int blockZ;
    protected final int border;
    protected final int chunkCount;
    protected final int size;
    protected final Size blockSize;
    protected final Size chunkSize;
    protected final Cell[] blocks;
    protected final GenChunk[] chunks;
    protected final Resource<Cell[]> blockResource;
    protected final Resource<GenChunk[]> chunkResource;
    protected final AtomicInteger active = new AtomicInteger();
    protected final AtomicInteger disposed = new AtomicInteger();
    protected final Disposable.Listener<Tile> listener;

    public Tile(int regionX, int regionZ, int size, int borderChunks, TileResources resources, Disposable.Listener<Tile> listener) {
        this.size = size;
        this.regionX = regionX;
        this.regionZ = regionZ;
        this.listener = listener;
        this.chunkX = regionX << size;
        this.chunkZ = regionZ << size;
        this.blockX = Size.chunkToBlock(this.chunkX);
        this.blockZ = Size.chunkToBlock(this.chunkZ);
        this.border = borderChunks;
        this.chunkSize = Size.chunks(size, borderChunks);
        this.blockSize = Size.blocks(size, borderChunks);
        this.chunkCount = this.chunkSize.size * this.chunkSize.size;
        this.blockResource = resources.blocks.get(this.blockSize.arraySize);
        this.chunkResource = resources.chunks.get(this.chunkSize.arraySize);
        this.blocks = this.blockResource.get();
        this.chunks = this.chunkResource.get();
    }

    public int getGenerationSize() {
        return this.size;
    }

    @Override
    public void dispose() {
        if (this.disposed.incrementAndGet() >= this.chunkCount) {
            this.listener.onDispose(this);
        }
    }

    @Override
    public void close() {
        if (this.active.compareAndSet(0, -1)) {
            if (this.blockResource.isOpen()) {
                for (Cell cell : this.blocks) {
                    if (cell == null) continue;
                    cell.reset();
                }
                this.blockResource.close();
            }
            if (this.chunkResource.isOpen()) {
                Arrays.fill(this.chunks, null);
                this.chunkResource.close();
            }
        }
    }

    public long getRegionId() {
        return Tile.getRegionId(this.getRegionX(), this.getRegionZ());
    }

    public int getRegionX() {
        return this.regionX;
    }

    public int getRegionZ() {
        return this.regionZ;
    }

    public int getBlockX() {
        return this.blockX;
    }

    public int getBlockZ() {
        return this.blockZ;
    }

    public int getOffsetChunks() {
        return this.border;
    }

    public int getChunkCount() {
        return this.chunks.length;
    }

    public int getBlockCount() {
        return this.blocks.length;
    }

    public Size getChunkSize() {
        return this.chunkSize;
    }

    public Size getBlockSize() {
        return this.blockSize;
    }

    public Filterable filterable() {
        return new FilterRegion();
    }

    public Cell getCell(int blockX, int blockZ) {
        int relBlockX = this.blockSize.border + this.blockSize.mask(blockX);
        int relBlockZ = this.blockSize.border + this.blockSize.mask(blockZ);
        int index = this.blockSize.indexOf(relBlockX, relBlockZ);
        return this.blocks[index];
    }

    public Cell getRawCell(int blockX, int blockZ) {
        int index = this.blockSize.indexOf(blockX, blockZ);
        return this.blocks[index];
    }

    public ChunkWriter getChunkWriter(int chunkX, int chunkZ) {
        int index = this.chunkSize.indexOf(chunkX, chunkZ);
        return this.computeChunk(index, chunkX, chunkZ);
    }

    public ChunkReader getChunk(int chunkX, int chunkZ) {
        int relChunkX = this.chunkSize.border + this.chunkSize.mask(chunkX);
        int relChunkZ = this.chunkSize.border + this.chunkSize.mask(chunkZ);
        int index = this.chunkSize.indexOf(relChunkX, relChunkZ);
        return this.chunks[index].open();
    }

    public void generate(Consumer<ChunkWriter> consumer) {
        for (int cz = 0; cz < this.chunkSize.total; ++cz) {
            for (int cx = 0; cx < this.chunkSize.total; ++cx) {
                int index = this.chunkSize.indexOf(cx, cz);
                GenChunk chunk = this.computeChunk(index, cx, cz);
                consumer.accept(chunk);
            }
        }
    }

    public void generate(int seed, Heightmap heightmap) {
        Rivermap riverMap = null;
        for (int cz = 0; cz < this.chunkSize.total; ++cz) {
            for (int cx = 0; cx < this.chunkSize.total; ++cx) {
                int index = this.chunkSize.indexOf(cx, cz);
                GenChunk chunk = this.computeChunk(index, cx, cz);
                for (int dz = 0; dz < 16; ++dz) {
                    for (int dx = 0; dx < 16; ++dx) {
                        float x = chunk.getBlockX() + dx;
                        float z = chunk.getBlockZ() + dz;
                        Cell cell = chunk.genCell(dx, dz);
                        heightmap.applyBase(seed, cell, x, z);
                        riverMap = Rivermap.get(seed, cell, riverMap, heightmap);
                        heightmap.applyRivers(seed, cell, x, z, riverMap);
                        heightmap.applyClimate(seed, cell, x, z);
                    }
                }
            }
        }
    }

    public void generate(int seed, HeightmapCache heightmap) {
        Rivermap riverMap = null;
        for (int cz = 0; cz < this.chunkSize.total; ++cz) {
            for (int cx = 0; cx < this.chunkSize.total; ++cx) {
                int index = this.chunkSize.indexOf(cx, cz);
                GenChunk chunk = this.computeChunk(index, cx, cz);
                for (int dz = 0; dz < 16; ++dz) {
                    for (int dx = 0; dx < 16; ++dx) {
                        int x = chunk.getBlockX() + dx;
                        int z = chunk.getBlockZ() + dz;
                        Cell cell = chunk.genCell(dx, dz);
                        riverMap = heightmap.generate(seed, cell, x, z, riverMap);
                    }
                }
            }
        }
    }

    public void generate(int seed, Heightmap heightmap, Batcher batcher) {
        for (int cz = 0; cz < this.chunkSize.total; ++cz) {
            for (int cx = 0; cx < this.chunkSize.total; ++cx) {
                int index = this.chunkSize.indexOf(cx, cz);
                GenChunk chunk = this.computeChunk(index, cx, cz);
                batcher.submit(new ChunkGenTask(seed, chunk, heightmap));
            }
        }
    }

    public void generate(int seed, Heightmap heightmap, float offsetX, float offsetZ, float zoom) {
        Rivermap riverMap = null;
        float translateX = offsetX - (float)this.blockSize.size * zoom / 2.0f;
        float translateZ = offsetZ - (float)this.blockSize.size * zoom / 2.0f;
        for (int cz = 0; cz < this.chunkSize.total; ++cz) {
            for (int cx = 0; cx < this.chunkSize.total; ++cx) {
                int index = this.chunkSize.indexOf(cx, cz);
                GenChunk chunk = this.computeChunk(index, cx, cz);
                for (int dz = 0; dz < 16; ++dz) {
                    for (int dx = 0; dx < 16; ++dx) {
                        float x = (float)(chunk.getBlockX() + dx) * zoom + translateX;
                        float z = (float)(chunk.getBlockZ() + dz) * zoom + translateZ;
                        Cell cell = chunk.genCell(dx, dz);
                        heightmap.applyBase(seed, cell, x, z);
                        riverMap = Rivermap.get(seed, cell, riverMap, heightmap);
                        heightmap.applyRivers(seed, cell, x, z, riverMap);
                        heightmap.applyClimate(seed, cell, x, z);
                    }
                }
            }
        }
    }

    public void generate(int seed, HeightmapCache heightmap, float offsetX, float offsetZ, float zoom) {
        Rivermap riverMap = null;
        float translateX = offsetX - (float)this.blockSize.size * zoom / 2.0f;
        float translateZ = offsetZ - (float)this.blockSize.size * zoom / 2.0f;
        for (int cz = 0; cz < this.chunkSize.total; ++cz) {
            for (int cx = 0; cx < this.chunkSize.total; ++cx) {
                int index = this.chunkSize.indexOf(cx, cz);
                GenChunk chunk = this.computeChunk(index, cx, cz);
                for (int dz = 0; dz < 16; ++dz) {
                    for (int dx = 0; dx < 16; ++dx) {
                        float x = (float)(chunk.getBlockX() + dx) * zoom + translateX;
                        float z = (float)(chunk.getBlockZ() + dz) * zoom + translateZ;
                        int px = NoiseUtil.floor(x);
                        int pz = NoiseUtil.floor(z);
                        Cell cell = chunk.genCell(dx, dz);
                        riverMap = heightmap.generate(seed, cell, px, pz, riverMap);
                    }
                }
            }
        }
    }

    public void generate(int seed, Heightmap heightmap, Batcher batcher, float offsetX, float offsetZ, float zoom) {
        float translateX = offsetX - (float)this.blockSize.size * zoom / 2.0f;
        float translateZ = offsetZ - (float)this.blockSize.size * zoom / 2.0f;
        batcher.size(this.chunkSize.total * this.chunkSize.total);
        for (int cz = 0; cz < this.chunkSize.total; ++cz) {
            for (int cx = 0; cx < this.chunkSize.total; ++cx) {
                int index = this.chunkSize.indexOf(cx, cz);
                GenChunk chunk = this.computeChunk(index, cx, cz);
                batcher.submit(new ChunkGenTask.Zoom(seed, chunk, heightmap, translateX, translateZ, zoom));
            }
        }
    }

    public void generateArea(int seed, Heightmap heightmap, Batcher batcher, int batchCount) {
        batcher.size(batchCount * batchCount);
        int batchSize = Tile.getBatchSize(batchCount, this.chunkSize);
        for (int dz = 0; dz < batchCount; ++dz) {
            int cz = dz * batchSize;
            for (int dx = 0; dx < batchCount; ++dx) {
                int cx = dx * batchSize;
                batcher.submit(new ChunkBatchTask(seed, cx, cz, batchSize, this, heightmap));
            }
        }
    }

    public void generateArea(int seed, Heightmap heightmap, Batcher batcher, int batchCount, float offsetX, float offsetZ, float zoom) {
        batcher.size(batchCount * batchCount);
        int batchSize = Tile.getBatchSize(batchCount, this.chunkSize);
        float translateX = offsetX - (float)this.blockSize.size * zoom / 2.0f;
        float translateZ = offsetZ - (float)this.blockSize.size * zoom / 2.0f;
        for (int dz = 0; dz < batchCount; ++dz) {
            int cz = dz * batchSize;
            for (int dx = 0; dx < batchCount; ++dx) {
                int cx = dx * batchSize;
                batcher.submit(new ChunkBatchTask.Zoom(seed, cx, cz, batchSize, this, heightmap, translateX, translateZ, zoom));
            }
        }
    }

    public void generateAreaStriped(int seed, Heightmap heightmap, Batcher batcher, int sections) {
        batcher.size(this.chunkSize.total * sections);
        int sectionLength = Tile.getBatchSize(sections, this.chunkSize);
        for (int cz = 0; cz < this.chunkSize.total; ++cz) {
            for (int s = 0; s < sections; ++s) {
                int cx = s * sectionLength;
                batcher.submit(new ChunkStripeBatchTask(seed, cx, cz, sectionLength, this, heightmap));
            }
        }
    }

    public void generateAreaStriped(int seed, Heightmap heightmap, Batcher batcher, int sections, float offsetX, float offsetZ, float zoom) {
        batcher.size(this.chunkSize.total * sections);
        int sectionLength = Tile.getBatchSize(sections, this.chunkSize);
        float translateX = offsetX - (float)this.blockSize.size * zoom / 2.0f;
        float translateZ = offsetZ - (float)this.blockSize.size * zoom / 2.0f;
        for (int cz = 0; cz < this.chunkSize.total; ++cz) {
            for (int s = 0; s < sections; ++s) {
                int cx = s * sectionLength;
                batcher.submit(new ChunkStripeBatchTask.Zoom(seed, cx, cz, sectionLength, this, heightmap, translateX, translateZ, zoom));
            }
        }
    }

    public void iterate(Cell.Visitor visitor) {
        for (int dz = 0; dz < this.blockSize.size; ++dz) {
            int z = this.blockSize.border + dz;
            for (int dx = 0; dx < this.blockSize.size; ++dx) {
                int x = this.blockSize.border + dx;
                int index = this.blockSize.indexOf(x, z);
                Cell cell = this.blocks[index];
                visitor.visit(cell, dx, dz);
            }
        }
    }

    public void generate(Cell.Visitor visitor) {
        for (int dz = 0; dz < this.blockSize.size; ++dz) {
            int z = this.blockSize.border + dz;
            for (int dx = 0; dx < this.blockSize.size; ++dx) {
                int x = this.blockSize.border + dx;
                int index = this.blockSize.indexOf(x, z);
                Cell cell = this.computeCell(index);
                visitor.visit(cell, dx, dz);
            }
        }
    }

    protected GenChunk computeChunk(int index, int chunkX, int chunkZ) {
        GenChunk chunk = this.chunks[index];
        if (chunk == null) {
            this.chunks[index] = chunk = new GenChunk(chunkX, chunkZ);
        }
        return chunk;
    }

    protected Cell computeCell(int index) {
        Cell cell = this.blocks[index];
        if (cell == null) {
            this.blocks[index] = cell = new Cell();
        }
        return cell;
    }

    protected static int getBatchSize(int batchCount, Size chunkSize) {
        int batchSize = chunkSize.total / batchCount;
        if (batchSize * batchCount < chunkSize.total) {
            ++batchSize;
        }
        return batchSize;
    }

    public static long getRegionId(int regionX, int regionZ) {
        return PosUtil.pack(regionX, regionZ);
    }

    public static int getRegionX(long id) {
        return PosUtil.unpackLeft(id);
    }

    public static int getRegionZ(long id) {
        return PosUtil.unpackRight(id);
    }

    protected class FilterRegion
    implements Filterable {
        protected FilterRegion() {
        }

        @Override
        public int getBlockX() {
            return Tile.this.blockX;
        }

        @Override
        public int getBlockZ() {
            return Tile.this.blockZ;
        }

        @Override
        public Size getSize() {
            return Tile.this.blockSize;
        }

        @Override
        public Cell[] getBacking() {
            return Tile.this.blocks;
        }

        @Override
        public Cell getCellRaw(int x, int z) {
            int index = Tile.this.blockSize.indexOf(x, z);
            if (index < 0 || index >= Tile.this.blockSize.arraySize) {
                return Cell.empty();
            }
            return Tile.this.blocks[index];
        }
    }

    public class GenChunk
    implements ChunkReader,
    ChunkWriter {
        private final int chunkX;
        private final int chunkZ;
        private final int blockX;
        private final int blockZ;
        private final int regionBlockX;
        private final int regionBlockZ;

        protected GenChunk(int regionChunkX, int regionChunkZ) {
            this.regionBlockX = regionChunkX << 4;
            this.regionBlockZ = regionChunkZ << 4;
            this.chunkX = Tile.this.chunkX + regionChunkX - Tile.this.getOffsetChunks();
            this.chunkZ = Tile.this.chunkZ + regionChunkZ - Tile.this.getOffsetChunks();
            this.blockX = this.chunkX << 4;
            this.blockZ = this.chunkZ << 4;
        }

        public GenChunk open() {
            Tile.this.active.getAndIncrement();
            return this;
        }

        @Override
        public void close() {
            Tile.this.active.decrementAndGet();
        }

        @Override
        public void dispose() {
            Tile.this.dispose();
        }

        @Override
        public int getChunkX() {
            return this.chunkX;
        }

        @Override
        public int getChunkZ() {
            return this.chunkZ;
        }

        @Override
        public int getBlockX() {
            return this.blockX;
        }

        @Override
        public int getBlockZ() {
            return this.blockZ;
        }

        @Override
        public Cell getCell(int blockX, int blockZ) {
            int relX = this.regionBlockX + (blockX & 0xF);
            int relZ = this.regionBlockZ + (blockZ & 0xF);
            int index = Tile.this.blockSize.indexOf(relX, relZ);
            return Tile.this.blocks[index];
        }

        @Override
        public Cell genCell(int blockX, int blockZ) {
            int relX = this.regionBlockX + (blockX & 0xF);
            int relZ = this.regionBlockZ + (blockZ & 0xF);
            int index = Tile.this.blockSize.indexOf(relX, relZ);
            return Tile.this.computeCell(index);
        }
    }
}

