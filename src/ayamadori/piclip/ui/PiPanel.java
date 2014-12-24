/*
 * PiPanelAP.java v0.8
 *
 * Created on 2013/5/16
 *
 */

package ayamadori.piclip.ui;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.midlet.MIDlet;
import com.nokia.mid.ui.DirectUtils;
import com.nokia.mid.ui.VirtualKeyboard;
import com.nokia.mid.ui.gestures.GestureEvent;
import com.nokia.mid.ui.gestures.GestureInteractiveZone;
import com.nokia.mid.ui.gestures.GestureListener;
import com.nokia.mid.ui.gestures.GestureRegistrationManager;
import ayamadori.piclip.dic.CombinedDictionary;
import ayamadori.piclip.dic.Dictionary;
import ayamadori.piclip.util.FixedFont;
import ayamadori.piclip.util.Util;

/**
 * @author Ayamadori
 * @version 0.8
 */

// 主クラス
public abstract class PiPanel extends Canvas implements GestureListener {

	private MIDlet midlet;

	private Font font;// 標準フォント

//	private int iFontHeight;// フォント高さ
	private int iScrWidth;// 画面幅
	private int iScrHeight;// 画面高さ
//	private int iOffset;// 表示オフセット
	private int iFirstLine;// 本文の表示開始行番号
	private int iMaxLine;// 最大表示行数
//	private int iSpace;// 予測候補表示の間隔

	private StringBuffer sbPre;// 確定文字
	private StringBuffer sbCur;// 編集文字
	private int iPreIdx;// 確定文字中のキャレット位置
	private int iCurIdx;// 編集文字中のキャレット位置

	private CombinedDictionary dictionary;
	private String prefix;
	private String[] cand;// 予測候補

//	private final int ACTIVE_COLOR = 0x29A7CC;
	private final int BACKGROUND_COLOR = 0xF4F4F4;
	private final int BACKGROUND_CANDIDATES_COLOR = 0x191919;
	private final int TEXT_COLOR = 0x585858;
	private final int TEXT_CANDIDATES_COLOR = 0xFFFFFF;
	private final int TEMP_COLOR = 0x4C34FD;
	private final int FONT_HEIGHT = 16;
	private final int MARGIN = 6;
	private final int OFFSET = 10;
	public final int MODE_EDIT = 0;
	public final int MODE_VIEW = 1;
	private FlickKeyboard keyboard;
	private int iBoader;// Boader between TextBox and Candidates
	// Touch area
	private GestureInteractiveZone gizTBArea;
	private GestureInteractiveZone gizCandsArea;
	private int iCandArea[];
	private int iDragRepeat;
	private int mode;

	// -----------------------------------------------------------------------
	// コンストラクタ
	public PiPanel(MIDlet mid) {
		// 基本
		midlet = mid;

		// 入力文字関連
		sbPre = new StringBuffer("");
		sbCur = new StringBuffer("");
		iPreIdx = 0;
		iCurIdx = 0;
		iFirstLine = 0;
		
		mode = MODE_VIEW;

		// Initialize

		iScrWidth = getWidth();
		iScrHeight = getHeight();
//		iBoader = iScrHeight - VirtualKeyboard.getHeight() - (FONT_HEIGHT + MARGIN * 2) * 2;
		iMaxLine = (iBoader - (FONT_HEIGHT + FONT_HEIGHT * 2)) / (FONT_HEIGHT + MARGIN) - 1;// 何行"目"まで表示できるか？

		// Get the CustomKeyboardControl singleton instance
		VirtualKeyboard.hideOpenKeypadCommand(true);
		keyboard = FlickKeyboard.getFlickKeyboardControl(this);
		
		iBoader = iScrHeight - keyboard.getHeight() - (FONT_HEIGHT + MARGIN * 2) * 2;

		iCandArea = new int[4 * 40];
		iDragRepeat = 0;
		gizTBArea = new GestureInteractiveZone(GestureInteractiveZone.GESTURE_TAP | GestureInteractiveZone.GESTURE_DRAG);
		gizTBArea.setRectangle(0, 0, iScrWidth, iBoader);
		GestureRegistrationManager.register(this, gizTBArea);
		gizCandsArea = new GestureInteractiveZone(GestureInteractiveZone.GESTURE_TAP
				| GestureInteractiveZone.GESTURE_FLICK);

		// Set the GestureListener object myGestureListener for the Canvas object myCanvas.
		GestureRegistrationManager.setListener(this, this);
		
		dictionary = new CombinedDictionary(this);
//		dictionary.openDictionary();
		cand = new String[Dictionary.MAX_CANDIDATE];

//		keyboard.launch();
		
		setCommands();
	}
	
