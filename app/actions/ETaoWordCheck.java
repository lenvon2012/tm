package actions;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sun.util.logging.resources.logging;

public class ETaoWordCheck {
	public static boolean isChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
				|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
				|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
			return true;
		}
		return false;
	}

	public static boolean isMessyCode(String strName) {
		Pattern p = Pattern.compile("\\s*|\t*|\r*|\n*");
		Matcher m = p.matcher(strName);
		String after = m.replaceAll("");
		String temp = after.replaceAll("\\p{P}", "");
		char[] ch = temp.trim().toCharArray();
		float chLength = ch.length;
		float count = 0;
		for (int i = 0; i < ch.length; i++) {
			char c = ch[i];
			if (!Character.isLetterOrDigit(c)) {

				if (!isChinese(c)) {
					count = count + 1;
					System.out.print(c);
				}
			}
		}
		float result = count / chLength;
		if (result > 0.4) {
			return true;
		} else {
			return false;
		}

	}

	public static void main(String[] args) {
		try {
			System.out.println(isUTF8(new String("%E6%B0%91%E6%97%8F").getBytes("iso-8859-1")));
			System.out.println(isUTF8(new String("%C5%B7%D6%DE%D5%BE2014%CF%C4").getBytes("iso-8859-1")));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static boolean isUTF8(byte[] rawtext) {
		int score = 0;
		int i, rawtextlen = 0;
		int goodbytes = 0, asciibytes = 0;
		// Maybe also use UTF8 Byte Order Mark: EF BB BF
		// Check to see if characters fit into acceptable ranges
		rawtextlen = rawtext.length;
		for (i = 0; i < rawtextlen; i++) {
			if ((rawtext[i] & (byte) 0x7F) == rawtext[i]) {
				// 最高位是0的ASCII字符
				asciibytes++;
				// Ignore ASCII, can throw off count
			} else if (-64 <= rawtext[i]
					&& rawtext[i] <= -33
					// -0x40~-0x21
					&& // Two bytes
					i + 1 < rawtextlen && -128 <= rawtext[i + 1]
					&& rawtext[i + 1] <= -65) {
				goodbytes += 2;
				i++;
			} else if (-32 <= rawtext[i]
					&& rawtext[i] <= -17
					&& // Three bytes
					i + 2 < rawtextlen && -128 <= rawtext[i + 1]
					&& rawtext[i + 1] <= -65 && -128 <= rawtext[i + 2]
					&& rawtext[i + 2] <= -65) {
				goodbytes += 3;
				i += 2;
			}
		}
		if (asciibytes == rawtextlen) {
			return false;
		}
		score = 100 * goodbytes / (rawtextlen - asciibytes);
		// If not above 98, reduce to zero to prevent coincidental matches
		// Allows for some (few) bad formed sequences
		if (score > 98) {
			return true;
		} else if (score > 95 && goodbytes > 30) {
			return true;
		} else {
			return false;
		}
	}
	
	public static String toGb2312(String str) {
		if (str == null)
			return null;
		String retStr = str;
		byte b[];
		try {
			b = str.getBytes("ISO8859_1");
			for (int i = 0; i < b.length; i++) {
				byte b1 = b[i];
				if (b1 == 63)
					break; // 1
				else if (b1 > 0)
					continue;// 2
				else if (b1 < 0) { // 不可能为0，0为字符串结束符
				// 小于0乱码
					retStr = new String(b, "GB2312");
					break;
				}
			}
		} catch (UnsupportedEncodingException e) {
			// e.printStackTrace();
		}
		return retStr;
	}
}
