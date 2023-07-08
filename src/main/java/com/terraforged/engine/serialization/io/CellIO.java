/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.serialization.io;

import com.terraforged.engine.cell.Cell;
import com.terraforged.engine.world.biome.type.BiomeType;
import com.terraforged.engine.world.terrain.TerrainType;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class CellIO {
    public static void readTo(DataInput in, Cell cell) throws IOException {
        cell.value = in.readFloat();
        cell.erosion = in.readFloat();
        cell.sediment = in.readFloat();
        cell.gradient = in.readFloat();
        cell.moisture = in.readFloat();
        cell.temperature = in.readFloat();
        cell.continentId = in.readFloat();
        cell.continentEdge = in.readFloat();
        cell.terrainRegionId = in.readFloat();
        cell.terrainRegionEdge = in.readFloat();
        cell.biomeRegionId = in.readFloat();
        cell.biomeRegionEdge = in.readFloat();
        cell.macroBiomeId = in.readFloat();
        cell.riverMask = in.readFloat();
        cell.continentX = in.readInt();
        cell.continentZ = in.readInt();
        cell.erosionMask = in.readBoolean();
        cell.terrain = TerrainType.get(in.readInt());
        cell.biome = BiomeType.get(in.readInt());
    }

    public static void writeTo(Cell cell, DataOutput out) throws IOException {
        out.writeFloat(cell.value);
        out.writeFloat(cell.erosion);
        out.writeFloat(cell.sediment);
        out.writeFloat(cell.gradient);
        out.writeFloat(cell.moisture);
        out.writeFloat(cell.temperature);
        out.writeFloat(cell.continentId);
        out.writeFloat(cell.continentEdge);
        out.writeFloat(cell.terrainRegionId);
        out.writeFloat(cell.terrainRegionEdge);
        out.writeFloat(cell.biomeRegionId);
        out.writeFloat(cell.biomeRegionEdge);
        out.writeFloat(cell.macroBiomeId);
        out.writeFloat(cell.riverMask);
        out.writeInt(cell.continentX);
        out.writeInt(cell.continentZ);
        out.writeBoolean(cell.erosionMask);
        out.writeInt(cell.terrain.getId());
        out.writeInt(cell.biome.getId());
    }
}

