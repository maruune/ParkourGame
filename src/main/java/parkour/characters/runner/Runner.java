package parkour.characters.runner;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import parkour.characters.Character;

public final class Runner extends Character {
    private static final BufferedImage SPRITE = loadSprite("MarioStand.png");
    private static final BufferedImage JUMP_SPRITE = loadSprite("MarioJump.png");

    public Runner() {
        super(1, "Mario", "Double jump to reach higher platforms and clear gaps.",
                Color.RED, 6, true, false, false);
    }

    public BufferedImage getSprite(boolean jumping) {
        return jumping && JUMP_SPRITE != null ? JUMP_SPRITE : SPRITE;
    }

    private static BufferedImage loadSprite(String resourceName) {
        try (InputStream input = Runner.class.getResourceAsStream(resourceName)) {
            if (input == null) return null;
            BufferedImage image = ImageIO.read(input);
            if (image == null) return null;

            int minX = image.getWidth();
            int minY = image.getHeight();
            int maxX = -1;
            int maxY = -1;
            int[] row = new int[image.getWidth()];
            for (int y = 0; y < image.getHeight(); y++) {
                image.getRGB(0, y, image.getWidth(), 1, row, 0, image.getWidth());
                for (int x = 0; x < row.length; x++) {
                    if ((row[x] >>> 24) == 0) continue;
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
            }

            if (maxX < minX || maxY < minY) return null;
            return image.getSubimage(minX, minY, maxX - minX + 1, maxY - minY + 1);
        } catch (IOException exception) {
            return null;
        }
    }
}
