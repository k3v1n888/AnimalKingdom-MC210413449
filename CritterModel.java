
import java.util.*;
import java.awt.Point;
import java.awt.Color;
import java.lang.reflect.*;

public class CritterModel {

    public static final double HOP_ADVANTAGE = 0.2; // 20% advantage

    private int height;
    private int width;
    private Critter[][] grid;
    private Map<Critter, PrivateData> info;
    private SortedMap<String, Integer>critterCount;
    private boolean debugView;
    private int simulationCount;
    private static boolean created;
    
    public CritterModel(int width, int height) {

        if (created)
            throw new RuntimeException("Only one world allowed");
        created = true;

        this.width = width;
        this.height = height;
        grid = new Critter[width][height];
        info = new HashMap<Critter, PrivateData>();
        critterCount = new TreeMap<String, Integer>();
        this.debugView = false;
    }

    public Iterator<Critter> iterator() {
        return info.keySet().iterator();
    }

    public Point getPoint(Critter c) {
        return info.get(c).p;
    }

    public Color getColor(Critter c) {
        return info.get(c).color;
    }

    public String getString(Critter c) {
        return info.get(c).string;
    }

    public void add(int number, Class<? extends Critter> critter) {
        Random r = new Random();
        Critter.Direction[] directions = Critter.Direction.values();
        if (info.size() + number > width * height)
            throw new RuntimeException("adding too many critters");
        for (int i = 0; i < number; i++) {
            Critter next;
            try {
                next = makeCritter(critter);
            } catch (IllegalArgumentException e) {
                System.out.println("ERROR: " + critter + " does not have" +
                                   " the appropriate constructor.");
                System.exit(1);
                return;
            } catch (Exception e) {
                System.out.println("ERROR: " + critter + " threw an " +
                                   " exception in its constructor.");
                System.exit(1);
                return;
            }
            int x, y;
            do {
                x = r.nextInt(width);
                y = r.nextInt(height);
            } while (grid[x][y] != null);
            grid[x][y] = next;
            
            Critter.Direction d = directions[r.nextInt(directions.length)];
            info.put(next, new PrivateData(new Point(x, y), d));
        }
        String name = critter.getName();
        if (!critterCount.containsKey(name))
            critterCount.put(name, number);
        else
            critterCount.put(name, critterCount.get(name) + number);
    }

