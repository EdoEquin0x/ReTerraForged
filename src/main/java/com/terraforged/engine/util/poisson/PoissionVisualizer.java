/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.util.poisson;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import com.terraforged.noise.Source;

public class PoissionVisualizer {
	
    public static void main(String[] args) {
        int size = 512;
        int radius = 5;
        int chunkSize = 16;
        int chunks = size / chunkSize;
        BufferedImage image = new BufferedImage(size, size, 1);
        Poisson poisson = new Poisson(radius);
        PoissonContext context = new PoissonContext(213L, new Random());
        context.density = Source.simplex(213, 200, 2).clamp(0.25, 0.75).map(0.0, 1.0);
        long time = 0L;
        long count = 0L;
        int chunkX = 342;
        int chunkZ = 546;
        for (int cz = 0; cz < chunks; ++cz) {
            for (int cx = 0; cx < chunks; ++cx) {
                long start = System.nanoTime();
                poisson.visit(213, chunkX + cx, chunkZ + cz, context, (x, z) -> {
                    if ((x -= chunkX << 4) < 0 || x >= image.getWidth() || (z -= chunkZ << 4) < 0 || z >= image.getHeight()) {
                        return;
                    }
                    image.setRGB(x, z, Color.WHITE.getRGB());
                });
                time += System.nanoTime() - start;
                ++count;
            }
        }
        double total = (double)time / 1000000.0;
        double avg = total / (double)count;
        System.out.printf("Total time: %.3fms, Average Per Chunk: %.3fms\n", total, avg);
        JFrame frame = new JFrame();
        frame.add(new JLabel(new ImageIcon(image)));
        frame.setVisible(true);
        frame.pack();
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(3);
    }
}

