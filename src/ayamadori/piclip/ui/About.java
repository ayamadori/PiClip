package ayamadori.piclip.ui;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.midlet.MIDlet;

public class About implements CommandListener {
	
	private final String TEXT =
			"PiClip\n" +
			"\n" +
			"Version 1.0.0\n" +
			"\n" +
			"2014 Copyright (c) ayamadori. All rights reserved.\n" +
			"<https://sites.google.com/site/ayamadori/>\n" +
			"\n" +	
			"=========================\n" +	
			"This app includes following software.\n" +	
			"\n" +	
			"econodic\n" +
			"<http://www8.atpages.jp/pulpwood/isweb/omake/econodic.html>\n" +
			"\n" +
			"Unicode Emoji Dictionary\n" +
			"<http://sourceforge.jp/projects/moji/>";
	
	private MIDlet midlet;
	private Command cmdBack;
	private Form window;
	private Displayable parent;
	
	public About(MIDlet midlet, Displayable parent) {
		window = new Form("About");		
		window.append(TEXT);
		
		cmdBack = new Command("Back", Command.BACK, 0);
		window.addCommand(cmdBack);
	
		this.midlet = midlet;
		this.parent = parent;
		Display.getDisplay(this.midlet).setCurrent(window);
		
		window.setCommandListener(this);
	}

	public void commandAction(Command c, Displayable d) {
		if(c == cmdBack) {
			Display.getDisplay(midlet).setCurrent(parent);
		}
	}
}