    @SuppressWarnings("unchecked")
    private Critter makeCritter(Class critter) throws Exception {
        Constructor c = critter.getConstructors()[0];
        if (critter.toString().equals("class Bear")) {
            // flip a coin
            boolean b = Math.random() < 0.5;
            return (Critter) c.newInstance(new Object[] {b});
        } else {
            return (Critter) c.newInstance();
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getAppearance(Critter c) {
        // Override specified toString if debug flag is true
        if (!debugView) 
            return info.get(c).string;
        else {
            PrivateData data = info.get(c);
            if (data.direction == Critter.Direction.NORTH) return "^";
            else if (data.direction == Critter.Direction.SOUTH) return "v";
            else if (data.direction == Critter.Direction.EAST) return ">";
            else return "<";
        }
    }
    
    public void toggleDebug() {
        this.debugView = !this.debugView;
    }

    private boolean inBounds(int x, int y) {
        return (x >= 0 && x < width && y >= 0 && y < height);
    }

    private boolean inBounds(Point p) {
        return inBounds(p.x, p.y);
    }

    private Critter.Direction rotate(Critter.Direction d) {
        if (d == Critter.Direction.NORTH) return Critter.Direction.EAST;
        else if (d == Critter.Direction.SOUTH) return Critter.Direction.WEST;
        else if (d == Critter.Direction.EAST) return Critter.Direction.SOUTH;
        else return Critter.Direction.NORTH;
    }

    private Point pointAt(Point p, Critter.Direction d) {
        if (d == Critter.Direction.NORTH) return new Point(p.x, p.y - 1);
        else if (d == Critter.Direction.SOUTH) return new Point(p.x, p.y + 1);
        else if (d == Critter.Direction.EAST) return new Point(p.x + 1, p.y);
        else return new Point(p.x - 1, p.y);
    }

    private Info getInfo(PrivateData data, Class original) {
        Critter.Neighbor[] neighbors = new Critter.Neighbor[4];
        Critter.Direction d = data.direction;
        boolean[] neighborThreats = new boolean[4];
        for (int i = 0; i < 4; i++) {
            neighbors[i] = getStatus(pointAt(data.p, d), original);
            if (neighbors[i] == Critter.Neighbor.OTHER) {
                Point p = pointAt(data.p, d);
                PrivateData oldData = info.get(grid[p.x][p.y]);
                neighborThreats[i] = d == rotate(rotate(oldData.direction));
            }
            d = rotate(d);
        }
        return new Info(neighbors, data.direction, neighborThreats);
    }

    private Critter.Neighbor getStatus(Point p, Class original) {
        if (!inBounds(p))
            return Critter.Neighbor.WALL;
        else if (grid[p.x][p.y] == null)
            return Critter.Neighbor.EMPTY;
        else if (grid[p.x][p.y].getClass() == original)
            return Critter.Neighbor.SAME;
        else
            return Critter.Neighbor.OTHER;
    }

    @SuppressWarnings("unchecked")
    public void update() {
        simulationCount++;
        Object[] list = info.keySet().toArray();
        Collections.shuffle(Arrays.asList(list));

        Set<Critter> locked = new HashSet<Critter>();
        
        for (int i = 0; i < list.length; i++) {
            Critter next = (Critter)list[i];
            PrivateData data = info.get(next);
            if (data == null) {

                continue;
            }

            boolean hadHopped = data.justHopped;
            data.justHopped = false;
            Point p = data.p;
            Point p2 = pointAt(p, data.direction);

            Critter.Action move = next.getMove(getInfo(data, next.getClass()));
            if (move == Critter.Action.LEFT)
                data.direction = rotate(rotate(rotate(data.direction)));
            else if (move == Critter.Action.RIGHT)
                data.direction = rotate(data.direction);
            else if (move == Critter.Action.HOP) {
                if (inBounds(p2) && grid[p2.x][p2.y] == null) {
                    grid[p2.x][p2.y] = grid[p.x][p.y];
                    grid[p.x][p.y] = null;
                    data.p = p2;
                    locked.add(next); 
                    data.justHopped = true; 
                }
            } else if (move == Critter.Action.INFECT) {
                if (inBounds(p2) && grid[p2.x][p2.y] != null
                    && grid[p2.x][p2.y].getClass() != next.getClass()
                    && !locked.contains(grid[p2.x][p2.y])
                    && (hadHopped || Math.random() >= HOP_ADVANTAGE)) {
                    Critter other = grid[p2.x][p2.y];
       
                    PrivateData oldData = info.get(other);
                    
                    String c1 = other.getClass().getName();
                    critterCount.put(c1, critterCount.get(c1) - 1);
                    String c2 = next.getClass().getName();
                    critterCount.put(c2, critterCount.get(c2) + 1);
                    info.remove(other);
                   
                    try {
                        grid[p2.x][p2.y] = makeCritter(next.getClass());
                     
                        locked.add(grid[p2.x][p2.y]);
                    } catch (Exception e) {
                        throw new RuntimeException("" + e);
                    }
                  
                    info.put(grid[p2.x][p2.y], oldData);
                  
                    oldData.justHopped = false;
                }
            }
        }
        updateColorString();
    }

    public void updateColorString() {
        for (Critter next : info.keySet()) {
            info.get(next).color = next.getColor();
            info.get(next).string = next.toString();
        }
    }

    public Set<Map.Entry<String, Integer>> getCounts() {
        return Collections.unmodifiableSet(critterCount.entrySet());
    }

    public int getSimulationCount() {
        return simulationCount;
    }

    private class PrivateData {
        public Point p;
        public Critter.Direction direction;
        public Color color;
        public String string;
        public boolean justHopped;

        public PrivateData(Point p, Critter.Direction d) {
            this.p = p;
            this.direction = d;
        }

        public String toString() {
            return p + " " + direction;
        }
    }

    private static class Info implements CritterInfo {
        private Critter.Neighbor[] neighbors;
        private Critter.Direction direction;
        private boolean[] neighborThreats;

        public Info(Critter.Neighbor[] neighbors, Critter.Direction d,
                    boolean[] neighborThreats) {
            this.neighbors = neighbors;
            this.direction = d;
            this.neighborThreats = neighborThreats;
        }

        public Critter.Neighbor getFront() {
            return neighbors[0];
        }

        public Critter.Neighbor getBack() {
            return neighbors[2];
        }

        public Critter.Neighbor getLeft() {
            return neighbors[3];
        }

        public Critter.Neighbor getRight() {
            return neighbors[1];
        }

        public Critter.Direction getDirection() {
            return direction;
        }

        public boolean frontThreat() {
            return neighborThreats[0];
        }
        
        public boolean backThreat() {
            return neighborThreats[2];
        }
            
        public boolean leftThreat() {
            return neighborThreats[3];
        }
        
        public boolean rightThreat() {
            return neighborThreats[1];
        }
    }
}