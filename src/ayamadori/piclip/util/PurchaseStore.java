package ayamadori.piclip.util;

import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;

public class PurchaseStore {
	private RecordStore paymentStore = null;
	private String repositoryName;

	public PurchaseStore(String repositoryName) {
		this.repositoryName = repositoryName;
	}

	public void store(String productID) {
		try {
			paymentStore = RecordStore.openRecordStore(repositoryName, true);
			if (!isRecordExist(productID)) {
				byte[] record = productID.getBytes();
				paymentStore.addRecord(record, 0, record.length);
			}
			paymentStore.closeRecordStore();
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}

	public boolean isPurchased(String productID) {
		boolean purchased = false;
		try {
			paymentStore = RecordStore.openRecordStore(repositoryName, false);
			purchased = isRecordExist(productID);
			paymentStore.closeRecordStore();
		} catch (Exception e) {
//			e.printStackTrace();
		}
		return purchased;
	}

	private boolean isRecordExist(String productID) {
		boolean recordExist = false;
		try {
			if (paymentStore != null) {
				RecordEnumeration paymentEnum = paymentStore.enumerateRecords(
						null, null, false);
				while (paymentEnum.hasNextElement()) {
					String element = new String(paymentEnum.nextRecord());
					if (element.equals(productID)) {
						recordExist = true;
						break;
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();

		}
		return recordExist;
	}
}