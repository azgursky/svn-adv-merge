package com.headstrong.advmerge;

public class AddData {
	String copyFrom;
	boolean copyFromLocal;
	String mergeFrom;
	long startRev;
	long endRev;
	
	public AddData(String copyFrom) {
		this.copyFrom = copyFrom;
		this.copyFromLocal = false;
	}
	
	public AddData(String copyFrom, long startRev, long endRev, String mergeFrom) {
		this.copyFrom = copyFrom;
		this.startRev = startRev;
		this.endRev = endRev;
		this.mergeFrom = mergeFrom;
		this.copyFromLocal = true;
	}

	public String getCopyFrom() {
		return copyFrom;
	}

	public boolean isCopyFromLocal() {
		return copyFromLocal;
	}

	public long getStartRev() {
		return startRev;
	}

	public long getEndRev() {
		return endRev;
	}

	public String getMergeFrom() {
		return mergeFrom;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((copyFrom == null) ? 0 : copyFrom.hashCode());
		result = prime * result + (copyFromLocal ? 1231 : 1237);
		result = prime * result + (int) (endRev ^ (endRev >>> 32));
		result = prime * result
				+ ((mergeFrom == null) ? 0 : mergeFrom.hashCode());
		result = prime * result + (int) (startRev ^ (startRev >>> 32));
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
		AddData other = (AddData) obj;
		if (copyFrom == null) {
			if (other.copyFrom != null)
				return false;
		} else if (!copyFrom.equals(other.copyFrom))
			return false;
		if (copyFromLocal != other.copyFromLocal)
			return false;
		if (endRev != other.endRev)
			return false;
		if (mergeFrom == null) {
			if (other.mergeFrom != null)
				return false;
		} else if (!mergeFrom.equals(other.mergeFrom))
			return false;
		if (startRev != other.startRev)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AddData [copyFrom=" + copyFrom + ", copyFromLocal="
				+ copyFromLocal + ", mergeFrom=" + mergeFrom + ", startRev="
				+ startRev + ", endRev=" + endRev + "]";
	}

	
}
