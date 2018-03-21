package actions.clouddata;

import java.text.NumberFormat;
import java.util.List;

import com.taobao.api.domain.QueryRow;

public class WordInfo {
	
	public String word;
	
	public String pv;
	
	public String click;
	
	public String uv;
	
	public String alipay_winner_num;
	
	public String alipay_trade_num;
	
	public String alipay_trade_amt;
	
	public String alipay_auction_num;
	
	public String thedate;
	
	public WordInfo() {
		super();
	}

	public WordInfo(QueryRow row) {
		if(row != null) {
			List<String> values = row.getValues();
			this.word = values.get(3);
			this.pv = values.get(4);
			this.click = values.get(5);
			this.uv = values.get(6);
			this.alipay_winner_num = values.get(7);
			this.alipay_trade_num = values.get(8);
			this.alipay_trade_amt = values.get(9);
			this.alipay_auction_num = values.get(10);
		}
	}
	
	/*
	 ** thedate, seller_id, word, impression, click, uv, alipay_winner_num, alipay_trade_num, 
	 ** alipay_trade_amt, alipay_auction_num
	 */
	public static WordInfo build(QueryRow row) {
		WordInfo info = new WordInfo();
		if(row != null) {
			List<String> values = row.getValues();
			info.thedate = values.get(0);
			info.word = values.get(2);
			info.pv = values.get(3);
			info.click = values.get(4);
			info.uv = values.get(5);
			info.alipay_winner_num = values.get(6);
			info.alipay_trade_num = values.get(7);
			info.alipay_trade_amt = values.get(8);
			info.alipay_auction_num = values.get(9);
		}
		return info;
	}
	
	public static WordInfo buildWithNoDate(QueryRow row) {
		WordInfo info = new WordInfo();
		if(row != null) {
			List<String> values = row.getValues();
			info.word = values.get(1);
			info.pv = values.get(2);
			info.click = values.get(3);
			info.uv = values.get(4);
			info.alipay_winner_num = values.get(5);
			info.alipay_trade_num = values.get(6);
			info.alipay_trade_amt = values.get(7);
			info.alipay_auction_num = values.get(8);
		}
		return info;
	}
	
	public static WordInfo buildAppWithNoDate(QueryRow row) {
		WordInfo info = new WordInfo();
		if(row != null) {
			List<String> values = row.getValues();
			info.word = values.get(2);
			info.pv = values.get(3);
			info.click = "0";
			info.uv = values.get(4);
			info.alipay_winner_num = values.get(5);
			info.alipay_trade_num = values.get(6);
			info.alipay_trade_amt = values.get(7);
			info.alipay_auction_num = values.get(8);
		}
		return info;
	}
	
    public static String[] wordInfo2String(WordInfo wordInfo){
        String[] wordInfoStr = new String[7];
        
        // 关键词
        wordInfoStr[0] = wordInfo.word;
        // 浏览量
        wordInfoStr[1] = wordInfo.pv;
        // 访客数
        wordInfoStr[2] = wordInfo.uv;
        // 成交人数
        wordInfoStr[3] = wordInfo.alipay_winner_num;
        // 成交件数
        wordInfoStr[4] = wordInfo.alipay_auction_num;
        // 成交金额
        wordInfoStr[5] = String .format("%.2f", Double.valueOf(wordInfo.alipay_trade_amt));
        // 成交转化率
        NumberFormat nf  = NumberFormat.getPercentInstance();
        nf.setMinimumFractionDigits(2);
        double alipayWinnerNum = Double.parseDouble(wordInfo.alipay_winner_num);
        double uvNum = Double.parseDouble(wordInfo.uv);
        wordInfoStr[6] = uvNum == 0D ? "0.00%" : nf.format(alipayWinnerNum / uvNum);
        return wordInfoStr;
    }
    
}
