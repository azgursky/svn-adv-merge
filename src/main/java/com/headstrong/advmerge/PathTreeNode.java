package com.headstrong.advmerge;

import java.io.StringWriter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class PathTreeNode<T> {
	private PathTreeNode<T> parent;
	private String name;
	private Map<String, PathTreeNode<T>> children = new TreeMap<String, PathTreeNode<T>>();
	private T data;
	
	public PathTreeNode(String name) {
		this.name = name;
	}
	
	public void setData(T data) {
		this.data = data;
	}
	
	public T getData() {
		return data;
	}
	
	public String getName() {
		return name;
	}
	
	protected void setParent(PathTreeNode<T> parent) {
		this.parent = parent;
	}
	
	public PathTreeNode<T> getParent() {
		return parent;
	}
	
	protected Map<String, PathTreeNode<T>> getChildren() {
		return children;
	}

	public PathTreeNode<T> putTree(PathTreeNode<T> child) {
		PathTreeNode<T> existingChild = children.get(child.getName());
		if (existingChild != null) {
			for (PathTreeNode<T> childChild : child.getChildren().values()) {
				existingChild.putTree(childChild);
			}
			return existingChild;
		}
		
		children.put(child.getName(), child);
		child.setParent(this);
		return child;
	}
	
	public PathTreeNode<T> getOrCreateChild(String path) {
		return getChild(path, true);
	}
	
	public PathTreeNode<T> getChild(String path, boolean createMissingChildren) {
		String[] pathFragments = path.split("/");
		PathTreeNode<T> currentNode = this;
		for (String pathFragment : pathFragments) {
			PathTreeNode<T> child = currentNode.getChildren().get(pathFragment);
			if (child == null) {
				if (createMissingChildren) {
					child = currentNode.putTree(new PathTreeNode<T>(pathFragment));
				} else {
					return null;
				}
			}
			currentNode = child;
		}
		return currentNode;
	}
	
	public List<PathTreeNode<T>> getParentsBelow(PathTreeNode<T> topParent) {
		List<PathTreeNode<T>> parents = new LinkedList<PathTreeNode<T>>();
		PathTreeNode<T> node = this;
		while (node != null) {
			if (node.equals(topParent)) {
				return parents;
			}
			parents.add(node);
			node = node.getParent();
		}
		
		if (topParent == null) {
			return parents;
		}
		
		throw new RuntimeException(topParent.getRelativePath(null) + "is not in parent hierarchy of " + getRelativePath(null));
	}
	
	public boolean hasChildren() {
		return children.size() > 0;
	}
	
	public String getRelativePath(PathTreeNode<T> parent) {
		List<PathTreeNode<T>> fragments = getParentsBelow(parent);
		Collections.reverse(fragments);
		
		StringBuilder sb = null;
		for (PathTreeNode<T> fragment : fragments) {
			if (sb == null) {
				sb = new StringBuilder(fragment.getName());
			} else {
				sb.append('/').append(fragment.getName());
			}
		}
		
		if (sb != null) {
			return sb.toString();
		}
		
		return "";
	}
	
	public void accept(PathTreeVisitor<T> visitor) {
		visitor.visit(this);
	}
	
	public void acceptChildren(PathTreeVisitor<T> visitor) {
		for (PathTreeNode<T> child : children.values()) {
			child.accept(visitor);
		}
	}
	
	@Override
	public String toString() {
		StringWriter sw = new StringWriter();
		PathTreeDumpVisitor<T> dumpVisitor = new PathTreeDumpVisitor<T>(sw);
		this.accept(dumpVisitor);
		return sw.toString();
	}
	
}
