package ayamadori.piclip.dic;

public class Phrases extends Dictionary {

//	private String[] tempCand;

	public Phrases() {
		super("phrases");
//		tempCand = new String[MAX_CANDIDATE];
	}

	public void search(String[] cand, String prefix, String yomi, int max) {
		// No include original word in Candidates
		candLength = 0;
		if (prefix == null) return;
		if (prefix.length() > 0) {
			// load dictionary
			loadDic(prefix.charAt(0) % SPLIT);
			if (dicData.length() < prefix.length()) return;
			// 検索が遅いので別の方法を試してみる(各行頭から線形検索)
			int lineNum = 0;
			int index = dicLine[lineNum];
			boolean addCand;
			while (candLength < cand.length && index < dicData.length()) {
				// 一応先頭から全ての文字を比較する
				addCand = true;
				for (int i = 0; i < prefix.length(); i++) {
					if (prefix.charAt(i) != dicData.charAt(index + i)) {
						addCand = false;
						break;
					}
				}
				// プレフィックスは常に完全一致
				index += prefix.length();
				if (index < dicData.length() && dicData.charAt(index) != ' ') addCand = false;
				if (yomi.length() > 0 && addCand) {
					int last;
					if (lineNum < dicLine.length-1 && dicLine[lineNum+1] != 0) {
						last = dicLine[lineNum+1];
					} else {
						last = dicData.length()-1;
					}
					int start = lastIndexOf(dicData, ' ', last) + 1;
					for (int i = 0; i < yomi.length(); i++) {
						if (toHiraSei(yomi.charAt(i)) != dicData.charAt(start + i)) {
							addCand = false;
							break;
						}
					}
					// 完全一致検索
					if (searchMode == PREDICT_FULL_MATCH && dicData.charAt(start + yomi.length()) != '\n') addCand = false;
				}
				if (addCand) {
					int start = indexOf(dicData, ' ', index) + 1;
					int end = indexOf(dicData, ' ', start);
					char[] tempChars = new char[end - start];
					dicData.getChars(start, end, tempChars, 0);
					String temp = new String(tempChars);
					// 重複候補は除く
					if (!temp.equals(prefix.toString())) {
						cand[candLength] = temp;
						// No include original word
						candDicLine[candLength] = lineNum;
						candLength++;
//						if(candLength >= max) return;
					}
				}
				lineNum++;
				if ((index = dicLine[lineNum]) == 0) break;
			}
		}
	}
}
