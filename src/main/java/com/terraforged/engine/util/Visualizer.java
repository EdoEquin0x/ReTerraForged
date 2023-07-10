/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.util;

import com.terraforged.engine.util.pos.PosIterator;
import com.terraforged.noise.Module;
import com.terraforged.noise.Source;
import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class Visualizer {
    public static void main(String[] args) {
        int size = 512;
        Module noise = Source.simplex(123, 40, 2).warp(Source.RAND, 124, 2, 1, 4.0);
        BufferedImage image = new BufferedImage(size, size, 1);
        PosIterator iterator = PosIterator.area(0, 0, size, size);
        while (iterator.next()) {
            float value = noise.getValue(iterator.x(), iterator.z());
            image.setRGB(iterator.x(), iterator.z(), Visualizer.getMaterial(value));
        }
        JFrame frame = new JFrame();
        frame.add(new JLabel(new ImageIcon(image)));
        frame.setVisible(true);
        frame.pack();
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(3);
    }

    private static int getMaterial(float value) {
        if ((double)value > 0.6) {
            if ((double)value < 0.75) {
                return Color.HSBtoRGB(0.05f, 0.4f, 0.2f);
            }
            return Color.HSBtoRGB(0.05f, 0.4f, 0.4f);
        }
        return Color.HSBtoRGB(0.25f, 0.4f, 0.6f);
    }
}

