package parkour.characters.ghost;

import java.awt.Color;
import parkour.characters.Character;

public final class Ghost extends Character {
    public Ghost() {
        super(4, "GHOST", "Become untouchable for a moment with Shift.",
            new Color(128, 0, 128), 6, false, false, true);
    }
}
