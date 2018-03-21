
package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import job.subscribe.SubscribeInfo;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.mvc.Scope.Session;

import com.ciaosir.client.utils.NumberUtil;

public class ExcelUtil {

    private static final Logger log = LoggerFactory.getLogger(ExcelUtil.class);

    public static final String TAG = "ExcelUtil";

    private static InputStream input;

    private static Workbook wb = null;

    private static List<String[]> dataList = new ArrayList<String[]>(100);

    private static int sheetLine = 60000;

    private static int pageSize = 100;

    /**
     * 初始化
     * 
     * @param file
     * @return
     */
    public static void init(File file) {
        try {
            InputStream input = new FileInputStream(file);
            wb = WorkbookFactory.create(input);
            if (!dataList.isEmpty()) {// 如果不为空则清空
                dataList.clear();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 使用文件名初始化
     * 
     * @param fileName
     */
    public static void init(String fileName) {
        try {
            InputStream input = new FileInputStream(fileName);
            wb = WorkbookFactory.create(input);
            if (!dataList.isEmpty()) {// 如果不为空则清空
                dataList.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 重新设置input
     * 
     * @param file
     */
    public static void reset(File file) {
        try {
            InputStream input = new FileInputStream(file);
            wb = WorkbookFactory.create(input);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 使用文件名初始化
     * 
     * @param fileName
     */
    public static void reset(String fileName) {
        try {
            InputStream input = new FileInputStream(fileName);
            wb = WorkbookFactory.create(input);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 清空资源
     */
    public static void close() {
        if (input != null) {
            try {
                input.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (wb != null) {
            wb = null;
        }
    }

    /**
     * 读取xls|xlsx格式的excel文件，返回一个新的List
     * 
     * @param sheetIndex
     * @param isNew
     *            是否是新建一个列表
     * @param start
     *            读取的开始下标
     * @param end
     *            读取的结束下标
     * @return
     */
    public static List<String[]> getData(int sheetIndex, boolean isNew, int start, int end) {
        List<String[]> dataList = ExcelUtil.dataList;
        if (isNew) {// 是否产生新的列表
            dataList = new ArrayList<String[]>(100);
        }

        // 列数
        int columnNum = 0;

        Sheet sheet = wb.getSheetAt(sheetIndex);
        if (sheet.getRow(0) != null) {
            columnNum = sheet.getRow(0).getLastCellNum() - sheet.getRow(0).getFirstCellNum();// 列数
        }

        if (columnNum > 0) {
            int cnt = 0, rowId = 0;
            for (Row row : sheet) {
                if (rowId >= start && rowId <= end) {// 如果在规定的行内
                    String[] singleRow = new String[columnNum];
                    for (int i = 0; i < columnNum; i++) {
                        Cell cell = row.getCell(i, Row.CREATE_NULL_AS_BLANK);
                        switch (cell.getCellType()) {
                            case Cell.CELL_TYPE_BLANK:// 空白
                                singleRow[i] = "";
                                break;
                            case Cell.CELL_TYPE_BOOLEAN:// 布尔值
                                singleRow[i] = Boolean.toString(cell.getBooleanCellValue());
                                break;
                            case Cell.CELL_TYPE_NUMERIC:// 数值
                                if (DateUtil.isCellDateFormatted(cell)) {
                                    singleRow[i] = String.valueOf(cell.getDateCellValue());
                                } else {
                                    cell.setCellType(Cell.CELL_TYPE_STRING);
                                    String temp = cell.getStringCellValue();
                                    // 判断是否包含小数点，如果不含小数点，则以字符串读取，如果含小数点，则转换为Double类型的字符串
                                    if (temp.indexOf(".") > -1) {
                                        singleRow[i] = String.valueOf(new Double(temp)).trim();
                                    } else {
                                        singleRow[i] = temp.trim();
                                    }
                                }
                                break;
                            case Cell.CELL_TYPE_STRING:// 字符串
                                singleRow[i] = cell.getStringCellValue().trim();
                                break;
                            case Cell.CELL_TYPE_ERROR:// 错误
                                singleRow[i] = "";
                                break;
                            case Cell.CELL_TYPE_FORMULA:// 公式
                                cell.setCellType(Cell.CELL_TYPE_STRING);
                                singleRow[i] = cell.getStringCellValue();
                                if (singleRow[i] != null) {
                                    singleRow[i] = singleRow[i].replaceAll("#N/A", "").trim();
                                }
                                break;
                            default:
                                singleRow[i] = "";
                                break;
                        }
                    }
                    cnt = 0;
                    for (int i = 0; i < columnNum; i++) {
                        if ("".equals(singleRow[i]) || singleRow[i].length() < 1)
                            cnt++;
                    }
                    if (cnt != columnNum)
                        dataList.add(singleRow);
                }
                rowId++;
            }
        }

        if (dataList.size() > 0)// 去掉第一行，因为上面用的start和end都算在内，所以start需要删除
            dataList.remove(0);// 在这里就去掉标题，免得在后面处理
        return dataList;
    }

    /**
     * 读取某个导入的Excel，按页
     * 
     * @return
     */
    public static List<String[]> readExcelFile(Session session, String filename, int pageNo) {
        ExcelUtil.reset(filename);// 用reset
        // 起始页码
        int pageStart = (pageNo - 1) * pageSize;// 有一行标题，会在取出的时候去掉，注意取的时候包括头不包括尾
        // 终止页码
        int pageEnd = pageStart + pageSize;

        // 起始表单
        int sheetIdxStart = pageStart / sheetLine;
        // 终止表单
        int sheetIdxEnd = pageEnd / sheetLine;

        // 存放结果
        List<String[]> infos = new ArrayList<String[]>(1000);
        if (sheetIdxStart == sheetIdxEnd) {
            infos = ExcelUtil.getData(sheetIdxStart, true, pageStart, pageEnd);// 用new
        } else {
            List<String[]> customersStart = ExcelUtil.getData(sheetIdxStart, true, pageStart, sheetLine);// 用new
            List<String[]> customersEnd = ExcelUtil.getData(sheetIdxEnd, true, 0, pageEnd - sheetLine);// 用new

            infos.addAll(customersStart);
            infos.addAll(customersEnd);
        }

        ExcelUtil.close();// 清空资源
        return infos;
    }

    /**
     * 写Excel 文件(已经分表)
     * 
     * @param records
     *            记录数据
     * @param fields
     *            记录对应的标题
     * @param sheetName
     *            需要保存的表单名称
     * @param fileName
     *            存储的文件名
     */
    public static void writeToExcel(List<String[]> records, String fields, String sheetName, String fileName) {

        sheetName = removeSlash(sheetName);

        String[] fieldsArray = fields.split(",");
        HSSFWorkbook wb = new HSSFWorkbook();
        // 记录数
        long recordsNum = records.size();
        // 表单数量
        long sheetNum = recordsNum / sheetLine;

        for (int k = 0; k < sheetNum; k++) {
            String sheetNameIdx = sheetName.concat(String.valueOf(k));
            HSSFSheet sheet = wb.createSheet(sheetNameIdx);
            CreationHelper createHelper = wb.getCreationHelper();

            for (int i = 0; i < fieldsArray.length; i++) {
                sheet.setColumnWidth(i, (int) 35.7 * 150);
            }

            Cell cell;
            Row row;

            row = sheet.createRow((short) 0);
            // 写表单头
            for (int i = 0; i < fieldsArray.length; i++) {
                cell = row.createCell(i);
                cell.setCellValue(createHelper.createRichTextString(fieldsArray[i]));
            }

            try {
                // 写数据
                for (int i = 0; i < 60000; i++) {
                    int idxI = k * 60000 + i;
                    /**
                     * short:超过数据范围
                     */
                    row = sheet.createRow(i);
                    for (int j = 0; j < fieldsArray.length; j++) {
                        cell = row.createCell(j);// 创建一列
                        if (records.get(idxI).length > j) {// 有足够多的数据
                            cell.setCellValue(records.get(idxI)[j]);
                        } else {
                            cell.setCellValue("");
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 剩余的记录写到一个表单
        if (sheetLine * sheetNum < recordsNum + 1) {
            String sheetNameIdx = sheetName.concat(String.valueOf(sheetNum + 1));
            HSSFSheet sheet = wb.createSheet(sheetNameIdx);
            CreationHelper createHelper = wb.getCreationHelper();

            for (int i = 0; i < fieldsArray.length; i++) {
                sheet.setColumnWidth(i, (int) 35.7 * 150);
            }

            Cell cell;
            Row row;

            row = sheet.createRow((short) 0);
            // 写表单头
            for (int i = 0; i < fieldsArray.length; i++) {
                cell = row.createCell(i);
                cell.setCellValue(createHelper.createRichTextString(fieldsArray[i]));
            }

            int recordsRemain = (int) (recordsNum - 60000 * sheetNum);
            for (int i = 0; i < recordsRemain; i++) {
                row = sheet.createRow((i + 1));
                for (int j = 0; j < fieldsArray.length; j++) {
                    cell = row.createCell(j);// 创建一列
                    int recordsIdx = (int) (i + 60000 * sheetNum);

                    if (records.get(recordsIdx).length > j) {// 有足够多的数据
                        cell.setCellValue(records.get(recordsIdx)[j]);
                    } else {
                        cell.setCellValue("");
                    }
                }
            }
        }

        /**
         * 写文件到Excel
         */
        FileOutputStream fileOut;
        try {
            fileOut = new FileOutputStream(fileName);
            wb.write(fileOut);
            fileOut.flush();
            fileOut.close();
//            System.out.println("fileName:" + fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String writeToExcel(File fileDir, long day, SubscribeInfo dayInfo, List<SubscribeInfo> hourInfoList,
            String fields) {

        String[] fieldsArray = fields.split(",");
        HSSFWorkbook wb = new HSSFWorkbook();

        String exportDay = utils.DateUtil.ymdsdf.format(new Date(day));
        String sheetName = exportDay.concat("-订购记录");

        HSSFSheet sheet = wb.createSheet(sheetName);
        CreationHelper createHelper = wb.getCreationHelper();

        for (int i = 0; i < fieldsArray.length; i++) {
            sheet.setColumnWidth(i, (int) 35.7 * 150);
        }

        Cell cell;
        Row row;

        row = sheet.createRow((short) 0);
        // 写表单头
        for (int i = 0; i < fieldsArray.length; i++) {
            cell = row.createCell(i);
            cell.setCellValue(createHelper.createRichTextString(fieldsArray[i]));
        }

        row = sheet.createRow((short) 1);

        cell = row.createCell(0);
        cell.setCellValue(createHelper.createRichTextString(exportDay));

        cell = row.createCell(1);
        cell.setCellValue(dayInfo.getApru());

        cell = row.createCell(2);
        cell.setCellValue(NumberUtil.doubleFormatter(dayInfo.getPayRate() * 100) / 100);

        cell = row.createCell(3);
        cell.setCellValue(dayInfo.getTotalNum());

        cell = row.createCell(4);
        cell.setCellValue(dayInfo.getPayNum());

        cell = row.createCell(5);
        cell.setCellValue(dayInfo.getTotalPayFee());

        cell = row.createCell(6);
        cell.setCellValue(dayInfo.getPerCustomerPay());

        row = sheet.createRow((short) 3);
        // 写表单头
        for (int i = 0; i < fieldsArray.length; i++) {
            cell = row.createCell(i);
            cell.setCellValue(createHelper.createRichTextString(fieldsArray[i]));
        }

        for (int i = 0; i < hourInfoList.size(); i++) {
            SubscribeInfo hourInfo = hourInfoList.get(i);
            row = sheet.createRow(i + 4);

            cell = row.createCell(0);
            cell.setCellValue(hourInfo.getHourOrDay());

            cell = row.createCell(1);
            cell.setCellValue(hourInfo.getApru());

            cell = row.createCell(2);
            cell.setCellValue(NumberUtil.doubleFormatter(hourInfo.getPayRate() * 100) / 100);

            cell = row.createCell(3);
            cell.setCellValue(hourInfo.getTotalNum());

            cell = row.createCell(4);
            cell.setCellValue(hourInfo.getPayNum());

            cell = row.createCell(5);
            cell.setCellValue(hourInfo.getTotalPayFee());

            cell = row.createCell(6);
            cell.setCellValue(hourInfo.getPerCustomerPay());

        }

        /**
         * 写文件到Excel
         */

        String fileName = sheetName.concat(".xls");
        File file = new File(fileDir, fileName);
        FileOutputStream fileOut;
        try {
            fileOut = new FileOutputStream(file);
            wb.write(fileOut);
            fileOut.flush();
            fileOut.close();
        } catch (FileNotFoundException e) {
            log.warn(e.getMessage(), e);
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
        }

        return file.getPath();
    }

    public static String removeSlash(String str) {
        Pattern p = Pattern.compile("/");
        Matcher m = p.matcher(str);
        return m.replaceAll(" ");
    }

}
