
package utils;

import java.io.File;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.utils.WebUtils;
import com.taobao.api.ApiException;

public class ATSUtils {

    private static final Logger log = LoggerFactory.getLogger(ATSUtils.class);

    private static final String TAG = "ATSUtils";

    private static final String CTYPE_OCTET = "application/octet-stream";

    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

    public static File download(String url, File targetFile) throws ApiException {
        HttpURLConnection conn = null;
        OutputStream output = null;
        try {
            conn = getConnection(new URL(url));
            String ctype = conn.getContentType();
            if (CTYPE_OCTET.equals(ctype)) {
                output = new FileOutputStream(targetFile);
                copy(conn.getInputStream(), output);
            } else {
                String rsp = WebUtils.getResponseAsString(conn);
                throw new ApiException(rsp);
            }
        } catch (IOException e) {
            throw new ApiException(e.getMessage());
        } finally {
            closeQuietly(output);
            if (conn != null) {
                conn.disconnect();
            }
        }
        return targetFile;
    }

    /**
     * 通过HTTP GET方式下载文件到指定的目录。
     * 
     * @param url
     *            需要下载的URL
     * @param toDir
     *            需要下载到的目录
     * @return 下载后的文件
     */
    public static File download(String url, File toDir, String fileName) throws ApiException {
        toDir.mkdirs();
        HttpURLConnection conn = null;
        OutputStream output = null;
        File file = null;
        try {
            conn = getConnection(new URL(url));
            String ctype = conn.getContentType();
            if (CTYPE_OCTET.equals(ctype)) {
                file = new File(toDir, fileName);
                output = new FileOutputStream(file);
                copy(conn.getInputStream(), output);
            } else {
                String rsp = WebUtils.getResponseAsString(conn);
                throw new ApiException(rsp);
            }
        } catch (IOException e) {
            throw new ApiException(e.getMessage());
        } finally {
            closeQuietly(output);
            if (conn != null) {
                conn.disconnect();
            }
        }
        return file;
    }

    private static HttpURLConnection getConnection(URL url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setRequestProperty("Accept", "application/zip;text/html");
        return conn;
    }

    private static int copy(InputStream input, OutputStream output) throws IOException {
        long count = copyStream(input, output);
        if (count > Integer.MAX_VALUE) {
            return -1;
        }
        return (int) count;
    }

    private static long copyStream(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[1024];
        long count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    private static void closeQuietly(OutputStream output) {
        try {
            if (output != null) {
                output.close();
            }
        } catch (IOException ioe) {
            log.warn(ioe.getMessage(), ioe);
        }
    }

    private static void closeQuietly(InputStream input) {
        try {
            if (input != null) {
                input.close();
            }
        } catch (IOException ioe) {
            log.warn(ioe.getMessage(), ioe);
        }
    }

    public static File unZip(File source, File target) throws IOException {

        ZipInputStream gin = null;
        FileOutputStream fout = null;
        ZipFile zipFile = null;
        try {
            fout = new FileOutputStream(target);
            zipFile = new ZipFile(source);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            ZipEntry nextElement = entries.nextElement();
            copy(zipFile.getInputStream(nextElement), fout);
            fout.close();
        } finally {
            if (zipFile != null) {
                zipFile.close();
            }
            closeQuietly(gin);
            closeQuietly(fout);
        }
        return target;

    }

    public static File unGzip(File source, File target) throws IOException {

        GZIPInputStream gin = null;
        FileOutputStream fout = null;
        try {
            FileInputStream fin = new FileInputStream(source);
            gin = new GZIPInputStream(fin);
            fout = new FileOutputStream(target);
            copy(gin, fout);
            gin.close();
            fout.close();
        } finally {
            closeQuietly(gin);
            closeQuietly(fout);
        }
        return target;
    }

}
