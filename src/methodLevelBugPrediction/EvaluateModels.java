package methodLevelBugPrediction;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;

import methodLevelBugPrediction.utilities.FileUtilities;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.GreedyStepwise;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.Logistic;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.functions.SimpleLogistic;
import weka.classifiers.meta.AttributeSelectedClassifier;
import weka.classifiers.rules.DecisionTable;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
//import weka.classifiers.trees.ADTree;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.MultiFilter;
import weka.filters.supervised.instance.ClassBalancer;
import weka.filters.unsupervised.attribute.Remove;

public class EvaluateModels {
	
	private static final String WORKING_PATH_INPUT = "input/";
	private static final String WORKING_PATH_OUTPUT = "output/";

    private static String output = "project-name;classifier;model-name;TP;FP;FN;TN;accuracy;precision;recall;f-measure;auc-roc;mcc\n";
    private static String output2 = "";
    private static String projectName = "";

    public static void main(String[] args) {

        LinkedHashMap<String, Classifier> classifiers = new LinkedHashMap<String, Classifier>();
        //classifiers.put("ZeroR", new weka.classifiers.rules.ZeroR());
        classifiers.put("SMO", new weka.classifiers.functions.SMO());        
        classifiers.put("REPTree", new weka.classifiers.trees.REPTree());
        classifiers.put("RandomForest", new weka.classifiers.trees.RandomForest());
        classifiers.put("NaiveBayes", new weka.classifiers.bayes.NaiveBayes());
        classifiers.put("Logistic", new weka.classifiers.functions.Logistic());
        classifiers.put("IBk", new weka.classifiers.lazy.IBk());

        try {
            Vector<String> projects = readProjects(WORKING_PATH_INPUT + "projects_to_classify.txt");
            for (String project : projects) {
                Vector<String> releases = readReleases(WORKING_PATH_INPUT + project + "_releases.csv");
                // File dir = new File("/Users/luca/TUProjects/SANER/output/");

                for (int i = 0; i < releases.size() - 1; i++) {
                    // if (!project.isHidden()) {
                    String trainingSet = WORKING_PATH_INPUT + project + releases.get(i) + ".csv";
                    String testSet = WORKING_PATH_INPUT + project + releases.get(i + 1) + ".csv";

                    System.out.println("Evaluating " + releases.get(i) + "/" + (releases.size() - 1) + " ===> " + trainingSet);

                    // Load Training and Test set from CSV file
                    Instances originTraining = EvaluateModels.readFile(trainingSet);
                    Instances originTest = EvaluateModels.readFile(testSet);

                    // EvaluateModels.projectName = trainingSet.substring(trainingSet.lastIndexOf("/") + 1, trainingSet.length());
                    EvaluateModels.projectName = project;

                    /*
                    Instances allModelTraining = EvaluateModels.selectAllFeatures(originTraining);
                    Instances allModelTesting = EvaluateModels.selectAllFeatures(originTest);
                    */

                    output2 += String.format("\\textbf{%s} &&&&&& \\\\ %s", EvaluateModels.projectName, System.lineSeparator());

                    for (Entry<String, Classifier> entry : classifiers.entrySet()) {

                    	/*
                        EvaluateModels.evaluateModel(entry.getValue(), allModelTraining, allModelTesting, "allModel", entry.getKey());
                        */

                        EvaluateModels.evaluateModel(entry.getValue(), originTraining, originTest, "default", entry.getKey());
                        EvaluateModels.output += "\n";
                    }
                    // }
                }
            }

            // output = output.replaceAll("\\.", ",");
            System.out.println();
            System.out.println(EvaluateModels.output2);
            
            FileUtilities.writeFile(output, WORKING_PATH_OUTPUT + "outputOK/output.csv");

            System.out.println("\n ******** Ended ******** \n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static private Vector<String> readProjects(String filename) {
        Vector<String> list = new Vector<String>();
        try {
            Scanner s = new Scanner(new File(filename));
            while (s.hasNext()) {
                String project = s.next();
                if (!project.contains("//"))
                    list.add(project);
            }
            s.close();
        } catch (FileNotFoundException e) {
            System.out.println("The file " + filename + " does not exist.");
        }
        return list;
    }

    static private Vector<String> readReleases(String filename) {
        Vector<String> list = new Vector<String>();
        try {
            Scanner s = new Scanner(new File(filename));
            while (s.hasNext()) {
                String row = s.nextLine();
                List<String> elements = Arrays.asList(row.split(","));
                if (elements.size() > 0)
                    if (!elements.get(0).equals("0") && !elements.get(0).equals("ID"))
                        list.add(elements.get(0));
            }
            s.close();
        } catch (FileNotFoundException e) {
            System.out.println("The file " + filename + " does not exist.");
        }
        return list;
    }

    private static void evaluateModel(Classifier pClassifier, Instances pInstancesTraining, Instances pInstanceTesting, String pModelName, String pClassifierName) throws Exception {

        int folds = 10;

        // randomize data
        Random rand = new Random(42);
        Instances randData = new Instances(pInstancesTraining);
        randData.randomize(rand);
        if (randData.classAttribute().isNominal())
        	randData.stratify(folds);

    	// apply filter(s)
        MultiFilter filter = new MultiFilter();
        filter.setInputFormat(randData);
        filter.setFilters(new Filter[]{
        		new weka.filters.unsupervised.attribute.Normalize()
        });
        randData = Filter.useFilter(randData, filter);
        
        // perform cross-validation and add predictions
        // Instances predictedData = null;
        Evaluation eval = new Evaluation(pInstancesTraining);

        int positiveValueIndexOfClassFeature = 1;

        for (int n = 0; n < folds; n++) {
        	        	
        	// Split into training / test.
            Instances train = randData.trainCV(folds, n);
            Instances test = randData.testCV(folds, n);        	

            // Filter(s) on training only (!)
            MultiFilter multiFilter = new MultiFilter();
            multiFilter.setInputFormat(train);
            multiFilter.setFilters(new Filter[]{
            		new weka.filters.supervised.instance.ClassBalancer()
            });            
            train = Filter.useFilter(train, multiFilter);

            // Build and evaluate classifier
            pClassifier.buildClassifier(train);
            eval.evaluateModel(pClassifier, test);
        }
        //System.out.println(eval.toSummaryString("=== " + pClassifierName + ": Summary  ===", false));
        System.out.println(eval.toClassDetailsString("=== " + pClassifierName + ": Detailed Accuracy By Class ==="));


        double mcc = eval.matthewsCorrelationCoefficient(positiveValueIndexOfClassFeature);

        double accuracy =

                (eval.numTruePositives(positiveValueIndexOfClassFeature) + eval.numTrueNegatives(positiveValueIndexOfClassFeature)) /

                        (eval.numTruePositives(positiveValueIndexOfClassFeature) + eval.numFalsePositives(positiveValueIndexOfClassFeature) + eval.numFalseNegatives(positiveValueIndexOfClassFeature)
                                + eval.numTrueNegatives(positiveValueIndexOfClassFeature));

        double fmeasure = 2
                * ((eval.precision(positiveValueIndexOfClassFeature) * eval.recall(positiveValueIndexOfClassFeature)) / (eval.precision(positiveValueIndexOfClassFeature) + eval.recall(positiveValueIndexOfClassFeature)));

        EvaluateModels.output += EvaluateModels.projectName + ";" 
        		+ pClassifierName + ";" 
        		+ pModelName + ";" 
        		+ eval.numTruePositives(positiveValueIndexOfClassFeature) + ";"
                + eval.numFalsePositives(positiveValueIndexOfClassFeature) + ";" 
        		+ eval.numFalseNegatives(positiveValueIndexOfClassFeature) + ";" 
                + eval.numTrueNegatives(positiveValueIndexOfClassFeature) + ";" 
        		+ accuracy + ";" 
                + eval.precision(positiveValueIndexOfClassFeature) + ";" 
        		+ eval.recall(positiveValueIndexOfClassFeature) + ";" 
                + fmeasure + ";" 
        		+ eval.areaUnderROC(positiveValueIndexOfClassFeature) + ";" 
                + mcc + "\n";
        
        output2 += String.format("%s & %s & %s & %s & %.2f & %.2f & %.2f \\\\ %s", 
        		pClassifierName,
        		eval.numTruePositives(positiveValueIndexOfClassFeature),
                eval.numFalsePositives(positiveValueIndexOfClassFeature),
        		eval.numFalseNegatives(positiveValueIndexOfClassFeature), 
                eval.precision(positiveValueIndexOfClassFeature),
        		eval.recall(positiveValueIndexOfClassFeature), 
                fmeasure,
                System.lineSeparator());        
    }

    private static Instances readFile(String pPath) throws Exception {
        ConverterUtils.DataSource source = new ConverterUtils.DataSource(pPath);
        // DataSource source = new DataSource(pPath);
        Instances data = source.getDataSet();
        // setting class attribute if the data format does not provide this information
        // For example, the XRFF format saves the class attribute information as well
        if (data.classIndex() == -1)
            data.setClassIndex(data.numAttributes() - 1);

        return data;
    }


    private static Instances selectAllFeatures(Instances pOrigin) throws Exception {
        int[] nonHassanIndexes = new int[] { 0, 1, 4 };

        Remove remove = new Remove();
        remove.setAttributeIndicesArray(nonHassanIndexes);
        remove.setInputFormat(pOrigin);
        Instances newData = Filter.useFilter(pOrigin, remove);

        return newData;
    }
}
