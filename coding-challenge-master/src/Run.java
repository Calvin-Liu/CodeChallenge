import java.util.*;
import java.io.*;

class Run {

	public Run() {
	}

	public static void main(String[] args) {
		ParseJSON parse = new ParseJSON();
		try {
			FileReader file = new FileReader("../tweet_input/tweets.txt");
			parse.read(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}