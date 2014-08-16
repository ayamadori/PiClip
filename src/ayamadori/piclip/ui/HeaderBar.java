package ayamadori.piclip.ui;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import com.nokia.mid.ui.DirectUtils;

public class HeaderBar {// extends CanvasGraphicsItem {

	private static final int BACKGROUND_COLOR = 0xF4F4F4;
	private static final int PRIMARY_TEXT_COLOR = 0x29A7CC;
	private static final int PRIMARY_TEXT_SIZE = 18;
	private static final int OFFSET_X = 10;
	private static final int OFFSET_Y = 8;
	private static final int HEIGHT = 40;

	private int width;
	
	public HeaderBar(Canvas canvas) {
//		super(canvas.getWidth(), HEIGHT);
//		setParent(canvas);
//		setVisible(true);
		width = canvas.getWidth();
	}

	protected void paint(Graphics g) {
		g.setColor(BACKGROUND_COLOR);
		g.fillRect(0, 0, width, HEIGHT);
		g.setColor(PRIMARY_TEXT_COLOR);
		Font font = DirectUtils.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, PRIMARY_TEXT_SIZE);
		g.setFont(font);
//		int bottom = OFFSET + font.getHeight();
//		g.drawString("PiClip", OFFSET, bottom, Graphics.BOTTOM | Graphics.LEFT);
		g.drawString("PiClip", OFFSET_X, OFFSET_Y, Graphics.TOP | Graphics.LEFT);
	}
	
	public int getHeight() {
		return HEIGHT;
	}
}
