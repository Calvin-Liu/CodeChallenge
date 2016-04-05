import java.util.*;
import java.io.*;

class Run {

	public Run() {
	}

	public static void main(String[] args) {
		ParseJSON parse = new ParseJSON();
		try {
			FileReader file = new FileReader("../data-gen/tweets.txt");
            //FileReader file = new FileReader(args[0]);
			parse.read(file, "../tweet_output/output.txt");
			//parse.read(file, args[1]);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}