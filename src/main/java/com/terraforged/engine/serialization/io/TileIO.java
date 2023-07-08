/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.serialization.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.terraforged.engine.concurrent.Disposable;
import com.terraforged.engine.tile.Tile;
import com.terraforged.engine.tile.gen.TileResources;
import com.terraforged.engine.util.pos.PosUtil;

public class TileIO {

	public static DataInputStream getInput(Path dir, int x, int z) throws IOException {
        if (!Files.exists(dir, new LinkOption[0])) {
            Files.createDirectories(dir, new FileAttribute[0]);
        }
        Path path = dir.resolve(TileIO.getFileName(x, z));
        return new DataInputStream(new GZIPInputStream(new BufferedInputStream(Files.newInputStream(path, new OpenOption[0]))));
    }

    public static DataOutputStream getOutput(Path dir, Tile tile) throws IOException {
        if (!Files.exists(dir, new LinkOption[0])) {
            Files.createDirectories(dir, new FileAttribute[0]);
        }
        Path path = dir.resolve(TileIO.getFileName(tile));
        return new DataOutputStream(new GZIPOutputStream(new BufferedOutputStream(Files.newOutputStream(path, new OpenOption[0]))));
    }

    public static String getFileName(Tile tile) {
        return TileIO.getFileName(tile.getRegionX(), tile.getRegionZ());
    }

    public static String getFileName(int rx, int rz) {
        return PosUtil.pack(rx, rz) + ".tile";
    }

    public static void writeTo(Tile tile, DataOutput out) throws IOException {
        out.writeInt(tile.getRegionX());
        out.writeInt(tile.getRegionZ());
        out.writeInt(tile.getGenerationSize());
        TileIO.writeCells(tile, out);
    }

    public static Tile readFrom(DataInput in, TileResources resources, Disposable.Listener<Tile> listener) throws IOException {
        int x = in.readInt();
        int z = in.readInt();
        int size = in.readInt();
        Tile tile = new Tile(x, z, size, 0, resources, listener);
        TileIO.readCells(in, tile);
        return tile;
    }

    private static void writeCells(Tile tile, DataOutput out) throws IOException {
        try {
            tile.iterate((cell, dx, dz) -> {
                try {
                    CellIO.writeTo(cell, out);
                }
                catch (IOException e) {
                    throw new RuntimeIOException(e);
                }
            });
        }
        catch (RuntimeIOException e) {
            throw e.getCause();
        }
    }

    private static void readCells(DataInput in, Tile tile) throws IOException {
        try {
            tile.generate((cell, dx, dz) -> {
                try {
                    CellIO.readTo(in, cell);
                }
                catch (IOException e) {
                    throw new RuntimeIOException(e);
                }
            });
        }
        catch (RuntimeIOException e) {
            throw e.getCause();
        }
    }
}

