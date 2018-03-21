package utils;

import java.io.File;
import java.util.List;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import actions.WordsAction;

import com.ciaosir.client.pojo.IWordBase;

public class intervalTran {
	private static final Logger log = LoggerFactory.getLogger(intervalTran.class);

    public static final String TAG = "intervalTran";
	public static String priceTran(long price){
		if (price<31) return "0.01-0.3";
		else if(price<51) return "0.31-0.5";
		else if(price<81) return "0.51-0.8";
		else if(price<111) return "0.81-1.1";
		else return "1.1以上";		
	}
	public static String clickTran(long click){
		if (click<11) return "10以下";
		else if(click<31) return "11-30";
		else if(click<51) return "31-50";
		else if(click<81) return "51-80";
		else if(click<101) return "81-100";
		else return "101以上";		
	}
	public static String competitionTran(long competition){
		if (competition<201) return "200以下";
		else if(competition<501) return "201-500";
		else if(competition<801) return "501-800";
		else if(competition<1201) return "801-1200";
		else if(competition<1601) return "1201-1600";
		else if(competition<2001) return "1601-2000";
		else return "2000以上";		
	}
	public static String pvTran(long pv){
		if (pv<101) return "100以下";
		else if(pv<501) return "101-500";
		else if(pv<2001) return "501-2000";
		else if(pv<5001) return "2001-5000";
		else if(pv<10001) return "5001-10000";
		else return "10000以上";		
	}
	public static String scoreTran(long score){
		if (score<11) return "10以下";
		else if(score<31) return "11-30";
		else if(score<51) return "31-50";
		else if(score<81) return "51-80";
		else if(score<101) return "81-100";
		else return "101以上";		
	}
	public static String scountTran(long scount){
		if (scount<11) return "10以下";
		else if(scount<31) return "11-30";
		else if(scount<51) return "31-50";
		else if(scount<81) return "51-80";
		else if(scount<121) return "81-120";
		else if(scount<161) return "121-160";
		else if(scount<201) return "161-200";
		else return "200以上";		
	}
	public static void createAllExcel(String[] numIid,long userId){
		long numIidLong ;
		WritableWorkbook book = null;
		try {
			book = Workbook.createWorkbook(new File( "word.xls"));
			for (int k=0;k<numIid.length;k++){
			
				numIidLong=Long.parseLong(numIid[k]);
				List<IWordBase> wordBase=WordsAction.buildRecommend(numIidLong,userId);
				
		            WritableSheet sheet = book.createSheet(numIid[k], k);
		            Label label;
		            int j=0;
		        
		            label = new Label(j++, 0, "词组");
		            sheet.addCell(label);
		            label = new Label(j++, 0, "出价(元)");
		            sheet.addCell(label);
		            label = new Label(j++, 0, "质量得分");
		            sheet.addCell(label);
		            label = new Label(j++, 0, "点击数");
		            sheet.addCell(label);
		            label = new Label(j++, 0, "展现量");
		            sheet.addCell(label);
		            label = new Label(j++, 0, "点击率*10000");
		            sheet.addCell(label);
		            label = new Label(j++, 0, "竞争指数");
		            sheet.addCell(label);
		            //label = new Label(j++, 0, "tStrikeFocus");
		            //sheet.addCell(label);
		            //label = new Label(j++, 0, "SearchFocus");
		            //sheet.addCell(label);
			        //label = new Label(j++, 0, "Status");
			        //sheet.addCell(label);
			        //label = new Label(j++, 0, "Cid");
			        //sheet.addCell(label);
			        //label = new Label(j++, 0, "Match");
			        //sheet.addCell(label);
		            for (int i = 0; i < wordBase.size(); i++) {
		            	j=0;
		                label = new Label(j++, i+1, wordBase.get(i).getWord());
		                sheet.addCell(label);
		                label = new Label(j++, i+1, intervalTran.priceTran(wordBase.get(i).getPrice()));
		                sheet.addCell(label);
		                label = new Label(j++, i+1, intervalTran.scoreTran(wordBase.get(i).getScore()));
		                sheet.addCell(label);
		                label = new Label(j++, i+1, intervalTran.clickTran(wordBase.get(i).getClick()));
		                sheet.addCell(label);
		                label = new Label(j++, i+1, intervalTran.pvTran(wordBase.get(i).getPv()));
		                sheet.addCell(label);
		                label = new Label(j++, i+1, intervalTran.scountTran(wordBase.get(i).getScount()));
		                sheet.addCell(label);
		                label = new Label(j++, i+1, intervalTran.competitionTran(wordBase.get(i).getCompetition()));
		                sheet.addCell(label);
		                //label = new Label(j++, i+1, String.valueOf(wordBase.get(i).getStrikeFocus()));
		                //sheet.addCell(label);
		                //label = new Label(j++, i+1, String.valueOf(wordBase.get(i).getSearchFocus()));
		                //sheet.addCell(label);
		                //label = new Label(j++, i+1, String.valueOf(wordBase.get(i).getStatus()));
		                //sheet.addCell(label);
		                //label = new Label(j++, i+1, String.valueOf(wordBase.get(i).getCid()));
		                //sheet.addCell(label);
		                //label = new Label(j++, i+1, String.valueOf(wordBase.get(i).getMatch()));
		                //sheet.addCell(label);
		            }
		        }
			book.write();
	        book.close();
		}catch(Exception e)
		{
			log.error(e.toString());
		}
	}
	
}
