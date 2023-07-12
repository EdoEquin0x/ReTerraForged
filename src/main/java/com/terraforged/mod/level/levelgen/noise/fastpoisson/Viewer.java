/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.level.levelgen.noise.fastpoisson;

import java.awt.image.BufferedImage;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import com.terraforged.mod.noise.Source;
import com.terraforged.mod.noise.util.NoiseUtil;
import com.terraforged.mod.util.pos.PosUtil;

public class Viewer {
    public static void main(String[] args) {
        Random random = new Random(12345L);
        FastPoisson poisson = new FastPoisson();
        BufferedImage image = new BufferedImage(128, 128, 1);
        JLabel label = new JLabel(new ImageIcon(image));
        Viewer.render(0.0f, 0.0f, image, poisson, random, label);
        JFrame frame = new JFrame();
        frame.add(label);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(3);
        frame.setVisible(true);
    }

    private static void render(float ox, float oz, BufferedImage image, FastPoisson poisson, Random random, JLabel label) {
        Viewer.render(ox, oz, image, poisson, random);
        label.setIcon(new ImageIcon(image.getScaledInstance(512, 512, 2)));
        SwingUtilities.invokeLater(() -> {
            try {
                Thread.sleep(20L);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            Viewer.render(ox, oz + 0.5f, image, poisson, random, label);
        });
    }

    private static void render(float px, float pz, BufferedImage image, FastPoisson poisson, Random random) {
        int ix = (int)px;
        int iz = (int)pz;
        int chunkX = ix >> 4;
        int chunkZ = iz >> 4;
        PosUtil.iterate(0, 0, image.getWidth(), image.getHeight(), image, (dx, dz, img) -> {
            int x = NoiseUtil.round(ix + dx);
            int z = NoiseUtil.round(iz + dz);
            int xx = x & 0xF;
            int zz = z & 0xF;
            int color = xx == 0 || zz == 0 ? 0 : 0x222222;
            image.setRGB(dx, dz, color);
        });
        FastPoissonContext config = new FastPoissonContext(4, 0.75f, 0.2f, Source.ONE);
        int lengthX = image.getWidth() >> 4;
        int lengthZ = image.getHeight() >> 4;
        PosUtil.iterate(chunkX, chunkZ, lengthX + 1, lengthZ + 1, null, (cx, cz, ctx) -> {
            int color = NoiseUtil.hash(cx, cz);
            random.setSeed(PosUtil.pack(cx, cz));
            poisson.visit(1, cx, cz, random, config, image, (x, z, img) -> {
                int relX = x - ix;
                int relZ = z - iz;
                if (PosUtil.contains(relX, relZ, 0, 0, image.getWidth(), image.getHeight())) {
                    image.setRGB(relX, relZ, color);
                }
            });
        });
    }
}

