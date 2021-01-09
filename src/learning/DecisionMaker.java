package learning;

import java.util.ArrayList;

public class DecisionMaker {
	
	private NeuralNetworkClassifier classifier = new NeuralNetworkClassifier();
	
	public double threshold = 0.5;
	
	public DecisionMaker() {
	}
	
	public void initFromModel(String path, boolean binary) {
		try {
			classifier.loadModel(path, binary);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public NeuralNetworkClassifier getClassifier() {
		return this.classifier;
	}
	
	public void initFromLearningDataset(String path) {
		try {
			classifier.trainModel(path);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private ArrayList<Double> decisions = new ArrayList<>();
	public double getAndResetAveCls1(boolean reset) {
		if(decisions.size()==0)return 0;
		double sum = 0;
		for(Double d:decisions)if(d>=threshold)sum+=d;
		double ave = sum/decisions.size();
		if(reset)decisions = new ArrayList<>();
		return ave;
	}
	public double getAndResetAveCls0(boolean reset) {
		if(decisions.size()==0)return -1;
		double sum = 0;
		for(Double d:decisions)if(d<threshold)sum+=d;
		double ave = sum/decisions.size();
		if(reset)decisions = new ArrayList<>();
		return ave;
	}
	public double getAndResetStdDevCls0(boolean reset) {
		if(decisions.size()==0)return -1;
		
		double ave = getAndResetAveCls0(false);
		
		double sumStd = 0;
		for(Double d:decisions)if(d<threshold)sumStd+=Math.abs(d-ave);

		double std = sumStd/decisions.size();
		if(reset)decisions = new ArrayList<>();
		return std;
	}
	public double getAndResetStdDevCls1(boolean reset) {
		if(decisions.size()==0)return 0;
		
		double ave = getAndResetAveCls1(false);
		
		double sumStd = 0;
		for(Double d:decisions)if(d>=threshold)sumStd+=Math.abs(d-ave);

		double std = sumStd/decisions.size();
		if(reset)decisions = new ArrayList<>();
		return std;
	}
	
	/**
	 * decide whether a node should be added to the group or not by calculating its features and using a machine learning model
	 * @param node
	 * @param group
	 * @param graph
	 * @return true if the node should be added
	 */
	public boolean decide(String node, TrueCommunitiesFeatures calc) {

		double[] features = calc.calculateFeaturesForExpandingModel(node);
		
		double result = classifier.predict(features);
		decisions.add(result);
		if(result>=threshold) {
			return true;
		}else {
			return false;
		}
	}
	
	public boolean decide(double rawDecision) {
		decisions.add(rawDecision);
		if(rawDecision>=threshold) {
			return true;
		}else {
			return false;
		}
	}
	
	public double rawDecision(String node, TrueCommunitiesFeatures calc) {
		double[] features = calc.calculateFeaturesForExpandingModel(node);
		double result = classifier.predict(features);
		return result;
	}
}