	protected void drawBackground(Graphics g) {
		// Fill background
		g.setColor(BACKGROUND_COLOR);
		g.fillRect(0, 0, getWidth(), getHeight());
	}
	
	protected void drawText(Graphics g, int startYPosition) {
		// font in TextBox
		font = DirectUtils.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, FONT_HEIGHT);
		g.setFont(font);

		int curLength = sbCur.length();
		int length = sbPre.length() + curLength;
		g.setColor(TEXT_COLOR);
		int x = OFFSET;
		int y = startYPosition;
		int caretX = x, caretY = y;
		int i = 0;
		int strLine = 0;

		// 本文を描画----------------------------------------------------------------
		while (i < length) {
			int cpl = 0;
			char ch = 0;
			String codePoint = null;
			// 文字を抽出
			if (i < iPreIdx) {
				if((cpl = Util.codePointlength(sbPre, i)) > 1) codePoint = Util.getCodePoint(sbPre, i); //outer BMP
				else ch = sbPre.charAt(i);
//				ch = sbPre.charAt(i);
			} else if (i < iPreIdx + curLength) {
				cpl = Util.codePointlength(sbCur, i - iPreIdx);
				ch = sbCur.charAt(i - iPreIdx);
			} else {
				if((cpl = Util.codePointlength(sbPre, i - curLength)) > 1) codePoint = Util.getCodePoint(sbPre, i - curLength); //outer BMP
				else ch = sbPre.charAt(i - curLength);
//				ch = sbPre.charAt(i - curLength);
			}

			// 文字幅を計算
//			int cWidth = (ch == '\n') ? 0 : font.charWidth(ch);
			int cWidth;
			if(codePoint != null) cWidth = font.stringWidth(codePoint);
//			else if (ch != '\n') cWidth = font.charWidth(ch);
			else if (ch != '\n') cWidth = FixedFont.charWidth(font, ch);
			else cWidth = 0;

			// 画面の幅に収まらないか、改行記号がでたとき改行
			if ((x + cWidth) > (iScrWidth - OFFSET) || ch == '\n') { //codePoint.equals("\n")) {
				strLine++;
				// System.out.println("strLine="+strLine);
				x = OFFSET;
				if (strLine > iFirstLine) {
					y = y + FONT_HEIGHT + MARGIN;
					if (strLine - iFirstLine > iMaxLine + 1) break;
				} else if (iPreIdx + iCurIdx - 1 < i)// こうしないと一部うまくスクロールしない
				{
					// 上スクロール(=表示開始行を1減らす)
					iFirstLine--;
					paint(g);
					return;// これが無いとpaint()の残りの描画処理を行ってしまう？
				}
				if (strLine - iFirstLine > iMaxLine && iPreIdx + iCurIdx > i) {
					// 下スクロール(=表示開始行を1増やす)
					iFirstLine++;
					paint(g);
					return;// これが無いとpaint()の残りの描画処理を行ってしまう？
				}
			}

			// 文字を削除していくとうまくスクロールしないことがあるから、その対処
			if (i == length - 1 && strLine < iFirstLine) {
				// 上スクロール(=表示開始行を1減らす)
				iFirstLine--;
				paint(g);
				return;// これが無いとpaint()の残りの描画処理を行ってしまう？
			}

			// 文字列を描画
			if (strLine >= iFirstLine) {
				// 未確定文字列
				if (i >= iPreIdx && i < iPreIdx + curLength) {
					// underline
					g.setColor(TEMP_COLOR);
					g.drawLine(x, y + FONT_HEIGHT + 4, x + cWidth - 1, y + FONT_HEIGHT + 4);
				}
				
				// キャレット位置
				if (i == iPreIdx + iCurIdx - cpl) {
					caretX = x + cWidth;
					caretY = y;
				}

				// don't draw outside of TextBox
				if (y + (FONT_HEIGHT + MARGIN) > iBoader) break;

				// 文字を描画
				g.setColor(TEXT_COLOR);
				if(codePoint != null) g.drawString(codePoint, x, y, Graphics.TOP | Graphics.LEFT); //outer BMP
//				else if (ch != '\n') g.drawChar(ch, x, y, Graphics.TOP | Graphics.LEFT);
				else if (ch != '\n') FixedFont.drawChar(g, font, ch, x, y, Graphics.TOP | Graphics.LEFT);
//				if (ch != '\n') g.drawChar(ch, x, y, Graphics.TOP | Graphics.LEFT);
			}

			// 文字幅を加算
			x += cWidth;
//			i++;
			i += cpl;
		}

