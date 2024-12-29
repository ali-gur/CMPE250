public class ParkingLot {
    private final int capacityConstraint;
    private final int truckLimit;
    private final Queue<Truck> waitingQueue;
    private final Queue<Truck> readyQueue;

    public ParkingLot(int capacityConstraint, int truckLimit) {
        this.capacityConstraint = capacityConstraint;
        this.truckLimit = truckLimit;
        this.waitingQueue = new Queue<>();
        this.readyQueue = new Queue<>();
    }

    public int getCapacityConstraint() {
        return capacityConstraint;
    }

    public Queue<Truck> getWaitingQueue() {
        return waitingQueue;
    }

    public Queue<Truck> getReadyQueue() {
        return readyQueue;
    }

    public boolean isFull() {
        return waitingQueue.size() + readyQueue.size() == truckLimit;
    }
}
