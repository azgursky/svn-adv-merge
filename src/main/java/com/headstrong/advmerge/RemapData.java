package com.headstrong.advmerge;

import java.util.LinkedList;
import java.util.List;

public class RemapData {
	private boolean deleted;
	private List<PathTreeNode<RemapData>> copies = new LinkedList<PathTreeNode<RemapData>>();
	
	public boolean isDeleted() {
		return deleted;
	}
	
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
	
	public List<PathTreeNode<RemapData>> getCopies() {
		return copies;
	}
	
	public void setCopies(List<PathTreeNode<RemapData>> copies) {
		this.copies = copies;
	}

	@Override
	public String toString() {
		StringBuilder sb = null;
		for (PathTreeNode<RemapData> pathTree : copies) {
			if (sb == null) {
				sb = new StringBuilder();
			} else {
				sb.append(", ");
			}
			sb.append(pathTree.getRelativePath(null));
		}
		return "[deleted=" + deleted + ", copies=" + (sb == null ? "None" : sb.toString()) + "]";
	}
	
	
}