package ayamadori.piclip.util;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

public class FixedFont {
	
	public static void drawChar(Graphics g, Font font, char character, int x, int y, int anchor) {
			char[] data = {character, '\u3000'};
			int offset = 0;
			if((anchor & Graphics.HCENTER) == Graphics.HCENTER) {
				offset = font.charWidth('\u3000') / 2;
			} else if((anchor & Graphics.RIGHT) == Graphics.RIGHT) {
				offset = font.charWidth('\u3000');
			}
			g.drawChars(data, 0, data.length, x + offset, y, anchor);
	}
	
	public static void drawString(Graphics g, Font font, String str, int x, int y, int anchor) {
		str = str + "\u3000";
		int offset = 0;
		if((anchor & Graphics.HCENTER) == Graphics.HCENTER) {
			offset = font.charWidth('\u3000') / 2;
		} else if((anchor & Graphics.RIGHT) == Graphics.RIGHT) {
			offset = font.charWidth('\u3000');
		}
		g.drawString(str, x + offset, y, anchor);
	}
	
	public static int charWidth(Font font, char ch) {
		char[] data = {ch, '\u3000'};
		return font.charsWidth(data, 0, 2) - font.charWidth('\u3000');
	}
	
	public static int stringWidth(Font font, String str) {
		str = str + "\u3000";
		return font.stringWidth(str) - font.charWidth('\u3000');
	}

}
