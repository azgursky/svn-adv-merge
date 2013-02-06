package com.headstrong.advmerge;

public class OriginInfo {
	private VersionedPath versionedPath;
	private VersionedPath ancestor;
	
	public OriginInfo(VersionedPath versionedPath, VersionedPath ancestor) {
		this.versionedPath = versionedPath;
		this.ancestor = ancestor;
	}
	
	public VersionedPath getVersionedPath() {
		return versionedPath;
	}
	
	public VersionedPath getAncestor() {
		return ancestor;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((ancestor == null) ? 0 : ancestor.hashCode());
		result = prime * result
				+ ((versionedPath == null) ? 0 : versionedPath.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OriginInfo other = (OriginInfo) obj;
		if (ancestor == null) {
			if (other.ancestor != null)
				return false;
		} else if (!ancestor.equals(other.ancestor))
			return false;
		if (versionedPath == null) {
			if (other.versionedPath != null)
				return false;
		} else if (!versionedPath.equals(other.versionedPath))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "OriginInfo [versionedPath=" + versionedPath + ", ancestor="
				+ ancestor + "]";
	}
}