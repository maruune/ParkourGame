package parkour.characters.jett;

import java.awt.Color;
import java.awt.image.BufferedImage;
import parkour.characters.Character;
import parkour.characters.SpriteLoader;

public final class Jett extends Character {
    private static final BufferedImage STAND_SPRITE = SpriteLoader.load("JettStand.png");
    private static final BufferedImage JUMP_SPRITE = SpriteLoader.load("JettJump.png");
    private static final BufferedImage DASH_SPRITE = SpriteLoader.load("JettDash.png");

    public Jett() {
        super(2, "Jett", "Dash quickly across wide gaps with Shift.",
                Color.DARK_GRAY, 6, false, true, false);
    }

    @Override
    public BufferedImage getSprite(boolean jumping) {
        return jumping && JUMP_SPRITE != null ? JUMP_SPRITE : STAND_SPRITE;
    }

    @Override
    public BufferedImage getDashSprite() {
        return DASH_SPRITE;
    }
}