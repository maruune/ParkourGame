package parkour.levels;

import parkour.levels.level01.Level01;
import parkour.levels.level02.Level02;
import parkour.levels.level03.Level03;
import parkour.levels.level04.Level04;
import parkour.levels.level05.Level05;
import parkour.levels.level06.Level06;
import parkour.levels.level07.Level07;
import parkour.levels.level08.Level08;
import parkour.levels.level09.Level09;
import parkour.levels.level10.Level10;
import parkour.levels.level11.Level11;

public final class LevelFactory {
    private LevelFactory() { }

    public static Level create(int id) {
        switch (id) {
            case 1: return new Level01();
            case 2: return new Level02();
            case 3: return new Level03();
            case 4: return new Level04();
            case 5: return new Level05();
            case 6: return new Level06();
            case 7: return new Level07();
            case 8: return new Level08();
            case 9: return new Level09();
            case 10: return new Level10();
            case 11: return new Level11();
            default: throw new IllegalArgumentException("Unknown level: " + id);
        }
    }
}
