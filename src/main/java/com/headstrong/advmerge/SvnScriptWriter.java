package com.headstrong.advmerge;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class SvnScriptWriter implements Closeable {
	
	private Writer writer;
	
	public SvnScriptWriter(File file) throws FileNotFoundException {
		writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(file)));
	}
	
	public Writer getWriter() {
		return writer;
	}
	
	public void appendCopy(String copyFrom, String copyTo) throws IOException {
		writer
		.append("svn copy ")
		.append("--parents ")
		.append(copyFrom).append(' ')
		.append(copyTo).append(' ')
		.append('\n');
	}
	
	public void appendPropDel(String path) throws IOException {
		writer
		.append("svn propdel svn:mergeinfo ")
		.append(path).append(' ')
		.append('\n');
	}
	
	public void appendDelete(String path) throws IOException {
		writer
		.append("svn delete ")
		.append(path).append(' ')
		.append('\n');
	}
	
	public void appendMerge(long startRev, long endRev, String sourcePath, String targetPath) throws IOException {
		writer
		.append("svn merge ")
		.append("-r").append(Long.toString(startRev)).append(':').append(Long.toString(endRev)).append(' ')
		.append("--ignore-ancestry ")
		.append("--accept postpone ")
		.append(sourcePath).append(' ')
		.append(targetPath).append(' ')
		.append('\n');
	}
	
	public void appendRecordMerge(long startRev, long endRev, String sourcePath, String targetPath) throws IOException {
		writer
		.append("svn merge ")
		.append("--record-only ")
		.append("-r").append(Long.toString(startRev)).append(":").append(Long.toString(endRev)).append(' ')
		.append(sourcePath).append(' ')
		.append(targetPath).append(' ')
		.append('\n');
	}
	
	public void close() throws IOException {
		writer.close();
	}

}
