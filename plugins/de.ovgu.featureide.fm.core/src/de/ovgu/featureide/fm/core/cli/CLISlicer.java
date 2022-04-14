package de.ovgu.featureide.fm.core.cli;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import de.ovgu.featureide.fm.core.analysis.cnf.CNF;
import de.ovgu.featureide.fm.core.analysis.cnf.ClauseList;
import de.ovgu.featureide.fm.core.analysis.cnf.formula.FeatureModelFormula;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.io.ProblemList;
import de.ovgu.featureide.fm.core.io.expression.ExpressionGroupFormat;
import de.ovgu.featureide.fm.core.io.manager.FeatureModelManager;
import de.ovgu.featureide.fm.core.io.manager.FileHandler;
import de.ovgu.featureide.fm.core.job.SliceFeatureModel;
import de.ovgu.featureide.fm.core.job.LongRunningMethod;
import de.ovgu.featureide.fm.core.job.LongRunningWrapper;
import de.ovgu.featureide.fm.core.job.monitor.ConsoleMonitor;
import de.ovgu.featureide.fm.core.io.IFeatureModelFormat;
import de.ovgu.featureide.fm.core.io.IPersistentFormat;
import de.ovgu.featureide.fm.core.base.impl.FMFormatManager;

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
		if (selectedFeatures == null) {
			throw new IllegalArgumentException("No features selected, use: f1,f3");
		}

		final FileHandler<IFeatureModel> fileHandler = FeatureModelManager.getFileHandler(fmFile);
		if (fileHandler.getLastProblems().containsError()) {
			throw new IllegalArgumentException(fileHandler.getLastProblems().getErrors().get(0).error);
		}

        final Collection<String> selectedFeatures = new ArrayList<String>();
        final LongRunningMethod<IFeatureModel> method = new SliceFeatureModel(fileHandler.getObject(), selectedFeatures, true);
    
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
