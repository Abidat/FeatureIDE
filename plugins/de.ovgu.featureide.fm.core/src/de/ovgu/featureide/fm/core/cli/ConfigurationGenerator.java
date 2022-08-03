/* FeatureIDE - A Framework for Feature-Oriented Software Development
 * Copyright (C) 2005-2019  FeatureIDE team, University of Magdeburg, Germany
 *
 * This file is part of FeatureIDE.
 *
 * FeatureIDE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FeatureIDE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FeatureIDE.  If not, see <http://www.gnu.org/licenses/>.
 *
 * See http://featureide.cs.ovgu.de/ for further information.
 */
package de.ovgu.featureide.fm.core.cli;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import de.ovgu.featureide.fm.core.analysis.cnf.CNF;
import de.ovgu.featureide.fm.core.analysis.cnf.ClauseList;
import de.ovgu.featureide.fm.core.analysis.cnf.LiteralSet;
import de.ovgu.featureide.fm.core.analysis.cnf.SolutionList;
import de.ovgu.featureide.fm.core.analysis.cnf.formula.FeatureModelFormula;
import de.ovgu.featureide.fm.core.analysis.cnf.generator.configuration.AllConfigurationGenerator;
import de.ovgu.featureide.fm.core.analysis.cnf.generator.configuration.IConfigurationGenerator;
import de.ovgu.featureide.fm.core.analysis.cnf.generator.configuration.PairWiseConfigurationGenerator;
import de.ovgu.featureide.fm.core.analysis.cnf.generator.configuration.RandomConfigurationGenerator;
import de.ovgu.featureide.fm.core.analysis.cnf.generator.configuration.SPLCAToolConfigurationGenerator;
import de.ovgu.featureide.fm.core.analysis.cnf.generator.configuration.twise.TWiseConfigurationGenerator;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.io.IPersistentFormat;
import de.ovgu.featureide.fm.core.io.ProblemList;
import de.ovgu.featureide.fm.core.io.csv.ConfigurationListFormat;
import de.ovgu.featureide.fm.core.io.expression.ExpressionGroupFormat;
import de.ovgu.featureide.fm.core.io.manager.FeatureModelManager;
import de.ovgu.featureide.fm.core.io.manager.FileHandler;
import de.ovgu.featureide.fm.core.job.LongRunningWrapper;
import de.ovgu.featureide.fm.core.job.SliceFeatureModel;
import de.ovgu.featureide.fm.core.job.monitor.ConsoleMonitor;

/**
 * Command line interface for sampling algorithms.
 *
 * @author Sebastian Krieter
 */
public class ConfigurationGenerator extends ACLIFunction {

	private String algorithm;
	private Path outputFile;
	private Path outputFileMeta;
	private Path fmFile;
	private Path expressionFile;
	private List<String> featurefilterList;
	private int t;
	private int m;
	private int limit;

	@Override
	public String getId() {
		return "genconfig";
	}

