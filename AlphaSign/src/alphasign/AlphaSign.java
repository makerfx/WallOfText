package alphasign;

import java.io.OutputStream;

import jssc.SerialPort;
import jssc.SerialPortException;

public class AlphaSign 
{
	private static final int maxBuffer = 10000;
	byte[] buffer;
	int bufferLength;
	
	OutputStream outputStream;
	
	SerialPort port;
	
	public AlphaSign() {
		buffer = new byte[maxBuffer];
		bufferLength = 0;
		

                
	}
	
	public void openPort(String portname) {
		port = new SerialPort(portname);
	       
        try {
        	port.openPort();//Open serial port
			port.setParams(SerialPort.BAUDRATE_9600, 
			               SerialPort.DATABITS_8,
			               SerialPort.STOPBITS_1,
			               SerialPort.PARITY_NONE);
		} catch (SerialPortException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	public void closePort() {
		try {
			port.closePort();
		} catch (SerialPortException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void test() {

		configureMemory(-1);

		createTestImage(-1, 'M', 4, 4, '2');


		displayImageToPanel(-1);

	}
	
	public void attachOutputStream(OutputStream os) {
		outputStream = os;
	}
	

	public void clearMemory(int panelIndex) {
		beginCommand(getAddressFromIndex(panelIndex));
		addByte('E');
		addByte('$');
		endCommand();
  
		sendMessage();
	}
		  
	public void configureMemory(int panelIndex) {
		beginCommand(getAddressFromIndex(panelIndex));
		addByte('E');
		addByte('$');
		addString("AAU00FFFFFE");
		addString("MDU18A02000");
		endCommand();
		
		sendMessage();
	}

	
	public void createTestImage(int panelIndex, char file, int row, int col, char color) {
		
		beginCommand(getAddressFromIndex(panelIndex));
		addByte(AS_CommandCode.WSDOTS);
		addByte(file);
		
		String dotSize = "";
		dotSize += String.format("%02x%02x", row, col);
		addString(dotSize);
		
		for (int r = 0; r < row; r++) {
			for (int c = 0; c < col; c++) {
				addByte(color);
			}
			addByte('\r');
		}
		
		endCommand();
		
		sendMessage();
		
	}
	
	
	public boolean writeLinesOfTextToPanels(String[] lines, byte color) {
		int linesPerPanel = 0;
		

		if (lines.length > 40) {
			return false;
		}
		
		if (lines.length <= 10) {
			linesPerPanel = 1;
		} else {
			if (lines.length <= 20) {
				linesPerPanel = 2;
			} else {
				if (lines.length <= 30) {
					linesPerPanel = 3;
				} else {
					linesPerPanel = 4;
				}
			}
		}
		
		String[] panels = breakIntoPanels(linesPerPanel, lines);
		for (int i = 0; i < 10; i++) {
			writeTextToPanel(-1, linesPerPanel, panels[i], color);
		}
		
		return true;
		
	}
	
	
	public boolean writeTextAutoFit(String text, byte color) {			
		text = text.replace("\r", " ");
		
		String[] words = text.split(" ");
		
		String[] result = null;
		
		int linesPerPanel = 1;
		boolean done = false;
		while (!done) {
			result = attemptFit(linesPerPanel, words);
			if (result != null) {
				done = true;
			} else {
				linesPerPanel++;
				if (linesPerPanel > 4) {
					done = true;
				}
			}
		}

		if (result != null) {
			String[] panels = breakIntoPanels(linesPerPanel, result);
			for (int i = 0; i < 9; i++) {
				writeTextToPanel(i, linesPerPanel, panels[i], color);
			}
			return true;
		} else {
			return false;
		}
	}
	
	public String[] breakIntoPanels(int linesPerPanel, String[] lines) {
		String[] panels = new String[9];
		for (int i = 0; i < 9; i++) {
			panels[i] = "";
		}
		
		int lineOnPanel = 0;
		int panelIndex = 0;
		for (int lineIndex = 0; lineIndex < lines.length; lineIndex++) {
			panels[panelIndex] += lines[lineIndex];
			panels[panelIndex] += "\r";
			
			lineOnPanel++;
			if (lineOnPanel == linesPerPanel) {
				panelIndex++;
				lineOnPanel = 0;
			}
						
		}
		
		return panels;
	}
	
	public String[] attemptFit(int linesPerPanel, String[] words) {
		String[] result = null;
		
		int maxPerLine = 0;
		int totalLines = 0;
		switch (linesPerPanel) {
		case 1:
			maxPerLine = 17;
			totalLines = 9;
			break;
		case 2:
			maxPerLine = 20;
			totalLines = 18;
			break;
		case 3:
			maxPerLine = 27;
			totalLines = 27;
			break;
		case 4:
			maxPerLine = 30;
			totalLines = 36;
			break;			
		}
		
		result = new String[totalLines];
		for (int i = 0; i < totalLines; i++) {
			result[i] = "";
		}
		
		int currentLine = 0;
		
		
		if (words[0].length() > maxPerLine) {
			return null;
		} else {
			result[0] += words[0];
		}
		
		for (int index = 1; index < words.length; index++) {
			if (words[index].trim().length() > 0) {
				if ((result[currentLine].length() + words[index].length() + 1) > maxPerLine) {
					currentLine++;
					if (currentLine < totalLines) {
						result[currentLine] += words[index];
					} else {
						return null;
					}
				} else {
					result[currentLine] += " ";
					result[currentLine] += words[index];
				}								
			}
		}
		
		return result;
	}

	public void writeTextToPanel(int panelIndex, int numberOfLines, String text) {
		writeTextToPanel(panelIndex, numberOfLines, text, AS_Color.RED, AS_Mode.HOLD, AS_SpecialMode.TWINKLE);
	}
	
	public void writeTextToPanel(int panelIndex, int linesPerPanel, String text, byte initialColor) {
		writeTextToPanel(panelIndex, linesPerPanel, text, initialColor, AS_Mode.HOLD, AS_SpecialMode.TWINKLE);
	}

	public void writeTextToPanel(int panelIndex, int linesPerPanel, String text, byte initialColor, byte mode) {
		writeTextToPanel(panelIndex, linesPerPanel, text, initialColor, mode, AS_SpecialMode.TWINKLE);
	}
	
	public void writeTextToPanel(int panelIndex, int linesPerPanel, String text, byte initialColor, byte mode, byte specialMode) {
		
		beginCommand(getAddressFromIndex(panelIndex));

		// Write text file to position 'A'
		addByte(AS_CommandCode.WTEXT);		
		addByte('A');
		
		// Escape, position, mode and special
		addByte(AS_ControlCode.ESC);
		addByte(AS_Position.FILL);
		addByte(mode);
		if (mode == AS_Mode.SPECIAL) {
			addByte(specialMode);
		}
		
		if (initialColor != AS_Color.AUTOCOLOR) {
			addByte(AS_Format.SELECTCHARCOLOR);
			addByte(initialColor);
		}
		
		// Character set (line height)
		switch (linesPerPanel) {
		case 1:
			addByte(AS_Format.SELECTCHARSET);    
			addByte(AS_Charset.FHIGH);
			
			break;
      
		case 2:
			addByte(AS_Format.SELECTCHARSET);    
			addByte(AS_Charset._10HIGH);
			break;
      
		case 3:
			addByte(AS_Format.SELECTCHARSET);    
			addByte(AS_Charset._7HIGH);
			break;
      
		case 4:
			addByte(AS_Format.SELECTCHARSET);    
			addByte(AS_Charset._5HIGH);
			break;
		}
		
		// Text
		addString(text);
		
		endCommand();
		
		sendMessage();

	}

	
	public void displayImageToPanel(int panelIndex) {
		
		// Start a write text command
		beginCommand(getAddressFromIndex(panelIndex));
		addByte(AS_CommandCode.WTEXT);
		addByte('A');
		
		// set position and mode
		addByte(AS_ControlCode.ESC);
		addByte(AS_Position.FILL);
		addByte(AS_Mode.HOLD);
		
		addByte(AS_Format.CALLSDOTS);
		addByte('M');
		
		endCommand();
		
		sendMessage();
		

	}	
	
	
	protected void beginCommand(String address) {
		bufferLength = 0;
		addByte(AS_ControlCode.NUL);
		addByte(AS_ControlCode.SOH);
		addByte('Z');
		addString(address);
		addByte(AS_ControlCode.STX);			
	}
	
	protected void endCommand() {
		addByte(AS_ControlCode.EOT);
	}
	
	protected void addByte(int c) {
		addByte((byte)c);
	}
	protected void addByte(char c) {
		addByte((byte)c);
	}
	protected void addByte(byte b) {
		if (bufferLength < maxBuffer) {
			buffer[bufferLength++] = b;		
		} else {
			System.out.println("Buffer overrun");
		}
	}
	
	protected void addString(String s) {
		byte[] bytes = s.getBytes();
		for (int i = 0; i < bytes.length; i++) {
			addByte(bytes[i]);
		}
	}
	
	
	protected String getAddressFromIndex(int index) {
		String address = "00";

		switch(index) {
		case 0:
			address = "01";
			break;
		case 1:
			address = "02";
			break;
		case 2:
			address = "03";
			break;
		case 3:
			address = "04";
			break;
		case 4:
			address = "05";
			break;
		case 5:
			address = "06";
			break;
		case 6:
			address = "07";
			break;
		case 7:
			address = "08";
			break;
		case 8:
			address = "09";
			break;
		case 9:
			address = "0A";
			break;
		default:
			address = "00";
			break;
		}

		return address;
	}

	protected void sendMessage() {
		try {		
			for (int i = 0; i < bufferLength; i++) {
				port.writeByte(buffer[i]);
			}
			bufferLength = 0;
		} catch (SerialPortException e) {
			e.printStackTrace();
		}
	}
}
