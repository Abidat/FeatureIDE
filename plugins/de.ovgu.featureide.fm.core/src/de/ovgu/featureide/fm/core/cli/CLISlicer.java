package de.ovgu.featureide.fm.core.cli;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.io.manager.FeatureModelManager;
import de.ovgu.featureide.fm.core.io.manager.FileHandler;
import de.ovgu.featureide.fm.core.job.SliceFeatureModel;
import de.ovgu.featureide.fm.core.job.LongRunningMethod;
import de.ovgu.featureide.fm.core.job.LongRunningWrapper;
import de.ovgu.featureide.fm.core.job.monitor.ConsoleMonitor;
import de.ovgu.featureide.fm.core.io.IPersistentFormat;

/**
 * Slice a feature model according to a feature and thus create a sub-view of the entire model.
 * 
 */
public class CLISlicer extends ACLIFunction {
    
    private String selectedFeatures;
	private Path outputFile;
	private Path fmFile;

	@Override
	public String getId() {
		return "slicer";
	}

	@Override
	public void run(List<String> args) {
		parseArguments(args);
        
        
		if (fmFile == null) {
			throw new IllegalArgumentException("No feature model specified!");
		}
		if (outputFile == null) {
			throw new IllegalArgumentException("No output file specified!");
		}
		if (selectedFeatures == null || !selectedFeatures.contains("\"")) {
			throw new IllegalArgumentException("No features selected, use: \"f1\",\"f3\" including the hyphens.");
		}

		final Collection<String> selectedFeatureList = splitFeatureCLIArguments(selectedFeatures);


		final FileHandler<IFeatureModel> fileHandler = FeatureModelManager.getFileHandler(fmFile);
		if (fileHandler.getLastProblems().containsError()) {
			throw new IllegalArgumentException(fileHandler.getLastProblems().getErrors().get(0).error);
		}

        final LongRunningMethod<IFeatureModel> method = new SliceFeatureModel(fileHandler.getObject(), selectedFeatureList, true);
    
        //final IRunner<IFeatureModel> runner = LongRunningWrapper.getRunner(method, "Slicing Feature Model");
        final IPersistentFormat<IFeatureModel> format = fileHandler.getFormat();
        //final IPersistentFormat<IFeatureModel> format = FMFormatManager.getInstance().getFormatByContent(fmFile);
		final IFeatureModel result = LongRunningWrapper.runMethod(method, new ConsoleMonitor<>());
		FeatureModelManager.save(result, outputFile,  format);
	}

	private void resetArguments() {
		selectedFeatures = null;
		outputFile = null;
		fmFile = null;
	}

	public static Collection<String> splitFeatureCLIArguments(String features){
		String[] splitFeatures = features.split("\"");
		return Arrays.stream(splitFeatures).filter(item -> item.trim().length() > 1 && !item.trim().equals(",")).collect(Collectors.toList());
	}

	private void parseArguments(List<String> args) {
		resetArguments();
		for (final Iterator<String> iterator = args.iterator(); iterator.hasNext();) {
			final String arg = iterator.next();
			if (arg.startsWith("-")) {
				switch (arg.substring(1)) {
				case "feat": {
					selectedFeatures = getArgValue(iterator, arg);
					break;
				}
				case "o": {
					outputFile = Paths.get(getArgValue(iterator, arg));
					break;
				}
				case "fm": {
					fmFile = Paths.get(getArgValue(iterator, arg));
					break;
				}
				default: {
					throw new IllegalArgumentException(arg);
				}
				}
			} else {
				throw new IllegalArgumentException(arg);
			}
		}
	}

	private String getArgValue(final Iterator<String> iterator, final String arg) {
		if (iterator.hasNext()) {
			return iterator.next();
		} else {
			throw new IllegalArgumentException("No value specified for " + arg);
		}
	}
}
