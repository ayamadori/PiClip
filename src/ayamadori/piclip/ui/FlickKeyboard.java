package ayamadori.piclip.ui;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import ayamadori.piclip.ui.PiPanel;
import ayamadori.piclip.util.FixedFont;
import com.nokia.mid.ui.DirectUtils;
import com.nokia.mid.ui.gestures.GestureEvent;
import com.nokia.mid.ui.gestures.GestureInteractiveZone;
import com.nokia.mid.ui.gestures.GestureRegistrationManager;

public class FlickKeyboard {

	private PiPanel parent;
//	private PiPanel_NewAsha parent;
//	private Canvas parent;
	
	public static final int KEY_CLEAR = -8;
	public static final int KEY_SHIFT = -15;

	private static final int BUTTON_WIDTH = 56;
	private static final int BUTTON_HEIGHT = 32;
	private static final int MARGIN = 4;
	private static final int ROUND = 4;
	private static final int BACKGROUND_COLOR = 0x191919;
	private static final int BUTTON_COLOR = 0x424242;
	private static final int ACTIVE_COLOR = 0x29A7CC;
	private static final int TEXT_COLOR = 0xFFFFFF;
//	private static final int TEXT_SUB_COLOR = 0x000000;
	private static final int TEXT_SIZE = 16;
//	private static final int LONG_PRESS_TIME_INTERVAL = 150;
	
	private static final String[] KEYS_KANA = { "わをんー　", "あいうえお", "かきくけこ", "さしすせそ", "たちつてと", "なにぬねの", "はひふへほ", "まみむめも",
		"や　ゆ　よ", "らりるれろ", "、。？！　", "ABC" };
    private static final String[] KEYS_ABC = { "@;/& ", " ()[]", "abc  ", "def  ", "ghi  ", "jkl  ", "mno  ", "pqrs ",
		"tuv  ", "wxyz ", ".,-_ ", "1&" };
    private static final String[] KEYS_SYMBOL = { "0'\"° ", "1@%#「", "2\\|/」", "3+=-~", "4・*&(", "5<^>)", "6～…→ ", "7\u00A5$〒€",
		"8×÷* ", "9:;_ ", ".,♪* ", "かな" };
	
	private boolean keyboard_visibility;
	private int pressed_button;
	private GestureInteractiveZone gizButtonArea;
	private int buttonArea[];
	private String[] keys;
	
	private Image imgButtonClear;
	private Image imgButtonClearPressed;
	private Image imgButtonShift;
	private Image imgButtonShiftPressed;
	private Image imgButtonEnter;
	private Image imgButtonEnterPressed;

	private FlickKeyboard(PiPanel parent) {
//	private FlickKeyboard(PiPanel_NewAsha parent) {
//	private FlickKeyboard(Canvas parent) {
		this.parent = parent;
		buttonArea = new int[16 * 2];
//		gizButtonArea = new GestureInteractiveZone(GestureInteractiveZone.GESTURE_TAP|GestureInteractiveZone.GESTURE_FLICK|GestureInteractiveZone.GESTURE_RECOGNITION_START|GestureInteractiveZone.GESTURE_RECOGNITION_END|GestureInteractiveZone.GESTURE_LONG_PRESS_REPEATED);
		gizButtonArea = new GestureInteractiveZone(GestureInteractiveZone.GESTURE_TAP|GestureInteractiveZone.GESTURE_FLICK|GestureInteractiveZone.GESTURE_RECOGNITION_START|GestureInteractiveZone.GESTURE_RECOGNITION_END);
		
		keyboard_visibility = false;
		pressed_button = -1;

		try {
			imgButtonClear = Image.createImage("/button/button_clear.png");
			imgButtonClearPressed = Image.createImage("/button/button_clear_pressed.png");
			imgButtonShift = Image.createImage("/button/button_shift.png");
			imgButtonShiftPressed = Image.createImage("/button/button_shift_pressed.png");
			imgButtonEnter = Image.createImage("/button/button_enter.png");
			imgButtonEnterPressed = Image.createImage("/button/button_enter_pressed.png");
		} catch (Exception e) {
			e.printStackTrace();
		}

//		gizButtonArea.setLongPressTimeInterval(LONG_PRESS_TIME_INTERVAL);
	}

