package job.watermarker;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import play.jobs.Job;

import com.ciaosir.client.utils.NumberUtil;

import controllers.WaterMarker;

//@Every("24h")
public class ClearOutDateFilesJob extends Job {
	public void doJob() {
		File targetFolder = WaterMarker.WaterMarkerOutDir;
		File[] childFolders = targetFolder.listFiles();
		if (childFolders == null || childFolders.length == 0)
			return;
		List<File> deleteFolderList = new ArrayList<File>();
		
		for (int i = 0; i < childFolders.length; i++) {
			String folderName = childFolders[i].getName();
			int dayNum = NumberUtil.parserInt(folderName, 0);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	        String dayStr = sdf.format(new Date());
	        int nowDayNum = NumberUtil.parserInt(dayStr, 0);
	        
	        if (nowDayNum > dayNum + 1)
	        	deleteFolderList.add(childFolders[i]);
		}
		
		for (File deleteFolder : deleteFolderList) {
			deleteDirectory(deleteFolder);
		}
	}
	
	/**  
	 * 删除单个文件  
	 * @param   sPath    被删除文件的文件名  
	 * @return 单个文件删除成功返回true，否则返回false  
	 */  
	public static boolean deleteFile(File file) {   
	    // 路径为文件且不为空则进行删除   
	    if (file.isFile() && file.exists()) {   
	        return file.delete();   
	    }   
	    return false;   
	}  
	
	/**  
	 * 删除目录（文件夹）以及目录下的文件  
	 * @param   sPath 被删除目录的文件路径  
	 * @return  目录删除成功返回true，否则返回false  
	 */  
	public static boolean deleteDirectory(File folder) {   

	    //如果dir对应的文件不存在，或者不是一个目录，则退出   
	    if (!folder.exists() || !folder.isDirectory()) {   
	        return false;   
	    }   
	    boolean flag = true;   
	    //删除文件夹下的所有文件(包括子目录)   
	    File[] files = folder.listFiles();   
	    for (int i = 0; i < files.length; i++) {   
	        //删除子文件   
	        if (files[i].isFile()) {   
	            flag = deleteFile(files[i]);   
	            if (!flag) 
	            	break;   
	        } //删除子目录   
	        else {   
	            flag = deleteDirectory(files[i]);   
	            if (!flag) 
	            	break;   
	        }   
	    }   
	    if (!flag) 
	    	return false;   
	    //删除当前目录   
	    if (folder.delete()) {   
	        return true;   
	    } else {   
	        return false;   
	    }   
	}  
}
