package moe.wolfgirl.probejs.next.java;

import moe.wolfgirl.probejs.next.ClassPath;

import java.util.*;

/**
 * We want to resolve the package structure of the class paths, as translating the types
 * into TypeScript declaration files needs index.d.ts file to be generated for each package,
 * like java/index.d.ts -> java/lang/index.d.ts.
 * <br>
 * This class is responsible for building the package tree and traversing it. Actual
 * dump code will be elsewhere.
 */
public class PackageTree {
    private final Node root = new Node(null, true);

    public void addClassPath(ClassPath classPath) {
        List<String> segments = classPath.segments();
        Node current = root;
        // Walk all segments except last as package nodes
        for (int i = 0; i < segments.size() - 1; i++) {
            final int depth = i + 1;
            current = current.children.computeIfAbsent(segments.get(i),
                    k -> new Node(new ClassPath(segments.subList(0, depth)), true));
        }
        // Last segment is a class node
        String last = segments.getLast();
        current.children.put(last, new Node(classPath, false));
    }

    public void removeClassPath(ClassPath classPath) {
        List<String> segments = classPath.segments();
        // Collect nodes along the path
        List<Node> path = new ArrayList<>();
        path.add(root);
        Node current = root;
        for (int i = 0; i < segments.size() - 1; i++) {
            current = current.children.get(segments.get(i));
            if (current == null) return;
            path.add(current);
        }
        // Remove the class node
        String last = segments.getLast();
        if (current.children.remove(last) == null) return;
        // Clean up empty package nodes bottom-up
        for (int i = path.size() - 1; i > 0; i--) {
            Node node = path.get(i);
            if (node.children.isEmpty()) {
                path.get(i - 1).children.remove(segments.get(i - 1));
            } else {
                break;
            }
        }
    }

    // Traverse in a DFS way, or we might have redundant file checks
    public Iterable<Node> traverse(boolean includeClasses) {
        List<Node> result = new ArrayList<>();
        Deque<Node> stack = new ArrayDeque<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            Node node = stack.pop();
            if (node != root) {
                if (node.isPackage || includeClasses) {
                    result.add(node);
                }
            }
            // Push children in reverse order so leftmost is processed first
            List<Node> childList = new ArrayList<>(node.children.values());
            Collections.reverse(childList);
            for (Node child : childList) {
                stack.push(child);
            }
        }
        return result;
    }

    public Iterable<Node> traverse() {
        return traverse(false);
    }

    public Node getRoot() {
        return root;
    }

    public static class Node {
        boolean isPackage;
        private final ClassPath classPath;
        final Map<String, Node> children = new LinkedHashMap<>();

        Node(ClassPath classPath, boolean isPackage) {
            this.classPath = classPath;
            this.isPackage = isPackage;
        }

        public ClassPath getClassPath() {
            return classPath;
        }

        public List<ClassPath> getSubPackages() {
            List<ClassPath> result = new ArrayList<>();
            for (Node child : children.values()) {
                if (child.isPackage) {
                    result.add(child.classPath);
                }
            }
            return result;
        }

        public List<ClassPath> getClasses() {
            List<ClassPath> result = new ArrayList<>();
            for (Node child : children.values()) {
                if (!child.isPackage) {
                    result.add(child.classPath);
                }
            }
            return result;
        }
    }
}