	public static FlickKeyboard getFlickKeyboardControl(PiPanel parent) {
//	public static FlickKeyboard getFlickKeyboardControl(PiPanel_NewAsha parent) {
		return new FlickKeyboard(parent);
	}

	public void drawFlickKeyboard(Graphics g) {
		if (!keyboard_visibility) return;
		
		Font font = DirectUtils.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, TEXT_SIZE);
		g.setFont(font);
		setButtonArea();

		g.setColor(BACKGROUND_COLOR);
		g.fillRect(getXPosition(), getYPosition(), getWidth(), getHeight());

		for (int i = 0; i < 16; i++) {
			if (i == 3) {
				if (pressed_button == 3) g.drawImage(imgButtonClearPressed, buttonArea[i * 2], buttonArea[i * 2 + 1],
						Graphics.TOP | Graphics.LEFT);
				else
					g.drawImage(imgButtonClear, buttonArea[i * 2], buttonArea[i * 2 + 1], Graphics.TOP | Graphics.LEFT);
			} else if (i == 12) {
				if (pressed_button == 12) g.drawImage(imgButtonShiftPressed, buttonArea[i * 2], buttonArea[i * 2 + 1],
						Graphics.TOP | Graphics.LEFT);
				else
					g.drawImage(imgButtonShift, buttonArea[i * 2], buttonArea[i * 2 + 1], Graphics.TOP | Graphics.LEFT);
			} else if (i == 15) {
				if (pressed_button == 15) g.drawImage(imgButtonEnterPressed, buttonArea[i * 2], buttonArea[i * 2 + 1],
						Graphics.TOP | Graphics.LEFT);
				else
					g.drawImage(imgButtonEnter, buttonArea[i * 2], buttonArea[i * 2 + 1], Graphics.TOP | Graphics.LEFT);
			} else {
				if (pressed_button == i) g.setColor(ACTIVE_COLOR);
				else
					g.setColor(BUTTON_COLOR);
				g.fillRoundRect(buttonArea[i * 2], buttonArea[i * 2 + 1], BUTTON_WIDTH, BUTTON_HEIGHT, ROUND, ROUND);
			}
		}
		
