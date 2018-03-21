package dao.autolist;

import java.sql.ResultSet;
import java.sql.SQLException;

import models.autolist.AutoListRecord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import transaction.JDBCBuilder;

public class AutoListRecordDao {
    public final static Logger log = LoggerFactory
            .getLogger(AutoListRecordDao.class);

    public static AutoListRecord findAutoListRecordByUserId(Long userId) {

        String query = "select " + SelectAllProperties + " from "
                + AutoListRecord.TABLE_NAME + " where userId = ?";

        return new JDBCBuilder.JDBCExecutor<AutoListRecord>(query, userId) {

            @Override
            public AutoListRecord doWithResultSet(ResultSet rs)
                    throws SQLException {
                if (rs.next()) {
                    return parseAutoListRecord(rs);
                } else {
                    return null;
                }
            }

        }.call();

    }

    public static boolean saveOrUpdateAutoListRecord(AutoListRecord record) {
        return record.jdbcSave();
    }

    public static boolean isCalcuComplete(Long userId) {

        String query = "select " + SelectAllProperties + " from "
                + AutoListRecord.TABLE_NAME + " where userId = ?";

        AutoListRecord record = new JDBCBuilder.JDBCExecutor<AutoListRecord>(
                query, userId) {

            @Override
            public AutoListRecord doWithResultSet(ResultSet rs)
                    throws SQLException {
                if (rs.next()) {
                    return parseAutoListRecord(rs);
                } else {
                    return null;
                }
            }

        }.call();

        if (record == null)
            return false;
        return record.getIsCalcuComplete();
    }

    private static final String SelectAllProperties = " id,userId,isTurnOn,isCalcuComplete,"
            + "createTime,updateTime,distriType,autoListSchedule,distriTime,distriHours ";

    private static AutoListRecord parseAutoListRecord(ResultSet rs) {
        try {

            AutoListRecord record = new AutoListRecord();
            record.setId(rs.getLong(1));
            record.setUserId(rs.getLong(2));
            record.setIsTurnOn(rs.getBoolean(3));
            record.setIsCalcuComplete(rs.getBoolean(4));
            record.setCreateTime(rs.getLong(5));
            record.setUpdateTime(rs.getLong(6));
            record.setDistriType(rs.getInt(7));
            record.setAutoListSchedule(rs.getString(8));
            record.setDistriTime(rs.getString(9));
            record.setDistriHours(rs.getString(10));

            return record;

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    }

}
