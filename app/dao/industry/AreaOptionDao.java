package dao.industry;

import models.industry.AreaOptionPlay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AreaOptionDao {
	private static final Logger log = LoggerFactory.getLogger(AreaOptionDao.class);

	public static String queryAreaNameById(long areaId) {
		AreaOptionPlay areaPlay = AreaOptionPlay.findByAreaId(areaId);
		//log.error(areaPlay + "-------------------------------------");
		if (areaPlay == null)
			return "";
		else
			return areaPlay.getName();
	}
}
