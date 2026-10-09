package parkour.characters.mario;

import java.awt.Color;
import java.awt.image.BufferedImage;
import parkour.characters.Character;
import parkour.characters.SpriteLoader;

public final class Mario extends Character {
    private static final BufferedImage STAND_SPRITE = SpriteLoader.load("MarioStand.png");
    private static final BufferedImage JUMP_SPRITE = SpriteLoader.load("MarioJump.png");

    public Mario() {
        super(1, "Mario", "Double jump to reach higher platforms and clear gaps.",
                Color.RED, 6, true, false, false);
    }

    @Override
    public BufferedImage getSprite(boolean jumping) {
        return jumping && JUMP_SPRITE != null ? JUMP_SPRITE : STAND_SPRITE;
    }
}