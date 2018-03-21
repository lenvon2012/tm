
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import javax.imageio.ImageIO;

import org.json.JSONObject;

import play.Play;
import models.itemCopy.dto.PddItemDto;
import models.itemCopy.dto.SalePropDto;
import models.itemCopy.dto.SkuDto;
import sun.misc.BASE64Encoder;
import utils.ApiUtil;
import utils.CommonUtil;
import utils.FileUtil;
import utils.ToolBy1688;
import utils.oyster.DateUtil;
import actions.pdd.CopyToPddAction;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.domain.ItemProp;
import com.taobao.api.domain.Sku;
import com.taobao.api.request.ItempropsGetRequest;
import com.taobao.api.response.ItempropsGetResponse;

public class Test {

	private static String TZG_APP_KEY = "21255586";

	private static String TZG_APP_SECRET = "04eb2b1fa4687fbcdeff12a795f863d4";
	public static String url = "http://gw.api.taobao.com/router/rest";
	public static String appkey = "21348761";
	public static String appSecret = "74854fd22c37b749b7d86b7fafd45a96";
	public static String sessionkey = "610022944f53a1b3f51b83d1337d4f011baa41ad009a28479742176";

	public static TaobaoClient client;

	static {
		client = new DefaultTaobaoClient(url, TZG_APP_KEY, TZG_APP_SECRET);
	}

	public static String getDlhInfo(Long cid) {
		try {
			Gson gson = new Gson();
			ItempropsGetRequest req = new ItempropsGetRequest();
			req.setCid(cid);
			req.setFields("name,must,is_taosir,taosir_do");
			ItempropsGetResponse rsp = client.execute(req);
			// System.out.println(gson.toJson(rsp.getItemProps())+"\n\n");
			// System.out.println(rsp.getBody());

			// JSONObject data=JSON.parseObject(rsp.getBody());
			//
			// JSONObject
			// itemPropsJO=data.getJSONObject("itemprops_get_response").getJSONObject("item_props");
			//
			// JSONArray itemProps=itemPropsJO.getJSONArray("item_prop");

			// JSONArray itemProps=data.getJSONArray("item_props");

			System.out.println(rsp.getBody());

			System.out.println(gson.toJson(rsp.getItemProps()));

			//
			System.out.println("\n\n");
		} catch (ApiException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * resizePNG:Resize the PNG file.
	 * 
	 * @author lazybone,2010.08.16
	 * 
	 * @param fromFile
	 * @param fromW
	 * @param fromH
	 * @param toFile
	 * @param toW
	 * @param toH
	 */
	public static void resizeRPNG(String fromFile, int fromW, int fromH,
			String toFile, int toW, int toH) {
		try {
			BufferedImage to = new BufferedImage(toW, toH,
					BufferedImage.TYPE_INT_RGB);
			Graphics2D g2d = to.createGraphics();
			to = g2d.getDeviceConfiguration().createCompatibleImage(toW, toH,
					Transparency.TRANSLUCENT);
			g2d.dispose();
			g2d = to.createGraphics();
			File f2 = new File(fromFile);
			BufferedImage bi2 = ImageIO.read(f2);
			Image from = bi2.getScaledInstance(fromW, fromH, bi2.SCALE_DEFAULT);
			int a = 128;
			int b = 128;
			int tileCount = 8;
			int offset = 50;
			for (int i = 0; i < tileCount; i++)
				for (int j = 0; j < tileCount; j++) {
					g2d.drawImage(from, i * a - i * offset - offset / 2, j * a
							- j * offset - offset / 2, i * a + a - i * offset
							- offset / 2, j * a + a - j * offset - offset / 2,
							i * b, j * b, i * b + b, j * b + b, null);
				}
			g2d.dispose();
			ImageIO.write(to, "png", new File(toFile));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {

		System.out.println(1);

	}

	/**
	 * @Title: GetImageStrFromUrl
	 * @Description: TODO(将一张网络图片转化成Base64字符串)
	 * @param imgURL
	 *            网络资源位置
	 * @return Base64字符串
	 */
	public static String GetImageStrFromUrl(String imgURL) {
		byte[] data = null;
		try {
			// 创建URL
			URL url = new URL(imgURL);
			// 创建链接
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(15 * 1000);
			InputStream inStream = conn.getInputStream();

			data = readInputStream(inStream);
			inStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// 对字节数组Base64编码
		BASE64Encoder encoder = new BASE64Encoder();
		// 返回Base64编码过的字节数组字符串
		return encoder.encode(data);
	}

	/**
	 * 从输入流中获取字节数组
	 * 
	 * @param inputStream
	 * @return
	 * @throws IOException
	 */
	public static byte[] readInputStream(InputStream inputStream)
			throws IOException {
		byte[] buffer = new byte[1024];
		int len = 0;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		while ((len = inputStream.read(buffer)) != -1) {
			bos.write(buffer, 0, len);
		}
		bos.close();
		return bos.toByteArray();
	}

}
