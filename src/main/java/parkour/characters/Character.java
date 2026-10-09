package parkour.characters;

import java.awt.Color;
import java.awt.image.BufferedImage;

public abstract class Character {
    private final int id;
    private final String name;
    private final String description;
    private final Color color;
    private final int moveSpeed;
    private final boolean doubleJump;
    private final boolean dash;
    private final boolean ghostMode;

    protected Character(int id, String name, String description, Color color, int moveSpeed,
            boolean doubleJump, boolean dash, boolean ghostMode) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.color = color;
        this.moveSpeed = moveSpeed;
        this.doubleJump = doubleJump;
        this.dash = dash;
        this.ghostMode = ghostMode;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Color getColor() { return color; }
    public int getMoveSpeed() { return moveSpeed; }
    public boolean canDoubleJump() { return doubleJump; }
    public boolean canDash() { return dash; }
    public boolean canUseGhostMode() { return ghostMode; }
    public BufferedImage getSprite(boolean jumping) { return null; }
    public BufferedImage getDashSprite() { return null; }
}
