
 /*
	   	
						                   - -
						                 { X X }
						 +===========oOO===(_)===OOo=========+
						 |_____|_____|_____|_____|_____|_____|
						 |__|_____|_____|_____|_____|_____|__|
						 |__|__|  					   |__|__|
						 |_____| 					   |_____|
						 |_____| 	 Wot! No Kilroy?   |_____|
						 |_____|  	   				   |_____|
						 |__|__|_______________________|__|__|
						 |_____|_____|_____|_____|_____|_____|
						 +===================================+
	    
*/

import org.apache.commons.io.FileUtils;


import org.bytedeco.javacpp.opencv_ml.TrainData;
import org.datavec.api.records.reader.SequenceRecordReader;
import org.datavec.api.records.reader.impl.csv.CSVSequenceRecordReader;
import org.datavec.api.split.NumberedFileInputSplit;
import org.datavec.api.util.ClassPathResource;
import org.deeplearning4j.datasets.datavec.SequenceRecordReaderDataSetIterator;
import org.deeplearning4j.eval.RegressionEvaluation;
import org.deeplearning4j.examples.userInterface.util.GradientsAndParamsListener;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.GravesLSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RefineryUtilities;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerMinMaxScaler;
import org.nd4j.linalg.exception.ND4JException;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;


public class Forecast {
	
	
	
	//	Data Members start
	private List<String> data;
	private File initDir =  initBaseFile("/multiTimestep");
	private File trainDir = new File(initDir, "multiTimestepTrain"); 
	private int trainSize;
	private int numberOfTimeSteps;
	private int miniBatchSize;
	private int seed;
	private double momentum;
	private double learningRate;
	private int internalLayerCommunicationSize;
	private int numberOfEpochs;
	private NormalizerMinMaxScaler normalizer;
	private DataSetIterator trainDataIter;
	private List<String> forecast;
	private MultiLayerConfiguration networkConfiguration;
	private MultiLayerNetwork network;
	
	// Data members stop
	
	
	/*
	 * 
	 * Public constructors. User needs to provide data, train size, number of time steps and batch size
	 * Batch size should be factor of train size
	 * 
	 */
	public Forecast(List<String> data,int trainSize,int numberOfTimeSteps,int miniBatchSize) throws IOException,InterruptedException{
		
		this(data, trainSize, numberOfTimeSteps, miniBatchSize, 140, 0.9, 0.15,10,50);
		//Default arguments are based on experimental data and can be in-efficient for different data
	}
	
	/*
	 * This public constructor gives user more freedom to tweak parameters of Multi-layer Network
	 */
	public Forecast(List<String> data,int trainSize,int numberOfTimeSteps,int miniBatchSize,int seed,double momentum, double learningRate,int internalLayerCommunicationSize, int numberOfEpochs) throws IOException,InterruptedException{
	this.data = data;
	this.trainSize = trainSize;
	this.numberOfTimeSteps = numberOfTimeSteps;
	this.miniBatchSize = miniBatchSize;
	this.seed = seed;
	this.momentum = momentum;
	this.learningRate = learningRate;
	this.internalLayerCommunicationSize = internalLayerCommunicationSize;
	this.numberOfEpochs = numberOfEpochs;
	this.forecast = new ArrayList<>();
	this.normalizer = new NormalizerMinMaxScaler(0, 1); 
	this.fit();	
	}
	
	/*
	 * Returns the forecast result
	 */
	public List<String> getForecast(){
		return this.forecast;
	}
	
	/*
	 * The program needs to create files of data in batched, this function is used to create a base directory
	 */
	private File initBaseFile(String fileName){
        try {
            return new ClassPathResource(fileName).getFile();
        } catch (IOException e) {
            throw new Error(e);
        }
    }
	
	
	
	
	/*
	 * The function performs the following tasks : 
	 * 
	 * 1) Create training files
	 * 2) Create a data iterator for these files
	 * 3) Create a data Normalizer and hence, normalizes the training data
	 * 4) Creates a Multi-layer Network as specified (default or user parameters)
	 * 5) Train the network
	 * 6) Make predictions of 1 time step.
	 * 
	 */
	
	private void fit() throws IOException, InterruptedException{
		
		this.prepTrainingFiles();
		this.prepTraingIterator();
		this.prepNormalizer();
		this.buildNetwork();
		this.trainNetwork();
		this.predict();
	}
	
