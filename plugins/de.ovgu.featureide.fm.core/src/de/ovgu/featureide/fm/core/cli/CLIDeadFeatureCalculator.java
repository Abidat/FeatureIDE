package de.ovgu.featureide.fm.core.cli;

import java.util.List;

public class CLIDeadFeatureCalculator extends ACLIFunction {

    @Override
    public void run(List<String> args) {

        // luckily there is a test scenario for the dead feature calculation:

        // DeadFeatureExplanationCreatorTests
        // FeatureModelExplanationCreatorFactory.getDefault().getDeadFeatureExplanationCreator();
        //
        //final DeadFeatureExplanationCreator c = getInstance();
		//final IFeatureModel fm = Commons.loadTestFeatureModelFromFile("car.xml");
		//c.setFeatureModel(fm);
		//c.setSubject(fm.getFeature("Bluetooth"));
		//assertTrue(isValid(c.getExplanation()));
		//c.setSubject(fm.getFeature("Manual"));
		//assertTrue(isValid(c.getExplanation()));
	}

    @Override
    public String getId() {
        return "dead_feature_detect";
    }
    
}
