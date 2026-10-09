package parkour.characters.flash;

import java.awt.Color;
import parkour.characters.Character;

public final class Flash extends Character {
    public Flash() {
        super(3, "FLASH", "Move at top speed to cross tricky sections.",
            Color.YELLOW, 10, false, false, false);
    }
}
