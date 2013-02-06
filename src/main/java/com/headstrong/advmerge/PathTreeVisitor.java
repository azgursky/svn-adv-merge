package com.headstrong.advmerge;

public interface PathTreeVisitor<T> {
	public void visit(PathTreeNode<T> tree);
}
