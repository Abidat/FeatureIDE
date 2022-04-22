/* FeatureIDE - A Framework for Feature-Oriented Software Development
 * Copyright (C) 2005-2017  FeatureIDE team, University of Magdeburg, Germany
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

import static java.nio.file.Files.exists;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.beans.Transient;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import de.ovgu.featureide.Commons;
import de.ovgu.featureide.fm.core.analysis.cnf.CNF;
import de.ovgu.featureide.fm.core.analysis.cnf.SolutionList;
import de.ovgu.featureide.fm.core.analysis.cnf.formula.FeatureModelFormula;
import de.ovgu.featureide.fm.core.analysis.cnf.generator.configuration.twise.SampleTester;
import de.ovgu.featureide.fm.core.analysis.cnf.generator.configuration.twise.TWiseCoverageCriterion;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.io.csv.ConfigurationListFormat;
import de.ovgu.featureide.fm.core.io.manager.FeatureModelManager;
import de.ovgu.featureide.fm.core.io.manager.FileHandler;
import de.ovgu.featureide.fm.core.cli.CLISlicer;

/**
 * Tests sampling algorithms.
 *
 * @author Sebastian Krieter
 */
public class CLISlicerTest {

	private final static Path modelDirectory = Commons.getRemoteOrLocalFolder(Commons.TEST_FEATURE_MODEL_PATH).toPath();

	private final List<String> modelNames = Arrays.asList( //
			"basic", //
			"simple", //
			"car", //
			"gpl_medium_model");

	@Test
	public void TestFeatureSplit() {
		// Command line argument parser tests
		assertEquals(CLISlicer.splitFeatureCLIArguments("\"one\", \"two\"").size(), 2);
		assertEquals(CLISlicer.splitFeatureCLIArguments("\"one\", \"two\"").iterator().next(), "one");
	}


	@Test
	public void AllCoverage() {
		testSize("basic", "\"all\"");
		testSize("simple", "\"all\",\"none\"");
		testSize("car", "\"all\"");
		testSize("gpl_medium_model", "\"all\", \"medium\"");
	}


	private static void testSize(String modelName, String features) {
		final Path modelFile = modelDirectory.resolve(modelName + ".xml");
		final SampleTester tester = sample(modelFile, features);
//		assertFalse("Invalid solutions for " + modelName, tester.hasInvalidSolutions());
//		assertEquals("Wrong number of configurations for " + modelName, tester.getSize());
	}

	private static SampleTester sample(final Path modelFile, String features) {
		try {
			final Path inFile = Files.createTempFile("input", ".xml");
			Files.write(inFile, Files.readAllBytes(modelFile));

			final Path outFile = Files.createTempFile("output", "");

			final ArrayList<String> args = new ArrayList<>();
			args.add("-feat");
			args.add(features);
			args.add("-o");
			args.add(outFile.toString());
			args.add("-fm");
			args.add(inFile.toString());

			new CLISlicer().run(args);

			assertTrue("", exists(outFile, LinkOption.NOFOLLOW_LINKS));

			//final SolutionList sample = new SolutionList();
			//FileHandler.load(outFile, sample, new ConfigurationListFormat());
			final FileHandler<IFeatureModel> fileHandler = FeatureModelManager.getFileHandler(modelFile);
			if (fileHandler.getLastProblems().containsError()) {
				fail(fileHandler.getLastProblems().getErrors().get(0).error.getMessage());
			}
//			final CNF cnf = new FeatureModelFormula(fileHandler.getObject()).getCNF();

//			final SampleTester tester = new SampleTester(cnf);
			//tester.setSample(sample.getSolutions());
			return null;
		} catch (final IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
			return null;
		}
	}
}
