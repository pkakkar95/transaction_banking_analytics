

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


import java.util.List;

import java.util.concurrent.TimeUnit;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Calendar;


public class SeriesHandler {
	
	
	private String filePath;
	private int dateColoumnIndex;
	private int dataColoumnIndex;
	private String dateColoumnName;
	private String dataColoumnName;
	private List<Date> dateList;
	private List<String> data;
	private List<String> dateData;
	private String dateFormat;
	private Forecast dateForecast;
	private Forecast dataForecast;
	
	/*
	 * Requires : 
	 * 
	 * 1) Complete file path to training data csv
	 * 2) Column name for dates
	 * 3) Column name for training data
	 * 4) Date format string to process date
	 * 
	 */
	
	public SeriesHandler(String filePath, String dateColoumn, String dataColoumn,String dateFormat) throws IOException,FileNotFoundException, ParseException{
		this.filePath = filePath;
		this.dateFormat = dateFormat;
		this.data = new ArrayList<>();
		this.dateList = new ArrayList<>();
		this.dateData = new ArrayList<>();
		boolean firstRow = true;
		this.dataColoumnName = dataColoumn;
		this.dateColoumnName = dateColoumn;
		String line;
		BufferedReader br = new BufferedReader(new FileReader(this.filePath));
		while((line = br.readLine())!= null){
			if(firstRow){
				String[] split = line.split(",");
				for(int i =0; i < split.length;i++){
					if(split[i].equals(dateColoumn)){
						this.dateColoumnIndex = i;
					}
					if(split[i].equals(dataColoumn)){
						this.dataColoumnIndex = i;
					}
				}
				firstRow = false;
			}
			else{
				String[] split = line.split(",");
				this.data.add(split[this.dataColoumnIndex]);
				this.dateData.add(split[this.dateColoumnIndex]);
			}
		}
		br.close();
		
		for(int i =0;i<this.dateData.size();i++){
			DateFormat dateF = new SimpleDateFormat(this.dateFormat);
			Date date = dateF.parse(this.dateData.get(i));
			this.dateList.add(date);
		}
	}
	
	/*
	 * This is an untested code. Need to check is it is working on a real case
	 * 
	 * It will enable us to directly send user csv files
	 */
	
	public void addDublicates(){
		Date lastDate = this.dateList.get(0);
		List<String> compiled = new ArrayList<>();
		List<String> compiledDateData = new ArrayList<>();
		List<Date> compiledDate = new ArrayList<>();
		double container = 0.0;
		for(int i = 0; i<this.dateList.size();i++){
			if(this.dateList.get(i).equals(lastDate)){
				container += Double.parseDouble(this.data.get(i));
				
			}else{
				compiledDate.add(lastDate);
				System.out.println(i + ": "+container);
				compiledDateData.add(this.dateData.get(i-1));
				compiled.add(""+container);
				container = Double.parseDouble(this.data.get(i)); 
				lastDate = this.dateList.get(i);
			}
			if(i == this.dateList.size()-1){
				if(this.dateList.get(i).equals(lastDate)){
					compiledDate.add(lastDate);
					compiledDateData.add(this.dateData.get(i-1));
					compiled.add(""+container);
				}
				else{
					compiledDate.add(this.dateList.get(i));
					compiledDateData.add(this.dateData.get(i));
					compiled.add(this.data.get(i));
				}
			}
		}
		this.dateList = compiledDate;
		this.data = compiled;
		this.dateData = compiledDateData;
	}
	
	/*
	 * 
	 * This method initiates forecast and saves the data to given file.
	 * Requires:
	 * 1) Full path for target file
	 * 2) train size
	 * 2) number of time Steps
	 * 3) minibatch size
	 * 
	 */
	public void createAndSaveForecast(String savePath,int trainSize,int numberOfTimeSteps,int miniBatchSize) throws IOException, InterruptedException{
		this.createAndSaveForecast(savePath, trainSize, numberOfTimeSteps, miniBatchSize,140, 0.9, 0.15,10,50);
	}
	/*
	 * This method can be used to tweak other parameters of LSTM
	 */
	public void createAndSaveForecast(String savePath,int trainSize,int numberOfTimeSteps,int miniBatchSize,int seed,double momentum, double learningRate,int internalLayerCommunicationSize, int numberOfEpochs) throws IOException, InterruptedException{
		List<String> differencedSeries = new ArrayList<>();
		for(int i = 0;i<this.dateList.size();i++){
			if(i==0){
				differencedSeries.add("0");
			}
			else{
				int days = (int) TimeUnit.DAYS.convert(this.dateList.get(i).getTime() - this.dateList.get(i-1).getTime(), TimeUnit.MILLISECONDS);
				differencedSeries.add(days+"");
			}
		}
		
		this.dataForecast = new Forecast(this.data, trainSize, numberOfTimeSteps, miniBatchSize, seed, momentum,  learningRate, internalLayerCommunicationSize,  numberOfEpochs);
		this.dateForecast = new Forecast(differencedSeries, trainSize, numberOfTimeSteps, miniBatchSize, seed, momentum,  learningRate, internalLayerCommunicationSize,  numberOfEpochs);
		
		List<String> forecastedData = this.dataForecast.getForecast();
		List<String> forecastedDate = this.dateForecast.getForecast();
		List<Date> finalDates = new ArrayList<>();
		int dayToMili = 24*60*60*1000;
		for(int i = 0 ; i <forecastedDate.size();i++){
			int diff = (int)Double.parseDouble(forecastedDate.get(i));
			if(i==0){
				long time = this.dateList.get(this.dateList.size()-1).getTime() + diff*dayToMili;
				finalDates.add(new Date(time));
				
			}else{
				long time = finalDates.get(i-1).getTime() + diff*dayToMili;
				finalDates.add(new Date(time));
			}
		}
		forecastedDate = new ArrayList<>();
		DateFormat df = new SimpleDateFormat(this.dateFormat);
		for (int i = 0 ; i <finalDates.size();i++){
			Date d = finalDates.get(i);
			forecastedDate.add(df.format(d));
		}
		this.saveCsv(savePath,forecastedData,forecastedDate);
		
		
	}
	/*
	 * Saves data to given file
	 */
	private void saveCsv(String savePath,List<String> forecastedData,List<String> finalDates) throws IOException{
		FileWriter writer = new FileWriter(savePath);
		writer.write(this.dateColoumnName+","+this.dataColoumnName+"\n");
		for(int i = 0; i < forecastedData.size();i++){
			writer.write(finalDates.get(i)+","+forecastedData.get(i)+"\n");
		} 
		writer.close();
	}

}


/*

										  - -
										{ 0 0 }
						+===========oOO===(_)===OOo=========+
						|_____|_____|_____|_____|_____|_____|
						|__|_____|_____|_____|_____|_____|__|
						|__|__|				  		  |__|__|
						|_____|						  |_____|
						|_____| 	Kilroy Was Here   |_____|
						|_____|						  |_____|
						|__|__|_______________________|__|__|
						|_____|_____|_____|_____|_____|_____|
						+===================================+

*/
