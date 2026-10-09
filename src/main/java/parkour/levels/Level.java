package parkour.levels;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class Level {
    private final int id;
    private final String name;
    private final boolean rainHazard;
    private final List<Rectangle> platforms = new ArrayList<Rectangle>();
    private final List<Rectangle> enemies = new ArrayList<Rectangle>();
    private final Rectangle goal;
    private final Random mapRandom;
    private int genX;
    private int genY;

    protected Level(int id, int length, String name, boolean rainHazard) {
        this.id = id;
        this.name = name;
        this.rainHazard = rainHazard;
        this.mapRandom = new Random(id * 1000L);

        platforms.add(new Rectangle(0, 500, 200, 50));
        genX = 200;
        genY = 450;
        for (int segment = 0; segment < length; segment++) {
            generateSegment();
        }
        goal = new Rectangle(genX + 100, genY - 50, 40, 50);
        platforms.add(new Rectangle(genX, genY, 200, 50));
    }

    private void generateSegment() {
        int type = mapRandom.nextInt(6);
        if (id < 4 && type > 3) type = 0;

        switch (type) {
            case 0:
                genX += 50 + mapRandom.nextInt(50);
                genY -= 20 - mapRandom.nextInt(30);
                platforms.add(new Rectangle(genX, genY, 120, 20));
                if (id > 7 && mapRandom.nextInt(10) > 8) {
                    enemies.add(new Rectangle(genX + 40, genY - 20, 20, 20));
                }
                genX += 120;
                break;
            case 1:
                int gap = Math.min(80 + id * 5, 160);
                genX += gap;
                platforms.add(new Rectangle(genX, genY, 100, 20));
                genX += 100;
                break;
            case 2:
                for (int step = 0; step < 3; step++) {
                    genX += 60;
                    genY -= 50;
                    platforms.add(new Rectangle(genX, genY, 80, 20));
                }
                break;
            case 3:
                genX += 50;
                platforms.add(new Rectangle(genX, genY, 400, 20));
                if (mapRandom.nextInt(10) > 8) {
                    enemies.add(new Rectangle(genX + 200, genY - 20, 20, 20));
                }
                genX += 400;
                break;
            case 4:
                for (int island = 0; island < 4; island++) {
                    genX += 100;
                    genY += mapRandom.nextInt(40) - 20;
                    platforms.add(new Rectangle(genX, genY, 70, 20));
                }
                genX += 70;
                break;
            case 5:
                genX += 50;
                platforms.add(new Rectangle(genX + 10, genY - 50, 30, 10));
                platforms.add(new Rectangle(genX + 10, genY - 100, 30, 10));
                enemies.add(new Rectangle(genX + 50, genY - 200, 20, 200));
                platforms.add(new Rectangle(genX, genY, 50, 20));
                platforms.add(new Rectangle(genX + 160, genY, 50, 20));
                genX += 200;
                break;
            default:
                throw new IllegalStateException("Unexpected segment type: " + type);
        }
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public boolean hasRainHazard() { return rainHazard; }
    public List<Rectangle> getPlatforms() { return platforms; }
    public List<Rectangle> getEnemies() { return enemies; }
    public Rectangle getGoal() { return goal; }
}
