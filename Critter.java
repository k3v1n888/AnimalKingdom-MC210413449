
import java.awt.*;

public class Critter {
    public static enum Neighbor {
        WALL, EMPTY, SAME, OTHER
    };

    public static enum Action {
        HOP, LEFT, RIGHT, INFECT
    };

    public static enum Direction {
        NORTH, SOUTH, EAST, WEST
    };

    public Action getMove(CritterInfo info) {
        return Action.LEFT;
    }

    public Color getColor() {
        return Color.BLACK;
    }

    public String toString() {
        return "?";
    }

    public final boolean equals(Object other) {
        return this == other;
    }
}