package ayamadori.piclip.dic;

public class Words extends Dictionary {
	
	public Words() {
		super("words");
	}

	public void search(String[] cand, String prefix, String yomi, int max) {
		// No include original word in Candidates
		candLength = 0;
		if (yomi == null) return;
		if (yomi.length() > 0) {
			// load dictionary
			loadDic(toHiraSei(yomi.charAt(0)) % SPLIT);
			if (dicData.length() == 0) return;
			// 検索が遅いので別の方法を試してみる(各行頭から線形検索)
			int lineNum = 0;
			int index = dicLine[lineNum];
			boolean addCand;
			while (candLength < cand.length && index < dicData.length()) {
				// 文字列を比較する
				addCand = true;
				for (int i = 0; i < yomi.length(); i++) {
					if (toHiraSei(yomi.charAt(i)) != dicData.charAt(index + i)) {
						addCand = false;
						break;
					}
				}
				// 完全一致検索
				index += yomi.length();
				if (searchMode == PREDICT_FULL_MATCH && index < dicData.length() && dicData.charAt(index) != ' ') addCand = false;
				if (addCand) {
					int start = indexOf(dicData, ' ', index) + 1;
					int end = indexOf(dicData, '\n', start);
					char[] tempChars = new char[end - start];
					dicData.getChars(start, end, tempChars, 0);
					String temp = new String(tempChars);
					// 重複候補は除く
					if (!temp.equals(yomi.toString())) {
						cand[candLength] = temp;
						// No include original word
						candDicLine[candLength] = lineNum;
						candLength++;
						if(candLength >= max) return;
					}
				}
				lineNum++;
				if ((index = dicLine[lineNum]) == 0) break;
			}
		}
	}

}
