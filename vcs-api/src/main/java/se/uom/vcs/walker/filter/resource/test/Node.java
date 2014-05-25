package se.uom.vcs.walker.filter.resource.test;

import java.util.HashSet;
import java.util.Set;

import se.uom.vcs.walker.filter.resource.AbstractPathFilter;

public class Node {

    String name;
    Set<Node> children;
    Node parent;
    TreeFilter filter;

    public static final Node ALL = new Node("*");
    public static final Node CHILD = new Node(":");
    public static final Node MODIFIED = new Node("?");
    public static final Node SUFFIX = new Node(">");
    
    public Node(String name) {
	if (name == null) {
	    throw new IllegalArgumentException("name is null");
	}
	name = name.trim();
	if (name.isEmpty()) {
	    throw new IllegalArgumentException("name is empty");
	}
	this.name = name;
	this.children = new HashSet<Node>();
    }

    public Node getParent() {
	return this.parent;
    }
    
    public void addChild(Node child) {
	if (child == null) {
	    throw new IllegalArgumentException("child is null");
	}
	if (child.equals(this)) {
	    throw new IllegalArgumentException(
		    "can not add its self to children");
	}
	children.add(child);
	if (child.parent != null) {
	    child.parent.removeChild(child);
	}
	child.parent = this;
	child.setFilter(filter);
    }

    public void removeChild(Node child) {
	children.remove(child);
	child.parent = null;
	child.setFilter(null);
    }

    public String getPath() {
	StringBuilder path = new StringBuilder(name);
	Node current = this.parent;
	
	while (current != null) {
	    if(!current.name.equals("/")){
		path.insert(0, '/');
	    }
	    path.insert(0, current.name);
	    current = current.parent;
	}
	return path.toString();
    }

    public void setFilter(TreeFilter filter) {
	this.filter = filter;
	for (Node child : this.children) {
	    child.setFilter(filter);
	}
    }

    public Set<Node> getChildren() {
	Set<Node> result = new HashSet<Node>();
	if (filter == null) {
	    for (Node node : this.children) {
		result.add(node);
	    }
	} else {
	    for (Node node : this.children) {
		if (filter.allow(node)) {
		    result.add(node);
		}
	    }
	}
	return result;
    }

    public Node getChild(String name) {
	if (name == null) {
	    throw new IllegalArgumentException("name is null");
	}
	if (filter == null) {
	    for (Node child : this.children) {
		if (child.name.equals(name)) {
		    return child;
		}
	    }
	} else {
	    for (Node child : this.children) {
		if (child.name.equals(name) && filter.allow(child)) {
		    return child;
		}
	    }
	}
	return null;
    }

    public static boolean containsPath(Node node, String path) {
	if (path == null) {
	    throw new IllegalArgumentException("path is null");
	}
	if (node == null) {
	    throw new IllegalArgumentException("node is null");
	}

	return containsPath(path.split("/"), 0, node);
    }

    private static boolean containsPath(String[] segments, int current,
	    Node node) {
	if (current == segments.length - 1) {
	    return node.getChild(segments[current]) != null;
	}

	Node child = null;
	for (Node c : node.getChildren()) {
	    if (c.name.equals(segments[current])) {
		child = c;
		break;
	    }
	}
	if (child != null) {
	    return containsPath(segments, ++current, child);
	}

	return false;
    }

    public static void getPaths(Node node, Set<String> paths) {
	Set<Node> children = node.getChildren();
	if (children.isEmpty()) {
	    paths.add(node.getPath());
	} else {
	    for (Node child : children) {
		getPaths(child, paths);
	    }
	}
    }

    public static Node getNode(Node root, String path) {
	if (root == null) {
	    throw new IllegalArgumentException("node is null");
	}
	if (path == null) {
	    throw new IllegalArgumentException("path is null");
	}
	return getNode(root, path.split("/"), 0);
    }

    public static Node getNode(Node root, String[] segments, int current) {
	if(root == null || current >= segments.length) {
	    return null;
	}
	
	if(current == segments.length - 1) {
	    return root.getChild(segments[current]);
	}
	return  getNode(root.getChild(segments[current]), segments, ++current);
    }
    
    public Node getRoot() {
	// find the root node
	Node root = this;
	while (root.parent != null) {
	    root = root.parent;
	}
	return root;
    }

    public void addPath(String path) {
	if(path == null) {
	    throw new IllegalArgumentException("path is null");
	}
	path = path.trim();
	if(path.isEmpty()) {
	    throw new IllegalArgumentException("path is empty");
	}
	
	String[] segments = path.split("/");
	int i = 0;
	Node current = this; 
	while(i < segments.length) {
	    Node child = null;
	    for(Node c : current.children) {
		if(c.name.equals(segments[i])) {
		    child = c;
		    break;
		}
	    }
	    if(child == null) {
		child = new Node(segments[i]);
		current.addChild(child);
	    }
	    i++;
	    current = child;
	}
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) {
	
	Node tree = new Node("/");
	tree.addPath("a/b/c");
	tree.addPath("c/d/e");
	tree.addPath("a/c");
	tree.addPath("a/b");
	Set<String> paths = new HashSet<String>();
	Node.getPaths(tree, paths);
	System.out.println(paths);
	System.out.println(Node.getNode(tree, "c/d").getPath());
	System.out.println(tree.containsPath(tree, "cx"));
	System.out.println(tree.getPath());
    }

    public static void setPrefixes(Node tree, String... prefixes) {
    
    }
    
    public static class PrefixFilter implements TreeFilter {

	
	@Override
	public boolean allow(Node node) {
	    // TODO Auto-generated method stub
	    return false;
	}
	
    }
}
