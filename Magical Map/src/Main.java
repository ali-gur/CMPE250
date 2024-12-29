import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;

public class Main {
    // Path to the nodes file containing grid nodes and their types
    public static String nodesFile;

    // Path to the edges file containing connections (edges) between nodes and their travel times
    public static String edgesFile;

    // Path to the objectives file containing goals, radius, and node traversal tasks
    public static String objectivesFile;

    // Path for output results to be saved
    public static String outputDir;

    // Matrix to track revealed/unrevealed nodes in the grid
    public static int[][] maskMatrix;

    // HashMap to store the type of each node (e.g., impassable, normal) keyed by "x-y" coordinates
    public static HashMap<String, Integer> colorMap = new HashMap<>();

    // Adjacency map representing the graph structure as an adjacency list
    // Key: Node "x-y", Value: List of pairs representing neighboring nodes and their travel times
    public static HashMap<String, ArrayList<Pair<String, Double>>> adjacencyMap = new HashMap<>();

    // List to store precomputed offsets for revealing nodes within a circular radius
    public static ArrayList<int[]> offsets = new ArrayList<>();

    // Grid dimensions: width (number of columns) and height (number of rows)
    public static int gridWidth;  // Number of columns in the grid
    public static int gridHeight; // Number of rows in the grid

    // Counter to keep track of objectives reached during traversal
    public static int counter = 0;