		g.setColor(TEXT_COLOR);
		// text on button_mode
//		g.drawString(keys[11], (MARGIN+BUTTON_WIDTH)*7/2, getYPosition()+MARGIN*3+BUTTON_HEIGHT, Graphics.TOP|Graphics.HCENTER);
		FixedFont.drawString(g, font, keys[11], (MARGIN+BUTTON_WIDTH)*7/2, getYPosition()+MARGIN*3+BUTTON_HEIGHT, Graphics.TOP|Graphics.HCENTER);
		// text on button_right_arrow
		g.drawString("→", (MARGIN+BUTTON_WIDTH)*7/2, getYPosition()+MARGIN*4+BUTTON_HEIGHT*2, Graphics.TOP|Graphics.HCENTER);
		// text on button_pound=10
		if(keys.equals(KEYS_KANA))
//		if(font.stringWidth(keys[10]) > BUTTON_WIDTH-MARGIN*8)
//			g.drawChar(keys[10].charAt(0), (MARGIN+BUTTON_WIDTH)*5/2, parent.getHeight()-MARGIN-BUTTON_HEIGHT, Graphics.TOP|Graphics.HCENTER);
			FixedFont.drawChar(g, font, keys[10].charAt(0), (MARGIN+BUTTON_WIDTH)*5/2, parent.getHeight()-MARGIN-BUTTON_HEIGHT, Graphics.TOP|Graphics.HCENTER);
		else
//		    g.drawString(keys[10], (MARGIN+BUTTON_WIDTH)*5/2, parent.getHeight()-MARGIN-BUTTON_HEIGHT, Graphics.TOP|Graphics.HCENTER);
			FixedFont.drawString(g, font, keys[10], (MARGIN+BUTTON_WIDTH)*5/2, parent.getHeight()-MARGIN-BUTTON_HEIGHT, Graphics.TOP|Graphics.HCENTER);
		// text on button_0
		if(!keys.equals(KEYS_ABC))
//		if(font.stringWidth(keys[0]) > BUTTON_WIDTH-MARGIN*8)
//			g.drawChar(keys[0].charAt(0), (MARGIN+BUTTON_WIDTH)*3/2, parent.getHeight()-MARGIN-BUTTON_HEIGHT, Graphics.TOP|Graphics.HCENTER);
			FixedFont.drawChar(g, font, keys[0].charAt(0), (MARGIN+BUTTON_WIDTH)*3/2, parent.getHeight()-MARGIN-BUTTON_HEIGHT, Graphics.TOP|Graphics.HCENTER);
		else
//		    g.drawString(keys[0], (MARGIN+BUTTON_WIDTH)*3/2, parent.getHeight()-MARGIN-BUTTON_HEIGHT, Graphics.TOP|Graphics.HCENTER);
			FixedFont.drawString(g, font, keys[0], (MARGIN+BUTTON_WIDTH)*3/2, parent.getHeight()-MARGIN-BUTTON_HEIGHT, Graphics.TOP|Graphics.HCENTER);
		// text on button_1 ~ button_9
		int x = (BUTTON_WIDTH + MARGIN) / 2;
		int y = getYPosition() + MARGIN*2;
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				if (!keys.equals(KEYS_ABC))
//				if (keys.equals(KEYS_KANA))
//				if(font.stringWidth(keys[(i * 3) + j + 1]) > BUTTON_WIDTH-MARGIN*8)
//					g.drawChar(keys[(i * 3) + j + 1].charAt(0), x, y, Graphics.TOP | Graphics.HCENTER);
					FixedFont.drawChar(g, font, keys[(i * 3) + j + 1].charAt(0), x, y, Graphics.TOP | Graphics.HCENTER);
				else
//					g.drawString(keys[(i * 3) + j + 1], x, y, Graphics.TOP | Graphics.HCENTER);
					FixedFont.drawString(g, font, keys[(i * 3) + j + 1], x, y, Graphics.TOP | Graphics.HCENTER);
				x += BUTTON_WIDTH + MARGIN;
			}
			x = (BUTTON_WIDTH + MARGIN) / 2;
			y += BUTTON_HEIGHT + MARGIN;
		}
		int keycode = buttonNumToKeycode(pressed_button);
		if(pressed_button > -1 && keycode > -1) paintSubMenu(keycode, g, font);
	}
	
	private void setButtonArea() {
		int x = MARGIN / 2;
		int y = getYPosition() + MARGIN;
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				int num = i * 4 + j;
				buttonArea[num * 2] = x;
				buttonArea[num * 2 + 1] = y;
				x += BUTTON_WIDTH + MARGIN;
			}
			x = MARGIN / 2;
			y += BUTTON_HEIGHT + MARGIN;
		}		
	}

	private void paintSubMenu(int keycode, Graphics g, Font font) {
		int x = buttonArea[pressed_button*2];
		int y = buttonArea[pressed_button*2+1]-BUTTON_WIDTH-MARGIN;
		g.setColor(ACTIVE_COLOR);
		g.fillRoundRect(x, y, BUTTON_WIDTH, BUTTON_WIDTH, ROUND, ROUND);
		g.setColor(BUTTON_COLOR);
		g.fillRoundRect(x+BUTTON_WIDTH/3, y+BUTTON_WIDTH/3, BUTTON_WIDTH/3+MARGIN/2, BUTTON_WIDTH/3+MARGIN/2, ROUND, ROUND);
		g.setColor(TEXT_COLOR);
//		g.drawChar(keys[keycode].charAt(0), x+BUTTON_WIDTH/2, y+BUTTON_WIDTH/3-MARGIN/2, Graphics.TOP|Graphics.HCENTER);
		FixedFont.drawChar(g, font, keys[keycode].charAt(0), x+BUTTON_WIDTH/2, y+BUTTON_WIDTH/3-MARGIN/2, Graphics.TOP|Graphics.HCENTER);
//		g.setColor(TEXT_SUB_COLOR);
//		g.drawChar(keys[keycode].charAt(1), x+MARGIN/2, y+BUTTON_WIDTH/3-MARGIN/2, Graphics.TOP|Graphics.LEFT);
//		g.drawChar(keys[keycode].charAt(2), x+BUTTON_WIDTH/2, y-MARGIN/3, Graphics.TOP|Graphics.HCENTER);
//		g.drawChar(keys[keycode].charAt(3), x+BUTTON_WIDTH-MARGIN/2, y+BUTTON_WIDTH/3-MARGIN/2, Graphics.TOP|Graphics.RIGHT);
//		g.drawChar(keys[keycode].charAt(4), x+BUTTON_WIDTH/2, y+BUTTON_WIDTH+MARGIN/3, Graphics.BOTTOM|Graphics.HCENTER);
		FixedFont.drawChar(g, font, keys[keycode].charAt(1), x+MARGIN/2, y+BUTTON_WIDTH/3-MARGIN/2, Graphics.TOP|Graphics.LEFT);
		FixedFont.drawChar(g, font, keys[keycode].charAt(2), x+BUTTON_WIDTH/2, y-MARGIN/3, Graphics.TOP|Graphics.HCENTER);
		FixedFont.drawChar(g, font, keys[keycode].charAt(3), x+BUTTON_WIDTH-MARGIN/2, y+BUTTON_WIDTH/3-MARGIN/2, Graphics.TOP|Graphics.RIGHT);
		FixedFont.drawChar(g, font, keys[keycode].charAt(4), x+BUTTON_WIDTH/2, y+BUTTON_WIDTH+MARGIN/3, Graphics.BOTTOM|Graphics.HCENTER);		
	}
	
	private int buttonNumToKeycode(int buttonNum) {
		if (buttonNum < 3) return buttonNum+1;
		else if (buttonNum < 4) return -1;
		else if (buttonNum < 7) return buttonNum;
		else if (buttonNum < 8) return -1;
		else if (buttonNum < 11) return buttonNum-1;
		else if (buttonNum < 13) return -1;
		else if (buttonNum < 14) return 0;
		else if (buttonNum < 15) return 10;
		else return -1;
	}

	public void gestureAction(Object container, GestureInteractiveZone gestureInteractiveZone, GestureEvent gestureEvent) {
		if (!gestureInteractiveZone.equals(gizButtonArea)) return;
		int x = gestureEvent.getStartX();
		int y = gestureEvent.getStartY();
		// System.out.println("gizButtonPressed: (" + x + "," + y + ")");
		for (int i = 0; i < 16; i++) {
			if (x > buttonArea[i * 2] - 1 && x < buttonArea[i * 2] + BUTTON_WIDTH - 1 && y > buttonArea[i * 2 + 1] - 1
					&& y < buttonArea[i * 2 + 1] + BUTTON_HEIGHT - 1) {
				// System.out.println("tapped ButtonArea = " + i);
				int keycode = buttonNumToKeycode(i);
				switch (gestureEvent.getType()) {
					case GestureInteractiveZone.GESTURE_RECOGNITION_START:
						pressed_button = i;
						parent.repaint(buttonArea[i * 2], buttonArea[i * 2 + 1] - BUTTON_WIDTH - MARGIN, BUTTON_WIDTH,
								BUTTON_WIDTH + MARGIN + BUTTON_HEIGHT);
						break;
					case GestureInteractiveZone.GESTURE_RECOGNITION_END:
						pressed_button = -1;
						parent.repaint(buttonArea[i * 2], buttonArea[i * 2 + 1] - BUTTON_WIDTH - MARGIN, BUTTON_WIDTH,
								BUTTON_WIDTH + MARGIN + BUTTON_HEIGHT);
						break;
					case GestureInteractiveZone.GESTURE_TAP:
						if (keycode > -1) {
							parent.keyPressed(keys[keycode].charAt(0));
						} else {
							if (i == 3) {
								parent.keyPressed(KEY_CLEAR);
							} else if (i == 7) {
								if (keys.equals(KEYS_KANA)) keys = KEYS_ABC;
								else if (keys.equals(KEYS_ABC)) keys = KEYS_SYMBOL;
								else if (keys.equals(KEYS_SYMBOL)) keys = KEYS_KANA;
								parent.repaint(getXPosition(), getYPosition(), getWidth(), getHeight());
								return;
							} else if (i == 11) {
								parent.keyPressed(parent.getKeyCode(Canvas.RIGHT));
							} else if (i == 12) {
								parent.keyPressed(KEY_SHIFT);
							} else if (i == 15) {
								parent.keyPressed(parent.getKeyCode(Canvas.FIRE));
							}
							parent.repaint(buttonArea[i * 2], buttonArea[i * 2 + 1] - BUTTON_WIDTH - MARGIN,
									BUTTON_WIDTH, BUTTON_HEIGHT);
						}
						break;
					case GestureInteractiveZone.GESTURE_FLICK:
						if (keycode > -1) {
							float direction = gestureEvent.getFlickDirection();
							if (direction > Math.PI / 4 && direction < Math.PI * 3 / 4) {
								// flick DOWN
								parent.keyPressed(keys[keycode].charAt(4));
							} else if (direction > Math.PI * (-3) / 4 && direction < Math.PI * (-1) / 4) {
								// flick UP
								parent.keyPressed(keys[keycode].charAt(2));
							} else if (direction < Math.PI * (-3) / 4 || direction > Math.PI * 3 / 4) {
								// flick LEFT
								parent.keyPressed(keys[keycode].charAt(1));
							} else if (direction > Math.PI * (-1) / 4 && direction < Math.PI / 4) {
								// flick RIGHT
								parent.keyPressed(keys[keycode].charAt(3));
							}
						}
						break;
//					case GestureInteractiveZone.GESTURE_LONG_PRESS_REPEATED:
//						if (i == 3) {
//							parent.keyPressed(KEY_CLEAR);
//						} else if (i == 11) {
//							parent.keyPressed(parent.getKeyCode(Canvas.RIGHT));
//						} else if (i == 15) {
//							parent.keyPressed(parent.getKeyCode(Canvas.FIRE));
//						}
//						parent.repaint(buttonArea[i * 2], buttonArea[i * 2 + 1] - BUTTON_WIDTH - MARGIN,
//						 BUTTON_WIDTH, BUTTON_HEIGHT);
//						break;
				}
			}
		}
	}

	public void launch() {
        keys = KEYS_KANA;
		keyboard_visibility = true;
		GestureRegistrationManager.register(parent, gizButtonArea);
		parent.repaint(getXPosition(), getYPosition(), getWidth(), getHeight());
	}

	public void dismiss() {
		keyboard_visibility = false;
		GestureRegistrationManager.unregister(parent, gizButtonArea);
		parent.repaint();
	}

	public boolean isVisible() {
		return keyboard_visibility;
	}

	public int getWidth() {
		return parent.getWidth();
	}

	public int getHeight() {
		return BUTTON_HEIGHT * 4 + MARGIN * 6;
	}

	public int getXPosition() {
		return 0;
	}

	public int getYPosition() {
		return parent.getHeight() - getHeight();
	}

}
