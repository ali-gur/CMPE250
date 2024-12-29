class AVLNode {
    int key, height;
    int leftSum, rightSum, sum;
    AVLNode left, right, parent;
    ParkingLot parkingLot;

    public AVLNode(ParkingLot parkingLot, AVLNode parent) {
        this.key = parkingLot.getCapacityConstraint();
        this.parkingLot = parkingLot;
        height = 0;
        this.parent = parent;
        this.leftSum = 0;
        this.rightSum = 0;
        this.sum = 0;
    }

    public void updateSum() {
        this.sum = this.parkingLot.getReadyQueue().size() + this.parkingLot.getWaitingQueue().size();
    }
}

public class AVLTree {
    public AVLNode root;

    public AVLTree() {
        this.root = null;
    }

    public int height(AVLNode node) {
        return (node == null) ? -1 : node.height;
    }

    public int sum(AVLNode node) {
        return (node == null) ? 0 : node.sum + node.leftSum + node.rightSum;
    }

    public int balanceFactor(AVLNode node) {
        if (node == null)
            return 0;
        return height(node.left) - height(node.right);
    }

    public void updateSumTree(AVLNode node) {
        AVLNode current = this.root;

        while (current.key != node.key) {
            if (current.key < node.key) {
                current = current.right;
            } else {
                current = current.left;
            }
        }

        while (current.parent != null) {

            current.updateSum();

            current.leftSum = sum(current.left);
            current.rightSum = sum(current.right);
            current = current.parent;
        }
        current.updateSum();
        current.leftSum = sum(current.left);
        current.rightSum = sum(current.right);
    }

    public AVLNode rightRotate(AVLNode y) {
        AVLNode x = y.left;
        AVLNode T2 = x.right;

        x.right = y;
        x.rightSum = sum(y);
        y.left = T2;
        y.leftSum = sum(T2);

        if (T2 != null) T2.parent = y;
        x.parent = y.parent;
        y.parent = x;

        y.height = Math.max(height(y.left), height(y.right)) + 1;
        x.height = Math.max(height(x.left), height(x.right)) + 1;

        return x;
    }

    public AVLNode leftRotate(AVLNode x) {
        AVLNode y = x.right;
        AVLNode T2 = y.left;

        y.left = x;
        y.leftSum = sum(x);
        x.right = T2;
        x.rightSum = sum(T2);

        if (T2 != null) T2.parent = x;
        y.parent = x.parent;
        x.parent = y;

        x.height = Math.max(height(x.left), height(x.right)) + 1;
        y.height = Math.max(height(y.left), height(y.right)) + 1;

        return y;
    }

    public AVLNode insert(AVLNode root, AVLNode parent, ParkingLot parkingLot) {
        if (root == null)
            return new AVLNode(parkingLot, parent);

        if (parkingLot.getCapacityConstraint() < root.key) {
            root.left = insert(root.left, root, parkingLot);
        } else if (parkingLot.getCapacityConstraint() > root.key) {
            root.right = insert(root.right, root, parkingLot);
        } else {
            return root;
        }

        root.height = 1 + Math.max(height(root.left), height(root.right));
        root.leftSum = sum(root.left);
        root.rightSum = sum(root.right);


        int balance = balanceFactor(root);

        if (balance > 1 && parkingLot.getCapacityConstraint() < root.left.key)
            return rightRotate(root);

        if (balance < -1 && parkingLot.getCapacityConstraint() > root.right.key)
            return leftRotate(root);

        if (balance > 1 && parkingLot.getCapacityConstraint() > root.left.key) {
            root.left = leftRotate(root.left);
            return rightRotate(root);
        }

        if (balance < -1 && parkingLot.getCapacityConstraint() < root.right.key) {
            root.right = rightRotate(root.right);
            return leftRotate(root);
        }

        return root;
    }

    public AVLNode delete(AVLNode root, int key) {
        if (root == null) {
            return root;
        }

        if (key < root.key) {
            root.left = delete(root.left, key);
        } else if (key > root.key) {
            root.right = delete(root.right, key);
        } else {
            // Node to be deleted found
            if (root.left == null && root.right == null) {
                // Node with no child (leaf node)
                root = null;

            } else if (root.left == null || root.right == null) {
                // Node with only one child
                AVLNode temp = (root.left != null) ? root.left : root.right;

                temp.parent = root.parent;
                root = temp;

            } else {
                // Node with two children
                AVLNode successor = findSuccessor(root.right);

                // Copy the successor's key and other values to the current node
                root.key = successor.key;
                root.parkingLot = successor.parkingLot;
                root.leftSum = successor.leftSum;
                root.rightSum = successor.rightSum;
                root.sum = successor.sum;

                // Delete the successor
                root.right = delete(root.right, successor.key);
            }
        }

        // If the tree had only one node, return
        if (root == null) {
            return root;
        }

        // Update height and balance factors
        root.height = Math.max(height(root.left), height(root.right)) + 1;
        root.leftSum = sum(root.left);
        root.rightSum = sum(root.right);

        // Balance the tree if it has become unbalanced
        int balance = balanceFactor(root);

        // LL case
        if (balance > 1 && balanceFactor(root.left) >= 0) {
            return rightRotate(root);
        }

        // LR case
        if (balance > 1 && balanceFactor(root.left) < 0) {
            root.left = leftRotate(root.left);
            return rightRotate(root);
        }

        // RR case
        if (balance < -1 && balanceFactor(root.right) <= 0) {
            return leftRotate(root);
        }

        // RL case
        if (balance < -1 && balanceFactor(root.right) > 0) {
            root.right = rightRotate(root.right);
            return leftRotate(root);
        }

        return root;
    }

    public AVLNode findSuccessor(AVLNode node) {
        AVLNode current = node;
        while (current.left != null)
            current = current.left;
        return current;
    }
}
