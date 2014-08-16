/*
 * PiClip.java v0.8
 *
 * Created on 2013/5/16
 */

package ayamadori.piclip;

import javax.microedition.io.ConnectionNotFoundException;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;
//import javax.microedition.rms.RecordStore;
//import javax.microedition.rms.RecordStoreException;
//import javax.microedition.rms.RecordStoreNotFoundException;
import ayamadori.piclip.ui.PiPanel_NewAsha;
import ayamadori.piclip.util.PurchaseStore;
import com.nokia.mid.ui.Clipboard;
import com.nokia.payment.NPayException;
import com.nokia.payment.NPayListener;
import com.nokia.payment.NPayManager;
import com.nokia.payment.ProductData;
import com.nokia.payment.PurchaseData;


/**
 * @author Ayamadori
 * @version 0.9
 */

public class PiClip extends MIDlet implements NPayListener {

	public static Display display;
	private PiPanel_NewAsha piPanel;	
//	private long start;
	public static boolean enableCopy;
	public static boolean upgraded;
	
	// In-App-Purchase
	private NPayManager paymentManager;
	private static final int numProducts = 1;
	static final String[] productIds = new String[numProducts];
	private PurchaseStore storeRepo;
	private Alert alert;	

	public PiClip() {
		display = Display.getDisplay(this);
//		start = System.currentTimeMillis();
//		piPanel = new PiPanel_NewAsha(this);
		
//		if (piPanel != null) {
//			System.out.println("Loading time = " + (System.currentTimeMillis()-start));
//			display.setCurrent(piPanel);
//		}
	}

	public void startApp() {
		
		// In-App-Purchase
		try {
			// "Following commented code is only for testing purpose."
			// to re-test the purchased item which is already stored in the record store
			// and to see normal flow next time, first you need to remove the item from record store.
			// to do that, un-comment the following code that removes the purchased item from 
			// record store and after you will see normal flow next time when buying the same item
			
//			try {
//				RecordStore.deleteRecordStore("PiClip_Upgrade");
//			} catch (RecordStoreNotFoundException e) {
//				e.printStackTrace();
//			} catch (RecordStoreException e) {
//				e.printStackTrace();
//			}
			storeRepo = new PurchaseStore("PiClip_Upgrade");
			paymentManager = new NPayManager(this);
			// Need to set Listener even if not use productDataReceived(), purchaseCompleted()
			// -> Show NullPointerException
			paymentManager.setNPayListener(this);
			
			// Check is NIAP enabler library installed in device or not
			if (!paymentManager.isNPayAvailable()) {
				askForMidletInstallation();
				return;
			}

			// populate productIds array
			setProductIds();

			// get localized prices based on the above product ids and price
			// point mapped in the JAD
			// after the below call, prices and other info comes to
			// productDataReceived() method
			paymentManager.getProductData(productIds);
			
		} catch (NPayException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		upgraded = storeRepo.isPurchased(productIds[0]);
		System.out.println("upgraded=" + upgraded);
		
		piPanel = new PiPanel_NewAsha(this);
//		
		if (piPanel != null) {
			display.setCurrent(piPanel);
		}

	}

	public void pauseApp() {
		System.out.println("Paused");
	}

	public void destroyApp(boolean unconditional) {
		System.out.println("Destroyed");
		if (enableCopy) {
			try {
				Clipboard.copyToClipboard(piPanel.getString());
				// System.out.println("SUCCESS: Copied to clipboard");
			} catch (Exception e) {
				System.out.println("FAILED TO COPY: " + e.toString());
			}
		}
		if (piPanel != null) piPanel = null;
	}
	
	public void purchaceUpgrade() {
		try {
			paymentManager.purchaseProduct(productIds[0]);
		} catch (NPayException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isUpgraded() {
//		return storeRepo.isPurchased(productIds[0]);
		return upgraded;
	}
	
	private void askForMidletInstallation() {

		alert = new Alert(
				"Missing component",
				"You need to install Nokia In App Payment support to use this application!",
				null, AlertType.CONFIRMATION);
		final Command cmdInstallEnabler = new Command("Install", Command.OK, 1);
		final Command cmdExit = new Command("Exit", Command.CANCEL, 2);
		alert.addCommand(cmdInstallEnabler);
		alert.addCommand(cmdExit);
		alert.setTimeout(Alert.FOREVER);
		alert.setCommandListener(new CommandListener() {
			public void commandAction(Command c, Displayable d) {
				if (c == cmdInstallEnabler) {
					try {
						paymentManager.launchNPaySetup();
					} catch (IllegalStateException e) {
						e.printStackTrace();
					} catch (ConnectionNotFoundException e) {
						e.printStackTrace();
					}
				}
				if (c == cmdExit) {
					// Kill this midlet
					notifyDestroyed();
				}
			}
		});
		display.setCurrent(alert);
		return;
	}

	private void setProductIds() {
		productIds[0] = "1288794";
//		productIds[0] = "success";
	}

	public void productDataReceived(ProductData[] dataItems) {
		// for updated product purchase info or restore
		
	}

	public void purchaseCompleted(PurchaseData data) {

		if (data.getStatus() == PurchaseData.PURCHASE_SUCCESS
				|| data.getStatus() == PurchaseData.PURCHASE_RESTORE_SUCCESS) {
			storeRepo.store(productIds[0]);
			upgraded = true;
			piPanel.viewMode();
		} else if (data.getStatus() == PurchaseData.PURCHASE_FAILED) {
			
		}

	}

}
