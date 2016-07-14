package de.ovgu.featureide.visualisation;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import de.ovgu.featureide.core.IFeatureProject;
import de.ovgu.featureide.fm.core.base.IFeature;
import de.ovgu.featureide.fm.core.configuration.Configuration;
import de.ovgu.featureide.fm.core.io.manager.ConfigurationManager;
import de.ovgu.featureide.fm.core.io.manager.FileHandler;

public class ConfigAnalysisUtils {

	public static boolean[][] getConfigsMatrix(IFeatureProject featureProject, boolean ignoreCoreFeatures) throws CoreException {

		Collection<String> featureList = featureProject.getFeatureModel().getFeatureOrderList();
		if(ignoreCoreFeatures){
			List<IFeature> coreFeatures = featureProject.getFeatureModel().getAnalyser().getCoreFeatures();
			for(IFeature coref : coreFeatures){
				featureList.remove(coref.getName());
			}
		}
		Collection<IFile> configs = new ArrayList<IFile>();

		IFolder configsFolder = featureProject.getConfigFolder();
		for (IResource res : configsFolder.members()) {
			if (res instanceof IFile) {
				if (((IFile) res).getName().endsWith(".config")) {
					configs.add((IFile) res);
				}
			}
		}

		boolean[][] matrix = new boolean[configs.size()][featureList.size()];

		int iconf = 0;
		for (IFile config : configs) {
			final Configuration configuration = new Configuration(featureProject.getFeatureModel());
			FileHandler.load(Paths.get(config.getLocationURI()), configuration, ConfigurationManager.getFormat(config.getName()));
			Set<String> configFeatures = configuration.getSelectedFeatureNames();
			int ifeat = 0;
			for (String f : featureList) {
				matrix[iconf][ifeat] = configFeatures.contains(f);
				ifeat++;
			}
			iconf++;
		}

		return matrix;
	}

	
}
