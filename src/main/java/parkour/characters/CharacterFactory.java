package parkour.characters;

import parkour.characters.flash.Flash;
import parkour.characters.ghost.Ghost;
import parkour.characters.ninja.Ninja;
import parkour.characters.runner.Runner;

public final class CharacterFactory {
    private CharacterFactory() { }

    public static Character create(int id) {
        switch (id) {
            case 1: return new Runner();
            case 2: return new Ninja();
            case 3: return new Flash();
            case 4: return new Ghost();
            default: throw new IllegalArgumentException("Unknown character: " + id);
        }
    }
}
