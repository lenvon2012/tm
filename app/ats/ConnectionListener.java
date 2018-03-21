
package ats;

import java.text.SimpleDateFormat;
import java.util.Date;

import play.Logger;

import com.taobao.api.internal.stream.connect.ConnectionLifeCycleListener;

public class ConnectionListener implements ConnectionLifeCycleListener {
    public static SimpleDateFormat sdf = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss");

    public void onConnect() {
        Logger.info(getLoggerInfo("onConnect", sdf.format(new Date())
                + ":connect success"));
    }

    public void onDisconnect() {
        Logger.info(getLoggerInfo("onDisconnect", sdf.format(new Date())
                + ":disconnect..."));
    }

    public void onException(Throwable throwable) {
        Logger.info(getLoggerInfo("onException", sdf.format(new Date()) + ":"
                + throwable));
    }

    public void onConnectError(Exception e) {
        Logger.info(getLoggerInfo("onConnectError", sdf.format(new Date())
                + ":connection timeout:" + e));
    }

    public void onReadTimeout() {
        Logger.info(getLoggerInfo("onReadTimeout", sdf.format(new Date())
                + ":Read timeout"));
    }

    public void onSysErrorException(Exception e) {
        Logger.info(getLoggerInfo("onSysErrorException", sdf.format(new Date())
                + ":SysErrorException:" + e));
    }

    public void onReconnect() {
        Logger.info(getLoggerInfo("onReconnect", sdf.format(new Date())
                + ":reconnect"));
    }

    public void onMaxReadTimeoutException() {
        Logger.info(getLoggerInfo("onMaxReadTimeoutException",
                sdf.format(new Date()) + ":maxReadTimeoutException"));
    }

    private String getLoggerInfo(String functionName, String loggerInfo) {
        return (new Date().toString()).concat(" ConnectionListener.")
                .concat(functionName).concat(":").concat(loggerInfo);
    }

    @Override
    public void onBeforeConnect() {
    }

}
