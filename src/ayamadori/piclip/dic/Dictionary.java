/**
 * Dictionary.java v0.9.0
 * Created on 2013/09/25
 */
package ayamadori.piclip.dic;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.io.InputStreamReader;
import javax.microedition.rms.RecordStore;

/**
 * 辞書
 * @author owner
 */
public abstract class Dictionary {
	
	// 辞書検索モード用定数
	// 通常予測モード
	public static final int PREDICT_NORMAL = 0;
	// 完全一致変換モード
	public static final int PREDICT_FULL_MATCH = 1;	
	public static final int MAX_CANDIDATE = 35;
	protected static final int SPLIT = 128;
	
	public Dictionary(String name) {
		
		this.name = name;
		// StringBufferを使った基本辞書。最初から50KB確保
		dicData = new StringBuffer(1024*50);//Hold 50kB
		// 辞書index
		dicIndex = -1;
		// 現在の予測候補数
		candLength = 0;
		// No include original word in Candidates
		candDicLine = new int[MAX_CANDIDATE];
		// 辞書検索モード
		searchMode = PREDICT_NORMAL;
		
//		dicstream = new InputStream[SPLIT];

		// initialize recordstore
		try {
//			RecordStore record = RecordStore.openRecordStore(name, true);
			record = RecordStore.openRecordStore(name, true);
			System.out.println("RMSsize=" + record.getSize());
			if (record.getNumRecords() < 128) {
				byte b[] = new byte[1];
				for (int i = 0; i < SPLIT; i++) {
					record.addRecord(b, 0, b.length);
//					System.out.println("RMSsize=" + record.getSize() + ", Next=" + record.getNextRecordID());
				}
			}
//			record.closeRecordStore();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void loadDic(int index) {
//		try {
//			dicstream[index].mark(0);
//			dicstream[index].reset();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		if(dicIndex != index)
			dicIndex = index;
		else
			return;
		// reset dictionary
		if (dicData.length() > 0) dicData.delete(0, dicData.length());
		InputStream is = null;
		byte[] buffer = new byte[8192];
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		baos = new ByteArrayOutputStream();
		try {
			String postfix = Integer.toHexString(index).toUpperCase();
			if (postfix.length() < 2) postfix = "0" + postfix;
			is = getClass().getResourceAsStream("/dic/" + name + "/" + name + postfix);
//			is = dicstream[index];
//			InputStreamReader reader = new InputStreamReader(is, "UTF-8");
//			String temp;
			loadHisory();
//			int[] lineLength = (histories.length > 0)? new int[histories.length] : new int[5000]; 
//			int offset;
//			while ((temp = readLine(reader)) != null) {
//				offset = 
//				dicData.insert(offset, temp);
//			}
			int size;
			while ((size = is.read(buffer)) > -1) {
//			dicstream[index].reset();
//			dicstream[index].mark(0);
//			dicstream[index].reset();
//			while ((size = dicstream[index].read(buffer)) > -1) {
				baos.write(buffer, 0, size);
			}
			if (baos.size() > 0) {
				byte[] data = baos.toByteArray();
				String temp = new String(data, "UTF-8");
				dicLine = new int[temp.length()];
				dicLine[0] = 0;
				int enter = 0, start = 0, i = 1;
				while ((enter = temp.indexOf('\n', start)) > -1 && enter < temp.length() - 1) {
					start = enter + 1;
					dicLine[i] = start;
					i++;
				}
				loadHisory();
				if(histories.length > 0) {
					for(i=0; i<histories.length; i++) {
						start = dicLine[histories[i]];
						int end = temp.indexOf('\n', start);
						char[] tempChars = new char[end - start + 1];
						temp.getChars(start, end + 1, tempChars, 0);
						dicData.append(tempChars);
					}
					enter = 0;
					start = 0;
					i = 1;
					while ((enter = indexOf(dicData, '\n', start)) > -1 && enter < dicData.length() - 1) {
						start = enter + 1;
						dicLine[i] = start;
						i++;
					}
				} else {
					dicData.append(temp);
				}
				temp = null;
//				System.out.println("sbDic:\n" + sbDic.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
				baos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// 予測エンジン
	public abstract void search(String[] cand, String prefix, String yomi, int max);

	// 現在の予測候補数を返す
	public int getCandsSize() {
		return candLength;
	}

	// 文字を平仮名の清音に変換
	protected char toHiraSei(char ch) {
		// 片仮名を平仮名に変換
		if (ch >= '\u30a1' && ch <= '\u30f4') {
			ch -= 96;
		}
		// 濁音、半濁音、小文字を大文字の清音に変換
		if (((ch >= '\u3041' && ch <= '\u304a') || (ch >= '\u3083' && ch <= '\u3088')) && ch % 2 != 0) {
			ch++;
		} else if (ch >= '\u304b' && ch <= '\u3062' && ch % 2 == 0) {
			ch--;
		} else if (ch == '\u3063' || ch == '\u3065') {
			ch = '\u3064';
		} else if (ch == '\u3067' || ch == '\u3069') {
			ch--;
		} else if (ch >= '\u306f' && ch <= '\u307d' && ch % 3 != 0) {
			if (ch % 3 == 2) {
				ch -= 2;
			} else if (ch % 3 == 1) {
				ch--;
			}
		} else if (ch == '\u308e') {
			ch++;
		}
		return ch;
	}
	
	// ---------------------------------------------------------------------------------------------

	// 現在の検索モードを返す
	public int searchMode() {
		return searchMode;
	}

	// ---------------------------------------------------------------------------

	// 検索モードを設定
	public void setSearchMode(int mode) {
		searchMode = (mode == PREDICT_FULL_MATCH) ? PREDICT_FULL_MATCH : PREDICT_NORMAL;
	}

	// -----------------------------------------------------------------------------------

	// 候補を確定したときその候補を最上段に移動＝学習機能
	public void learning(int candNumber) {
		if (candNumber >= candLength || candDicLine[candNumber] == 0) return;
		// initialize histories
		if (histories.length == 0) {
			boolean first = true;
			for (int i = dicLine.length; i > 1; i--) {
				if (dicLine[i - 1] > 0) {
					if(first) {
						histories = new int[i];
						histories[0] = 0;
						first = false;
					}
					histories[i-1] = i-1;
				}
			}
		}
		int temp = histories[candDicLine[candNumber]];
		System.arraycopy(histories, 0, histories, 1, candDicLine[candNumber]);
		histories[0] = temp;
		saveHistory();
//		try {
//			dicstream[dicIndex].reset();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		dicIndex = -1;
	}

	// -----------------------------------------------------------------------------
	// Stringを使うとメモリ消費が激しいのでStringBufferだけで済ませる
	protected int indexOf(StringBuffer sb, char ch, int start) {
		if (start < 0) start = 0;
		while (start < sb.length()) {
			if (sb.charAt(start) == ch) return start;
			start++;
		}
		return -1;
	}
	
	// Stringを使うとメモリ消費が激しいのでStringBufferだけで済ませる
	protected int lastIndexOf(StringBuffer sb, char ch, int start) {
		if (start > sb.length()-1) start = sb.length()-1;
		while (start > -1) {
			if (sb.charAt(start) == ch) return start;
			start--;
		}
		return -1;
	}

	// ---------------------------------------------------------------------
	
	// Load history
	private void loadHisory() {
//		RecordStore record = null;
		try {
//			record = RecordStore.openRecordStore(name, false);
			byte[] byteData = record.getRecord(dicIndex + 1);
			histories = new int[byteData.length / 2];
			for (int i = 0; i < histories.length; i++) {
				histories[i] = ((byteData[i * 2] << 8) & 0xFF00) | (byteData[i * 2 + 1] & 0xFF);
			}
		} catch (Exception e) {
			System.err.println(e);
		} finally {
			try {
//				record.closeRecordStore();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	// Save history
	private void saveHistory() {
		byte[] byteData = new byte[histories.length * 2];
		for (int i = 0; i < histories.length; i++) {
			int x = histories[i];
			// 2 bytes = 65536 is more than dictionary lines
			byteData[i * 2] = (byte) ((x >> 8) & 0xFF);
			byteData[i * 2 + 1] = (byte) (x & 0xFF);
		}
//		RecordStore record = null;
		try {
//			record = RecordStore.openRecordStore(name, true);
			record.setRecord(dicIndex + 1, byteData, 0, byteData.length);
//			System.out.println("SAVE: RMSsize=" + record.getSize());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
//				record.closeRecordStore();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// Reset history
	public void resetHistory() {
		try {
			record.closeRecordStore();
			RecordStore.deleteRecordStore(name);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
//	// Open dictionary
//	public void openDictionary() {
//		try {
//			for (int i=0; i<SPLIT; i++) {
//				String postfix = Integer.toHexString(i).toUpperCase();
//				if (postfix.length() < 2) postfix = "0" + postfix;
//				dicstream[i] = getClass().getResourceAsStream("/dic/" + name + "/" + name + postfix);
//			}
//			record = RecordStore.openRecordStore(name, false);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

//	// Close dictionary
//	public void closeDictionary() {
//		try {
//			for (int i=0; i<SPLIT; i++) {
//				dicstream[i].close();
//			}
//			baos.close();
//			record.closeRecordStore();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

//	// from http://developer.nokia.com/Community/Wiki/Reading_a_text_file_line_by_line_in_Java_ME
//	/**
//     * Reads a single line using the specified reader.
//     * @throws java.io.IOException if an exception occurs when reading the
//     * line
//     */
//    private String readLine(InputStreamReader reader) throws IOException {
//        // Test whether the end of file has been reached. If so, return null.
//        int readChar = reader.read();
//        if (readChar == -1) {
//            return null;
//        }
//        StringBuffer string = new StringBuffer("");
//        // Read until end of file or new line
//        while (readChar != -1 && readChar != '\n') {
//            // Append the read character to the string. Some operating systems
//            // such as Microsoft Windows prepend newline character ('\n') with
//            // carriage return ('\r'). This is part of the newline character
//            // and therefore an exception that should not be appended to the
//            // string.
//            if (readChar != '\r') {
//                string.append((char)readChar);
//            }
//            // Read the next character
//            readChar = reader.read();
//        }
//        return string.toString();
//    }

	protected int[] dicLine;
	protected int candLength;
	protected int[] candDicLine;
	protected StringBuffer dicData;
	protected int searchMode;
	
	private int dicIndex;
	private int histories[];
	private String name;
	
	private RecordStore record;
//	private InputStream[] dicstream;
//	private ByteArrayOutputStream baos;
}
