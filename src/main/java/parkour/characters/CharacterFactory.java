package parkour.characters;

import parkour.characters.flash.Flash;
import parkour.characters.gojo.Gojo;
import parkour.characters.jett.Jett;
import parkour.characters.mario.Mario;

public final class CharacterFactory {
    private CharacterFactory() { }

    public static Character create(int id) {
        switch (id) {
            case 1: return new Mario();
            case 2: return new Jett();
            case 3: return new Flash();
            case 4: return new Gojo();
            default: throw new IllegalArgumentException("Unknown character: " + id);
        }
    }
}
