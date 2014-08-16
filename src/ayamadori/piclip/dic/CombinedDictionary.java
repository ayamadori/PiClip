package ayamadori.piclip.dic;

import ayamadori.piclip.ui.PiPanel;

public class CombinedDictionary extends Thread{
	
	private Words wordsDic;
	private Phrases phrasesDic;
	private String[] wcand;
	private PiPanel parent;
//	private String prefix;
//	private String yomi;
//	private String[] cand;
//	private String prevPrefix;
//	private String prevYomi;
//	private int mode;
//	private int prevMode;
//	private boolean searching;
//	private Thread th;
 
	public CombinedDictionary(PiPanel parent) {
		super();
		wordsDic = new Words();
		phrasesDic = new Phrases();
		wcand = new String[Dictionary.MAX_CANDIDATE];
//		cand = new String[Dictionary.MAX_CANDIDATE];
//		prefix = "";
//		yomi = "";
//		prevPrefix = "";
//		prevYomi = "";
//		mode = Dictionary.PREDICT_NORMAL;
//		prevMode = Dictionary.PREDICT_NORMAL;
//		searching = false;
		this.parent = parent;
		this.start();
	}
	
//	public synchronized void search(final String prefix, final String yomi) {
//		this.prefix = prefix;
//		this.yomi = yomi;
//		this.prefix = (prefix == null)? "" : prefix;
//		this.yomi = (yomi == null)? "" : yomi;
//		searching = true;
//		System.out.println("prefix="+this.prefix+", yomi="+this.yomi);
//		notifyAll();
//	}
//	
//	public synchronized void run() {
//		while (true) {
//			System.out.println("Looping...");
//			if (yomi.equals(prevYomi) && prefix.equals(prevPrefix) && mode == prevMode) {
//			if (!searching) {
//				try {
////					Thread.sleep(50);
//					this.wait();
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			} else {
////				System.out.println("Search");
//				prevPrefix = prefix;
//				prevYomi = yomi;
//				prevMode = mode;
//				phrasesDic.search(cand, prefix, yomi, cand.length);
//				int pcount = phrasesDic.getCandsSize();
//				wordsDic.search(wcand, prefix, yomi, wcand.length-pcount);
//				int wcount = wordsDic.getCandsSize();
//				if (wcount == 0) {
//					parent.repaint();
////					return;
//					continue;
//				}
//				System.out.println("pcount="+pcount+", wcount="+wcount);
//				System.arraycopy(wcand, 0, cand, pcount, wcount);
////				int count = Dictionary.MAX_CANDIDATE - pcount;
////				if (wcount < count) {
////					System.arraycopy(wcand, 0, cand, pcount, wcount);
////				} else {
////					System.arraycopy(wcand, 0, cand, pcount, count);
////				}
//				parent.setCandidates(cand);
//				parent.repaint();
////				searching = false;
//				
////				try {
////					Thread.sleep(10);
////				} catch (InterruptedException e) {
////					e.printStackTrace();
////				}
//			}
//		}
//	}
	
	public void search(final String[] cand, final String prefix, final String yomi) {
//		if(th != null) th = null;
//		th = new Thread() {
//		new Thread() {
//			public void run() {
//				System.out.println("prefix="+prefix+", yomi="+yomi);
				phrasesDic.search(cand, prefix, yomi, cand.length);
				int pcount = phrasesDic.getCandsSize();
				wordsDic.search(wcand, prefix, yomi, wcand.length-pcount);
				int wcount = wordsDic.getCandsSize();
//				if (wcount == 0) {
//					parent.repaint();
//					return;
//				}
				System.arraycopy(wcand, 0, cand, pcount, wcount);
//				int count = Dictionary.MAX_CANDIDATE - pcount;
//				if (wcount < count) {
//					System.arraycopy(wcand, 0, cand, pcount, wcount);
//				} else {
//					System.arraycopy(wcand, 0, cand, pcount, count);
//				}
				parent.repaint();
//			}
//		};
//		th.start();
//		}.start();
	}
	
    public void learning(int candNumber) {
		if(candNumber < phrasesDic.getCandsSize()) {
			phrasesDic.learning(candNumber);
		} else {
			wordsDic.learning(candNumber - phrasesDic.getCandsSize());
		}
	}
    
    public void resetHistory() {
    	wordsDic.resetHistory();
		phrasesDic.resetHistory();
	}
    
    public int searchMode() {
    	int mode = wordsDic.searchMode();
    	if(mode != phrasesDic.searchMode())
    		phrasesDic.setSearchMode(mode);
		return wordsDic.searchMode();		
	}
    
    public void setSearchMode(int mode) {
		wordsDic.setSearchMode(mode);
		phrasesDic.setSearchMode(mode);
//		this.mode = mode;
	}
    
 	public int getCandsSize() {
 		int size = phrasesDic.getCandsSize() + wordsDic.getCandsSize();
 		if(size > Dictionary.MAX_CANDIDATE) {
 			size = Dictionary.MAX_CANDIDATE;
 		}
 		return size;
 	}
 	
// 	public void openDictionary() {
// 		wordsDic.openDictionary();
// 		phrasesDic.openDictionary();		
//	}
// 	
// 	public void closeDictionary() {
// 		wordsDic.closeDictionary();
//		phrasesDic.closeDictionary();
//	}
 	
}
