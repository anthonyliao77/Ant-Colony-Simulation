package org.evensen.ants;

public class FoodSource {
    private final Position position;
    private int radius;
    private int amount;

    public FoodSource(final Position position, final int radius, final int amount) {
        this.position = position;
        this.radius = radius;
        this.amount = amount;
    }

    public boolean containsFood() {
        return this.amount > 0;
    }

    public void takeFood() {
        if (containsFood())
            this.amount--;
    }

    public Position getPosition() {
        return this.position;
    }

    public int getRadius() {
        return this.radius;
    }
}