	/*
	 * creates multiple files to be used by data iterator
	 */
	 private  void prepTrainingFiles() throws IOException{
		 FileUtils.cleanDirectory(trainDir);
    	 for (int i = 0; i < this.trainSize - this.numberOfTimeSteps;i++){
    		 String featureName = "/trainFeature_" + i + ".csv";
    		 String LabelName = "/trainLabel_" + i + ".csv";
    		 for(int j = 0; j < this.numberOfTimeSteps; j++){
    			 System.out.println(Paths.get(this.trainDir.getAbsoluteFile() + featureName));
    			 Files.write(Paths.get(this.trainDir.getAbsoluteFile() + featureName), this.data.get(i + j).concat(System.lineSeparator()).getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
    		 }
    		 Files.write(Paths.get(this.trainDir.getAbsoluteFile() + LabelName),this.data.get(i + this.numberOfTimeSteps).concat(System.lineSeparator()).getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
    	 }
    }
	 /*
	  * Creates data Iterator
	  */
	 private void prepTraingIterator() throws IOException, InterruptedException{
		 
		 	SequenceRecordReader trainFeatures = new CSVSequenceRecordReader();
	        trainFeatures.initialize(new NumberedFileInputSplit(this.trainDir.getAbsoluteFile() + "/trainFeature_%d.csv", 0, trainSize - numberOfTimeSteps -  1));
	        SequenceRecordReader trainLabels = new CSVSequenceRecordReader();
	        trainLabels.initialize(new NumberedFileInputSplit(this.trainDir.getAbsoluteFile() + "/trainLabel_%d.csv", 0, trainSize - numberOfTimeSteps - 1));
	        this.trainDataIter = new SequenceRecordReaderDataSetIterator(trainFeatures, trainLabels, miniBatchSize, -1, true, SequenceRecordReaderDataSetIterator.AlignmentMode.ALIGN_END);
	        
		 
	 }
	 /*
	  * Create normalizer and pre-processes training data
	  */
	 private void prepNormalizer(){
		 this.normalizer.fitLabel(true);
	     this.normalizer.fit(this.trainDataIter);              
	     this.trainDataIter.reset();
	     this.trainDataIter.setPreProcessor(this.normalizer);
	 }
	 /*
	  * Creates a network of specified conditions
	  */
	 private void buildNetwork(){
		 this.networkConfiguration = new NeuralNetConfiguration.Builder()
	                .seed(this.seed)
	                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
	                .iterations(1)
	                .weightInit(WeightInit.XAVIER)
	                .updater(Updater.NESTEROVS).momentum(this.momentum)
	                .learningRate(this.learningRate)
	                .list()
	                .layer(0, new GravesLSTM.Builder().activation(Activation.TANH).nIn(1).nOut(this.internalLayerCommunicationSize)
	                    .build())
	                .layer(1, new RnnOutputLayer.Builder(LossFunctions.LossFunction.MSE)
	                    .activation(Activation.IDENTITY).nIn(this.internalLayerCommunicationSize).nOut(1).build())
	                .build();
		 
		 this.network = new MultiLayerNetwork(this.networkConfiguration);
		 this.network.init();
		 
	 }
	 /*
	  * Trains the network
	  */
	 private void trainNetwork(){
		 for (int i = 0; i < this.numberOfEpochs; i++) {
             this.network.fit(this.trainDataIter);
             this.trainDataIter.reset();
         }
         while (this.trainDataIter.hasNext()) {
             DataSet t = this.trainDataIter.next();
             this.network.rnnTimeStep(t.getFeatureMatrix());
         }

         this.trainDataIter.reset();
	 }
	 /*
	  * predicts one time step
	  */
	 private void predict(){
		 INDArray initialInput = createIndArrayFromStringList(this.data, this.trainSize - this.numberOfTimeSteps - 1, this.numberOfTimeSteps);
         INDArray initialLabel = createIndArrayFromStringList(this.data, this.trainSize - 1, 1);
         
         this.normalizer.transform(initialInput);
         INDArray output = this.network.output(initialInput,true);
         List<String> tempOutput = new ArrayList<>();
         tempOutput.add( "" + output.data().getDouble(output.data().length()-1));
         for(int i = 1; i < this.numberOfTimeSteps ; i++){
         	List<String> input= new ArrayList<>(); 
         	for (int j = this.trainSize - this.numberOfTimeSteps - 1; j < this.trainSize;j++){
         		input.add(this.data.get(j));
         	}
         	for(int k=0;k<tempOutput.size();k++){
         		input.add(tempOutput.get(k));
         	}
         	INDArray tempInput = createIndArrayFromStringList(input, 0, tempOutput.size());
         	this.normalizer.transform(tempInput);
         	INDArray outputx = this.network.output(tempInput);
         	tempOutput.add(""+outputx.data().getDouble(outputx.data().length()-1));
         
         }
         INDArray tempForecast = createIndArrayFromStringList(tempOutput, 0, tempOutput.size());
         
         this.normalizer.revertLabels(tempForecast);
         for(int i = 0; i < tempForecast.data().length();i++){
        	 this.forecast.add("" + tempForecast.data().getDouble(i));
         }
	 }
	 /*
	  * Helper Function
	  */
	 private INDArray createIndArrayFromStringList(List<String> rawStrings, int startIndex, int length) {
	        List<String> stringList = rawStrings.subList(startIndex, startIndex + length);

	        double[][] primitives = new double[1][stringList.size()];
	        for (int i = 0; i < stringList.size(); i++) {
	            String[] vals = stringList.get(i).split(",");
	            for (int j = 0; j < vals.length; j++) {
	                primitives[j][i] = Double.valueOf(vals[j]);
	            }
	        }

	        return Nd4j.create(new int[]{1, length}, primitives);
	 }


}



