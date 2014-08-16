package ayamadori.piclip.util;

public class Util {

	// http://www.s-cradle.com/developer/java/DBTestForm.html
	// Shift-JIS -> UTF-8
	/**
	 * URLエンコードします。エンコード時にShift-JISを用います。
	 * @param in エンコードして欲しい文字列
	 * @return エンコードされた文字列
	 */
	public static String URLencode(String in) {
		StringBuffer outBuf = new StringBuffer();
		for (int i = 0; i < in.length(); i++) {
			char temp = in.charAt(i);
			if (('a' <= temp && temp <= 'z')
					|| ('A' <= temp && temp <= 'Z')
					|| ('0' <= temp && temp <= '9')
					|| temp == '.' || temp == '-' || temp == '*' || temp == '_') {
				outBuf.append(temp);
			} else if (temp == ' ') {
				outBuf.append('+');
			} else {
				byte[] bytes;
				try {
					bytes = new String(new char[] { temp }).getBytes("UTF-8");
					for (int j = 0; j < bytes.length; j++) {
						int high = (bytes[j] >>> 4) & 0x0F;
						int low = (bytes[j] & 0x0F);
						outBuf.append('%');
						outBuf.append(Integer.toString(high, 16).toLowerCase());
						outBuf.append(Integer.toString(low, 16).toLowerCase());
					}
				} catch (Exception e) {
				}
			}
		}

		return outBuf.toString();
	}
	
	// count character length in one codePoint
	// http://alpha.mixi.co.jp/2012/10809/
	// http://alpha.mixi.co.jp/2012/10663/
	public static int codePointlength(StringBuffer sb, int index) {
		int i = 1;
		char c = sb.charAt(index);
		if (0xD800 <= c && c <= 0xDBFF) { // High Surrogates
			if(c == 0xD83C) {
		    	c = sb.charAt(index + 1); // Chack Low Surrogates
		    	if(c >= 0xDDE6 && c <= 0xDDFF) i = 4; // Regional Indicator
		    	else i = 2;
		    } else {
		    	i = 2;
		    }
		} else if (0xDC00 <= c && c <= 0xDFFF) { // Low Surrogates		    
			if(c >= 0xDDE6 && c <= 0xDDFF) {
		    	c = sb.charAt(index - 1); // Chack High Surrogates
		    	if(c == 0xD83C) i = 4; // Regional Indicator
		    	else i = 2;
		    } else {
		    	i = 2;
		    }
		}
		
		return i;
	}
	
	// return one codePoint include index
	// http://alpha.mixi.co.jp/2012/10809/
	// http://alpha.mixi.co.jp/2012/10663/
	public static String getCodePoint(StringBuffer sb, int index) {
		int i = 1;
		char[] temp;
		char c = sb.charAt(index);
		if (0xD800 <= c && c <= 0xDBFF) { // High Surrogates
		    if(c == 0xD83C) {
		    	c = sb.charAt(index + 1); // Chack Low Surrogates
		    	if(c >= 0xDDE6 && c <= 0xDDFF) i = 4; // Regional Indicator
		    	else i = 2;
		    } else {
		    	i = 2;
		    }
//		    System.out.println("index= " + index + ", char= " + Integer.toString(sb.charAt(0), 16));
			temp = new char[i];
			for(int j = 0; j < i; j++) {
				temp[j] = sb.charAt(index + j);
			}
		} else if (0xDC00 <= c && c <= 0xDFFF) { // Low Surrogates
			if(c >= 0xDDE6 && c <= 0xDDFF) {
		    	c = sb.charAt(index - 1); // Chack High Surrogates
		    	if(c == 0xD83C) i = 4; // Regional Indicator
		    	else i = 2;
		    } else {
		    	i = 2;
		    }
			temp = new char[i];
			for(int j = 0; j < i; j++) {
				temp[j] = sb.charAt(index - i + 1 + j);
			}
		} else {
			temp = new char[1];
			temp[0] = c;
		}
		
		return new String(temp);
	}

}
