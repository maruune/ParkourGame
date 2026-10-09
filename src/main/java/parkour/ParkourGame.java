package parkour;

import parkour.characters.Character;
import parkour.characters.CharacterFactory;
import parkour.characters.SpriteLoader;
import parkour.levels.Level;
import parkour.levels.LevelFactory;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ParkourGame extends JPanel implements Runnable, KeyListener, MouseListener, MouseMotionListener {

    // --- GAME CONSTANTS ---
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;
    private static final int GRAVITY = 1;
    private static final int JUMP_STRENGTH = -17; 
    private static final int FPS = 60;
    
    // Skill Constants
    private static final int DASH_SPEED = 15;
    private static final int DASH_DURATION = 10;
    private static final int DASH_COOLDOWN_DURATION = 15 * FPS;
    private static final int GHOST_DURATION = 120; 
    private static final int MARIO_JUMP_WIND_DURATION = 18;
    private static final BufferedImage WIND_SPRITE = SpriteLoader.load("wind.png");

    // --- GAME STATES ---
    private enum GameState { CHAR_SELECT, MAP_SELECT, PLAYING, GAME_OVER, WIN }
    private GameState currentState = GameState.CHAR_SELECT;

    // --- PLAYER VARIABLES ---
    private int playerX = 50, playerY = 400;
    private int playerWidth = 42, playerHeight = 70;
    private int velY = 0;
    private int velX = 0;
    private int facingDirection = 1;
    
    // --- CAMERA ---
    private double camX = 0;
    private double camY = 0;

    // --- CHARACTER SELECTION ---
    private Character selectedCharacter = CharacterFactory.create(1);

    // --- ABILITY STATES ---
    private boolean canDoubleJump = true; 
    private boolean isDashing = false;    
    private int dashTimer = 0;            
    private boolean canDash = true;       
    private int dashCooldownTimer = 0;
    private boolean isGhostMode = false;  
    private int ghostTimer = 0;           
    private boolean canGhost = true;      
    private int marioJumpWindTimer = 0;

    // --- MAP DATA ---
    private List<Rectangle> platforms = new ArrayList<Rectangle>();
    private List<Rectangle> enemies = new ArrayList<Rectangle>();
    private ArrayList<Rectangle> rainDrops = new ArrayList<>();
    private Rectangle goalRect;
    private Level currentLevel;
    private String mapName = "";
    private String deathMessage = "YOU DIED";
    private int currentMapId = 1;

    // --- UI BUTTONS ---
    private Rectangle[] charButtons = new Rectangle[4];
    private double[] characterCardScale = new double[4];
    private int hoveredCharacter = -1;
    private Rectangle[] mapButtons = new Rectangle[11];
    private Rectangle changeCharBtn = new Rectangle(580, 10, 200, 30);
    private Rectangle retryBtn = new Rectangle(250, 300, 300, 50);
    private Rectangle menuBtn = new Rectangle(250, 370, 300, 50);

    // --- SYSTEM ---
    private Thread gameThread; 
    private Random visualRandom = new Random(); 
    private int runAnimationTick = 0;
    private boolean runAnimationFrame = false;

    public ParkourGame() {
        this.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        this.setBackground(Color.BLACK);
        this.setFocusable(true);
        this.addKeyListener(this);
        this.addMouseListener(this);
        this.addMouseMotionListener(this);

        // Define UI Positions
        for(int i = 0; i < 4; i++) {
            charButtons[i] = new Rectangle(50 + (i * 180), 150, 150, 300);
            characterCardScale[i] = 1.0;
        }
        int x = 50, y = 150;
        for(int i = 0; i < 11; i++) {
            mapButtons[i] = new Rectangle(x, y, 150, 80);
            x += 180;
            if (x > 700) { x = 50; y += 100; }
        }
    }

    // --- OPTIMIZED GAME LOOP (60 FPS) ---
    public void startGame() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        double drawInterval = 1000000000 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        while (gameThread != null) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;

            if (delta >= 1) {
                update();
                repaint();
                delta--;
            }
        }
    }

    private void update() {
        updateRunAnimation();
        updateCharacterCardScale();
        if (currentState == GameState.PLAYING) {
            updatePhysics();
            updateRain(); 
            updateCamera();
            checkWinCondition();
            checkDeath();
            updateCooldowns();
        }
    }

    private void updateCharacterCardScale() {
        for (int i = 0; i < characterCardScale.length; i++) {
            double target = currentState == GameState.CHAR_SELECT && i == hoveredCharacter ? 1.08 : 1.0;
            characterCardScale[i] += (target - characterCardScale[i]) * 0.18;
            if (Math.abs(target - characterCardScale[i]) < 0.001) characterCardScale[i] = target;
        }
    }

    private void updateRunAnimation() {
        if (currentState == GameState.PLAYING && velX != 0 && velY == 0) {
            runAnimationTick++;
            if (runAnimationTick >= 8) {
                runAnimationFrame = !runAnimationFrame;
                runAnimationTick = 0;
            }
        } else {
            runAnimationTick = 0;
            runAnimationFrame = false;
        }
    }

    // --- PHYSICS ENGINE ---
    private void updatePhysics() {
        if (isDashing) {
            velX = facingDirection * DASH_SPEED; 
        } else if (selectedCharacter.getId() == 3) {
            if (velX > 0) velX = 10; 
            if (velX < 0) velX = -10;
        }

        if (isDashing) velY = 0;
        else velY += GRAVITY;

        playerX += velX;
        playerY += velY;

        Rectangle playerRect = new Rectangle(playerX, playerY, playerWidth, playerHeight);
        
        for (Rectangle rect : platforms) {
            // Cull off-screen objects
            if (rect.x > playerX + 100 || rect.x + rect.width < playerX - 100) {
                 if (rect.y > playerY + 100 || rect.y + rect.height < playerY - 100) continue;
            }

            if (playerRect.intersects(rect)) {
                if (velY > 0 && playerY + playerHeight - velY <= rect.y) {
                    playerY = rect.y - playerHeight;
                    velY = 0;
                    resetJumpAbilities(); 
                }
                else if (velY < 0 && playerY - velY >= rect.y + rect.height) {
                    playerY = rect.y + rect.height;
                    velY = 0;
                }
                else if (velX > 0 && playerX + playerWidth - velX <= rect.x) {
                    playerX = rect.x - playerWidth;
                    if(isDashing) isDashing = false;
                }
                else if (velX < 0 && playerX - velX >= rect.x + rect.width) {
                    playerX = rect.x + rect.width;
                    if(isDashing) isDashing = false;
                }
            }
        }
    }

    private void updateRain() {
        if (currentMapId != 11) return;

        // Initialize rain object pool if empty
        if (rainDrops.isEmpty()) {
            for(int i = 0; i < 30; i++) { 
                 rainDrops.add(new Rectangle(
                    playerX - 400 + visualRandom.nextInt(900), 
                    playerY - 600 - visualRandom.nextInt(800), 
                    28, 34
                ));
            }
        }

        for (Rectangle drop : rainDrops) {
            drop.y += 8; 
            // Teleport to top when it falls off screen
            if (drop.y > playerY + 500) {
                drop.y = (int)camY - 100 - visualRandom.nextInt(200);
                drop.x = (int)camX + visualRandom.nextInt(800);
            }
        }
    }

    private void updateCooldowns() {
        if (isDashing) { dashTimer--; if (dashTimer <= 0) isDashing = false; }
        if (!canDash && dashCooldownTimer > 0) {
            dashCooldownTimer--;
            if (dashCooldownTimer == 0) canDash = true;
        }
        if (isGhostMode) { ghostTimer--; if (ghostTimer <= 0) isGhostMode = false; }
        if (marioJumpWindTimer > 0) marioJumpWindTimer--;
    }

    private void updateCamera() {
        double targetCamX = playerX - 300;
        if (targetCamX < 0) targetCamX = 0;
        camX += (targetCamX - camX) * 0.1; 

        double targetCamY = playerY - 300;
        if (targetCamY > 0) targetCamY = 0; 
        camY += (targetCamY - camY) * 0.1;
    }

    private void resetJumpAbilities() {
        canDoubleJump = true; canGhost = true;
    }

    private void checkWinCondition() {
        if (new Rectangle(playerX, playerY, playerWidth, playerHeight).intersects(goalRect)) {
            currentState = GameState.WIN;
        }
    }

    private void checkDeath() {
        if (playerY > 2000) currentState = GameState.GAME_OVER; 
        if (!isGhostMode) {
            for (Rectangle enemy : enemies) {
                if (enemy.x > playerX + 100 || enemy.x + enemy.width < playerX - 100) continue;
                if (new Rectangle(playerX, playerY, playerWidth, playerHeight).intersects(enemy)) currentState = GameState.GAME_OVER;
            }
            if (currentMapId == 11) {
                for (Rectangle drop : rainDrops) {
                    if (new Rectangle(playerX, playerY, playerWidth, playerHeight).intersects(drop)) currentState = GameState.GAME_OVER;
                }
            }
        }
    }

    // --- INPUT HANDLING ---
    @Override
    public void mousePressed(MouseEvent e) {
        Point click = e.getPoint();
        if (currentState == GameState.CHAR_SELECT) {
            for (int i = 0; i < 4; i++) {
                if (charButtons[i].contains(click)) { selectedCharacter = CharacterFactory.create(i + 1); currentState = GameState.MAP_SELECT; return; }
            }
        } 
        else if (currentState == GameState.MAP_SELECT) {
            for (int i = 0; i < 11; i++) {
                if (mapButtons[i].contains(click)) { loadMap(i + 1); currentState = GameState.PLAYING; return; }
            }
        }
        else if (currentState == GameState.PLAYING) {
            if (changeCharBtn.contains(click)) currentState = GameState.CHAR_SELECT;
        }
        else if (currentState == GameState.GAME_OVER || currentState == GameState.WIN) {
            if (retryBtn.contains(click)) { loadMap(currentMapId); currentState = GameState.PLAYING; }
            if (menuBtn.contains(click)) currentState = GameState.MAP_SELECT;
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (currentState == GameState.PLAYING) {
            int speed = selectedCharacter.getMoveSpeed();
            if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) { velX = -speed; facingDirection = -1; }
            if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) { velX = speed; facingDirection = 1; }
            if (key == KeyEvent.VK_SPACE || key == KeyEvent.VK_W || key == KeyEvent.VK_UP) {
                if (velY == 0) {
                    velY = JUMP_STRENGTH;
                    if (selectedCharacter.getId() == 1) marioJumpWindTimer = MARIO_JUMP_WIND_DURATION;
                }
                else if (selectedCharacter.canDoubleJump() && canDoubleJump) {
                    velY = JUMP_STRENGTH;
                    canDoubleJump = false;
                    if (selectedCharacter.getId() == 1) marioJumpWindTimer = MARIO_JUMP_WIND_DURATION;
                }
            }
            if (key == KeyEvent.VK_SHIFT) {
                if (selectedCharacter.canDash() && canDash && !isDashing) {
                    isDashing = true;
                    canDash = false;
                    dashTimer = DASH_DURATION;
                    dashCooldownTimer = DASH_COOLDOWN_DURATION;
                    if (velX == 0) velX = facingDirection * DASH_SPEED;
                }
                if (selectedCharacter.canUseGhostMode() && canGhost && !isGhostMode) { isGhostMode = true; canGhost = false; ghostTimer = GHOST_DURATION; }
            }
        }
    }

    @Override public void keyReleased(KeyEvent e) {
        if (currentState == GameState.PLAYING && !isDashing) {
            int key = e.getKeyCode();
            if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_A || key == KeyEvent.VK_D) velX = 0;
        }
    }
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) { updateHoveredCharacter(e.getPoint()); }
    @Override public void mouseDragged(MouseEvent e) { updateHoveredCharacter(e.getPoint()); }
    @Override public void mouseMoved(MouseEvent e) { updateHoveredCharacter(e.getPoint()); }
    @Override public void keyTyped(KeyEvent e) {}

    private void updateHoveredCharacter(Point point) {
        int nextHoveredCharacter = -1;
        if (currentState == GameState.CHAR_SELECT) {
            for (int i = 0; i < charButtons.length; i++) {
                if (charButtons[i].contains(point)) {
                    nextHoveredCharacter = i;
                    break;
                }
            }
        }
        if (hoveredCharacter != nextHoveredCharacter) {
            hoveredCharacter = nextHoveredCharacter;
            repaint();
        }
    }

    // --- RENDERING ---
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (currentState == GameState.CHAR_SELECT) drawCharSelect(g2d);
        else if (currentState == GameState.MAP_SELECT) drawMapSelect(g2d);
        else if (currentState == GameState.PLAYING) drawGame(g2d);
        else if (currentState == GameState.WIN) drawWinScreen(g2d);
        else if (currentState == GameState.GAME_OVER) drawGameOverScreen(g2d);
    }

    private void drawCharSelect(Graphics2D g) {
        g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 40)); centerText(g, "CHOOSE YOUR HERO", 100);
        for(int i = 0; i < 4; i++) {
            Rectangle bounds = charButtons[i];
            int cardWidth = (int) Math.round(bounds.width * characterCardScale[i]);
            int cardHeight = (int) Math.round(bounds.height * characterCardScale[i]);
            Rectangle btn = new Rectangle(bounds.x - (cardWidth - bounds.width) / 2,
                    bounds.y - (cardHeight - bounds.height) / 2, cardWidth, cardHeight);
            Character character = CharacterFactory.create(i + 1);
            boolean hovered = i == hoveredCharacter;
            g.setColor(hovered ? new Color(42, 48, 58) : new Color(30, 30, 30));
            g.fill(btn);
            g.setColor(hovered ? Color.CYAN : Color.WHITE);
            g.setStroke(new BasicStroke(hovered ? 3 : 2));
            g.draw(btn);
            BufferedImage sprite = character.getSprite(false);
            if (sprite != null) {
                drawSprite(g, sprite, btn.x + 16, btn.y + 16, btn.width - 32, 196, 1, 0);
            } else {
                g.setColor(character.getColor());
                g.fillRect(btn.x + (btn.width - 38) / 2, btn.y + 68, 38, 64);
            }
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 18));
            FontMetrics nameMetrics = g.getFontMetrics();
            g.drawString(character.getName(), btn.x + (btn.width - nameMetrics.stringWidth(character.getName())) / 2,
                    btn.y + 232);
            if (hovered) {
                g.setColor(new Color(205, 215, 225));
                g.setFont(new Font("Arial", Font.PLAIN, 12));
                drawCenteredWrappedText(g, character.getDescription(), btn, btn.y + 255, 3);
            }
        }
    }

    private void drawCenteredWrappedText(Graphics2D g, String text, Rectangle bounds, int baseline, int maxLines) {
        FontMetrics metrics = g.getFontMetrics();
        int maxWidth = bounds.width - 20;
        StringBuilder line = new StringBuilder();
        int lineNumber = 0;
        for (String word : text.split(" ")) {
            String candidate = line.length() == 0 ? word : line + " " + word;
            if (metrics.stringWidth(candidate) > maxWidth && line.length() > 0) {
                drawCenteredLine(g, metrics, line.toString(), bounds, baseline + lineNumber * 15);
                lineNumber++;
                line.setLength(0);
                if (lineNumber >= maxLines) return;
            }
            if (line.length() > 0) line.append(' ');
            line.append(word);
        }
        if (line.length() > 0 && lineNumber < maxLines) {
            drawCenteredLine(g, metrics, line.toString(), bounds, baseline + lineNumber * 15);
        }
    }

    private void drawCenteredLine(Graphics2D g, FontMetrics metrics, String text, Rectangle bounds, int baseline) {
        g.drawString(text, bounds.x + (bounds.width - metrics.stringWidth(text)) / 2, baseline);
    }

    private void drawSprite(Graphics2D graphics, BufferedImage sprite, int x, int y,
            int width, int height, int direction, int verticalOffset) {
        Graphics2D spriteGraphics = (Graphics2D) graphics.create();
        spriteGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        double scale = Math.min(width / (double) sprite.getWidth(), height / (double) sprite.getHeight());
        int drawWidth = (int) Math.round(sprite.getWidth() * scale);
        int drawHeight = (int) Math.round(sprite.getHeight() * scale);
        int drawX = x + (width - drawWidth) / 2;
        int drawY = y + (height - drawHeight) / 2 + verticalOffset;
        spriteGraphics.translate(drawX + (direction < 0 ? drawWidth : 0), drawY);
        if (direction < 0) spriteGraphics.scale(-1, 1);
        spriteGraphics.drawImage(sprite, 0, 0, drawWidth, drawHeight, null);
        spriteGraphics.dispose();
    }

    private void drawJettWindTrail(Graphics2D graphics) {
        if (WIND_SPRITE == null) return;
        Composite previousComposite = graphics.getComposite();
        float dashProgress = dashTimer / (float) DASH_DURATION;
        for (int i = 0; i < 3; i++) {
            float alpha = Math.max(0.12f, dashProgress * (0.62f - i * 0.16f));
            graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            int trailX = playerX - facingDirection * (playerWidth + 8 + i * 20);
            int trailY = playerY + playerHeight / 3 + (i % 2 == 0 ? -5 : 5);
            drawSprite(graphics, WIND_SPRITE, trailX, trailY, 42, 34, facingDirection, 0);
        }
        graphics.setComposite(previousComposite);
    }

    private void drawMarioJumpWind(Graphics2D graphics) {
        if (WIND_SPRITE == null || marioJumpWindTimer <= 0) return;
        Composite previousComposite = graphics.getComposite();
        float alpha = marioJumpWindTimer / (float) MARIO_JUMP_WIND_DURATION;
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        drawSprite(graphics, WIND_SPRITE, playerX - 12, playerY + playerHeight - 8,
                playerWidth + 24, 28, facingDirection, 0);
        graphics.setComposite(previousComposite);
    }

    private void drawMapSelect(Graphics2D g) {
        g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 40)); centerText(g, "SELECT A LEVEL", 80);
        String[] labels = {"1. Warm Up", "2. Islands", "3. Lava", "4. Hard I", "5. Tunnel", "6. Marathon", "7. Precision", "8. The Wall", "9. Kaizo", "10. Gauntlet", "11. Cup Rain"};
        for(int i = 0; i < 11; i++) {
            Rectangle btn = mapButtons[i];
            g.setColor(i == 10 ? new Color(0, 0, 50) : new Color(40, 40, 40)); g.fill(btn);
            g.setColor(i == 10 ? Color.CYAN : Color.WHITE); g.setStroke(new BasicStroke(2)); g.draw(btn);
            g.setFont(new Font("Arial", Font.BOLD, 14));
            int textX = btn.x + (btn.width - g.getFontMetrics().stringWidth(labels[i]))/2;
            g.drawString(labels[i], textX, btn.y + 45);
        }
    }

    private void drawGame(Graphics2D g2d) {
        g2d.translate(-camX, -camY);

        if(isGhostMode) { g2d.setColor(new Color(200, 100, 255, 100)); g2d.fillOval(playerX-5, playerY-5, playerWidth+10, playerHeight+10); }
        if (isDashing && selectedCharacter.getId() == 2) drawJettWindTrail(g2d);
        if (selectedCharacter.getId() == 1) drawMarioJumpWind(g2d);
        BufferedImage sprite = isDashing ? selectedCharacter.getDashSprite() : null;
        if (sprite == null) sprite = selectedCharacter.getSprite(velY != 0);
        if (sprite != null) {
            int bounce = velX != 0 && velY == 0 && runAnimationFrame ? 2 : 0;
            drawSprite(g2d, sprite, playerX, playerY,
                    playerWidth, playerHeight, facingDirection, -bounce);
        } else {
            g2d.setColor(selectedCharacter.getColor());
            g2d.fillRect(playerX, playerY, playerWidth, playerHeight);
        }

        g2d.setColor(Color.LIGHT_GRAY);
        for (Rectangle rect : platforms) {
            if (rect.x + rect.width < camX || rect.x > camX + WINDOW_WIDTH + 100) continue;
            g2d.fillRect(rect.x, rect.y, rect.width, rect.height);
        }

        g2d.setColor(Color.RED);
        for (Rectangle rect : enemies) {
             if (rect.x + rect.width < camX || rect.x > camX + WINDOW_WIDTH + 100) continue; 
            g2d.fillRect(rect.x, rect.y, rect.width, rect.height);
        }
        
        g2d.setColor(Color.GREEN); g2d.fillRect(goalRect.x, goalRect.y, goalRect.width, goalRect.height);

        if(currentLevel.hasRainHazard()) {
            for (Rectangle r : rainDrops) {
                if (r.x + r.width < camX || r.x > camX + WINDOW_WIDTH) continue;
                g2d.setColor(new Color(255, 215, 0)); 
                g2d.fillRect(r.x + 4, r.y + r.height - 6, r.width - 8, 6);
                g2d.fillRect(r.x + 10, r.y + r.height - 12, 8, 6);
                g2d.fillArc(r.x, r.y, r.width, r.height - 10, 0, -180);
            }
        }

        g2d.translate(camX, camY); 
        g2d.setColor(new Color(50, 50, 50)); g2d.fill(changeCharBtn); g2d.setColor(Color.WHITE); g2d.draw(changeCharBtn);
        g2d.setFont(new Font("Arial", Font.BOLD, 12)); g2d.drawString("CLICK TO CHANGE CHARACTER", changeCharBtn.x + 10, changeCharBtn.y + 20);
        g2d.drawString("Map: " + mapName, 20, 30);
        if(selectedCharacter.canDash()) {
            g2d.setColor(canDash ? Color.GREEN : Color.RED);
            int cooldownSeconds = (dashCooldownTimer + FPS - 1) / FPS;
            g2d.drawString("Dash: " + (canDash ? "READY" : cooldownSeconds + "s"), 20, 50);
        }
        else if(selectedCharacter.canUseGhostMode()) { g2d.setColor(canGhost ? Color.GREEN : (isGhostMode ? Color.MAGENTA : Color.RED)); g2d.drawString("Phase: " + (canGhost ? "READY" : (isGhostMode ? "ACTIVE" : "USED")), 20, 50); }
    }

    private void drawWinScreen(Graphics2D g) {
        g.setColor(Color.GREEN); g.setFont(new Font("Arial", Font.BOLD, 40)); centerText(g, "LEVEL COMPLETE!", 200); drawMenuButtons(g);
    }
    
    private void drawGameOverScreen(Graphics2D g) {
        g.setColor(Color.RED); g.setFont(new Font("Arial", Font.BOLD, 40)); centerText(g, deathMessage, 200); drawMenuButtons(g);
    }
    
    private void drawMenuButtons(Graphics2D g) {
        g.setColor(new Color(50, 50, 50)); g.fill(retryBtn); g.setColor(Color.WHITE); g.setStroke(new BasicStroke(2)); g.draw(retryBtn);
        g.setFont(new Font("Arial", Font.BOLD, 20)); g.drawString("RETRY LEVEL", retryBtn.x + 80, retryBtn.y + 32);
        g.setColor(new Color(50, 50, 50)); g.fill(menuBtn); g.setColor(Color.WHITE); g.draw(menuBtn);
        g.drawString("MAIN MENU", menuBtn.x + 90, menuBtn.y + 32);
    }
    
    private void centerText(Graphics g, String text, int y) {
        FontMetrics fm = g.getFontMetrics(); g.drawString(text, (WINDOW_WIDTH - fm.stringWidth(text)) / 2, y);
    }

    private void loadMap(int mapId) {
        rainDrops.clear();
        playerX = 50; playerY = 400; camX = 0; camY = 0;
        velX = 0; velY = 0;
        resetJumpAbilities(); isGhostMode = false; isDashing = false;
        canDash = true; dashCooldownTimer = 0; marioJumpWindTimer = 0;
        currentMapId = mapId; deathMessage = "YOU DIED"; 
        currentLevel = LevelFactory.create(mapId);
        platforms = currentLevel.getPlatforms();
        enemies = currentLevel.getEnemies();
        goalRect = currentLevel.getGoal();
        mapName = currentLevel.getName();
        if (currentLevel.hasRainHazard()) deathMessage = "YOU ARE NOOB! TRY AGAIN";
    }
}