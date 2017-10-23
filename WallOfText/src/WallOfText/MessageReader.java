package WallOfText;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MessageReader {


	String targetAddress = "";
	int currentMessage = 0;
	
	public MessageReader(String address) {
		targetAddress = address;
	}
	
	public String getNewMessage() {
		String file = "";
		switch (currentMessage) {
		case 0:
			file = "/home/pi/message0.txt";
			currentMessage = 1;
			break;
		case 1:
			file = "/home/pi/message1.txt";
			currentMessage = 2;
			break;
		case 2:
			file = "/home/pi/message2.txt";
			currentMessage = 3;
			break;
		case 3:
			file = "/home/pi/message3.txt";
			currentMessage = 4;
			break;
		case 4:
			file = "/home/pi/message4.txt";
			currentMessage = 5;
			break;
		case 5:
			file = "/home/pi/message5.txt";
			currentMessage = 0;
			break;
		
		}
		
		
		System.out.println("Reading file: " + file);
		BufferedReader br = null;
		String message = "";
		try {
			br = new BufferedReader(new FileReader(file));
			message = br.readLine();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
		    try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("Read data : " + message);
		
		return message;
		
	}
	
	/*
	public String getNewMessage() {
		String message = "";
		
		try {
			String jsonData = "";
			String output = "";
			URL url = new URL(targetAddress);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept",  "application/json");
			
			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
			}
			
			BufferedReader br = new BufferedReader(new InputStreamReader(
					(conn.getInputStream())));

			
			System.out.println("\n\tOutput from Server .... ");
			jsonData = "";
			while ((output = br.readLine()) != null) {
				System.out.println(output);
				jsonData += output;
			}

			conn.disconnect();
			
			message = jsonData;

		  } catch (MalformedURLException e) {

			e.printStackTrace();

		  } catch (IOException e) {

			e.printStackTrace();

		}
		
		return message;

	}
	*/
}
