/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.util;

import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.function.IntFunction;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import com.terraforged.engine.util.pos.PosIterator;
import com.terraforged.noise.util.NoiseUtil;

public class RollingGrid<T> {
    private final int size;
    private final int half;
    private final T[] grid;
    private final Generator<T> generator;
    private int startX = 0;
    private int startZ = 0;

    public RollingGrid(int size, IntFunction<T[]> constructor, Generator<T> generator) {
        this.size = size;
        this.half = size / 2;
        this.generator = generator;
        this.grid = constructor.apply(size * size);
    }

    public Iterable<T> getIterator() {
        return Arrays.asList(this.grid);
    }

    public PosIterator iterator() {
        return PosIterator.area(this.startX, this.startZ, this.size, this.size);
    }

    public int getStartX() {
        return this.startX;
    }

    public int getStartZ() {
        return this.startZ;
    }

    public int getSize() {
        return this.size;
    }

    public void setCenter(int seed, int x, int z) {
        this.setCenter(seed, x - this.half, z - this.half, true);
    }

    public void setCenter(int seed, int x, int z, boolean update) {
        this.setPos(seed, x - this.half, z - this.half, update);
    }

    public void setPos(int seed, int x, int z) {
        this.setPos(seed, x, z, true);
    }

    public void setPos(int seed, int x, int z, boolean update) {
        if (update) {
            int deltaX = x - this.startX;
            int deltaZ = z - this.startZ;
            this.move(seed, deltaX, deltaZ);
        } else {
            this.startX = x;
            this.startZ = z;
        }
    }

    public void move(int seed, int x, int z) {
        int index;
        if (x != 0) {
            int minX = x < 0 ? this.startX + x : this.startX + this.size - x + 1;
            int maxX = minX + Math.abs(x);
            for (int px = minX; px < maxX; ++px) {
                int dx = this.wrap(px);
                for (int dz = 0; dz < this.size; ++dz) {
                    index = this.index(dx, this.wrap(this.startZ + dz));
                    this.grid[index] = this.generator.generate(seed, px, this.startZ + dz);
                }
            }
        }
        if (z != 0) {
            int minZ = z < 0 ? this.startZ + z : this.startZ + this.size - z + 1;
            int maxZ = minZ + Math.abs(z);
            for (int pz = minZ; pz < maxZ; ++pz) {
                int dz = this.wrap(pz);
                for (int dx = 0; dx < this.size; ++dx) {
                    index = this.index(this.wrap(this.startX + dx), dz);
                    this.grid[index] = this.generator.generate(seed, this.startX + dx, pz);
                }
            }
        }
        this.startX += x;
        this.startZ += z;
    }

    public T get(int x, int z) {
        int mx = this.wrap(x += this.startX);
        int mz = this.wrap(z += this.startZ);
        return this.grid[this.index(mx, mz)];
    }

    public void set(int x, int z, T value) {
        int mx = this.wrap(x += this.startX);
        int mz = this.wrap(z += this.startZ);
        this.grid[this.index((int)mx, (int)mz)] = value;
    }

    private int index(int x, int z) {
        return z * this.size + x;
    }

    private int wrap(int value) {
        return (value % this.size + this.size) % this.size;
    }

    public static void main(String[] args) {
        final RollingGrid<Chunk> grid = RollingGrid.createGrid(32);
        final JLabel label = new JLabel();
        label.setIcon(new ImageIcon(RollingGrid.render(0, 0, grid)));
        label.setFocusable(true);
        label.addKeyListener(new KeyAdapter(){

            @Override
            public void keyTyped(KeyEvent e) {
                switch (e.getKeyChar()) {
                    case 'w': {
                        grid.move(0, 0, -1);
                        label.setIcon(new ImageIcon(RollingGrid.render(0, 0, grid)));
                        label.repaint();
                        break;
                    }
                    case 'a': {
                        grid.move(0, -1, 0);
                        label.setIcon(new ImageIcon(RollingGrid.render(0, 0, grid)));
                        label.repaint();
                        break;
                    }
                    case 's': {
                        grid.move(0, 0, 1);
                        label.setIcon(new ImageIcon(RollingGrid.render(0, 0, grid)));
                        label.repaint();
                        break;
                    }
                    case 'd': {
                        grid.move(0, 1, 0);
                        label.setIcon(new ImageIcon(RollingGrid.render(0, 0, grid)));
                        label.repaint();
                    }
                }
            }
        });
        JFrame frame = new JFrame();
        frame.add(label);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(3);
        frame.setVisible(true);
    }

    private static RollingGrid<Chunk> createGrid(int size) {
        RollingGrid<Chunk> grid = new RollingGrid<>(size, Chunk[]::new, (seed, x, z) -> new Chunk(x, z));
        PosIterator iterator = PosIterator.area(0, 0, size, size);
        while (iterator.next()) {
            int x = iterator.x();
            int z = iterator.z();
            grid.set(x, z, new Chunk(x, z));
        }
        return grid;
    }

    private static BufferedImage render(int x, int z, RollingGrid<Chunk> grid) {
        int size = grid.size << 4;
        BufferedImage image = new BufferedImage(size, size, 1);
        PosIterator chunkIterator = PosIterator.area(0, 0, grid.size, grid.size);
        while (chunkIterator.next()) {
            int chunkX = x + chunkIterator.x();
            int chunkZ = z + chunkIterator.z();
            Chunk chunk = grid.get(chunkX, chunkZ);
            if (chunk == null) continue;
            PosIterator pixel = PosIterator.area(chunkIterator.x() << 4, chunkIterator.z() << 4, 16, 16);
            while (pixel.next()) {
                image.setRGB(pixel.x(), pixel.z(), chunk.color.getRGB());
            }
        }
        return image;
    }

    private static class Chunk {
        private final Color color;

        public Chunk(int x, int z) {
            this.color = new Color(NoiseUtil.hash(x, z));
        }
    }

    public static interface Generator<T> {
        public T generate(int seed, int var1, int var2);
    }
}

