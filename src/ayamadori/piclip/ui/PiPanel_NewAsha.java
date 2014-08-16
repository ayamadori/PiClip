/*
 * PiPanelAP.java v0.8
 *
 * Created on 2013/5/16
 *
 */

package ayamadori.piclip.ui;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import ayamadori.piclip.PiClip;
import com.nokia.mid.ui.CategoryBar;
import com.nokia.mid.ui.ElementListener;

/**
 * @author Ayamadori
 * @version 0.9
 */

// 主クラス
public class PiPanel_NewAsha extends PiPanel implements CommandListener, ElementListener {

	private final int OFFSET = 10;
	
	// ソフトキー
	private CategoryBar cbAction;// ActionListに送る
	private Command cmdChgChar;// 平仮名-カタカナ，全角-半角の相互変換
	private Command cmdNormal;// 通常の予測検索
	private Command cmdFullMatch;// 完全一致検索
	private Command cmdBack;// 終了
	private Command cmdClearDic;// Crear dictionary
	private Command cmdAbout;// About

//	private MIDlet midlet;
	private PiClip midlet;
	private HeaderBar header;
	
	private Command cmdUpgrade;// Upgrade = Purchase

	// コンストラクタ
//	public PiPanel_NewAsha(MIDlet mid) {
	public PiPanel_NewAsha(PiClip mid) {
		super(mid);
		setFullScreenMode(true);
//		editMode();
		viewMode();
		midlet = mid;
		header = new HeaderBar(this);		
	}

	protected void setCommands() {
		// ソフトキー取り付け
		Image[] icons = new Image[3];
		try {
			icons[0] = Image.createImage("/categorybar/Node.png");
			icons[1] = Image.createImage("/categorybar/View-Earth.png");
			icons[2] = Image.createImage("/categorybar/Garbage.png");
		} catch (Exception e) {
			e.printStackTrace();
		}
		String[] labels = { "Share", "Browser", "Delete" };
		cbAction = new CategoryBar(icons, null, labels, CategoryBar.ELEMENT_MODE_RELEASE_SELECTED);
		cmdChgChar = new Command("Change Character Type", Command.ITEM, 1);
		cmdNormal = new Command("Normal predict", Command.ITEM, 2);
		cmdFullMatch = new Command("Full match", Command.ITEM, 2);
		cmdBack = new Command("Back", Command.BACK, 9);
		cmdClearDic = new Command("Clear Dictionary (App exit)", Command.ITEM, 3);
		cmdAbout = new Command("About", Command.ITEM, 5);
		cmdUpgrade = new Command("Upgrade", Command.ITEM, 4);
		
		addCommand(cmdBack);

		cbAction.setElementListener(this);
		cbAction.setVisibility(false);
		
		setCommandListener(this);
		
	}
	
	public void editMode() {
		// Fullscreen
		setFullScreenMode(true);
		setKeyboardVisibility(true);
		cbAction.setVisibility(false);
		setEditMode();
		removeCommand(cmdAbout);
		removeCommand(cmdUpgrade);
		addCommand(cmdChgChar);
		addCommand(cmdFullMatch);
		addCommand(cmdClearDic);
		
		repaint();
	}
	
	public void viewMode() {
		// Normal screen
		setFullScreenMode(false);
		setKeyboardVisibility(false);
		cbAction.setVisibility(true);
		setViewMode();
		removeCommand(cmdChgChar);
		removeCommand(cmdFullMatch);
		removeCommand(cmdNormal);
		removeCommand(cmdClearDic);
		addCommand(cmdAbout);
		
//		if (!midlet.isUpgraded()) {
		if (!PiClip.upgraded) {
			addCommand(cmdUpgrade);
		} else {
			removeCommand(cmdUpgrade);
		}
		
		repaint();
	}
	
	// 描画メソッド
	protected void paint(Graphics g) {
		drawBackground(g);
		header.paint(g);
		if(getMode() == MODE_VIEW && getString().length() == 0) {
			g.setFont(Font.getDefaultFont());
			g.setColor(0x585858);
			g.drawString("Tap to enter text", getWidth()/2, (getHeight()-header.getHeight())/2, Graphics.HCENTER|Graphics.BASELINE);
			if (PiClip.upgraded) {
				g.drawString("To copy text,", getWidth() / 2, header.getHeight() + (getHeight() - header.getHeight()) / 2, Graphics.HCENTER | Graphics.BASELINE);
				g.drawString("SWIPE to RIGHT or LEFT", getWidth() / 2, header.getHeight() + (getHeight() - header.getHeight()) / 2 + 20, Graphics.HCENTER | Graphics.BASELINE);
			} else {
				g.drawString("To enable copy & share,", getWidth() / 2, header.getHeight() + (getHeight() - header.getHeight()) / 2, Graphics.HCENTER | Graphics.BASELINE);
				g.drawString("SWIPE to UP -> [Upgrade]", getWidth() / 2, header.getHeight() + (getHeight() - header.getHeight()) / 2 + 20, Graphics.HCENTER | Graphics.BASELINE);
			}
		}
		drawText(g, header.getHeight() + OFFSET);
		drawCands(g);		
		drawKeyboard(g);
	}

	// ソフトキー操作
	public void commandAction(Command c, Displayable d) {

		if (c == cmdChgChar) {
			onChangeCharType();
		} else if (c == cmdFullMatch) {
			onFullMatch();
			changeToNormalCommand();
		} else if (c == cmdNormal) {
			onNormal();
			removeCommand(cmdNormal);
			addCommand(cmdFullMatch);
		} else if (c == cmdClearDic) {
			onClearDictionary();
		} else if (c == cmdBack) {
			onBack();
		} else if (c == cmdAbout) {
			onAbout();
			cbAction.setVisibility(false);
		}
		else if (c == cmdUpgrade) {
			midlet.purchaceUpgrade();
		}
		// 画面を更新
		repaint();
	}
	
	protected void changeToNormalCommand() {
		removeCommand(cmdFullMatch);
		addCommand(cmdNormal);
	}
	
	public void removeUpgradeCommand() {
		removeCommand(cmdUpgrade);
	}

	/**
	 * Handles CategoryBar events, tells the currently visible CategoryBarView to switch view to whatever item is tapped
	 * @param categoryBar
	 * @param selectedIndex
	 */
	public void notifyElementSelected(CategoryBar categoryBar, int selectedIndex) {
		// Enable copy on PiPanel only
		PiClip.enableCopy = false;
		// Go to ActionList
		ShareMenu al = new ShareMenu(midlet, getString());

		switch (selectedIndex) {
			case 1:
				// Hide CategoryBar
				cbAction.setVisibility(false);
				al.openBrowserMenu(this);
				break;
			case 2:
				onNewText();
				repaint();
				break;
			default:
				if (PiClip.upgraded) {
					PiClip.enableCopy = false;
					al.share();
				} else {
					midlet.purchaceUpgrade();
				}
				break;
		}
	}
	
	// hide CategoryBar when this Canvas show
	protected void showNotify() {
//		super.showNotify();
		if (getHeight() < 320) cbAction.setVisibility(true);
		if(PiClip.upgraded)
			PiClip.enableCopy = true;
	}
	

}
