package DecisionTree;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.experiment.DatabaseUtils;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.experiment.InstanceQuery;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NumericToNominal;



/**
 * Takes the training data-sets
 * 
 * @author Lakshmi Ravi
 * 
 */
public class CollegeFinder {
	private static Instances trainingData = null;
	private static J48 decisionTree = new J48();
	
	private static String DATA_SCHEMA="@relation studentInfo\n\n@attribute GPA numeric \n@attribute SAT_VERBAL numeric\n@attribute SAT_MATH numeric\n@attribute SAT_WRITING numeric \n@attribute ETHINICITY {Pacific_Islander, White} \n@attribute category {1,2,3,4,5}\n";
	
	
	
	private static Instance getTestInstance(String values){
		// Create the instance
		String testData = DATA_SCHEMA +"@data \n"+ values+",1\n";
		System.out.println(testData);
		InputStream is = new ByteArrayInputStream(testData.getBytes());
		BufferedReader buffer = new BufferedReader(new InputStreamReader(is));
		Instances testInstance = null;
		try {
			testInstance = new Instances(buffer);
		} catch (IOException e) {
			e.printStackTrace();
		}
		testInstance.setClassIndex(testInstance.numAttributes() - 1);
		return testInstance.firstInstance();	
	}
	
	/**
	 * Find the results of a given category
	 */
	public static String getCollegeNames(int category){
		Instances collegeName=null;
		String query = "select collegename from collegedata where category="+category;
		InstanceQuery getcollegeName=null;
		try {
			getcollegeName = new InstanceQuery();
			getcollegeName.setQuery(query);
			collegeName = getcollegeName.retrieveInstances();
		} catch (Exception e) {
			e.printStackTrace();
		}
		//Set the output class
		String values = collegeName.toString();
		return values;
	}
	
	/**
	 * Classifies a given test sample to a category
	 * @param testData
	 */
	public static String classifySample(String testData){
		Instance tData = getTestInstance(testData);
		String colleges =null;
		int op = tData.numAttributes()-1;
		try {
			double vale =(decisionTree.classifyInstance(tData));
			System.out.println(vale);
			int cate = (int) vale;
			colleges= getCollegeNames(cate);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return colleges;
	}
	
	/**
	 * Trains the model based on the training data
	 * Training data available fromSQL- database
	 */
	public static void trainModel() {
		System.out.println("Fetching data from Data-base");
		getData();
		decisionTree.setUnpruned(false);
		System.out.println("Building classifier");
		try {
			decisionTree.buildClassifier(trainingData);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println(decisionTree);
	}

	/**
	 * Load data from the database
	 */
	public static void getData() {
		String query = "select * from StudentCategory";
		try {
			//Queries the training data from the mentioned view
			InstanceQuery getTrainData = new InstanceQuery();
			getTrainData.setQuery(query);
			trainingData = getTrainData.retrieveInstances();
			System.out.println("Training data fetched");

			//Set the output class
			int opClass = trainingData.numAttributes();
			trainingData = filterData(trainingData,opClass);
			trainingData.setClassIndex(opClass-1);
		} catch (Exception e) {
			System.out.println("Error with Db!!" + e);
			e.printStackTrace();
		}

	}

	/*
	 * Method to convert numeric class attribute to nominal
	 */
	public static Instances filterData(Instances data, int opClass) {
		Instances filtered;
		NumericToNominal nTn = new NumericToNominal();

		try {
			System.out.println(opClass);
			nTn.setInputFormat(data);
			String opAttribute=""+opClass;
			//nTn.setAttributeIndices("6");
			nTn.setAttributeIndices(opAttribute);
			filtered = Filter.useFilter(data, nTn);

			return filtered;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void main(String[] args) {
		trainModel();
		classifySample("760,785,755,3.93,Pacific_Islander");	}
}