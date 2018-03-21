package configs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CWordsConfig {

	// 女装 连衣裙 cid = 50010850
	public static String[] dresses = new String[] { "吊带裙", "背心裙", "打底裙", "针织裙",
			"珍珠连衣裙", "长袖连衣裙", "蕾丝连衣裙", "大码连衣裙", "修身连衣裙", "瘦身连衣裙", "皮丝绒连衣裙",
			"毛衣裙", "冬季连衣裙", "收腰连衣裙", "秋冬连衣裙", "长袖裙", "丝绒连衣裙", "礼服裙", "复古连衣裙",
			"百褶裙", "毛呢连衣裙", "羊毛连衣裙", "浮花连衣裙", "长款连衣裙", "蓬蓬裙", "SPORT连衣裙",
			"雪纺连衣裙", "格子裙", "百褶连衣裙", "蛋糕裙", "背心裙", "雪纺裙", "高腰连衣裙", "背心长裙",
			"七分袖连衣裙", "花边袖连衣裙", "无袖背心裙", "短袖连衣裙", "公主连衣裙", "牛仔裙", "纯色连衣裙",
			"露胸连衣裙", "无袖连衣裙", "中袖连衣裙", "装饰连衣裙", "百搭连衣裙", "圆领套头", "雪纺连体裙",
			"连体裙", "娃娃领裙子", "针织连衣裙", "无袖背心裙", "礼服连衣裙", "显瘦连衣裙", "大摆连衣裙", "沙滩裙",
			"春款连衣裙", "拉链连衣裙", "娃娃衫", "仙女裙", "宝石领连衣裙", "露肩连衣裙", "背带裙", "包臀裙",
			"OP裙", "镂空背心裙", "party连衣裙", "莫代尔连衣裙", "雪纺波西米亚裙", "背心公主裙", "花朵雪纺裙",
			"直筒连衣裙", "牛仔连衣裙", "拼接雪纺裙" };
	public static List<String> dress = new ArrayList<String>();
	static {
		dress.addAll(Arrays.asList(dresses));
	}

	// 女装 t恤 cid = 50000671
	public static String[] TShirts = new String[] { "短t", "连帽t恤", "连帽t",
			"百搭短袖", "百搭款t恤", "沾纤t恤", "沾纤t", "短袖t恤", "短袖t", "圆领短袖t恤", "灰色短袖t恤",
			"棉t恤", "卡通短袖棉t恤", "长袖t恤", "圆领t恤", "纱衣t恤", "打底t恤", "polo衫", "七分袖t恤",
			"翻领t恤", "格子t恤", "修身t恤", "长款t恤", "条纹t恤", "打底衫", "印花t恤", "印花t",
			"百搭t", "烫钻t恤", "打底衫t恤", "白条纹短袖t恤", "针织衫", "t-shirt", "纯棉t恤",
			"海军领t恤", "中袖长t", "精品t恤", "弹力t恤", "袖长t恤", "潮t恤", "棉质t恤", "宽松t恤",
			"宽松t", "露肩袖t", "休闲t恤", "娃娃领t恤", "蝙蝠t恤", "刺绣t恤", "闪电t", "短袖t",
			"宝石t", "雪纺t", "雪纺t恤", "字母t恤", "字母t", "大码t恤", "大码t", "卡通t", "卡通t恤",
			"猫头鹰短袖", "斑马短袖t", "宽松短袖t", "印花棉t恤", "印花棉t", "雪纺t恤", "雪纺t", "条纹t恤",
			"条纹t", "气质t", "气质t恤", "钻链t", "钻链t恤", "露肩t恤", "露肩t", "荷叶边t恤",
			"荷叶边t", "个性t恤", "个性t", "大版t", "大版t恤", "简单t恤", "简单t", "蕾丝衫t恤",
			"蕾丝衫t", "无袖t恤", "无袖t", "v领t恤", "v领t", "背心t恤", "背心t", "水钻t恤", "水钻t",
			"卫衣t恤", "卫衣t", "开叉t恤", "开叉t", "接拼t恤", "接拼t", "短T", "连帽T恤", "连帽T",
			"百搭短袖", "百搭款T恤", "沾纤T恤", "沾纤T", "短袖T恤", "短袖T", "圆领短袖T恤", "灰色短袖T恤",
			"棉T恤", "卡通短袖棉T恤", "长袖T恤", "圆领T恤", "纱衣T恤", "打底T恤", "polo衫", "七分袖T恤",
			"翻领T恤", "格子T恤", "修身T恤", "长款T恤", "条纹T恤", "打底衫", "印花T恤", "印花T",
			"百搭T", "烫钻T恤", "打底衫T恤", "白条纹短袖T恤", "针织衫", "T-shirT", "纯棉T恤",
			"海军领T恤", "中袖长T", "精品T恤", "弹力T恤", "袖长T恤", "潮T恤", "棉质T恤", "宽松T恤",
			"宽松T", "露肩袖T", "休闲T恤", "娃娃领T恤", "蝙蝠T恤", "刺绣T恤", "闪电T", "短袖T",
			"宝石T", "雪纺T", "雪纺T恤", "字母T恤", "字母T", "大码T恤", "大码T", "卡通T", "卡通T恤",
			"猫头鹰短袖", "斑马短袖T", "宽松短袖T", "印花棉T恤", "印花棉T", "雪纺T恤", "雪纺T", "条纹T恤",
			"条纹T", "气质T", "气质T恤", "钻链T", "钻链T恤", "露肩T恤", "露肩T", "荷叶边T恤",
			"荷叶边T", "个性T恤", "个性T", "大版T", "大版T恤", "简单T恤", "简单T", "蕾丝衫T恤",
			"蕾丝衫T", "无袖T恤", "无袖T", "v领T恤", "v领T", "背心T恤", "背心T", "水钻T恤", "水钻T",
			"卫衣T恤", "卫衣T", "开叉T恤", "开叉T", "接拼T恤", "接拼T" };
	public static List<String> TShirt = new ArrayList<String>();
	static {
		TShirt.addAll(Arrays.asList(TShirts));
	}

	// 女装 衬衫 cid = 162104
	public static String[] Shirts = new String[] { "空调衫", "七分袖衬衫", "长袖衬衫", "格子衬衫",
		"女衬衣", "纱衣衬衫", "淑女衬衫", "衬衣", "女士衬衫", "短袖衬衫", "气质衬衣", "休闲衬衣", "棉质衬衣",
		"职业衫", "正装打底衫", "雪纺衬衫", "雪纺衬衣", "雪纺衫", "打底衫", "保暖衬衣", "真丝衬衫", "插肩衬衫",
		"牛仔衬衣", "牛仔衬衫", "桑蚕丝衬衫", "风琴衬衫", "长袖衬衣", "纯棉衬衫", "防晒衫", "口袋衬衫",
		"长款衬衫", "修身衬衣", "职业衬衫", "圆点衬衫", "无袖衬衣", "短袖衬衣", "泡泡袖衬衫", "印花衬衫",
		"棉衬衣", "中性衬衫", "真丝衬衫", "带袖衬衣", "透气衬衫", "性感衬衫", "翻领衬衫", "翻领衬衣", 
		"娃娃衫", "针织衫", "棉质衬衣", "圆点衬衣", "POLO衫", "蕾丝衫", "蕾丝衬衫", "蕾丝衬衣", "立领衬衫",
		"防晒衬衫"
	};
	public static List<String> Shirt = new ArrayList<String>();
	static {
		Shirt.addAll(Arrays.asList(Shirts));
	}

	// 女装 休闲裤,cid = 162201
	public static String[] CasualPants = new String[] {
		"喇叭裤", "低腰裤", "直筒裤", "骷髅短裤", "热裤", "中裤", "哈伦裤", "皮裤", "短裤", "格子裤", "裙裤",
		"打底裤", "七分裤", "九分裤", "铅笔裤", "学生裤", "西装裤", "太阳裙裤", "小脚裤", "运动裤", "少女裤",
		"秋裤", "连体裤", "紧腰短裤", "紧腰裤", "面料裤", "工装裤", "阔腿裤", "西装短裤", "羽绒裤", "哈伦长裤",
		"骷髅女裤", "雪纺短裤", "紧身裤", "分裤", "9分裤", "明星裤", "哈伦裤", "阔腿裤", "连衣裤", "韩版裤",
		"紧腰短裤", "安全裤", "锥形裤", "蓬蓬短裤", "蓬蓬裤", "休闲短裤", "弹力裤", "靴裤", "灯笼裤", "包臀裤",
		"卫裤", "7分裤", "花苞裤", "花裤", "长裤"
	};
	public static List<String> CasualPant = new ArrayList<String>();
	static {
		CasualPant.addAll(Arrays.asList(CasualPants));
	}

	// 女装 西装裤/正装裤, cid = 50022566
	public static String[] SuitPants = new String[] { 
		"9分裤", "九分裤", "小脚裤", "正装裤", "双脚裤", "长裤", "中腰短裤", "休闲裤", "职业裤", "印花裤",
		"印花长裤", "加绒裤", "西装裤"
	};
	public static List<String> SuitPant = new ArrayList<String>();
	static {
		SuitPant.addAll(Arrays.asList(SuitPants));
	}
	
	// 女装 打底裤, cid = 50007068
	public static String[] Leggings = new String[] { 
		"安全裤", "短裤", "牛仔裤", "踩脚裤", "保暖裤", "九分裤", "9分裤", "七分裤", "7分裤", "斑马裤", 
		"裙裤", "打底裙裤", "包臀裤", "小脚裤", "保险裤", "踩脚袜裤", "铅笔裤", "弹力裤", "弹性裤", "潮裤",
		"连体裤", "亮皮裤", "紧身裤", "加绒裤", "裤袜"
	};
	public static List<String> Legging = new ArrayList<String>();
	static {
		Legging.addAll(Arrays.asList(Leggings));
	}
	
	// 女装 牛仔裤, cid = 162205
	public static String[] Jeans = new String[] {
		"蓝布裤", "铅笔裤", "长裤", "直筒裤", "小脚裤", "提臀裤", "高腰裤", "水洗裤", "修身裤", "加绒裤",
		"打底裤", "弹力裤", "弹性裤", "裙裤", "热裤", "磨白牛仔裤", "紧身裤", "小垮裤", "流水线裤", "短裤",
		"牛仔短裤", "中腰裤", "五分裤", "5分裤", "七分裤", "7分裤", "九分裤", "9分裤", "中裤", "宽松裤",
		"破洞裤", "破洞牛仔裤", "牛仔中裤", "牛仔长裤", "中腰长裤", "中腰短裤", "微喇裤", "哈伦裤", "阔腿裤", 
		"复古长裤", "复古裤", "背带裤", "韩板裤", "韩版裤", "牛仔布裤", "布裤", "垮裤", "显瘦裤", "包臀裤",
		"花苞裤", "瘦脚裤", "喇叭裤", "休闲裤", "休闲牛仔裤", "做旧牛仔裤", "大码裤", "宽腰裤"
	};
	public static List<String> Jean = new ArrayList<String>();
	static {
		Jean.addAll(Arrays.asList(Jeans));
	}

	// 女装 蕾丝衫/雪纺衫, cid = 162116
	public static String[] Chiffons = new String[] {
		"蕾丝衫", "雪纺衫", "小外套", "打底衫", "高领衫", "针织衫", "长袖衫", "小衫", "绒上衣", "上衣", "连衣裙",
		"套头小衫", "翻领衫", "印花衬衫", "立领衫", "衬衫", "七分袖雪纺衫", "泡泡袖", "双层雪纺衫", 
		"印花雪纺", "印花雪纺衫", "喇叭袖衫", "喇叭袖雪纺衫", "雪纺衬衫", "衬衣", "碎花衬衫", "短袖t恤",
		"t恤", "娃娃衫", "雪纺上衣", "翻领雪纺", "无袖雪纺衫", "罩衫", "防晒衣", "开衫", "蝙蝠衫", 
		"雪纺上衣"
	};
	public static List<String> Chiffon = new ArrayList<String>();
	static {
		Chiffon.addAll(Arrays.asList(Chiffons));
	}

	// 女装 毛针织衫, cid = 50000697
	public static String[] Sweaters = new String[] { 
		"针织衫", "毛衫", "打底杉", "针织长袖", "羊毛衣", "羊毛衫", "开衫", "翻领衫", "立领衫", "防晒衣", 
		"防晒衫", "套头衫", "羊绒衫", "套头", "堆堆领", "坎肩", "蝙蝠袖", "外套", "罩衫", "高领衫", "毛毛衣", 
		"披肩", "七分袖", "泡泡袖", "波长袖", "蝙蝠衫", "连衣裙", "套头裙", "两件套", "貂绒毛衣", "毛衣",
		"小皮领", "皮领", "针织", "羊毛衫", "马甲", "空调衫", "小立领", "立领", "连衣裙"
	};
	public static List<String> Sweater = new ArrayList<String>();
	static {
		Sweater.addAll(Arrays.asList(Sweaters));
	}

	public static Map<Long, List<String>> categoryMap = new HashMap<Long, List<String>>();
	static {
		categoryMap.put(50010850L, dress);
		categoryMap.put(50000671L, TShirt);
		categoryMap.put(162104L, Shirt);
		categoryMap.put(162201L, SuitPant);
		categoryMap.put(50007068L, Legging);
		categoryMap.put(162205L, Jean);
		categoryMap.put(162116L, Chiffon);
		categoryMap.put(50000697L, Sweater);
	}

	public static Map<Long, String> WOMEN = new HashMap<Long, String>();
	static {
		WOMEN.put(50010850L, "连衣裙"); // 对应dress
		WOMEN.put(50000671L, "T恤"); // 对应TShirt
		WOMEN.put(162104L, "衬衫"); // 对应Shirt
		WOMEN.put(1622L, "裤子"); // 子类目：162201（休闲裤）；50022566（西装裤/正装裤）；50007068（打底裤）；162202（短裤/热裤）；50026651（棉裤/羽绒裤）；162203（中裤/五分裤）；162207（九分裤/七分裤）
		WOMEN.put(162205L, "牛仔裤"); // 对应Jean
		WOMEN.put(1623L, "半身裙");
		WOMEN.put(162105L, "小背心/小吊带");
		WOMEN.put(50013196L, "马夹");
		WOMEN.put(162116L, "蕾丝衫/雪纺衫"); // 对应Chiffon
		WOMEN.put(50000697L, "毛针织衫"); // 对应Sweater
		WOMEN.put(50011277L, "短外套");
		WOMEN.put(50008897L, "西装");
		WOMEN.put(50008898L, "卫衣/绒衫");
		WOMEN.put(162103L, "毛衣");
		WOMEN.put(50008901L, "风衣");
		WOMEN.put(50013194L, "毛呢外套");
		WOMEN.put(50008900L, "棉衣/棉服");
		WOMEN.put(50008899L, "羽绒服");
		WOMEN.put(50008904L, "皮衣");
		WOMEN.put(50008905L, "皮草");
		WOMEN.put(50008905L, "中老年女装");
		WOMEN.put(1629L, "大码女装");
		WOMEN.put(1624L, "职业套装/学生校服/工作制服");
		WOMEN.put(50011404L, "婚纱/旗袍/礼服");
		WOMEN.put(50008906L, "唐装/民族服装/舞台服装");
	}
}
