package me.mmmjjkx.titlechanger.texts;

public class RopeImplString {
    private static final int MAX_LEAF_LENGTH = 8;
    private Node root;

    private abstract static class Node {
        int weight;
        abstract char index(int i);
        abstract String substring(int start, int end);
        abstract void collectLeaves(StringBuilder builder);
    }

    private static class LeafNode extends Node {
        String data;

        LeafNode(String data) {
            this.data = data;
            this.weight = data.length();
        }

        @Override
        char index(int i) {
            return data.charAt(i);
        }

        @Override
        String substring(int start, int end) {
            return data.substring(start, end);
        }

        @Override
        void collectLeaves(StringBuilder builder) {
            builder.append(data);
        }
    }

    private static class InternalNode extends Node {
        Node left;
        Node right;

        InternalNode(Node left, Node right) {
            this.left = left;
            this.right = right;
            this.weight = left.weight;
        }

        @Override
        char index(int i) {
            if (i < weight) {
                return left.index(i);
            } else {
                return right.index(i - weight);
            }
        }

        @Override
        String substring(int start, int end) {
            if (end <= weight) {
                return left.substring(start, end);
            } else if (start >= weight) {
                return right.substring(start - weight, end - weight);
            } else {
                return left.substring(start, weight) + right.substring(0, end - weight);
            }
        }

        @Override
        void collectLeaves(StringBuilder builder) {
            left.collectLeaves(builder);
            right.collectLeaves(builder);
        }
    }

    public RopeImplString(String initial) {
        this.root = buildRope(initial, 0, initial.length());
    }

    private Node buildRope(String s, int start, int end) {
        int length = end - start;
        if (length <= MAX_LEAF_LENGTH) {
            return new LeafNode(s.substring(start, end));
        }
        int mid = start + length / 2;
        Node left = buildRope(s, start, mid);
        Node right = buildRope(s, mid, end);
        return new InternalNode(left, right);
    }

    public void concat(RopeImplString other) {
        this.root = new InternalNode(this.root, other.root);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        root.collectLeaves(builder);
        return builder.toString();
    }
}
