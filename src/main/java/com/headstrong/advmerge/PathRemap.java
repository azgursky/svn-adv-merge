package com.headstrong.advmerge;



public class PathRemap {
	
	PathTreeNode<RemapData> pathTreeRoot = new PathTreeNode<RemapData>("ROOT");
	
	public String remap(String path) {
		PathTreeNode<RemapData> pathTree = pathTreeRoot.getOrCreateChild(path);
		PathTreeNode<RemapData> remaped = remap(pathTree);
		if (remaped != null) {
			return remaped.getRelativePath(pathTreeRoot);
		}
		
		return null;
	}
	
	private PathTreeNode<RemapData> remap(PathTreeNode<RemapData> pathTree) {
		RemapData data = pathTree.getData();
		if (data == null || !data.isDeleted()) {
			if (pathTree.getParent() != null) {
				PathTreeNode<RemapData> remapedParent = remap(pathTree.getParent());
				if (remapedParent != null) {
					return remapedParent.getOrCreateChild(pathTree.getName());
				}
			} else {
				return pathTree;
			}
		}
		
		if (data != null) {
			for (PathTreeNode<RemapData> copy : data.getCopies()) {
				PathTreeNode<RemapData> remapedCopy = remap(copy);
				if (remapedCopy != null) {
					return remapedCopy;
				}
			}
		}
		
		return null;
	}
	
	public void copyPath(String pathFrom, long revisionFrom, String pathTo, long revisionTo) {
		PathTreeNode<RemapData> pathTree = pathTreeRoot.getOrCreateChild(pathTo);
		PathTreeNode<RemapData> pathTreeFrom = pathTreeRoot.getOrCreateChild(pathFrom);
		if (pathTree.equals(pathTreeFrom)) {
			return;
		}
		
		RemapData pathData = pathTreeFrom.getData();
		if (pathData == null) {
			pathData = new RemapData();
			pathTreeFrom.setData(pathData);
		}
		
		pathData.getCopies().add(pathTree);
		
		System.out.println("Path copied: " +  pathTo + "@" + revisionTo + " (from " + pathFrom + "@" + revisionFrom + ")");
	}
	
	public void deletePath(String path, long revision) {
		PathTreeNode<RemapData> pathTree = pathTreeRoot.getOrCreateChild(path);
		
		RemapData pathData = pathTree.getData();
		if (pathData == null) {
			pathData = new RemapData();
			pathTree.setData(pathData);
		}
		pathData.setDeleted(true);
		
		System.out.println("Path deleted: " +  path + "@" + revision);
	}
}
