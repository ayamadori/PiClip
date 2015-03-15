package ayamadori.piclip.ui;

import javax.microedition.content.Invocation;
import javax.microedition.content.Registry;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.List;
import javax.microedition.midlet.MIDlet;
//import sms.SMSScreen;
import ayamadori.piclip.PiClip;
import ayamadori.piclip.util.Util;

public class ShareMenu implements CommandListener {

	private MIDlet midlet;
	private Display display;
	private Displayable backScreen;
	private String text;
	private List shareList;
	private Command back;
	private Alert errorMessageAlert;

	public ShareMenu(MIDlet midlet, String text) {
		this.midlet = midlet;
		this.text = text;

		errorMessageAlert = new Alert("Error", null, null, AlertType.ERROR);
		errorMessageAlert.setTimeout(5000);
	}
	
	public void openBrowserMenu(Displayable backScreen){
		this.backScreen = backScreen;
		try {
			shareList = new List("Open Browser", List.IMPLICIT);
			shareList.append("Bing", Image.createImage("/icon/bing.png"));
//			shareList.append("Google Search", Image.createImage("/icon/google_search.png"));
//			shareList.append("Bing Translator", Image.createImage("/icon/bing_translator.png"));
			shareList.append("Translate", Image.createImage("/icon/google_translate.png"));
			shareList.append("Wikipedia", Image.createImage("/icon/wikipedia.png"));
			shareList.append("Twitter", Image.createImage("/icon/twitter.png"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		back = new Command("Back", Command.BACK, 1);
		shareList.addCommand(back);
		shareList.setCommandListener(this);
		display = Display.getDisplay(midlet);
		display.setCurrent(shareList);
	}

	public void commandAction(Command command, Displayable displayable) {
    	if (command == List.SELECT_COMMAND) {
    		switch (shareList.getSelectedIndex()) {
				case 1: // Translator
//					launch("http://www.bing.com/translator/m/?InputTextArea=" + Util.URLencode(text));
//					launch("http://www.bing.com/translator/?text=" + Util.URLencode(text));
					launch("http://translate.google.com/m?q=" + Util.URLencode(text));
    				break;
				case 2: // Wikipedia
//					launch("http://ja.m.wikipedia.org/wiki/" + Util.URLencode(text));
					launch("http://ja.m.wikipedia.org/w/index.php?search=" + Util.URLencode(text));
    				break;
				case 3: // twitter
					launch("https://twitter.com/intent/tweet?text=" + Util.URLencode(text));
					break;
    			default: // Bing
//    				launch("http://www.google.com/m?q=" + Util.URLencode(text));
					launch("http://m.bing.com/search?q=" + Util.URLencode(text));
					break;
    		}
    	} else if (command == back) {
        	display.setCurrent(backScreen);
        	//Enable copy on PiPanel only
        	PiClip.enableCopy = true;
        }
    }
	
	private void launch(String url) {
		try {
			midlet.platformRequest(url);
		} catch (Exception e) {
			errorMessageAlert.setString("Failed to launch");
			display.setCurrent(errorMessageAlert, shareList);
			e.printStackTrace();
		}
	}
	
	public void share() {
		String[] args = new String[1]; // Only the first element is required and used
		args[0] = new String("text=" + text); // Content to share

		try {
			Registry registry = Registry.getRegistry(midlet.getClass().getName());
//			String[] IDs = registry.getIDs();
//			String[] actions = registry.getActions();
//			String[] types = registry.getTypes();
//			String[] suffixes = registry.getSuffixes();
//			System.out.println("ID===============>");
//			for(int i=0; i<IDs.length; i++) {
//				System.out.println(IDs[i]);
//			}
//			System.out.println("Action===============>");
//			for(int i=0; i<actions.length; i++) {
//				System.out.println(actions[i]);
//			}
//			System.out.println("Type===============>");
//			for(int i=0; i<types.length; i++) {
//				System.out.println(types[i]);
//			}
//			System.out.println("Suffixes===============>");
//			for(int i=0; i<suffixes.length; i++) {
//				System.out.println(suffixes[i]);
//			}
			Invocation invocation = new Invocation(null, "text/plain", "com.nokia.share");
			invocation.setAction("share");
			invocation.setArgs(args);
			invocation.setResponseRequired(false);
			registry.invoke(invocation);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
