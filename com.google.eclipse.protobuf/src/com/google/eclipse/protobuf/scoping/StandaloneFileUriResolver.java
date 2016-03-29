package com.google.eclipse.protobuf.scoping;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.google.eclipse.protobuf.protobuf.Import;

/**
 * Resolve import-statements of protobuf in a standalone environment.<p/>
 * 
 * When using protobuf-dt in a standalone environment, the Eclipse-workbench-related
 * logic of FileUriResolver cannot be used (actually, there is no Guice binding when
 * running without the protobuf-dt UI plug-in). Thus, we try to mimic the semantics
 * of protobuf import-statements here.<p/>
 * 
 * The --proto_path option of protoc can be simulated by using the static method
 * addSearchPath. This resolver will search along all entries of the search path until
 * the imported file can be found. Before checking the search path, this resolver will
 * try to match the relative path starting from the directory where the importing file 
 * is located.<p/> 
 * 
 * @author Klaus Birken (itemis AG)
 *
 */
public class StandaloneFileUriResolver implements IFileUriResolver {

	private static List<String> searchPaths = new ArrayList<String>();
	
	public static void clearSearchPaths() {
		searchPaths.clear();
	}
	
	public static void addSearchPath(String path) {
		searchPaths.add(path);
	}
	
	@Override
	public void resolveAndUpdateUri(Import anImport) {
		// check if imported file is an absolute path
		String orig = anImport.getImportURI();
		if (new File(orig).isAbsolute()) {
			// it is absolute, do not resolve the import at all
			return;
		}
		
		// try resolving against local path first
		String local = anImport.eResource().getURI().trimSegments(1).toFileString();
		if (checkAndSet(anImport, local, local))
			return;
		
		// try all search path entries until we find a match
		for(String entry : searchPaths) {
			if (checkAndSet(anImport, local, entry))
				return;
		}

		// no match found, give up (i.e., do nothing)
	}

	private boolean checkAndSet(Import anImport, String importingPath, String checkPath) {
		boolean isAbsolute = new File(checkPath).isAbsolute();
		String candidate =
				(isAbsolute ? "" : importingPath + File.separator) +
				checkPath + File.separator + anImport.getImportURI();
		File f = new File(candidate);
		if (f.exists()) {
			// we found a file with this checkPath, update import element
			anImport.setImportURI(candidate);
			return true;
		}
		return false;
	}
}
