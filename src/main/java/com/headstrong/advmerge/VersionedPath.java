package com.headstrong.advmerge;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.util.SVNPathUtil;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc2.SvnTarget;

public class VersionedPath {
	private SVNURL root;
	private String path;
	private long revision;
	
	public VersionedPath(SVNURL root, String path, long revision) {
		if (path != null && !path.startsWith("/")) {
			path = "/" + path;
		}
		
		this.root = root;
		this.path = path;
		this.revision = revision;
	}

	public SVNURL getRoot() {
		return root;
	}

	public String getPath() {
		return path;
	}

	public long getRevision() {
		return revision;
	}
	
	public SvnTarget svnTarget() throws SVNException {
		return SvnTarget.fromURL(
    			SVNURL.create(root.getProtocol(), root.getUserInfo(), root.getHost(), root.getPort(), SVNPathUtil.append(root.getPath(), path), false), 
    			SVNRevision.create(revision));
		}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		result = prime * result + (int) (revision ^ (revision >>> 32));
		result = prime * result + ((root == null) ? 0 : root.hashCode());
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
		VersionedPath other = (VersionedPath) obj;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		if (revision != other.revision)
			return false;
		if (root == null) {
			if (other.root != null)
				return false;
		} else if (!root.equals(other.root))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return root + path + "@" + revision;
	}
	
	
}