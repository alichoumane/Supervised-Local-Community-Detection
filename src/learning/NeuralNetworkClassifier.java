package learning;

import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ConverterUtils.DataSource;

public class NeuralNetworkClassifier {

	private MultilayerPerceptron mlp;
	
	private Instances data;
	
	public MultilayerPerceptron getMultilayerPerceptron() {
		return mlp;
	}
	
	public NeuralNetworkClassifier() {
	}
	
	public void trainModel(String dataset) throws Exception {
		DataSource source = new DataSource(dataset);
		data = source.getDataSet();
		
		// setting class attribute if the data format does not provide this information
		// For example, the XRFF format saves the class attribute information as well
		if (data.classIndex() == -1)
			  data.setClassIndex(data.numAttributes() - 1);
		
		//training the classifier
		mlp = new MultilayerPerceptron();
		mlp.setLearningRate(0.1);
		mlp.setMomentum(0.2);
		mlp.setTrainingTime(2000);
		mlp.setHiddenLayers("3");
		mlp.buildClassifier(data);
		System.out.println("multilayer perceptron finished training in "+mlp.getTrainingTime()+"ms");
	}
	
	public void loadModel(String path, boolean binary) throws Exception {
		mlp = (MultilayerPerceptron) SerializationHelper.read(path);
		FastVector f = new FastVector();
		f.addElement(new Attribute("F1"));
		f.addElement(new Attribute("F2"));
		f.addElement(new Attribute("F3"));
		/*f.addElement(new Attribute("F4"));
		f.addElement(new Attribute("F5"));
		f.addElement(new Attribute("F6"));
		f.addElement(new Attribute("F7"));
		f.addElement(new Attribute("F8"));*/
		if(binary) {
			FastVector nominals = new FastVector();
			nominals.addElement("-1.0");nominals.addElement("1.0");
			f.addElement(new Attribute("result", nominals));
		}else {
			f.addElement(new Attribute("result"));
		}
		data = new Instances("empty set",f,0);
		if (data.classIndex() == -1)
			  data.setClassIndex(data.numAttributes() - 1);
	}
	
	public void saveModel(String path) throws Exception {
		SerializationHelper.write(path, mlp);
	}
	
	public double predict(double[] features) {
		Instance i = new Instance(1,features);
		i.setDataset(data);
		try {
			double clsLabel = mlp.classifyInstance(i);
			return clsLabel;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}
}
