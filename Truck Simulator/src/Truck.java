public class Truck {
    private final int id;
    private final int capacity;
    private int load;
    public Truck(int id, int capacity) {
        this.id = id;
        this.capacity = capacity;
        this.load = 0;
    }
    public int getId() {
        return id;
    }
    public int getCapacity() {
        return capacity;
    }
    public int getLoad() {
        return load;
    }

    public int loadTruck(int load, int capacityConstraint) {
        int remainingLoad;
        if (load >= capacityConstraint) {
            remainingLoad = load - capacityConstraint;
            this.load += capacityConstraint;
            return remainingLoad;
        }
        this.load += load;
        return 0;
    }

    public boolean isFull() {
        return this.load == this.capacity;
    }

    public void unloadTruck() {
        this.load = 0;
    }

}
