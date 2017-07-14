import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;

public class Main {
	public static void main(String[] args) throws FileNotFoundException, IOException, ParseException, InterruptedException {
		String filePath = "C:/Users/Prerna Kakkar/transaction_dummy_users/user2.csv";
		SeriesHandler seriesHandler = new SeriesHandler(filePath, "PAYMENT_DATE", "PAYMENT_AMOUNT", "yyyy-MM-dd");
		seriesHandler.addDublicates();
		seriesHandler.createAndSaveForecast("C:/Users/Prerna Kakkar/firstwebproject/WebContent/dataForecast3.csv", 60, 10, 5);
	}
}