    public static void main(String[] args) throws IOException {
        // File paths are passed as command-line arguments
        nodesFile = args[0]; // Path to the nodes file
        edgesFile = args[1]; // Path to the edges file
        objectivesFile = args[2]; // Path to the objectives file
        outputDir = args[3]; // Directory where the output file will be created

        // Load data from nodes and edges files
        loadColors(nodesFile); // Load node color data
        loadEdges(edgesFile);  // Load edge connections

        // Create output file for writing results
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputDir));

        try (BufferedReader br = new BufferedReader(new FileReader(objectivesFile))) {
            // Read the radius (first line of the objectives file)
            int radius = Integer.parseInt(br.readLine().trim());

            // Precompute offsets for revealing nodes based on the radius
            precomputeOffsets(radius);

            // Read the initial node (second line of the objectives file)
            String initialNode = br.readLine().trim().replace(" ", "-");

            ArrayList<Integer> currentOptions = null; // Stores current options for nodes
            ArrayList<Integer> nextOptions;          // Stores next options for nodes
            ArrayList<String> path;                  // To hold the shortest path between nodes

            String line; // To read lines from the objectives file
            while ((line = br.readLine()) != null && !line.trim().isEmpty()) {
                // Split the line into values
                String[] values = line.trim().split(" ");

                // Extract the target node (composed of two parts)
                String targetNode = values[0] + "-" + values[1];

                // Extract options (numbers starting from index 2)
                ArrayList<Integer> options = new ArrayList<>();
                for (int i = 2; i < values.length; i++) {
                    options.add(Integer.parseInt(values[i])); // Parse and add each option
                }
                nextOptions = new ArrayList<>(options); // Set next options

                // Process the current options if they exist
                if (currentOptions != null && !currentOptions.isEmpty()) {
                    HashMap<String, Integer> bestColorMap = null; // Holds the best color mapping
                    double bestTime = Double.MAX_VALUE;          // Holds the shortest time found
                    int bestOption = -1;                         // Holds the best option chosen

                    for (int x : currentOptions) { // Iterate through current options
                        // Create a new color map based on the current option
                        HashMap<String, Integer> newColorMap = makeTypeXto0(colorMap, x);

                        // Find the shortest path from the initial node to the target node
                        path = findShortestPath(initialNode, targetNode, newColorMap);

                        if (path.isEmpty()) { // If no valid path, skip to the next option
                            continue;
                        }

                        // Calculate the total travel time for the path
                        double time = 0.0;
                        for (int j = 0; j < path.size() - 1; j++) {
                            String currentNode = path.get(j);
                            String nextNode = path.get(j + 1);

                            // Get travel time between consecutive nodes
                            double segmentTime = getTravelTime(currentNode, nextNode);
                            time += segmentTime; // Accumulate time for this path
                        }

                        // Update the best option if a shorter time is found
                        if (time < bestTime) {
                            bestTime = time;
                            bestColorMap = newColorMap;
                            bestOption = x;
                        }
                    }

                    // Write the best option chosen to the output file
                    writer.write("Number " + bestOption + " is chosen!");
                    writer.newLine();

                    // Update the global color map with the bestColorMap
                    colorMap = bestColorMap;
                }

                // Traverse nodes and handle obstacles between the initial and target nodes
                traverseAndHandleObstacles(initialNode, targetNode, writer);

                // Write a message indicating the objective is reached
                writer.write("Objective " + ++counter + " reached!");
                writer.newLine();

                // Update current options and initial node for the next iteration
                currentOptions = nextOptions;
                initialNode = targetNode;
            }
        } catch (IOException e) {
            // Print the stack trace in case of an IO exception
            e.printStackTrace();
        }

        // Close the output file writer
        writer.close();
    }

    public static void traverseAndHandleObstacles(String initialNode, String targetNode,
                                                  BufferedWriter writer) throws IOException {
        // Reveal nodes around the initial position
        revealNodes(initialNode);

        // Find the shortest path from the initial node to the target node
        ArrayList<String> route = findShortestPath(initialNode, targetNode, colorMap);

        // Continue traversing until the route is empty or the target is reached
        while (!route.isEmpty()) {
            // Take the first node in the route as the current position
            String currentNode = route.get(0);

            // If the current node is the target node, stop the traversal
            if (currentNode.equals(targetNode)) {
                return;
            }

            // Reveal nodes around the current position to discover new information
            revealNodes(currentNode);

            // Flag to check if an obstacle is found in the route
            boolean obstacleFound = false;

            // Check the remaining nodes in the route for obstacles
            for (int i = 1; i < route.size(); i++) { // Start checking from the next node
                String nextNode = route.get(i);

                // Parse the next node to extract its x and y coordinates
                String[] parts = nextNode.split("-");
                int x = Integer.parseInt(parts[0]);
                int y = Integer.parseInt(parts[1]);

                // Check if the next node is impassable
                if (colorMap.getOrDefault(nextNode, 1) != 0 && maskMatrix[x][y] == 1) {
                    // If an obstacle is found, write a message to the output file
                    writer.write("Path is impassable!");
                    writer.newLine();

                    // Recalculate a new route from the current position to the target
                    ArrayList<String> newRoute = findShortestPath(currentNode, targetNode, colorMap);

                    // If no valid route is found, terminate the traversal
                    if (newRoute.isEmpty()) {
                        return;
                    }

                    // Update the route with the new path and mark the obstacle as handled
                    route = newRoute;
                    obstacleFound = true;
                    break;
                }
            }

            // If an obstacle was found, restart the traversal loop
            if (obstacleFound) {
                continue;
            }

            // Move to the next node in the route if no obstacle was found
            if (route.size() > 1) {
                String nextNode = route.get(1);

                // Write the movement action to the output file
                writer.write("Moving to " + nextNode);
                writer.newLine();

                // Remove the current node from the route after processing
                route.remove(0);
            }
        }
    }

    public static double getTravelTime(String fromNode, String toNode) {
        // Retrieve the list of neighbors for the given 'fromNode' from the adjacency map
        // If 'fromNode' has no neighbors, return an empty list as default
        ArrayList<Pair<String, Double>> neighbors = adjacencyMap.getOrDefault(fromNode, new ArrayList<>());

        // Iterate through the list of neighbors to find the target 'toNode'
        for (Pair<String, Double> neighbor : neighbors) {
            // Check if the current neighbor matches the destination node 'toNode'
            if (neighbor.getKey().equals(toNode)) {
                // Return the travel time (weight) associated with this edge
                return neighbor.getValue();
            }
        }

        // Return -1 if no edge exists between 'fromNode' and 'toNode'
        return -1;
    }

    public static void loadColors(String filename) throws IOException {
        // Open the file for reading using BufferedReader
        BufferedReader br = new BufferedReader(new FileReader(filename));

        String line;

        // Read the first line to get the grid dimensions (width and height)
        line = br.readLine();
        gridWidth = Integer.parseInt(line.split(" ")[0]);  // Extract grid width
        gridHeight = Integer.parseInt(line.split(" ")[1]); // Extract grid height

        // Initialize the mask matrix with the dimensions of the grid
        maskMatrix = new int[gridWidth][gridHeight];

        // Read the remaining lines in the file
        while ((line = br.readLine()) != null) {
            line = line.trim(); // Trim leading/trailing spaces
            String[] parts = line.split(" "); // Split the line into components

            // Extract node coordinates and type
            String node = parts[0] + "-" + parts[1]; // Combine x and y as "x-y" for the node
            int type = Integer.parseInt(parts[2]);   // Extract the node type (e.g., 0 or 1)

            // Add the node and its type to the colorMap
            colorMap.put(node, type);

            // Update the maskMatrix to mark impassable nodes (type == 1)
            if (type == 1) {
                maskMatrix[Integer.parseInt(parts[0])][Integer.parseInt(parts[1])] = 1;
            }
        }

        // Close the BufferedReader to release resources
        br.close();
    }

    public static void loadEdges(String filename) throws IOException {
        // Open the file for reading using BufferedReader
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line;

        // Read the file line by line
        while ((line = br.readLine()) != null) {
            line = line.trim(); // Remove leading and trailing spaces
            if (line.isEmpty()) continue; // Skip empty lines

            // Split the line into parts: "srcNode,destNode travelTime"
            String[] parts = line.split(" ");

            // Extract the nodes and travel time
            String[] nodes = parts[0].split(","); // Split source and destination nodes
            String srcNode = nodes[0];           // Source node
            String destNode = nodes[1];          // Destination node
            double travelTime = Double.parseDouble(parts[1]); // Extract travel time

            // Add the edge to the adjacency map for the source node
            if (!adjacencyMap.containsKey(srcNode)) {
                adjacencyMap.put(srcNode, new ArrayList<>()); // Initialize list if not present
            }
            adjacencyMap.get(srcNode).add(new Pair<>(destNode, travelTime)); // Add neighbor

            // Add the edge to the adjacency map for the destination node (undirected graph)
            if (!adjacencyMap.containsKey(destNode)) {
                adjacencyMap.put(destNode, new ArrayList<>()); // Initialize list if not present
            }
            adjacencyMap.get(destNode).add(new Pair<>(srcNode, travelTime)); // Add neighbor
        }

        // Close the BufferedReader to release system resources
        br.close();
    }

    public static ArrayList<String> findShortestPath(String startNode, String targetNode,
                                                     HashMap<String, Integer> colorMap) {
        // Min-heap to prioritize nodes based on their distance (travel time) from the start node
        Heap<Pair<String, Double>> heap = new Heap<>(Comparator.comparingDouble(Pair::getValue));

        // Map to store the shortest known distance to each node
        HashMap<String, Double> distances = new HashMap<>();

        // Map to store the previous node for reconstructing the path
        HashMap<String, String> previous = new HashMap<>();

        // Set to track visited nodes
        HashSet<String> visited = new HashSet<>();

        // Initialize the starting node's distance to 0 and add it to the heap
        distances.put(startNode, 0.0);
        heap.add(new Pair<>(startNode, 0.0));

        // Dijkstra's algorithm to explore nodes with the shortest distance
        while (!heap.isEmpty()) {
            // Extract the node with the smallest distance from the heap
            Pair<String, Double> current = heap.poll();
            String currentNode = current.getKey();

            // Skip the node if it has already been visited
            if (visited.contains(currentNode)) continue;

            // Mark the current node as visited
            visited.add(currentNode);

            // Stop the search if the target node is reached
            if (currentNode.equals(targetNode)) break;

            // Explore the neighbors of the current node
            for (Pair<String, Double> neighbor : adjacencyMap.getOrDefault(currentNode, new ArrayList<>())) {
                String neighborNode = neighbor.getKey();
                double travelTime = neighbor.getValue();

                // Parse the neighbor node's coordinates (x, y)
                String[] parts = neighborNode.split("-");
                int x = Integer.parseInt(parts[0]);
                int y = Integer.parseInt(parts[1]);

                // Skip the neighbor if it has already been visited
                if (visited.contains(neighborNode)) continue;

                // Skip the neighbor if it is impassable (based on colorMap and maskMatrix)
                if (colorMap.getOrDefault(neighborNode, 1) != 0 && maskMatrix[x][y] == 1) continue;

                // Calculate the new distance to the neighbor node
                double newDist = distances.getOrDefault(currentNode, Double.MAX_VALUE) + travelTime;

                // Update the shortest distance and path to the neighbor if a shorter path is found
                if (newDist < distances.getOrDefault(neighborNode, Double.MAX_VALUE)) {
                    distances.put(neighborNode, newDist);
                    previous.put(neighborNode, currentNode);
                    heap.add(new Pair<>(neighborNode, newDist)); // Add the neighbor to the heap with updated distance
                }
            }
        }

        // Reconstruct the shortest path from the target node to the start node
        ArrayList<String> path = new ArrayList<>();
        String step = targetNode;

        // Backtrack from the target node using the 'previous' map
        while (previous.containsKey(step)) {
            path.add(0, step); // Add the current step to the front of the path
            step = previous.get(step); // Move to the previous node
        }

        // Add the starting node to the path
        path.add(0, startNode);

        // Return the reconstructed shortest path
        return path;
    }

    public static void precomputeOffsets(int radius) {
        // Iterate over all possible x (dx) offsets within the given radius
        for (int dx = -radius; dx <= radius; dx++) {
            // Iterate over all possible y (dy) offsets within the given radius
            for (int dy = -radius; dy <= radius; dy++) {
                // Check if the point (dx, dy) lies within the circle of the given radius
                // The condition dx^2 + dy^2 <= radius^2 ensures a circular boundary
                if (dx * dx + dy * dy <= radius * radius) {
                    // Add the valid offset (dx, dy) to the offsets list
                    offsets.add(new int[]{dx, dy});
                }
            }
        }
    }

    public static void revealNodes(String currentNode) {
        // Parse the current node coordinates (x, y) from the string format "x-y"
        String[] parts = currentNode.split("-");
        int x = Integer.parseInt(parts[0]); // Extract the x-coordinate
        int y = Integer.parseInt(parts[1]); // Extract the y-coordinate

        // Iterate over the precomputed offsets to reveal nodes in the surrounding area
        for (int[] offset : offsets) {
            int nx = x + offset[0]; // Calculate the new x-coordinate
            int ny = y + offset[1]; // Calculate the new y-coordinate

            // Check if the new coordinates (nx, ny) are within the grid boundaries
            if (nx >= 0 && ny >= 0 && nx < gridWidth && ny < gridHeight) {
                // Update the maskMatrix to mark the node as "revealed"
                maskMatrix[nx][ny] = 1;
            }
        }
    }

    public static HashMap<String, Integer> makeTypeXto0(HashMap<String, Integer> colorMap, int x) {
        // Create a new HashMap to store the modified color map
        HashMap<String, Integer> newColorMap = new HashMap<>();

        // Iterate through all the keys (nodes) in the original color map
        for (String node : colorMap.getKeys()) {
            // Check if the current node's value (type) equals the specified value 'x'
            if (colorMap.get(node) == x) {
                // Set the node's value to 0 in the new color map
                newColorMap.put(node, 0);
            } else {
                // Otherwise, copy the node's original value to the new color map
                newColorMap.put(node, colorMap.get(node));
            }
        }

        // Return the new color map with updated values
        return newColorMap;
    }

    public static boolean areFilesIdentical(String filePath1, String filePath2) {
        try (
                // Open both files using BufferedReader for efficient line-by-line reading
                BufferedReader reader1 = new BufferedReader(new FileReader(filePath1));
                BufferedReader reader2 = new BufferedReader(new FileReader(filePath2))
        ) {
            String line1, line2; // Variables to hold lines read from the two files
            int count = 1;       // Line counter to track the current line number

            // Read lines from both files and compare them one by one
            while ((line1 = reader1.readLine()) != null && (line2 = reader2.readLine()) != null) {
                // Compare lines: if they are not equal, print the line number and differing content
                if (!line1.equals(line2)) {
                    System.out.println("line:" + count + " " + line1 + " " + line2);
                    return false; // Return false if lines are different
                }
                count++; // Increment the line counter
            }

            // Check if one file has extra lines by attempting to read the next line from each file
            // If both readers return null, the files have the same number of lines
            return reader1.readLine() == null && reader2.readLine() == null;

        } catch (IOException e) {
            // Handle any I/O exceptions that occur while reading the files
            e.printStackTrace();
            return false; // Return false if an exception occurs
        }
    }

}
