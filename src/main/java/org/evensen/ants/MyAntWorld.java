package org.evensen.ants;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MyAntWorld implements AntWorld {
    private int worldWidth;
    private int worldHeight;
    private float[][] foragingPheromones;
    private float[][] foodPheromones;
    private List<FoodSource> foodSources;
    private DispersalPolicy dispersalPolicy;
    private final int RADIUS;
    public int foodcounter = 0;

    private boolean[][] foodMat;
    public MyAntWorld(final int worldWidth, final int worldHeight, final int n, DispersalPolicy dispersalPolicy) {
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.foragingPheromones = new float[worldWidth][worldHeight];
        this.foodPheromones = new float[worldWidth][worldHeight];
        this.foodMat = new boolean[worldWidth][worldHeight];
        this.dispersalPolicy = dispersalPolicy;
        this.foodSources = new ArrayList<>();
        this.RADIUS = 20;

        for (int i = 0; i < worldWidth; i++) {
            for (int j = 0; j < worldHeight; j++) {
                this.foodMat[i][j] = false;
            }
        }
        for (int i = 0; i < n; i++) {
            FoodSource newFoodSource = randFoodSource();
            this.foodSources.add(newFoodSource);
            updateFoodMat(newFoodSource.getPosition(), this.RADIUS, true);
        }
    }



    @Override
    public int getWidth() {
        return this.worldWidth;
    }

    @Override
    public int getHeight() {
        return this.worldHeight;
    }

    @Override
    public boolean isObstacle(final Position p) {
        final float x = p.getX();
        final float y = p.getY();
        return 0 > x || 0 > y || x >= this.worldWidth || y >= this.worldHeight;
    }

    @Override
    public void dropForagingPheromone(final Position p, final float amount) {
        final int x = (int) p.getX();
        final int y = (int) p.getY();
        final float minAmount = Math.min(amount, 1.0f);

        if (0 <= x && x < this.worldWidth && 0 <= y && y < this.worldHeight) {
            this.foragingPheromones[x][y] += minAmount;
        }
    }

    @Override
    public void dropFoodPheromone(final Position p, final float amount) {
        final int x = (int) p.getX();
        final int y = (int) p.getY();
        final float minAmount = Math.min(amount, 1.0f);

        if (0 <= x && x < this.worldWidth && 0 <= y && y < this.worldHeight) {
            this.foodPheromones[x][y] = minAmount;
        }
    }

    @Override
    public void dropFood(final Position p) {
        this.foodcounter += 1;

    }

    private void updateFoodMat(Position foodPos, int radius, boolean value) {
        int x = (int) foodPos.getX();
        int y = (int) foodPos.getY();
        for (int i = Math.max(0, x - radius); i < Math.min(this.worldWidth, x + radius); i++) {
            for (int j = Math.max(0, y - radius); j < Math.min(this.worldHeight, y + radius); j++) {
                Position curPos = new Position(i, j);
                if (curPos.isWithinRadius(foodPos, radius)) {
                    this.foodMat[i][j] = value;
                }

                if (value == false) { // make sure that we don't remove overlapping sources
                    int overlaps = 0;
                    for (FoodSource source : this.foodSources) {
                        if (curPos.isWithinRadius(source.getPosition(), radius)) {
                            overlaps++;
                        }
                    }
                    if (overlaps > 1) { // position contains more than one sources, keep position true
                        this.foodMat[i][j] = true;
                    }
                }
            }
        }
    }

    private FoodSource randFoodSource() {
        Random random = new Random();
        int x = random.nextInt(0, this.worldWidth);
        int y = random.nextInt(0, this.worldHeight);
        Position pos = new Position(x, y);

        FoodSource randFoodSource = new FoodSource(pos, this.RADIUS, 2000);
        return randFoodSource;
    }

    @Override
    public void pickUpFood(final Position p) {
        FoodSource closestFoodSource = null;
        float minDistance = Float.MAX_VALUE;


        for (FoodSource foodSource : this.foodSources) {
            float x = (foodSource.getPosition().getX() - p.getX());
            float y = (foodSource.getPosition().getY() - p.getY());

            float distance = (float) Math.sqrt(x * x + y * y); // pythagoras

            if (distance < minDistance) {
                minDistance = distance;
                closestFoodSource = foodSource;
            }
        }

        closestFoodSource.takeFood(p);
        if (!closestFoodSource.containsFood(p)) {
            updateFoodMat(closestFoodSource.getPosition(), this.RADIUS, false);
            this.foodSources.remove(closestFoodSource);

            FoodSource newFoodSource = randFoodSource();
            updateFoodMat(newFoodSource.getPosition(), this.RADIUS, true);
            this.foodSources.add(newFoodSource);
        }
    }

    @Override
    public float getDeadAntCount(final Position p) {
        return 0;
    }

    @Override
    public float getForagingStrength(final Position p) {
        final int x = (int) p.getX();
        final int y = (int) p.getY();

        if (0 <= x && x < this.worldWidth && 0 <= y && y < this.worldHeight) {
            return this.foragingPheromones[x][y];
        }
        return 0.0f;
    }

    @Override
    public float getFoodStrength(final Position p) {
        final int x = (int) p.getX();
        final int y = (int) p.getY();

        if (0 <= x && x < this.worldWidth && 0 <= y && y < this.worldHeight) {
            return this.foodPheromones[x][y];
        }
        return 0.0f;
    }

    @Override
    public boolean containsFood(final Position p) {
        int x = (int) p.getX();
        int y = (int) p.getY();
        return this.foodMat[x][y];
    }

    @Override
    public long getFoodCount() {
        return this.foodcounter;
    }

    @Override
    public boolean isHome(final Position p) {
        final Position homeCenter;
        final float homeRadius;

        homeCenter = new Position(this.worldWidth, this.worldHeight / 2.0f);
        homeRadius = 20.0f;

        return homeCenter.isWithinRadius(p, homeRadius);
    }

    public void selfContainedDisperse(DispersalPolicy dispersalPolicy) {
        final float pheromoneAmount = 1.0f;

        for (FoodSource foodSource : this.foodSources) {
            Position foodPosition = foodSource.getPosition();

            dropFoodPheromone(foodPosition, pheromoneAmount);
        }
    }

    @Override
    public void dispersePheromones() {
        float[][] tmpForagingPheromones = new float[this.worldWidth][this.worldHeight];
        float[][] tmpFoodPheromones = new float[this.worldWidth][this.worldHeight];

        for (int x = 0; x < this.worldWidth; x++) {
            for (int y = 0; y < this.worldHeight; y++) {
                Position pos = new Position(x, y);

                if (!isObstacle(pos)) { // kontrollera om positionen är ett hinder (bara om inom världen eftersom inga custom hinder)
                    float[] newLevels = this.dispersalPolicy.getDispersedValue(this, pos);

                    // Använd det nya värdet från DispersalPolicy
                    tmpForagingPheromones[x][y] = newLevels[0];
                    tmpFoodPheromones[x][y] = newLevels[1];
                }
            }
        }

        // lägg tillbaka till original
        for (int x = 0; x < this.worldWidth; x++) {
            for (int y = 0; y < this.worldHeight; y++) {
                this.foragingPheromones[x][y] = tmpForagingPheromones[x][y];
                this.foodPheromones[x][y] = tmpFoodPheromones[x][y];
            }
        }
    }

    @Override
    public void setObstacle(final Position p, final boolean add) {

    }

    @Override
    public void hitObstacle(final Position p, final float strength) {

    }

    public class FoodSource {
        private final Position position;
        private final float radius;
        private int foodAmount;

        public FoodSource(final Position position, final float radius, final int foodAmount) {
            this.position = position;
            this.radius = radius;
            this.foodAmount = foodAmount;
        }

        public float getRadius() {
            return this.radius;
        }

        public boolean containsFood(Position p) {
            return this.foodAmount > 0;
        }

        @Override
        public String toString() {
            return "FoodSource{" +
                    "position=" + this.position +
                    ", radius=" + this.radius +
                    ", foodAmount=" + this.foodAmount +
                    '}';
        }

        public void takeFood(final Position p) {
            if (containsFood(p))
                this.foodAmount--;
        }

        public Position getPosition() {
            return this.position;
        }
    }
}