	/*
	 * (non-Javadoc)
	 * @see de.ovgu.featureide.fm.core.cli.ICLIFunction#run(java.util.List)
	 */
	@Override
	public void run(List<String> args) {
		parseArguments(args);

		if (fmFile == null) {
			throw new IllegalArgumentException("No feature model specified!");
		}
		if (outputFile == null) {
			throw new IllegalArgumentException("No output file specified!");
		}
		if (algorithm == null) {
			throw new IllegalArgumentException("No algorithm specified!");
		}

		final FileHandler<IFeatureModel> fileHandler = FeatureModelManager.getFileHandler(fmFile);
		if (fileHandler.getLastProblems().containsError()) {
			throw new IllegalArgumentException(fileHandler.getLastProblems().getErrors().get(0).error);
		}
		// final CNF cnf = new FeatureModelFormula(fileHandler.getObject()).getCNF();

//		final List<String> sliceList = new ArrayList<String>();
//		sliceList.add("HCP1_Mid");
//		sliceList.add("HCP1_Mid_2.1");
//		sliceList.add("HCP1_OBD_AppAnOBD01.Mid_2.1: 0x7C9");

		final FeatureModelFormula formula = new FeatureModelFormula(fileHandler.getObject());

		final CNF cnf = sliceOrNot(formula, fileHandler.getFormat());

		// final CNF slicedCNF = LongRunningWrapper.runMethod(new CNFSlicer(cnf, sliceList));

		final ArrayList<List<ClauseList>> expressionGroups;
		if (expressionFile != null) {
			expressionGroups = new ArrayList<>();
			final ProblemList lastProblems = FileHandler.load(expressionFile, expressionGroups, new ExpressionGroupFormat());
			if (lastProblems.containsError()) {
				throw new IllegalArgumentException(lastProblems.getErrors().get(0).error);
			}
		} else {
			expressionGroups = null;
		}

		IConfigurationGenerator generator = null;
		// we want to collect additional metadata to forward metrics for configuration analysis (postprocessing)
		final List<int[]> metaData = new ArrayList<int[]>();
		switch (algorithm.toLowerCase()) {
		case "icpl": {
			generator = new SPLCAToolConfigurationGenerator(cnf, "ICPL", t, limit);
			break;
		}
		case "chvatal": {
			generator = new SPLCAToolConfigurationGenerator(cnf, "Chvatal", t, limit);
			break;
		}
		case "incling": {
			generator = new PairWiseConfigurationGenerator(cnf, limit, metaData);
			break;
		}
		case "yasa": {
			if (expressionGroups == null) {
				generator = new TWiseConfigurationGenerator(cnf, t, limit);
			} else {
				generator = new TWiseConfigurationGenerator(cnf, expressionGroups, t, limit);
			}
			((TWiseConfigurationGenerator) generator).setIterations(m);
			break;
		}
		case "random": {
			generator = new RandomConfigurationGenerator(cnf, limit);
			break;
		}
		case "all": {
			generator = new AllConfigurationGenerator(cnf, limit);
			break;
		}
		default:
			throw new IllegalArgumentException("No algorithm specified!");
		}
		final List<LiteralSet> result = LongRunningWrapper.runMethod(generator, new ConsoleMonitor<>());
		FileHandler.save(outputFile, new SolutionList(cnf.getVariables(), result), new ConfigurationListFormat());
		// FileHandler.save(outputFileMeta, new SolutionList(cnf.getVariables(), metaData), new ConfigurationListFormat());
		try (BufferedWriter writer = Files.newBufferedWriter(outputFileMeta, StandardCharsets.UTF_8)) {
			for (final int[] valueList : metaData) {
				if (valueList.length > 3) {
					double relDelta = (double) (valueList[2]) / valueList[3];
					double relTotal = (double) (valueList[1]) / valueList[3];
					relDelta = Math.floor(relDelta * 100000.0) / 1000.0;
					relTotal = Math.floor(relTotal * 1000.0) / 10.0;
					writer.write(valueList[0] + ";" + relTotal + ";" + relDelta + ";" + valueList[3] + "\n");
				}
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method slices the original CNF if necessary (if slicing parameters are given and set) otherwise returns original CNF.
	 *
	 * @param formula
	 * @return
	 */
	private CNF sliceOrNot(final FeatureModelFormula formula, IPersistentFormat<IFeatureModel> format) {
		System.out.println(formula.getFeatureModel().getFeatureOrderList());

		// test finding parent:
		final var variable = formula.getFeatureModel().getFeature(featurefilterList.get(0));

		System.out.println(variable.getStructure().getParent().getFeature().getName());

		if (featurefilterList.size() > 0) {
			final SliceFeatureModel slicedModel = new SliceFeatureModel(formula.getFeatureModel(), formula.getFeatureModel().getFeatureOrderList(), true);
			final IFeatureModel resultSlice = LongRunningWrapper.runMethod(slicedModel, new ConsoleMonitor<>());
			final CNF cnf = new FeatureModelFormula(resultSlice).getCNF();
			FeatureModelManager.save(resultSlice, Paths.get(outputFileMeta.toString() + "_slice.xml"), format);
			return cnf;
		} else {
			return formula.getCNF();
		}
	}

	private void resetArguments() {
		algorithm = null;
		outputFile = null;
		outputFileMeta = null;
		fmFile = null;
		expressionFile = null;
		featurefilterList = new ArrayList<String>();
		t = 0;
		m = 1;
		limit = Integer.MAX_VALUE;
	}

	private void parseArguments(List<String> args) {
		resetArguments();
		for (final Iterator<String> iterator = args.iterator(); iterator.hasNext();) {
			final String arg = iterator.next();
			if (arg.startsWith("-")) {
				switch (arg.substring(1)) {
				case "a": {
					algorithm = getArgValue(iterator, arg);
					break;
				}
				case "o": {
					final String pathName = getArgValue(iterator, arg);
					outputFile = Paths.get(pathName);
					outputFileMeta = Paths.get(pathName + "_meta");
					break;
				}
				case "fm": {
					fmFile = Paths.get(getArgValue(iterator, arg));
					break;
				}
				case "t": {
					t = Integer.parseInt(getArgValue(iterator, arg));
					break;
				}
				case "m": {
					m = Integer.parseInt(getArgValue(iterator, arg));
					break;
				}
				case "l": {
					limit = Integer.parseInt(getArgValue(iterator, arg));
					break;
				}
				case "e": {
					expressionFile = Paths.get(getArgValue(iterator, arg));
					break;
				}
				case "ff": {
					// ff = feature filer = allow filtering of features from a given tree, must be a comma separated list
					final String featurefilterStr = getArgValue(iterator, arg);
					final StringTokenizer tokenizer = new StringTokenizer(featurefilterStr, ",");

					while (tokenizer.hasMoreTokens()) {
						// remove spaces as users could potentially add spaces before or after commas
						featurefilterList.add(tokenizer.nextToken().trim().replace("\"", ""));
					}

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
