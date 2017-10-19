package methodLevelBugPrediction;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Vector;

import methodLevelBugPrediction.utilities.FileUtilities;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.GreedyStepwise;
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
import weka.filters.supervised.instance.ClassBalancer;
import weka.filters.unsupervised.attribute.Remove;

public class EvaluateModels {

    private static String output = "project-name,classifier,model-name,TP,FP,FN,TN,accuracy,precision,recall,f-measure,auc-roc,mcc\n";
    private static String projectName = "";

    public static void main(String[] args) {

        HashMap<String, Classifier> classifiers = new HashMap<String, Classifier>();
        classifiers.put("SimpleLogistic", new SimpleLogistic());
        classifiers.put("MultilayerPerceptron", new MultilayerPerceptron());
        // classifiers.put("ADTree", new ADTree());
        classifiers.put("RandomForest", new RandomForest());
        classifiers.put("J48", new J48());
        //
        classifiers.put("NaiveBayes", new NaiveBayes());
        classifiers.put("Logistic", new Logistic());
        classifiers.put("DecisionTable", new DecisionTable());

        try {
            Vector<String> projects = readProjects("/Users/luca/TUProjects/SANER/projects_to_classify.txt");
            for (String project : projects) {
                Vector<String> releases = readReleases("/Users/luca/TUProjects/SANER/outputOK/" + project + "_releases.csv");
                // File dir = new File("/Users/luca/TUProjects/SANER/output/");

                for (int i = 0; i < releases.size() - 1; i++) {
                    // if (!project.isHidden()) {
                    String trainingSet = "/Users/luca/TUProjects/SANER/outputOK/" + project + "/" + releases.get(i) + "_allMetrics.csv";
                    String testSet = "/Users/luca/TUProjects/SANER/outputOK/" + project + "/" + releases.get(i + 1) + "_allMetrics.csv";

                    System.out.println("Evaluating " + releases.get(i) + "/" + (releases.size() - 1) + " ===> " + trainingSet);

                    // Load Training and Test set from CSV file
                    Instances originTraining = EvaluateModels.readFile(trainingSet);
                    Instances originTest = EvaluateModels.readFile(testSet);

                    // EvaluateModels.projectName = trainingSet.substring(trainingSet.lastIndexOf("/") + 1, trainingSet.length());
                    EvaluateModels.projectName = project;

                    Instances onlyStructuralModelTraining = EvaluateModels.selectStructuralFeaturesOnly(originTraining);
                    Instances onlyChangeModelTraining = EvaluateModels.selectChangeFeaturesOnly(originTraining);
                    Instances onlyCommentModelTraining = EvaluateModels.selectCommentFeaturesOnly(originTraining);
                    Instances structuralAndChangeModelTraining = EvaluateModels.selectStructuralAndChangeFeaturesOnly(originTraining);
                    Instances structuralAndCommentTraining = EvaluateModels.selectStructuralAndCommentFeaturesOnly(originTraining);
                    Instances changeAndCommentTraining = EvaluateModels.selectChangeAndCommentFeaturesOnly(originTraining);
                    Instances allModelTraining = EvaluateModels.selectAllFeatures(originTraining);

                    Instances onlyStructuralModelTest = EvaluateModels.selectStructuralFeaturesOnly(originTest);
                    Instances onlyChangeModelTesting = EvaluateModels.selectChangeFeaturesOnly(originTest);
                    Instances onlyCommentModelTestign = EvaluateModels.selectCommentFeaturesOnly(originTest);
                    Instances structuralAndChangeModelTesting = EvaluateModels.selectStructuralAndChangeFeaturesOnly(originTest);
                    Instances structuralAndCommentTesting = EvaluateModels.selectStructuralAndCommentFeaturesOnly(originTest);
                    Instances changeAndCommentTesting = EvaluateModels.selectChangeAndCommentFeaturesOnly(originTest);
                    Instances allModelTesting = EvaluateModels.selectAllFeatures(originTest);

                    for (Entry<String, Classifier> entry : classifiers.entrySet()) {

                        EvaluateModels.evaluateModel(entry.getValue(), onlyStructuralModelTraining, onlyStructuralModelTest, "structuralModel", entry.getKey());
                        EvaluateModels.evaluateModel(entry.getValue(), onlyChangeModelTraining, onlyChangeModelTesting, "changeModel", entry.getKey());
                        EvaluateModels.evaluateModel(entry.getValue(), onlyCommentModelTraining, onlyCommentModelTestign, "commentModel", entry.getKey());
                        EvaluateModels.evaluateModel(entry.getValue(), structuralAndChangeModelTraining, structuralAndChangeModelTesting, "structuralAndChangeModel", entry.getKey());
                        EvaluateModels.evaluateModel(entry.getValue(), structuralAndCommentTraining, structuralAndCommentTesting, "structuralAndCommentModel", entry.getKey());
                        EvaluateModels.evaluateModel(entry.getValue(), changeAndCommentTraining, changeAndCommentTesting, "changeAndCommentModel", entry.getKey());

                        EvaluateModels.evaluateModel(entry.getValue(), allModelTraining, allModelTesting, "allModel", entry.getKey());
                    }
                    // }
                }
            }

            // output = output.replaceAll("\\.", ",");
            FileUtilities.writeFile(output, "/Users/luca/TUProjects/SANER/outputOK/output.csv");

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

        // other options
        // int folds = 10;

        // randomize data
        // Random rand = new Random(42);
        // Instances randData = new Instances(pInstancesTraining);
        // randData.randomize(rand);
        // if (randData.classAttribute().isNominal())
        // randData.stratify(folds);

        // perform cross-validation and add predictions
        // Instances predictedData = null;
        Evaluation eval = new Evaluation(pInstancesTraining);

        int positiveValueIndexOfClassFeature = 0;
        // for (int n = 0; n < folds; n++) {
        // Instances train = randData.trainCV(folds, n);
        // Instances test = randData.testCV(folds, n);
        // // the above code is used by the StratifiedRemoveFolds filter, the
        // // code below by the Explorer/Experimenter:
        // // Instances train = randData.trainCV(folds, n, rand);
        //
        int classFeatureIndex = 0;
        for (int i = 0; i < pInstancesTraining.numAttributes(); i++) {
            if (pInstancesTraining.attribute(i).name().equals("buggy")) {
                classFeatureIndex = i;
                break;
            }
        }

        Attribute classFeature = pInstancesTraining.attribute(classFeatureIndex);
        for (int i = 0; i < classFeature.numValues(); i++) {
            if (classFeature.value(i).equals("TRUE")) {
                positiveValueIndexOfClassFeature = i;
            }
        }
        //
        // train.setClassIndex(classFeatureIndex);
        // test.setClassIndex(classFeatureIndex);

        AttributeSelectedClassifier classifier = new AttributeSelectedClassifier();
        CfsSubsetEval cfs = new CfsSubsetEval();
        GreedyStepwise search = new GreedyStepwise();
        search.setSearchBackwards(true);

        classifier.setClassifier(pClassifier);
        classifier.setEvaluator(cfs);
        classifier.setSearch(search);

        ClassBalancer filter = new ClassBalancer();
        filter.setInputFormat(pInstancesTraining);
        Instances balancedInstances = Filter.useFilter(pInstancesTraining, filter);

        // build and evaluate classifier
        classifier.buildClassifier(balancedInstances);
        eval.evaluateModel(classifier, pInstanceTesting);

        // add predictions

        // Instances pred = Filter.useFilter(pInstanceTesting, filter);
        //
        // if (predictedData == null)
        // predictedData = new Instances(pred, 0);
        //
        // for (int j = 0; j < pred.numInstances(); j++)
        // predictedData.add(pred.instance(j));
        // // }

        double mcc = eval.matthewsCorrelationCoefficient(positiveValueIndexOfClassFeature);

        double accuracy =

                (eval.numTruePositives(positiveValueIndexOfClassFeature) + eval.numTrueNegatives(positiveValueIndexOfClassFeature)) /

                        (eval.numTruePositives(positiveValueIndexOfClassFeature) + eval.numFalsePositives(positiveValueIndexOfClassFeature) + eval.numFalseNegatives(positiveValueIndexOfClassFeature)
                                + eval.numTrueNegatives(positiveValueIndexOfClassFeature));

        double fmeasure = 2
                * ((eval.precision(positiveValueIndexOfClassFeature) * eval.recall(positiveValueIndexOfClassFeature)) / (eval.precision(positiveValueIndexOfClassFeature) + eval.recall(positiveValueIndexOfClassFeature)));

        EvaluateModels.output += EvaluateModels.projectName + "," + pClassifierName + "," + pModelName + "," + eval.numTruePositives(positiveValueIndexOfClassFeature) + ","
                + eval.numFalsePositives(positiveValueIndexOfClassFeature) + "," + eval.numFalseNegatives(positiveValueIndexOfClassFeature) + "," + eval.numTrueNegatives(positiveValueIndexOfClassFeature) + "," + accuracy
                + "," + eval.precision(positiveValueIndexOfClassFeature) + "," + eval.recall(positiveValueIndexOfClassFeature) + "," + fmeasure + "," + eval.areaUnderROC(positiveValueIndexOfClassFeature) + "," + mcc
                + "\n";
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

    private static Instances selectStructuralFeaturesOnly(Instances pOrigin) throws Exception {
        // NB: It is an inverted process. To select indexes referring to structural
        // metrics, you need to remove the indexes that refer to non-structural ones.
        int[] nonStructuralIndexes = new int[] { 0, 1, 4, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33 };

        Remove remove = new Remove();
        remove.setAttributeIndicesArray(nonStructuralIndexes);
        remove.setInputFormat(pOrigin);
        Instances newData = Filter.useFilter(pOrigin, remove);

        return newData;
    }

    private static Instances selectCommentFeaturesOnly(Instances pOrigin) throws Exception {
        // NB: It is an inverted process. To select indexes referring to structural
        // metrics, you need to remove the indexes that refer to non-structural ones.
        int[] nonStructuralIndexes = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33 };

        Remove remove = new Remove();
        remove.setAttributeIndicesArray(nonStructuralIndexes);
        remove.setInputFormat(pOrigin);
        Instances newData = Filter.useFilter(pOrigin, remove);

        return newData;
    }

    private static Instances selectChangeFeaturesOnly(Instances pOrigin) throws Exception {
        // NB: It is an inverted process. To select indexes referring to structural
        // metrics, you need to remove the indexes that refer to non-structural ones.
        int[] nonStructuralIndexes = new int[] { 0, 1, 4, 2, 3, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18 };

        Remove remove = new Remove();
        remove.setAttributeIndicesArray(nonStructuralIndexes);
        remove.setInputFormat(pOrigin);
        Instances newData = Filter.useFilter(pOrigin, remove);

        return newData;
    }

    private static Instances selectStructuralAndChangeFeaturesOnly(Instances pOrigin) throws Exception {
        // NB: It is an inverted process. To select indexes referring to structural
        // metrics, you need to remove the indexes that refer to non-structural ones.
        int[] nonStructuralIndexes = new int[] { 0, 1, 4, 11, 12, 13, 14, 15, 16, 17, 18 };

        Remove remove = new Remove();
        remove.setAttributeIndicesArray(nonStructuralIndexes);
        remove.setInputFormat(pOrigin);
        Instances newData = Filter.useFilter(pOrigin, remove);

        return newData;
    }

    private static Instances selectStructuralAndCommentFeaturesOnly(Instances pOrigin) throws Exception {
        // NB: It is an inverted process. To select indexes referring to structural
        // metrics, you need to remove the indexes that refer to non-structural ones.
        int[] nonStructuralIndexes = new int[] { 0, 1, 4, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33 };

        Remove remove = new Remove();
        remove.setAttributeIndicesArray(nonStructuralIndexes);
        remove.setInputFormat(pOrigin);
        Instances newData = Filter.useFilter(pOrigin, remove);

        return newData;
    }

    private static Instances selectChangeAndCommentFeaturesOnly(Instances pOrigin) throws Exception {
        // NB: It is an inverted process. To select indexes referring to structural
        // metrics, you need to remove the indexes that refer to non-structural ones.
        int[] nonStructuralIndexes = new int[] { 0, 1, 4, 2, 3, 5, 6, 7, 8, 9, 10 };

        Remove remove = new Remove();
        remove.setAttributeIndicesArray(nonStructuralIndexes);
        remove.setInputFormat(pOrigin);
        Instances newData = Filter.useFilter(pOrigin, remove);

        return newData;
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
