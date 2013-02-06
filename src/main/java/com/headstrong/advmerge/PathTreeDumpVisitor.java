package com.headstrong.advmerge;

import java.io.IOException;
import java.io.Writer;

public class PathTreeDumpVisitor<T> implements PathTreeVisitor<T> {
	Writer writer;
	StringBuilder indent = new StringBuilder();
	
	PathTreeDumpVisitor(Writer writer) {
		this.writer = writer;
	}

	public void visit(PathTreeNode<T> tree) {
		try {
			writer.append(indent).append(tree.getName()).append(" ").append(tree.getData() == null ? "" : tree.getData().toString()).append('\n');
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		indent.append("  ");
		tree.acceptChildren(this);
		indent.delete(indent.length() - 2, indent.length());
	}
}
