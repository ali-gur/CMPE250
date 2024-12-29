import java.io.*;
import java.util.ArrayList;

public class Main {
    // AVL trees for managing parking lot states
    public static AVLTree allParkingLots = new AVLTree();  // Holds all parking lots
    public static AVLTree availableParkingLots = new AVLTree();  // Holds only available parking lots
    public static AVLTree waitingParkingLots = new AVLTree();  // Holds parking lots with trucks in waiting queue
    public static AVLTree readyParkingLots = new AVLTree();  // Holds parking lots with trucks in ready queue

    public static void main(String[] args) {
        String inputFilePath = args[0];
        String outputFilePath = args[1];

        try (
                BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
                BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))
        ) {
            String result = null;
            String line = null;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.trim().split(" ");
                String command = parts[0];
                ArrayList<Integer> params = new ArrayList<>();
                result = "";

                for (int i = 1; i < parts.length; i++) {
                    params.add(Integer.parseInt(parts[i]));
                }


                switch (command) {
                    case "create_parking_lot":
                        createParkingLot(params.get(0), params.get(1));
                        break;
                    case "add_truck":
                        Truck truck = new Truck(params.get(0), params.get(1));
                        result = addTruck(truck);
                        break;
                    case "ready":
                        result = ready(params.get(0));
                        break;
                    case "load":
                        result = load(params.get(0), params.get(1));
                        break;
                    case "delete_parking_lot":
                        deleteParkingLot(params.get(0));
                        break;
                    case "count":
                        result = String.valueOf(count(params.get(0)));
                        break;
                }

                if (!result.isEmpty()) {
                    writer.write(result);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public static void createParkingLot(int capacityConstraint, int truckLimit) {
        ParkingLot parkingLot = new ParkingLot(capacityConstraint, truckLimit);
        allParkingLots.root = allParkingLots.insert(allParkingLots.root, null, parkingLot);
        availableParkingLots.root = availableParkingLots.insert(availableParkingLots.root, null, parkingLot);
    }

    public static void deleteParkingLot(int capacityConstraint){
        allParkingLots.root = allParkingLots.delete(allParkingLots.root, capacityConstraint);
        availableParkingLots.root = availableParkingLots.delete(availableParkingLots.root, capacityConstraint);
        readyParkingLots.root = readyParkingLots.delete(readyParkingLots.root, capacityConstraint);
        waitingParkingLots.root = waitingParkingLots.delete(waitingParkingLots.root, capacityConstraint);
    }

    // Attempts to add a truck to an available parking lot based on the truck's capacity.
    // Returns the ID of the parking lot if added successfully, or "-1" if no suitable lot is found.
    public static String addTruck(Truck truck){
        AVLNode root = availableParkingLots.root;
        if (root == null) {
            return "-1";
        }

        AVLNode current = root;
        Stack<AVLNode> stack = new Stack<>();
        String result = "-1";
        int capacityConstraint = truck.getCapacity() - truck.getLoad();

        // Search for a lot with matching or greater capacity
        while (true) {
            if (current.key == capacityConstraint) {
                current.parkingLot.getWaitingQueue().enqueue(truck);
                waitingParkingLots.root = waitingParkingLots.insert(waitingParkingLots.root, null, current.parkingLot);
                result = current.key + "";
                allParkingLots.updateSumTree(current);
                if (current.parkingLot.isFull()) {
                    availableParkingLots.root = availableParkingLots.delete(availableParkingLots.root, current.key);
                }
                return result;
            } else if (current.key > capacityConstraint) {
                if (current.left == null) {
                    current = current.left;
                    break;
                }
                current = current.left;
            } else {
                if (current.right == null) {
                    break;
                }
                stack.push(current);
                current = current.right;
            }
        }

        // If no exact match is found, use the stack and find the lot with smallest larger id
        if (current != null || !stack.isEmpty()) {
            while (current != null) {
                stack.push(current);
                current = current.right;
            }
            current = stack.pop();
            current.parkingLot.getWaitingQueue().enqueue(truck);
            waitingParkingLots.root = waitingParkingLots.insert(waitingParkingLots.root, null, current.parkingLot);
            result = current.key + "";
            allParkingLots.updateSumTree(current);
            if (current.parkingLot.isFull()) {
                availableParkingLots.root = availableParkingLots.delete(availableParkingLots.root, current.key);
            }
            return result;
        }
        return "-1";
    }

    // Moves a truck from the waiting queue to the ready queue in the specified parking lot
    // Returns the truck ID and lot ID if successful, or "-1" if no matching lot is found
    public static String ready(int capacityConstraint){
        AVLNode root = waitingParkingLots.root;
        if (root == null) {
            return "-1";
        }

        AVLNode current = root;
        Stack<AVLNode> stack = new Stack<>();
        String result;

        // Traverse the AVL tree to find a node matching the capacity constraint
        while (true) {
            if (current.key == capacityConstraint) {
                Truck truck = current.parkingLot.getWaitingQueue().dequeue();
                current.parkingLot.getReadyQueue().enqueue(truck);
                result = truck.getId() + " " + current.key;
                readyParkingLots.root = readyParkingLots.insert(readyParkingLots.root, null, current.parkingLot);
                if (current.parkingLot.getWaitingQueue().isEmpty()) {
                    waitingParkingLots.root = waitingParkingLots.delete(waitingParkingLots.root, current.key);
                }
                return result;
            } else if (current.key < capacityConstraint) {
                if (current.right == null) {
                    current = current.right;
                    break;
                }
                current = current.right;
            } else {
                if (current.left == null) {
                    break;
                }
                stack.push(current);
                current = current.left;
            }
        }

        // If no exact match, attempt to find the closest available lot
        if (current != null || !stack.isEmpty()) {
            while (current != null) {
                stack.push(current);
                current = current.left;
            }
            current = stack.pop();
            Truck truck = current.parkingLot.getWaitingQueue().dequeue();
            current.parkingLot.getReadyQueue().enqueue(truck);
            result = truck.getId() + " " + current.key;
            readyParkingLots.root = readyParkingLots.insert(readyParkingLots.root, null, current.parkingLot);
            if (current.parkingLot.getWaitingQueue().isEmpty()) {
                waitingParkingLots.root = waitingParkingLots.delete(waitingParkingLots.root, current.key);
            }
            return result;
        }
        return "-1";
    }

    // Loads trucks in the specified parking lot until the load requirement is met or trucks are depleted
    // Returns a report of the trucks loaded and their new lot IDs or "-1" if no trucks were available
    public static String load(int capacityConstraint, int load){
        AVLNode root = readyParkingLots.root;
        if (root == null) {
            return "-1";
        }

        ArrayList<Integer> ids = new ArrayList<>();
        ArrayList<Integer> results = new ArrayList<>();
        ArrayList<Integer> keysToDelete = new ArrayList<>();
        String result;
        String report;
        Truck truck;

        AVLNode current = root;
        Stack<AVLNode> stack = new Stack<>();

        // Traverse AVL tree to find node matching capacity constraint
        while (true) {
            if (current.key == capacityConstraint) {
                while (load != 0 && !current.parkingLot.getReadyQueue().isEmpty()) {
                    truck = current.parkingLot.getReadyQueue().dequeue();
                    allParkingLots.updateSumTree(current);
                    availableParkingLots.root = availableParkingLots.insert(availableParkingLots.root, null, current.parkingLot);
                    load = truck.loadTruck(load, current.key);

                    if (truck.isFull()) {
                        truck.unloadTruck();
                    }

                    result = addTruck(truck);
                    results.add(Integer.parseInt(result));
                    ids.add(truck.getId());
                }
                if (load == 0) {
                    report = ids.get(0) + " " + results.get(0);
                    for (int i = 1; i < ids.size(); i++) {
                        report += " - " + ids.get(i);
                        report += " " + results.get(i);
                    }
                    if (current.parkingLot.getReadyQueue().isEmpty()) {
                        readyParkingLots.root = readyParkingLots.delete(readyParkingLots.root, current.key);
                    }
                    return report;
                }
                if (current.parkingLot.getReadyQueue().isEmpty()) {
                    keysToDelete.add(current.key);
                }
                current = current.right;
                break;
            } else if (current.key < capacityConstraint) {
                if (current.right == null) {
                    current = current.right;
                    break;
                }
                current = current.right;
            } else {
                if (current.left == null) {
                    break;
                }
                stack.push(current);
                current = current.left;
            }
        }

        // If no exact match, continue AVL traversal to load trucks from closest lots
        while (current != null || !stack.isEmpty()) {
            while (current != null) {
                stack.push(current);
                current = current.left;
            }
            current = stack.pop();
            while (load != 0 && !current.parkingLot.getReadyQueue().isEmpty()) {
                truck = current.parkingLot.getReadyQueue().dequeue();
                allParkingLots.updateSumTree(current);
                availableParkingLots.root = availableParkingLots.insert(availableParkingLots.root, null, current.parkingLot);
                load = truck.loadTruck(load, current.key);

                if (truck.isFull()) {
                    truck.unloadTruck();

                }

                result = addTruck(truck);
                results.add(Integer.parseInt(result));
                ids.add(truck.getId());

            }
            if (load == 0) {
                report = ids.get(0) + " " + results.get(0);
                for (int i = 1; i < ids.size(); i++) {
                    report += " - " + ids.get(i);
                    report += " " + results.get(i);
                }
                if (current.parkingLot.getReadyQueue().isEmpty()) {
                    readyParkingLots.root = readyParkingLots.delete(readyParkingLots.root, current.key);
                }
                for (int key:keysToDelete) {
                    readyParkingLots.root = readyParkingLots.delete(readyParkingLots.root, key);
                }
                return report;
            }
            if (current.parkingLot.getReadyQueue().isEmpty()) {
                keysToDelete.add(current.key);
            }
            current = current.right;
        }
        if (ids.isEmpty()) {
            return "-1";
        }
        report = ids.get(0) + " " + results.get(0);
        for (int i = 1; i < ids.size(); i++) {
            report += " - " + ids.get(i);
            report += " " + results.get(i);
        }
        for (int key:keysToDelete) {
            readyParkingLots.root = readyParkingLots.delete(readyParkingLots.root, key);
        }
        return report;

    }

    public static int count(int capacityConstraint){
        AVLNode root = allParkingLots.root;
        if (root == null) {
            return 0;
        }

        // Traverse the tree to find the smallest node with capacity greater than capacityConstraint
        AVLNode current = root;
        AVLNode minNode = null;
        int total = 0;
        while (current != null) {
            if (current.key > capacityConstraint) {
                minNode = current;
                current = current.left;
            } else {
                current = current.right;
            }
        }

        // Traverse upwards from minNode and sum counts of nodes with sufficient capacity
        boolean fromLeft = true;
        current = minNode;
        while (current != null) {
            if (fromLeft) {
                total += current.sum + current.rightSum;
            } else {
                if (current.key > capacityConstraint) {
                    total += current.sum;
                }
            }
            if (current.parent != null && current.key < current.parent.key) {
                fromLeft = true;
            } else {
                fromLeft = false;
            }
            current = current.parent;
        }
        return total;
    }

    public static boolean areFilesIdentical(String filePath1, String filePath2) {
        try (BufferedReader reader1 = new BufferedReader(new FileReader(filePath1));
             BufferedReader reader2 = new BufferedReader(new FileReader(filePath2))) {

            String line1, line2;

            int count = 1;

            // Read each line from both files and compare
            while ((line1 = reader1.readLine()) != null && (line2 = reader2.readLine()) != null) {
                if (!line1.equals(line2)) {
                    System.out.println("line:" + count + " " + line1 + " " + line2);

                    return false; // Lines are different
                }
                count++;
            }

            // Check if one file has extra lines
            return reader1.readLine() == null && reader2.readLine() == null;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}