package actions.pdd;

public class ImageTypePdd {

	// 主图
	// a. 尺寸750 x 352px
	// b. 大小100k以内
	// c. 图片格式仅支持JPG,PNG格式
	// d. 图片背景应以纯白为主, 商品图案居中显示
	// e. 图片不可以添加任何品牌相关文字或logo
	public static final int MAIN_IMG = 1;

	// 详情图
	// a. 尺寸要求宽度处于480~1200px之间，高度0-1500px之间
	// b. 大小1M以内
	// c. 数量限制在20张之间
	// d. 图片格式仅支持JPG,PNG格式
	public static final int DESC_IMG = 2;

	// 轮播图
	// a. 尺寸等宽高宽高不低于480px
	// b. 大小1M
	// c. 数量限制在10张以内
	// d. 图片格式仅支持JPG,PNG格式
	public static final int LB_IMG = 4;

	// 常规图
	public static final int NORMAL_IMG = 8;

	// SKU预览图
	// 等宽高，且高度不低于480px
	public static final int SKU_VIEW_IMG = 16;

}
