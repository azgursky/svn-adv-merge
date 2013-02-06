package com.headstrong.advmerge;

import java.io.File;
import java.io.IOException;

public class AddScriptVisitor implements PathTreeVisitor<AddData> {
	SvnScriptWriter scriptWriter;
	PathTreeNode<AddData> treeRoot;
	File target;
	
	StringBuilder indent = new StringBuilder();
	
	AddScriptVisitor(SvnScriptWriter scriptWriter, PathTreeNode<AddData> treeRoot, File target) {
		this.scriptWriter = scriptWriter;
		this.treeRoot = treeRoot;
		this.target = target;
	}

	public void visit(PathTreeNode<AddData> tree) {
		if (tree.getData() != null) {
			try {
				String targetFilePath = new File(target, tree.getRelativePath(treeRoot)).getCanonicalPath();
				if (tree.getData().isCopyFromLocal()) {
					scriptWriter.appendCopy(tree.getData().getCopyFrom(), targetFilePath);
					scriptWriter.appendMerge(tree.getData().getStartRev(), tree.getData().getEndRev(), tree.getData().getMergeFrom(), targetFilePath);
				} else {
					if (!tree.hasChildren()) {
						scriptWriter.appendCopy(tree.getData().getCopyFrom(), targetFilePath);
						scriptWriter.appendPropDel(targetFilePath);
						tree.acceptChildren(this);
					}
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
