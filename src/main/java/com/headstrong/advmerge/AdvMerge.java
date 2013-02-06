package com.headstrong.advmerge;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNMergeRange;
import org.tmatesoft.svn.core.SVNMergeRangeList;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.util.SVNPathUtil;
import org.tmatesoft.svn.core.internal.util.SVNURLUtil;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc2.SvnDiffStatus;
import org.tmatesoft.svn.core.wc2.SvnDiffSummarize;
import org.tmatesoft.svn.core.wc2.SvnGetInfo;
import org.tmatesoft.svn.core.wc2.SvnGetStatus;
import org.tmatesoft.svn.core.wc2.SvnInfo;
import org.tmatesoft.svn.core.wc2.SvnLog;
import org.tmatesoft.svn.core.wc2.SvnLogMergeInfo;
import org.tmatesoft.svn.core.wc2.SvnOperationFactory;
import org.tmatesoft.svn.core.wc2.SvnRevisionRange;
import org.tmatesoft.svn.core.wc2.SvnStatus;
import org.tmatesoft.svn.core.wc2.SvnTarget;

public class AdvMerge {
	SvnOperationFactory sof;
	
	private SvnTarget source;
	private SvnTarget target;
	
	VersionedPath sourceVersionedPath;
	VersionedPath targetVersionedPath;
	
	List<VersionedPath> sourceRootPaths = new LinkedList<VersionedPath>();
	
	private VersionedPath commonAncestor;
	private SVNMergeRangeList mergeRanges;
	
	private PathRemap targetPathRemap;
	
	@SuppressWarnings("deprecation")
	public AdvMerge(String source, String target) throws SVNException {
		this.source = SvnTarget.fromURL(SVNURL.parseURIDecoded(source), SVNRevision.HEAD);
		this.target = SvnTarget.fromFile(new File(target));

		sof = new SvnOperationFactory();
	}
	
	public void generateMergeScripts(File outputFolder, String filePrefix) throws SVNException, IOException {
		initCommonAncestor();
		initMergeRanges();
		initTargetPathRemap();
		
		
		for (SVNMergeRange range : mergeRanges.getRanges()) {
			File file = new File(outputFolder, filePrefix + "r" + range.toString());
			SvnScriptWriter svnScriptWriter = new SvnScriptWriter(file);
			try {
				generateScriptForRange(svnScriptWriter, range);
				System.out.println("Generated script: " + file.getAbsolutePath());
			} finally {
				svnScriptWriter.close();
			}
		}
	}
	
	private void initTargetPathRemap() throws SVNException {
    	SvnLog svnLog = sof.createLog();
    	svnLog.setStopOnCopy(false);
    	svnLog.setDiscoverChangedPaths(true);
    	svnLog.setDepth(SVNDepth.INFINITY);
    	SVNRevision firstRev = SVNRevision.create(commonAncestor.getRevision());
    	SVNRevision headRev = SVNRevision.HEAD;
    	SvnRevisionRange revRange = SvnRevisionRange.create(firstRev, headRev);
    	svnLog.setRevisionRanges(Arrays.asList(new SvnRevisionRange[] {revRange}));
    	svnLog.setSingleTarget(target);
    	
		targetPathRemap = new PathRemap();
		String currentRootPath = commonAncestor.getPath();
    	Collection<SVNLogEntry> logEntries = svnLog.run(new LinkedList<SVNLogEntry>());
    	for (SVNLogEntry logEntry : logEntries) {
    		for (SVNLogEntryPath logEntryPath : logEntry.getChangedPaths().values()) {
    			switch (logEntryPath.getType()) {
				case SVNLogEntryPath.TYPE_ADDED:
					if (logEntryPath.getCopyPath() != null) {
						if (currentRootPath.equals(logEntryPath.getCopyPath())) {
							currentRootPath = logEntryPath.getPath();
							System.out.println("Changed current path root: " + logEntryPath.getCopyPath() + " -> " + logEntryPath.getPath());
						} else {
							if (SVNPathUtil.isAncestor(currentRootPath, logEntryPath.getCopyPath())) {
								targetPathRemap.copyPath(
										SVNPathUtil.getPathAsChild(currentRootPath, logEntryPath.getCopyPath()), logEntryPath.getCopyRevision(),
										SVNPathUtil.getPathAsChild(currentRootPath, logEntryPath.getPath()), logEntry.getRevision());
							}
						}
					}
					break;

				case SVNLogEntryPath.TYPE_DELETED:
					targetPathRemap.deletePath(SVNPathUtil.getPathAsChild(currentRootPath, logEntryPath.getPath()), logEntry.getRevision());
					break;

				case SVNLogEntryPath.TYPE_REPLACED:
					targetPathRemap.deletePath(SVNPathUtil.getPathAsChild(currentRootPath, logEntryPath.getPath()), logEntry.getRevision());
					if (logEntryPath.getCopyPath() != null) {
						if (SVNPathUtil.isAncestor(currentRootPath, logEntryPath.getCopyPath())) {
							targetPathRemap.copyPath(
									SVNPathUtil.getPathAsChild(currentRootPath, logEntryPath.getCopyPath()), logEntryPath.getCopyRevision(),
									SVNPathUtil.getPathAsChild(currentRootPath, logEntryPath.getPath()), logEntry.getRevision());
							}
					}
					break;

				default:
					break;
				}
    		}
    	}
    	System.out.println(targetPathRemap.pathTreeRoot.toString());
	}
	
