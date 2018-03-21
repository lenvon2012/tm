package models.rpt.response;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.utils.NumberUtil;

public class RptUtils {

    public final static Logger log = LoggerFactory.getLogger(RptUtils.class);

    public static class SearchType {
        public final static int SEARCH = 0;
        public final static int CAT = 1;
        public final static int NOSEARCH = 2;
        public final static int SUMMARY = 3;
        public final static int SEARCH_CAT_NOSEARCH = 4;
    }

    public static int getSearchTypeId(String searchType) {
        if (searchType.equals("0") || searchType.equals("SEARCH")) {
            return SearchType.SEARCH;
        } else if (searchType.equals("1") || searchType.equals("CAT")) {
            return SearchType.CAT;
        } else if (searchType.equals("2") || searchType.equals("NOSEARCH")) {
            return SearchType.NOSEARCH;
        } else if (searchType.equals("SUMMARY")) {
            return SearchType.SUMMARY;
        }
        return -1;
    }

    public static String getSearchTypeStr(int searchTypeId) {
        String searchTypeStr = StringUtils.EMPTY;
        switch (searchTypeId) {
        case 0:
            searchTypeStr = "SEARCH";
            break;
        case 1:
            searchTypeStr = "CAT";
            break;
        case 2:
            searchTypeStr = "NOSEARCH";
            break;
        case 3:
            searchTypeStr = "SUMMARY";
            break;
        case 4:
            searchTypeStr = "SEARCH,CAT,NOSEARCH";
            break;

        default:
            searchTypeStr = "SUMMARY";
            break;
        }
        return searchTypeStr;
    }

    public static class Source {
        public final static int IN_SITE = 1;
        public final static int OUT_SITE = 2;
        public final static int INOUT_SITE = 3;
        public final static int SUMMARY = 4;
    }

    public static int getSourceId(String source) {
        if (source.equals("1")) {
            return Source.IN_SITE;
        } else if (source.equals("2")) {
            return Source.OUT_SITE;
        } else if (source.equals("SUMMARY")) {
            return Source.SUMMARY;
        } else if (source.equals("1,2,4,5")) {
            return Source.SUMMARY;
        }
        return -1;
    }

    public static String getSourceStr(int sourceId) {
        String sourceStr = StringUtils.EMPTY;
        switch (sourceId) {
        case 1:
            sourceStr = "1";
            break;
        case 2:
            sourceStr = "2";
            break;
        case 3:
            sourceStr = "1,2";
            //sourceStr = "1,2,4,5";
            break;
        case 4:
            sourceStr = "SUMMARY";
            //sourceStr = "1,2,4,5";
            break;
        default:
            sourceStr = "SUMMARY";
            //sourceStr = "1,2,4,5";
            break;
        }
        return sourceStr;
    }

    public static boolean checkDateTime(Long dateTime) {
        /**
         * 2011-01-01 2030-01-01
         */
        if (dateTime > 1293811200000L && dateTime < 1893427200000L) {
            return true;
        }
        return false;
    }


    public static Long getDateTime(Long dateTime, String dateStr) {
        if (dateTime == null || dateTime == NumberUtil.DEFAULT_LONG) {
            try {
                if (StringUtils.isEmpty(dateStr)) {
                    log.warn("dateStr is empty!!!");
                    return -1L;
                }

                dateStr = StringUtils.trim(dateStr);
                if (dateStr.length() != 10) {
                    log.error("Not Valid :" + dateStr + " with length:" + dateStr);
                }
                return new SimpleDateFormat("yyyy-MM-dd").parse(dateStr).getTime();

            } catch (Exception e) {
//                log.warn(e.getMessage(), e);
                log.error("DateTime str:" + dateStr +", exception:" + e.getMessage());
                try {
                    return new SimpleDateFormat("yyyy-MM-dd").parse(dateStr).getTime();
                } catch (ParseException e1) {
                    log.info(e1.getMessage(), e1);
                    return -1L;
                }
            }
        }

        return dateTime;
    }

}
