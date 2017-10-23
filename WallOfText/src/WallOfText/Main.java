package WallOfText;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import alphasign.AlphaSign;

public class Main {
	
	public static void main(String[] args) {
		
		MessageReader messageReader = new MessageReader("http://198.46.248.110/WoT/Message/GetMessage");
		
		
		AlphaSign sign = new AlphaSign();				
 		sign.openPort(args[0]);		 
		sign.configureMemory(-1);
		
		
		String oldData = "";
		while (true) {

			try {		
				String jsonData = messageReader.getNewMessage();
				
				if (jsonData != "") {
					if (jsonData.equals(oldData) != true) {
						oldData = jsonData;
																
				        JSONObject jo;

				        jo = (JSONObject) new JSONParser().parse(jsonData);
				        
				        long messageType = (long) jo.get("MessageType");
				        
				        int messageId = (int) messageType;
				        switch (messageId) {
				        case 1:
				        	handleAutoFitText(jo, sign);
				        	break;
				        case 2:
				        	handlePanelList(jo, sign);
				        	break;
				        case 3:
				        	handleGraphic(jo, sign);
				        	break;
				        }
					}
				}
				
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			for (int i = 0; i < 20; i++) {
				System.out.println("Sleeping ");
				sleep(1);
			}
		}

		
		
		
	}
	
	
	private static void handleAutoFitText(JSONObject jo, AlphaSign sign) {
		
        String fullText = (String) jo.get("Text");
        String fullTextColor = (String) jo.get("Color");
        
         
        System.out.println("Auto Fit Text");
        System.out.println(fullText);
        System.out.println(fullTextColor);

		sign.writeTextAutoFit(fullText, (byte) fullTextColor.charAt(0));
		
	}
	
	private static void handlePanelList(JSONObject jo, AlphaSign sign) {
		
		System.out.println("Panel List");
		JSONArray panelList = (JSONArray) jo.get("PanelList");
		Iterator i = panelList.iterator();
		int panelIndex = 0;
		
		while (i.hasNext()) {
			JSONObject panel = (JSONObject) i.next();
			long numLines = (long) panel.get("NumberOfLines");
			String color = (String) panel.get("Color");
			String text = (String) panel.get("Text");
			
			System.out.println("Panel " + panelIndex + " : lines " + numLines + " : color" + color + " text : " + text);
			
			sign.writeTextToPanel(panelIndex, (int)numLines, text, (byte) color.charAt(0));
			
			panelIndex++;
		}
		
	}
	
	
	
	
	private static void handleGraphic(JSONObject jo, AlphaSign sign) {
			String dots = (String) jo.get("Dots");
				         
	        System.out.println("graphic");
	        System.out.println(dots);
	       

		//	sign.writeTextAutoFit(fullText, (byte) fullTextColor.charAt(0));
					
	}
	

	public static void sleep(int seconds) {
		try {
			Thread.sleep(seconds * 1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
}