	private void generateScriptForRange(SvnScriptWriter scriptWriter, SVNMergeRange range) throws IOException, SVNException {
		scriptWriter.getWriter().write("rem Common ancestor is " + commonAncestor + "\n");
		scriptWriter.getWriter().write("rem Revisions to merge: " + mergeRanges + "\n");
		
		
		long startRev = range.getStartRevision();
		long endRev = range.getEndRevision();
		
		VersionedPath srcRootVPath = pathAtRevision(sourceVersionedPath, range.getEndRevision());
		
    	SvnDiffSummarize svnDiffSummarize = sof.createDiffSummarize();
    	svnDiffSummarize.setDepth(SVNDepth.INFINITY);
    	svnDiffSummarize.setIgnoreAncestry(false);
    	svnDiffSummarize.setSource(srcRootVPath.svnTarget(), SVNRevision.create(startRev), SVNRevision.create(endRev));
    	
    	Collection<SvnDiffStatus> diffStatuses = new LinkedList<SvnDiffStatus>();
    	svnDiffSummarize.run(diffStatuses);
    	
    	PathTreeNode<AddData> addTree = new PathTreeNode<AddData>(target.getFile().getCanonicalPath());
    	
    	for (SvnDiffStatus diffStatus : diffStatuses) {
    		if (!Arrays.asList(new Character[] {'A', 'M', 'R', 'D'}).contains(diffStatus.getModificationType().getCode())) {
    			continue;
    		}
    		
    		String changedRelativePath = diffStatus.getPath();
    		VersionedPath changedVPath = new VersionedPath(srcRootVPath.getRoot(), SVNPathUtil.append(srcRootVPath.getPath(), changedRelativePath), endRev);
    		String remapedPath = targetPathRemap.remap(changedRelativePath);
    		if (remapedPath == null) {
    			scriptWriter.getWriter()
    			.append("echo WARNING: change ").append(diffStatus.getModificationType().getCode()).append(" cannot be found in the target tree: ")
    			.append(changedRelativePath)
    			.append('\n');
    			continue;
    		}
    		String fullRemapedPath = new File(target.getFile(), remapedPath).getCanonicalPath();
    		
    		switch (diffStatus.getModificationType().getCode()) {
			case 'M':
				scriptWriter.appendMerge(startRev, endRev, changedVPath.toString(), fullRemapedPath);
				break;
				
			case 'R':
				scriptWriter.appendDelete(fullRemapedPath);
        		//Fall through as 'R' stands for Replace which is 'Delete' plus 'Add'
				//$FALL-THROUGH$
			case 'A':
				VersionedPath baseVPath = pathAtRevision(changedVPath, range.getStartRevision());
				String baseRelativePath = relativeSourcePath(baseVPath);
				String baseRemapedPath = baseRelativePath == null ? null : targetPathRemap.remap(baseRelativePath);
				if (changedRelativePath.equals(baseRelativePath) || baseRemapedPath == null || baseVPath.getRevision() > startRev) {
					AddData addData = new AddData(changedVPath.toString());
					addTree.getOrCreateChild(remapedPath).setData(addData);
					
//					scriptWriter.appendCopy(changedVPath.toString(), fullRemapedPath);
//					scriptWriter.appendPropDel(fullRemapedPath);
				} else {
					AddData addData = new AddData(new File(target.getFile(), baseRemapedPath).getCanonicalPath(), baseVPath.getRevision(), endRev, changedVPath.toString());
					addTree.getOrCreateChild(remapedPath).setData(addData);
					
//					scriptWriter.appendCopy(new File(target.getFile(), baseRemapedPath).getCanonicalPath(), fullRemapedPath);
//					scriptWriter.appendMerge(baseVPath.getRevision(), endRev, changedVPath.toString(), fullRemapedPath);
				}
				break;
			case 'D':
				scriptWriter.appendDelete(fullRemapedPath);
        		break;
			default:
				break;
    		}
    	}
    	System.out.println(addTree.toString());
    	AddScriptVisitor addScriptVisitor = new AddScriptVisitor(scriptWriter, addTree, target.getFile());
    	addTree.accept(addScriptVisitor);
    	
    	scriptWriter.appendRecordMerge(1l, endRev, sourceVersionedPath.toString(), target.getFile().getCanonicalPath());
	}
	
