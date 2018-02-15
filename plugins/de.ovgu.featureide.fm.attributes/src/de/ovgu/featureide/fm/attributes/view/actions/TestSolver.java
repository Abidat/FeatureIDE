package de.ovgu.featureide.fm.attributes.view.actions;

import java.util.Arrays;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.prop4j.Literal;
import org.prop4j.Node;
import org.prop4j.Or;
import org.prop4j.solver.ISatProblem;
import org.prop4j.solver.impl.SatProblem;
import org.prop4j.solver.impl.sat4j.Sat4jSatSolver;
import org.prop4j.solvers.impl.javasmt.sat.JavaSmtSatSolver;
import org.sosy_lab.java_smt.SolverContextFactory.Solvers;

import de.ovgu.featureide.fm.attributes.FMAttributesPlugin;
import de.ovgu.featureide.fm.attributes.view.FeatureAttributeView;
import de.ovgu.featureide.fm.core.base.FeatureUtils;

public class TestSolver extends Action {

	private FeatureAttributeView view;

	public TestSolver(FeatureAttributeView view, ImageDescriptor icon) {
		super("", icon);
		this.view = view;
	}

	@Override
	public void run() {
		if (view.getFeatureModel() != null) {
			// FMAttributesPlugin.getDefault().logInfo("" + view.getFeatureModel().getAnalyser().getCnf());
			Node cnf = view.getFeatureModel().getAnalyser().getCnf();
			ISatProblem problem = new SatProblem(cnf, FeatureUtils.getFeatureNamesPreorder(view.getFeatureModel()));
			JavaSmtSatSolver solver = new JavaSmtSatSolver(problem, Solvers.Z3, null);
			Sat4jSatSolver solver2 = new Sat4jSatSolver(problem, null);

			Literal root = new Literal(view.getFeatureModel().getStructure().getRoot().getFeature().getName());
			Literal newFeature1 = new Literal("NewFeature1");
			newFeature1.positive = false;
			root.positive = false;
			Node nodeClause = new Or(newFeature1, root);

			solver.push(nodeClause);
			solver2.push(nodeClause);

			Object[] solution = solver.findSolution();

			if (solution != null) {
				Arrays.sort(solution);
			} else {

				FMAttributesPlugin.getDefault().logInfo("Explanation:" + solver.getAllMinimalUnsatisfiableSubsets());
			}
			Object[] solution2 = solver2.findSolution();

			if (solution2 != null) {
				Arrays.sort(solution2);
			} else {
//				solver2.getMinimalUnsatisfiableSubsetIndexes();
			}

			FMAttributesPlugin.getDefault().logInfo("O: " + Arrays.toString(solution2) + "\nN:" + Arrays.toString(solution));
		}
	}

}