		// キャレット表示-----------------------------------------------------------
		if(sbCur.length() > 0 || sbPre.length() > 0 || mode == MODE_EDIT) {	
			g.setColor(TEXT_COLOR);
			g.drawLine(caretX, caretY, caretX, caretY + FONT_HEIGHT + 2);		
		}
	
		// 前ページあり表示-----------------------------------------------------------
		if (iFirstLine > 0) {
			int tempY = startYPosition - OFFSET;
			g.setColor(0xD0D0D0);
			g.drawLine(0, tempY, iScrWidth, tempY);
			g.setColor(0xDDDDDD);
			g.drawLine(0, tempY + 1, iScrWidth, tempY + 1);
			g.setColor(0xE7E7E7);
			g.drawLine(0, tempY + 2, iScrWidth, tempY + 2);
			g.setColor(0xEEEEEE);
			g.drawLine(0, tempY + 3, iScrWidth, tempY + 3);
		}
	}

//	protected int getCount() {
//		return sbPre.length() + sbCur.length();
//	}
    
    protected void drawCands(Graphics g) {
		// Fill background of candidates
		g.setColor(BACKGROUND_CANDIDATES_COLOR);
		g.fillRect(0, iBoader, iScrWidth, iScrHeight - iBoader);
		
		// 予測候補表示------------------------------------------------------------
		int i, x, y;
		// reset tap area
		for (i = 0; i < iCandArea.length; i++)
			iCandArea[i] = 0;

		// font of Candidates
		font = DirectUtils.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, FONT_HEIGHT);
		g.setFont(font);
		g.setColor(TEXT_CANDIDATES_COLOR);
		
		i = 0;
		x = 0;
		y = iBoader;
		while (i < dictionary.getCandsSize()) {
			String temp;
			if (cand[i].equals("\n")) {
				// temp = "\u23CE";
				temp = "LF";
			} else if (cand[i].equals(" ")) {
				// temp = "\u2423";
				temp = "SP";
			} else if (cand[i].equals("\u3000")) {
				temp = "[SP]";
			} else {
				temp = cand[i];
			}
//			int candWidth = font.stringWidth(temp) + MARGIN * 2;
			int candWidth = FixedFont.stringWidth(font, temp) + MARGIN * 2;
			if (candWidth < FONT_HEIGHT * 3) candWidth = FONT_HEIGHT * 3;

			// 画面の幅に収まらないとき改行。候補文字列が画面幅より長いときはそのまま表示
			if (x + candWidth > iScrWidth && candWidth <= iScrWidth) {
				x = 0;
				y += FONT_HEIGHT + MARGIN * 2;
				if (y > (iScrHeight - (FONT_HEIGHT + MARGIN * 2))) break;
			}

//			g.drawString(temp, x + candWidth / 2, y + MARGIN / 2, Graphics.TOP | Graphics.HCENTER);
			FixedFont.drawString(g, font, temp, x + candWidth / 2, y + MARGIN / 2, Graphics.TOP | Graphics.HCENTER);
			iCandArea[i * 3] = x;
			iCandArea[i * 3 + 1] = y;
			iCandArea[i * 3 + 2] = candWidth;

			x += candWidth;
			i++;
		}
	}

    protected void drawKeyboard(Graphics g) {
    	keyboard.drawFlickKeyboard(g);
	}
    
	// -----------------------------------------------------------------------

	// キーイベント
	protected void keyPressed(int keyCode) {
		if (keyCode == FlickKeyboard.KEY_SHIFT) {
			if (sbCur.length() < 1) return;
			char ch = sbCur.charAt(iCurIdx - 1);
			int kana = (ch >= '\u30a1' && ch <= '\u30fe') ? 96 : 0;// かな＞カナ変換

			// 大文字、小文字、濁音、半濁音、清音の相互変換
			if ((ch >= '\u3041' + kana && ch <= '\u3062' + kana)// 'あ'～'ぢ'
					|| (ch >= '\u3083' + kana && ch <= '\u3088' + kana))// 'ゃ'～'よ'
			{
				ch = (char) ((ch % 2 == 0) ? ch - 1 : ch + 1);
			} else if (ch == '\u3064' + kana)// 'つ'
			{
				ch--;
			} else if (ch == '\u3063' + kana)// 'っ'
			{
				ch += 2;
			} else if (ch == '\u3065' + kana)// 'づ'
			{
				ch--;
			} else if ((ch >= '\u3066' + kana && ch <= '\u3069' + kana)// 'て'～'ど'
					|| (ch >= '\u308e' + kana && ch <= '\u308f' + kana))// 'ゎ'～'わ'
			{
				ch = (char) ((ch % 2 == 0) ? ch + 1 : ch - 1);
			} else if (ch >= '\u306f' + kana && ch <= '\u307d' + kana)// 'は'～'ぽ'
			{
				ch = (char) ((ch % 3 == 2) ? ch - 2 : ch + 1);
			} else if (ch >= 'a' && ch <= 'z') {
				ch -= 32;
			} else if (ch >= 'A' && ch <= 'Z') {
				ch += 32;
			}
//			} else if (ch >= '\uff41' && ch <= '\uff5a')// 全角英数小文字
//			{
//				ch -= 32;
//			} else if (ch >= '\uff21' && ch <= '\uff3a')// 全角英数大文字
//			{
//				ch += 32;
//			}
			sbCur.setCharAt(iCurIdx - 1, ch);
			dictionary.search(cand, prefix, sbCur.toString());
//			dictionary.search(prefix, sbCur.toString());
		} else if (keyCode == FlickKeyboard.KEY_CLEAR) {
			deleteChar();
			// 完全一致モードなら通常予測に戻す
			if (dictionary.searchMode() == Dictionary.PREDICT_FULL_MATCH) {
				dictionary.setSearchMode(Dictionary.PREDICT_NORMAL);
				changeToFullMatchCommand();
			}
			dictionary.search(cand, prefix, sbCur.toString());
//			dictionary.search(prefix, sbCur.toString());
		} else if (keyCode == getKeyCode(RIGHT)) {
			if (sbCur.length() > 0) {
				if (iCurIdx < sbCur.length()) {
					// キャレット右移動
					iCurIdx++;
				} else {
					iCurIdx = 0;
				}
				dictionary.search(cand, prefix, sbCur.toString().substring(0, iCurIdx));
//				dictionary.search(prefix, sbCur.toString().substring(0, iCurIdx));
			} else {
				if (iPreIdx == sbPre.length()) sbPre.append(" ");
				iPreIdx += Util.codePointlength(sbPre, iPreIdx);
//				iPreIdx++;
			}
		} else if (keyCode == getKeyCode(FIRE)) {
			if (sbCur.length() > 0) {
				pressFire(sbCur.toString().substring(0, iCurIdx), -1);
			}
			else {
				sbPre.insert(iPreIdx, "\n");
				iPreIdx++;
			}
		} else {
			sbCur.insert(iCurIdx, (char) keyCode);
			iCurIdx++;
			dictionary.search(cand, prefix, sbCur.toString().substring(0, iCurIdx));
//			dictionary.search(prefix, sbCur.toString().substring(0, iCurIdx));
		}
		repaint();
	}
	
	protected abstract void changeToFullMatchCommand();
	// ------------------------------------------------------------------------
	
	// かな<>カナ、大文字<>小文字相互変換
		private char convertChar(char ch) {
			// かな＞カナ変換
			if (ch >= '\u3041' && ch <= '\u3093') ch += 96;
			// カナ＞かな変換
			else if (ch >= '\u30a1' && ch <= '\u30f3') ch -= 96;
			// 小文字＞大文字変換
		    else if (ch >= 'a' && ch <= 'z') ch -= 32;
			// 大文字＞小文字変換
		    else if (ch >= 'A' && ch <= 'Z') ch += 32;
//			// 半角＞全角変換
//			else if (ch >= '\u0021' && ch <= '\u007e') ch += 0xfee0;
//			else if (ch == ' ') ch = '\u3000';
//			// 全角＞半角変換
//			else if (ch >= '\uff01' && ch <= '\uff5e') ch -= 0xfee0;
//			else if (ch == '\u3000') ch = ' ';

			return ch;
		}

	// ------------------------------------------------------------------------

	// ソフトキー操作
	protected abstract void setCommands();

	protected void onChangeCharType() {
		// 入力文字列のかな<>カナ、大文字<>小文字相互変換
		if (sbCur.length() > 0) {
			for (int i = sbCur.length() - 1; i >= 0; i--) {
				sbCur.setCharAt(i, convertChar(sbCur.charAt(i)));
			}
			pressFire(sbCur.toString(), -1);
			repaint();
		}
	}

	protected void onFullMatch() {
		dictionary.setSearchMode(Dictionary.PREDICT_FULL_MATCH);
		dictionary.search(cand, prefix, sbCur.toString());
//		dictionary.search(prefix, sbCur.toString());
		repaint();
	}

	protected void onNormal() {
		dictionary.setSearchMode(Dictionary.PREDICT_NORMAL);
		dictionary.search(cand, prefix, sbCur.toString());
//		dictionary.search(prefix, sbCur.toString());
		// 予測候補は最初に戻す
		repaint();
	}

	protected void onClearDictionary() {
		// Clear dictionary
		dictionary.resetHistory();
		// App exit
		midlet.notifyDestroyed();
	}

	protected void onNewText() {
		sbPre.delete(0, sbPre.length());
		sbCur.delete(0, sbCur.length());
		iPreIdx = 0;
		iCurIdx = 0;
		iFirstLine = 0;
		// 完全一致モードなら通常予測に戻す
		if (dictionary.searchMode() == Dictionary.PREDICT_FULL_MATCH) {
			dictionary.setSearchMode(Dictionary.PREDICT_NORMAL);
			changeToFullMatchCommand();
		}
		dictionary.search(cand, prefix, sbCur.toString());
//		dictionary.search(prefix, sbCur.toString());
	}

	protected void onBack() {
		// Exit edit mode
		if(sbCur.length() > 0) {
			iCurIdx = sbCur.length();
			pressFire(sbCur.toString(), -1);
			iCurIdx = 0;
		}
		
		if(mode == MODE_VIEW) {
			midlet.notifyDestroyed();
		} else {
			mode = MODE_VIEW;
			viewMode();
		}
	}

	protected void onAbout() {
		new About(midlet, this);
	}

	// 1文字削除
	private void deleteChar() {
		if (sbCur.length() > 0) {
			if (iCurIdx > 0) {
				iCurIdx--;
				sbCur.deleteCharAt(iCurIdx);
			}
		} else if (iPreIdx > 0) {
			iPreIdx--;
			int cpl = Util.codePointlength(sbPre, iPreIdx);
			iPreIdx -= (cpl - 1);
//			System.out.println("iPreIdx = " + iPreIdx + ", cpl = " + cpl);
			if(cpl > 1) sbPre.delete(iPreIdx, iPreIdx + cpl + 1);
			else sbPre.deleteCharAt(iPreIdx);
//			sbPre.deleteCharAt(iPreIdx);
			prefix = null;
			if(sbPre.length() < 1) iFirstLine = 0;
		}
	}
	
	public abstract void editMode();
	
	public abstract void viewMode();
	
	protected void setKeyboardVisibility(boolean visibility) {
		if(visibility) keyboard.launch();
		else keyboard.dismiss();
	}
	
	protected void setEditMode() {
		iScrWidth = getWidth();
		iScrHeight = getHeight();
		iBoader = getHeight() - keyboard.getHeight() - (FONT_HEIGHT + MARGIN * 2) * 2;
		iMaxLine = (iBoader - (FONT_HEIGHT + FONT_HEIGHT * 2)) / (FONT_HEIGHT + MARGIN) - 1;// 何行"目"まで表示できるか？
		gizTBArea.setRectangle(0, 0, getWidth(), iBoader);
		gizCandsArea.setRectangle(0, iBoader, getWidth(), iScrHeight - iBoader - keyboard.getHeight());
		GestureRegistrationManager.register(this, gizCandsArea);
	}
	
	protected void setViewMode() {
		iScrWidth = getWidth();
		iScrHeight = getHeight();
		iBoader = getHeight();
		iMaxLine = (iBoader - (FONT_HEIGHT + FONT_HEIGHT * 2)) / (FONT_HEIGHT + MARGIN) - 1;// 何行"目"まで表示できるか？
		iFirstLine = 0;
		gizTBArea.setRectangle(0, 0, getWidth(), iBoader);
		GestureRegistrationManager.unregister(this, gizCandsArea);
	}
	

	// 決定キーを押したときの動作
	public void pressFire(String temp, int index) {
		// 文字列を確定
		sbPre.insert(iPreIdx, temp);
//		iPreIdx += temp.length();
		iPreIdx += temp.toCharArray().length;
		// 学習
		if(index > -1) dictionary.learning(index);
//		sbCur.delete(0, sbCur.length());
		int length = sbCur.length();
		sbCur.delete(0, iCurIdx);
		iCurIdx = length - iCurIdx;
		// 完全一致モードなら通常予測に戻す
		if (dictionary.searchMode() == Dictionary.PREDICT_FULL_MATCH) {
			dictionary.setSearchMode(Dictionary.PREDICT_NORMAL);
			changeToFullMatchCommand();
		}
		prefix = temp;
		dictionary.search(cand, prefix, sbCur.toString());
//		dictionary.search(prefix, sbCur.toString());
	}

	// Touch action
	public void gestureAction(Object container, GestureInteractiveZone gestureZone, GestureEvent gestureEvent) {
		if (gestureZone.equals(gizTBArea)) {
			// System.out.println("gizTBPressed: ("+gestureEvent.getStartX()+","+gestureEvent.getStartY()+")");
			switch (gestureEvent.getType()) {
				case GestureInteractiveZone.GESTURE_TAP:
					if(mode == MODE_EDIT) {
						iCurIdx = sbCur.length();
						if (sbCur.length() > 0) pressFire(sbCur.toString(), -1);
						iCurIdx = 0;
						mode = MODE_VIEW;
						viewMode();
					} else {
						mode = MODE_EDIT;
						editMode();
					}
					break;
				case GestureInteractiveZone.GESTURE_DRAG:
					int distanceX = gestureEvent.getDragDistanceX();
					int distanceY = gestureEvent.getDragDistanceY();

					// MAX fps = 30 msec/frame
					// System.out.println("MAX fps = " + System.getProperty("com.nokia.mid.ui.frameanimator.fps"));
					// MAX pps = 1500
					// System.out.println("MAX pps = " + System.getProperty("com.nokia.mid.ui.frameanimator.pps"));

					// Minimum sampling interval = about 30 msec
					// -> Respond on only 1/4 Drag event
					if (iDragRepeat < 3) {
						iDragRepeat++;
						return;
					} else {
						iDragRepeat = 0;
					}

					if (distanceY == 0) {
						// RIGHT or LEFT
						if (distanceX > 0) {
							// Drag RIGHT
							// System.out.println("Drag RIGHT");
							if (sbCur.length() > 0) {
								if (iCurIdx < sbCur.length()) {
									// キャレット右移動
									iCurIdx++;
								} else {
									iCurIdx = 0;
								}
								dictionary.search(cand, prefix, sbCur.toString().substring(0, iCurIdx));
//								dictionary.search(prefix, sbCur.toString().substring(0, iCurIdx));
							} else {
								if (iPreIdx < sbPre.length()) {
									// キャレット右移動
									iPreIdx += Util.codePointlength(sbPre, iPreIdx);
//									iPreIdx++;
								} else {
									iPreIdx = 0;
								}
							}
						} else {
							// Drag LEFT
							// System.out.println("Drag LEFT");
							if (sbCur.length() > 0) {
								if (iCurIdx > 0) {
									// キャレット右移動
									iCurIdx--;
								} else {
									iCurIdx = sbCur.length();
								}
								dictionary.search(cand, prefix, sbCur.toString().substring(0, iCurIdx));
//								dictionary.search(prefix, sbCur.toString().substring(0, iCurIdx));
							} else {
								if (iPreIdx > 0) {
									// キャレット左移動									
									iPreIdx--;
									iPreIdx -= (Util.codePointlength(sbPre, iPreIdx) - 1);
								} else {
									iPreIdx = sbPre.length();
								}
							}
						}
					} else if (distanceX == 0) {
						// UP or DOWN

						// Respond on normal screen only
						if (iScrHeight == 320) return;

						if (distanceY < 0) {
							// Drag UP
							// System.out.println("Drag UP");

							// キャレット上移動
							int wU = iScrWidth - OFFSET * 2;
							int temp = iPreIdx - 1;
							char ch = 0;
							int cpl;
							while (wU >= 0) {
								if (temp < 0) {
									temp = iPreIdx - 2;
									break;
								}
//								char ch = sbPre.charAt(temp);							
								String codePoint = null;
								if((cpl = Util.codePointlength(sbPre, temp)) > 1) codePoint = Util.getCodePoint(sbPre, temp); //outer BMP
								else ch = sbPre.charAt(temp);
								if (ch == '\n')// 途中に改行があればキャレットをそこに移動
								{
									temp -= 2;
									break;
								}
								if(codePoint != null) wU -= font.stringWidth(codePoint);
//								else wU -= font.charWidth(ch);
								else wU -= FixedFont.charWidth(font, ch);
								temp -= cpl;
//								wU -= font.charWidth(ch);
//								temp--;
							}
							iPreIdx = temp + 2;

						} else {
							// Drag DOWN
							// System.out.println("Drag DOWN");

							int wD = iScrWidth - OFFSET * 2;
							int temp = iPreIdx;
							char ch = 0;
							int cpl;
							
							// 現在改行位置にいればその次の文字に移動
							if (temp < sbPre.length() && sbPre.charAt(iPreIdx) == '\n') {
								temp++;
							}
							// キャレット下移動
							while (wD >= 0) {
								if (temp > sbPre.length() - 1) {
									temp = iPreIdx + 1;
									break;
								}
//								char ch = sbPre.charAt(temp);
								String codePoint = null;
								if((cpl = Util.codePointlength(sbPre, temp)) > 1) codePoint = Util.getCodePoint(sbPre, temp); //outer BMP
								else ch = sbPre.charAt(temp);
								if (ch == '\n')// 途中に改行があればキャレットをそこに移動
								{
									temp++;
									break;
								}
								if(codePoint != null) wD -= font.stringWidth(codePoint);
//								else wD -= font.charWidth(ch);
								else wD -= FixedFont.charWidth(font, ch);
								temp += cpl;
//								wD -= font.charWidth(ch);
//								temp++;
							}
							iPreIdx = temp - 1;
						}
					}
					repaint();
					break;
			}
		} else if (gestureZone.equals(gizCandsArea)) {
			switch (gestureEvent.getType()) {
				case GestureInteractiveZone.GESTURE_TAP:

					int x = gestureEvent.getStartX();
					int y = gestureEvent.getStartY();
					// If the tap point is back of Virtual Keyboard, no respond
					if (keyboard.isVisible() && y > iScrHeight - keyboard.getHeight()) return;
					for (int i = 0; i < 40; i++) {
						if ((x > iCandArea[i * 3] - 1) && (x < iCandArea[i * 3] + iCandArea[i * 3 + 2] - 1)
								&& (y > iCandArea[i * 3 + 1] - 1)
								&& (y < iCandArea[i * 3 + 1] + (FONT_HEIGHT + MARGIN * 2) - 1)) {
							pressFire(cand[i], i);
							break;
						}
					}
					if (!keyboard.isVisible()) keyboard.launch();
					gizCandsArea.setRectangle(0, iBoader, getWidth(), iScrHeight - iBoader - keyboard.getHeight());
					break;

				case GestureInteractiveZone.GESTURE_FLICK:

					float direction = gestureEvent.getFlickDirection();
					if (direction > Math.PI / 3 && direction < Math.PI * 2 / 3) {
						// flick DOWN
						if (gestureEvent.getStartY() < (iScrHeight - keyboard.getHeight()) && keyboard.isVisible()) {						
						    keyboard.dismiss();
						    gizCandsArea.setRectangle(0, iBoader, getWidth(), iScrHeight - iBoader);
						}
					} else if (direction > Math.PI * (-2) / 3 && direction < Math.PI * (-1) / 3) {
						// flick UP
						if (!keyboard.isVisible()) {
							keyboard.launch();
							gizCandsArea.setRectangle(0, iBoader, getWidth(), iScrHeight - iBoader - keyboard.getHeight());
						}
					}
					break;
			}
		}
		keyboard.gestureAction(container, gestureZone, gestureEvent);
		repaint();
		System.gc();
	}

	public String getString() {
		return sbPre.toString();
	}
	
	public int getMode() {
		return mode;
	}
}