	public VersionedPath pathAtRevision(VersionedPath vPath, long opRev) throws SVNException {
		OriginInfo origin = originInfo(vPath);

		while (origin.getVersionedPath().getRevision() > opRev && origin.getAncestor() != null) {
			vPath = origin.getAncestor();
			origin = originInfo(vPath);
		}
		
		return new VersionedPath(vPath.getRoot(), vPath.getPath(), Math.max(
				origin.getVersionedPath().getRevision(), opRev));
	}
	
	private void initMergeRanges() throws SVNException {
		SvnLogMergeInfo logMergeInfo = sof.createLogMergeInfo();
		logMergeInfo.setDepth(SVNDepth.INFINITY);
		logMergeInfo.setFindMerged(false);
		logMergeInfo.setSingleTarget(target);
		logMergeInfo.setSource(source);
		
		Collection<SVNLogEntry> entriesToMerge = logMergeInfo.run(new LinkedList<SVNLogEntry>());
		SVNMergeRangeList mergeRangeList = new SVNMergeRangeList(new SVNMergeRange[0]);
		for (SVNLogEntry logEntry : entriesToMerge) {
			mergeRangeList.mergeRevision(logEntry.getRevision());
		}
		
		SVNMergeRangeList optimizedRangeList = new SVNMergeRangeList(new SVNMergeRange[0]);
		SVNMergeRange prevRange = null;
		for (SVNMergeRange range : mergeRangeList.getRanges()) {
			if (prevRange != null) {
				long lastChangeRev = lastChangeRev(source, range.getStartRevision());
				if (lastChangeRev == prevRange.getEndRevision()) {
					range = new SVNMergeRange(prevRange.getEndRevision(), range.getEndRevision(), true);
				}
			}
			
			optimizedRangeList = optimizedRangeList.merge(new SVNMergeRangeList(range.getStartRevision(), range.getEndRevision(), true));
			prevRange = range;
		}
		mergeRanges = optimizedRangeList;
	}
	
	private String relativeSourcePath(VersionedPath vPath) {
		for (VersionedPath sourceRootPath : sourceRootPaths) {
			if (vPath.getRevision() >= sourceRootPath.getRevision()) {
				return SVNPathUtil.getPathAsChild(sourceRootPath.getPath(), vPath.getPath());
			}
		}
		return null;
	}
	
	private long lastChangeRev(SvnTarget branch, long rev) throws SVNException {
		SvnLog svnLog = sof.createLog();
    	svnLog.setStopOnCopy(false);
    	svnLog.setLimit(1);
    	svnLog.setDepth(SVNDepth.INFINITY);
    	SvnRevisionRange revRange = SvnRevisionRange.create(SVNRevision.create(rev - 1), SVNRevision.create(1));
    	svnLog.setRevisionRanges(Arrays.asList(new SvnRevisionRange[] {revRange}));
    	svnLog.setSingleTarget(branch);
    	
    	SVNLogEntry logEntry = svnLog.run();
		return logEntry.getRevision();
	}
	
