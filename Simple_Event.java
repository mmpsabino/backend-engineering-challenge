import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import org.json.*;

public class Simple_Event {

	public LocalDateTime timestamp;
	public double duration;

	// constructor
	public Simple_Event(LocalDateTime a, double b) {
		this.timestamp = a;
		this.duration = b;
	}

	// build the input ArrayList by parsing the JSON file
	public static ArrayList<Simple_Event> readFromJsonFile(String pathFile) {

		ArrayList<Simple_Event> input = new ArrayList<Simple_Event>();

		String line = null;
		LocalDateTime datetime = null;
		double d = 0;

		try {
			FileReader fileReader = new FileReader(pathFile);
			BufferedReader bufferedReader = new BufferedReader(fileReader);

			while ((line = bufferedReader.readLine()) != null) {

				JSONObject obj = new JSONObject(line);

				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
				datetime = LocalDateTime.parse(obj.getString("timestamp"), formatter).truncatedTo(ChronoUnit.MINUTES);

				d = obj.getInt("duration");

				Simple_Event e = new Simple_Event(datetime, d);
				input.add(e);
			}
			bufferedReader.close();
			
		} catch (JSONException | IOException ex) {
			System.out.println("Unable to read file '" + pathFile + "'");
		}
		return input;
	}

	// when calculating the moving_avg for a timestamp, the auxiliary method below, starting_point, 
	// will determine where to start in the input list, given the timestamp for which we are calculating the moving_avg
	public static int starting_point(LocalDateTime dt, ArrayList<Simple_Event> input) {

		int start = input.size() - 1;
		while (dt.isBefore(input.get(start).timestamp.plusMinutes(1)) && start > 0) {
			start = start - 1;
		}
		return start;
	}

	// simple moving_avg calculator
	public static double avg_calculator(double sum, int hits) {
		return sum / hits;
	}

	
	public static void main(String[] args) {

		//window_size is calculated given the input args[1] passed through the command line
		int window_size = Integer.parseInt(args[1]);
		
		ArrayList<Simple_Event> input = new ArrayList<Simple_Event>();
		//the input ArrayList is built by parsing the JSON file passed as input in command line
		input = readFromJsonFile(args[0]); 

		// size of the output array
		int total_response_size = (int) ChronoUnit.MINUTES.between(input.get(0).timestamp,
				input.get(input.size() - 1).timestamp) + 2;
				// the +2 above is meant to include the running minute of the last
				// event, and also the posterior minute (when the running
				// translation is concluded)

		ArrayList<Simple_Event> output = new ArrayList<Simple_Event>();

		//in the for loop below, I initialize all the entries of the output ArrayList, given the already known timestamps
		for (int i = 0; i < total_response_size; i++) {
			output.add(new Simple_Event(input.get(0).timestamp.plusMinutes(i), 0));
		}

		//the for loop below is where the output ArrayList is covered to actually calculate the moving average for each element
		for (int j = total_response_size - 1; j >= 0; j = j - 1) {

			boolean proceed = true;
			int hits = 0;
			double sum = 0;
			int curr = Simple_Event.starting_point(output.get(j).timestamp, input);

			while (proceed && curr >= 0
					&& !input.get(curr).timestamp.isBefore(output.get(j).timestamp.minusMinutes(window_size))) {

				if (!output.get(j).timestamp.minusMinutes(window_size).isAfter(input.get(curr).timestamp)
						&& output.get(j).timestamp.isAfter(input.get(curr).timestamp)) {
					sum = sum + input.get(curr).duration;
					curr = curr - 1;
					hits = hits + 1;
				} else {
					proceed = false;
				}
			}

			if (hits == 0) {
				output.get(j).duration = 0;
			} else {
				output.get(j).duration = avg_calculator(sum, hits);
			}
		}
		
		//print the desired output
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:00");
		for (int k = 0; k < output.size(); k++) {
			System.out.println("{\"date\": \"" + output.get(k).timestamp.format(formatter)
					+ "\", \"average_delivery_time\": \"" + output.get(k).duration + "\"}");
		}

	}

}