	private void initCommonAncestor() throws SVNException {
		SvnGetInfo svnGetInfo = sof.createGetInfo();
		svnGetInfo.setSingleTarget(source);
		SvnInfo svnInfo = svnGetInfo.run();
		sourceVersionedPath = new VersionedPath(
				svnInfo.getRepositoryRootUrl(),
				SVNURLUtil.getRelativeURL(svnInfo.getRepositoryRootUrl(), source.getURL(), false),
				svnInfo.getRevision());
		
		SvnGetStatus getStatus = sof.createGetStatus();
		getStatus.setSingleTarget(target);
		SvnStatus svnStatus = getStatus.run();
		targetVersionedPath = new VersionedPath(
				svnStatus.getRepositoryRootUrl(),
				svnStatus.getRepositoryRelativePath(),
				svnStatus.getRevision());
		
		if (!sourceVersionedPath.getRoot().equals(targetVersionedPath.getRoot())) {
			throw new RuntimeException("Merging from a different repository is not supported");
		}
		
		commonAncestor = findCommonAncestor(sourceVersionedPath, targetVersionedPath);
		System.out.println("Path1 is " + sourceVersionedPath);
		System.out.println("Path2 is " + targetVersionedPath);
		System.out.println("Common ancestor is " + commonAncestor);
	}
	
	private VersionedPath findCommonAncestor(VersionedPath path1, VersionedPath path2) throws SVNException {
		OriginInfo origin1 = originInfo(path1);
		OriginInfo origin2 = originInfo(path2);

		
		while (!origin1.getVersionedPath().equals(origin2.getVersionedPath())) {
			if (origin1.getVersionedPath().getRevision() > origin2.getVersionedPath().getRevision()) {
				sourceRootPaths.add(origin1.getVersionedPath());
				path1 = origin1.getAncestor();
				origin1 = originInfo(path1);
			} else {
				path2 = origin2.getAncestor();
				origin2 = originInfo(path2);
			}
		}
		
		// origins of the two paths are the same - both paths are on the same branch 
		VersionedPath commonAncestor = new VersionedPath(path1.getRoot(), path1.getPath(), Math.min(
				path1.getRevision(), path2.getRevision()));
		sourceRootPaths.add(commonAncestor);
		
		System.out.println("Source ancestor roots: " + sourceRootPaths);
		return commonAncestor;
	}
	
	private OriginInfo originInfo(VersionedPath versionedPath) throws SVNException {
    	SvnLog svnLog = sof.createLog();
    	svnLog.setStopOnCopy(true);
    	svnLog.setLimit(1);
    	svnLog.setDiscoverChangedPaths(true);
    	svnLog.setDepth(SVNDepth.EXCLUDE);
    	SvnRevisionRange revRange = SvnRevisionRange.create(SVNRevision.create(1), SVNRevision.create(versionedPath.getRevision()));
    	svnLog.setRevisionRanges(Arrays.asList(new SvnRevisionRange[] {revRange}));
    	svnLog.setSingleTarget(versionedPath.svnTarget());
    	
    	SVNLogEntry logEntry = svnLog.run();
    	for (SVNLogEntryPath changedPath : logEntry.getChangedPaths().values()) {
    		if (SVNPathUtil.isAncestor(changedPath.getPath(), versionedPath.getPath())) {
    	    	VersionedPath ancestor = null;
    	    	if (changedPath.getCopyPath() != null) {
        			String relativePath = SVNPathUtil.getPathAsChild(changedPath.getPath(), versionedPath.getPath());
        			ancestor = new VersionedPath(versionedPath.getRoot(), SVNPathUtil.append(changedPath.getCopyPath(), relativePath) , changedPath.getCopyRevision());
    	    	}
    	    	
    	    	return new OriginInfo(new VersionedPath(versionedPath.getRoot(), versionedPath.getPath(), logEntry.getRevision()), ancestor);
    		}
    	}
    	
    	throw new RuntimeException("Couldn't find first revision of the " + versionedPath);
	}
	
    public static void main( String[] args ) throws SVNException, IOException {
    	System.setProperty("svnkit.http.sslProtocols", "SSLv3");
    	
		if (args.length != 3) {
			printUsageAndExit();
		}
		
		String source = args[0];
		String target = args[1];
		String outputDirPath = args[2];
		

		AdvMerge advMerge = new AdvMerge(source, target);
		advMerge.generateMergeScripts(new File(outputDirPath), "merge-");
	}
	
	private static void printUsageAndExit() {
		System.out.println("Usage: java AdvMerge <source URL> <target working DIR> <scripts output DIR>");
		System.exit(1);
	}
}
