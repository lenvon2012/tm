
package bustbapi;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import models.user.User;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import result.TMResult;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.NumberUtil;
import com.ciaosir.client.utils.SplitUtils;
import com.google.gson.Gson;
import com.taobao.api.ApiException;
import com.taobao.api.FileItem;
import com.taobao.api.domain.Picture;
import com.taobao.api.domain.PictureCategory;
import com.taobao.api.request.PictureCategoryAddRequest;
import com.taobao.api.request.PictureCategoryGetRequest;
import com.taobao.api.request.PictureDeleteRequest;
import com.taobao.api.request.PictureGetRequest;
import com.taobao.api.request.PictureUploadRequest;
import com.taobao.api.response.PictureCategoryAddResponse;
import com.taobao.api.response.PictureCategoryGetResponse;
import com.taobao.api.response.PictureDeleteResponse;
import com.taobao.api.response.PictureGetResponse;
import com.taobao.api.response.PictureUploadResponse;

public class PicApi {

    private static final Logger log = LoggerFactory.getLogger(PicApi.PicCatsGet.class);

    public static final String TAG = "PicApi";

    public static class PicCatsGet extends
            TBApi<PictureCategoryGetRequest, PictureCategoryGetResponse, List<PictureCategory>> {

        User user;

        Long parentPicCid;

        public PicCatsGet(User user, Long parentCid) {
            super(user.getSessionKey());
            this.user = user;
            this.parentPicCid = parentCid;
        }

        @Override
        public PictureCategoryGetRequest prepareRequest() {
            PictureCategoryGetRequest req = new PictureCategoryGetRequest();
            if (parentPicCid != null) {
                req.setParentId(parentPicCid);
            }

            return req;
        }

        @Override
        public List<PictureCategory> validResponse(PictureCategoryGetResponse resp) {

            if (resp == null) {
                log.error("Null Resp Returned");
                return null;
            }
//            log.error("new gson : " + new Gson().toJson(resp));
            if (!resp.isSuccess()) {
                ErrorHandler.validTaoBaoResp(this, resp);
                return null;
            }

            return resp.getPictureCategories() == null ? ListUtils.EMPTY_LIST : resp.getPictureCategories();
        }

        @Override
        public List<PictureCategory> applyResult(List<PictureCategory> res) {
            return res;
        }

        @Override
        protected PictureCategoryGetResponse execProcess() throws ApiException {
            return super.validPictureCatResp();
        }
    }

    static String TM_CAT_NAME = "淘掌柜复制宝贝素材";

    static Map<Long, Long> userTMCat = new ConcurrentHashMap<Long, Long>();

    public static Long ensureTMCat(User user) {
        Long targetCat = null;
        targetCat = userTMCat.get(user.getId());
        if (targetCat != null) {
            return targetCat;
        }

        List<PictureCategory> call = new PicCatsGet(user, null).call();

        if (call == null) {
            return null;
        }
        for (PictureCategory pictureCategory : call) {
            if (TM_CAT_NAME.equals(pictureCategory.getPictureCategoryName())) {
                targetCat = pictureCategory.getPictureCategoryId();
                userTMCat.put(user.getId(), targetCat);
                return targetCat;
            }
        }

        PictureCategory picCat = new AddPicCategoryApi(user, null, TM_CAT_NAME).call();
        if (picCat == null) {
            return null;
        }
        targetCat = picCat.getPictureCategoryId();
        userTMCat.put(user.getId(), targetCat);
        return targetCat;
    }
    
	public static boolean removePicCat(User user) {
		userTMCat.remove(user.getId());
		Long catId = userTMCat.get(user.getId());
		if(catId == null) {
			return true;
		}
		return false;
	}

    private String trimPicCatName(String name) {
        if (StringUtils.isEmpty(name)) {
            return name;
        }
        int length = name.length();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            String curr = name.substring(i, i + 1);
            if (StringUtils.isNumeric(curr)) {
                sb.append(curr);
            }
        }
        return sb.toString();
    }

    public Map<Long, Long> ensureNumIidsToDirMap(User user, Collection<Long> numIids) {
        Long topCid = ensureTMCat(user);
        log.error("exist : top cid:" + topCid);
        if (topCid == null) {
            return null;
        }

        Map<Long, Long> numIidToPicCatId = new HashMap<Long, Long>();
        List<PictureCategory> cats = new PicCatsGet(user, topCid).call();

        for (PictureCategory pictureCategory : cats) {
            String name = trimPicCatName(pictureCategory.getPictureCategoryName().trim());
            Long numIid = NumberUtil.parserLong(name, -1L);
            log.info("[parse id for name :  ]" + numIid + ":{" + name + "}");
            if (numIid > 0L) {
                numIidToPicCatId.put(numIid, pictureCategory.getPictureCategoryId());
            }
        }

        List<Long> notLoaded = new ArrayList<Long>(numIids);

        Iterator<Long> it = notLoaded.iterator();
        while (it.hasNext()) {
            Long nextNumIid = it.next();
            if (numIidToPicCatId.containsKey(nextNumIid)) {
                it.remove();
            }
        }
        for (Long numIid : notLoaded) {
            PictureCategory cat = new AddPicCategoryApi(user, topCid, String.valueOf(numIid)).call();
            log.info("[created category:]" + new Gson().toJson(cat));
            if (cat != null) {
                numIidToPicCatId.put(numIid, cat.getPictureCategoryId());
            }
        }
        log.info("[created categorys:]" + numIidToPicCatId);
        return numIidToPicCatId;
    }

    public static class AddPicCategoryApi extends
            TBApi<PictureCategoryAddRequest, PictureCategoryAddResponse, PictureCategory> {
        Long parent;

        User user;

        String name;

        public AddPicCategoryApi(User user, Long parentId, String name) {
            super(user.getSessionKey());
            this.parent = parentId;
            this.user = user;
            this.name = name;
        }

        @Override
        public PictureCategoryAddRequest prepareRequest() {
            PictureCategoryAddRequest req = new PictureCategoryAddRequest();
            if (parent != null) {
                req.setParentId(parent);
            }
            req.setPictureCategoryName(name);

            return req;
        }

        @Override
        public PictureCategory validResponse(PictureCategoryAddResponse resp) {

            if (resp == null) {
                log.error("Null Resp Returned");
                return null;
            }
            if (!resp.isSuccess()) {
                ErrorHandler.validTaoBaoResp(this, resp);
                return null;
            }

            return resp.getPictureCategory();

        }

        @Override
        public PictureCategory applyResult(PictureCategory res) {
            return res;
        }
    }

    public static class AddPicture extends
            TBApi<PictureUploadRequest, PictureUploadResponse, Picture> {
        User user;

        byte[] b;

        File file;

        String title;

        Long catId;

        public static String PhoneClientType = "client:phone";

        public static String PCClientType = "client:computer";

        String clientType = "client:phone";

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }

        public byte[] getB() {
            return b;
        }

        public void setB(byte[] b) {
            this.b = b;
        }

        public File getFile() {
            return file;
        }

        public void setFile(File file) {
            this.file = file;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public Long getCatId() {
            return catId;
        }

        public void setCatId(Long catId) {
            this.catId = catId;
        }

        public String getClientType() {
            return clientType;
        }

        public void setClientType(String clientType) {
            this.clientType = clientType;
        }

        public AddPicture(User user, byte[] b, String title, Long catId) {
            super(user.getSessionKey());
            this.user = user;
            this.b = b;
            this.title = title;
            this.catId = catId;
        }

        public AddPicture(String sid, byte[] b, String title, Long catId, String clientType) {
            super(sid);
            this.b = b;
            this.title = title;
            this.catId = catId;
            this.clientType = clientType;
        }

        public AddPicture(User user, File file, String title, Long catId) {
            super(user.getSessionKey());
            this.user = user;
            this.file = file;
            this.title = title;
            this.catId = catId;
        }

        @Override
        public PictureUploadRequest prepareRequest() {
            PictureUploadRequest req = new PictureUploadRequest();
            req.setImageInputTitle(title);
            if (b != null) {
                req.setImg(new FileItem(title, b));
            }
            if (file != null) {
                req.setImg(new FileItem(file));
            }
            if (catId == null && user != null) {
                catId = ensureTMCat(user);
            }

            req.setClientType(this.clientType);
            req.setPictureCategoryId(catId);

            return req;
        }

        @Override
        public Picture validResponse(PictureUploadResponse resp) {

            if (resp == null) {
                log.error("Null Resp Returned");
                return null;
            }
//            log.info("[back add resp:]" + new Gson().toJson(resp));
            if (!resp.isSuccess()) {
                ErrorHandler.validTaoBaoResp(this, resp);
                return null;
            }

//            Picture pic = resp.getPicture();
//            return pic.getPicturePath();
            return resp.getPicture();
        }

        @Override
        public Picture applyResult(Picture res) {
            return res;
        }

    }
    
	/**
	 * 检查图片空间中待上传的图片是否已存在
	 */
	public static class pictureGetApi extends TBApi<PictureGetRequest, PictureGetResponse, List<Picture>> {
		
		private Long catId;

		private String title;

		public pictureGetApi(User user, Long catId, String title) {
			super(user.getSessionKey());
			this.catId = catId;
			this.title = title;
		}

		@Override
		public PictureGetRequest prepareRequest() {
			PictureGetRequest req = new PictureGetRequest();
			req.setPictureCategoryId(catId);
			req.setTitle(title);
			req.setDeleted("unfroze");
			
			return req;
		}

		@Override
		public List<Picture> validResponse(PictureGetResponse resp) {
			if (resp == null) {
				log.error("Null Resp Returned");
				return null;
			}
			ErrorHandler.validTaoBaoResp(this, resp);
			return resp.getPictures();
		}

		@Override
		public List<Picture> applyResult(List<Picture> res) {
			return res;
		}

	}

    public static class GetPicApi extends TBApi<PictureGetRequest, PictureGetResponse, List<Picture>> {

        protected User user;

        protected String urls;

        protected boolean hasInit = false;

        protected long pageNo = 1;

        static long pageSize = 16;

        List<Picture> pics = new ArrayList<Picture>();

        Long pictureId;

        Long parentCid;

        public GetPicApi(User user) {
            super(user.getSessionKey());
            this.user = user;
        }

        public GetPicApi(User user, String urls) {
            super(user.getSessionKey());
            this.user = user;
            this.urls = urls;
        }

        public GetPicApi(User user, Long parentCid) {
            super(user.getSessionKey());
            this.user = user;
            this.parentCid = parentCid;
        }

        @Override
        public PictureGetRequest prepareRequest() {
            PictureGetRequest req = new PictureGetRequest();
            if (urls != null && !StringUtils.isBlank(urls)) {
                req.setUrls(urls);
//                log.info("[urls:]" + urls);
            }
            if (this.parentCid != null) {
                req.setPictureCategoryId(parentCid);
            }
            if (this.pictureId != null) {
                req.setPictureId(pictureId);
            }

            log.info("[curr page num for user pic get:]" + pageNo + " for user:" + user.toIdNick());
            req.setPageNo(pageNo++);
            req.setPageSize(pageSize);

            return req;
        }

        @Override
        protected PictureGetResponse execProcess() throws ApiException {
            return validGetPicResp();
        }

        @Override
        public List<Picture> validResponse(PictureGetResponse resp) {
            if (resp == null) {
                log.error("Null Resp Returned");
                return null;
            }

//            log.info("[get pic :]" + new Gson().toJson(resp));
            ErrorHandler.validTaoBaoResp(resp);

            if (resp.getTotalResults() == null) {
                return null;
            }

            if (!hasInit) {
                long totalResult = resp.getTotalResults();
                this.iteratorTime = (int) CommonUtils.calculatePageCount(totalResult, pageSize) - 1;
                this.hasInit = true;
            }

            return resp.getPictures() == null ? ListUtils.EMPTY_LIST : resp.getPictures();
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }

        public String getUrls() {
            return urls;
        }

        public void setUrls(String urls) {
            this.urls = urls;
        }

        public List<Picture> getPics() {
            return pics;
        }

        public void setPics(List<Picture> pics) {
            this.pics = pics;
        }

        public Long getPictureId() {
            return pictureId;
        }

        public void setPictureId(Long pictureId) {
            this.pictureId = pictureId;
        }

        public Long getParentCid() {
            return parentCid;
        }

        public void setParentCid(Long parentCid) {
            this.parentCid = parentCid;
        }

        @Override
        public List<Picture> applyResult(List<Picture> res) {
            if (res != null) {
                pics.addAll(res);
            }
            return pics;
        }

    }

    public static class PicDeleteApi extends TBApi<PictureDeleteRequest, PictureDeleteResponse, Boolean> {
        User user;

        Collection<Long> ids;

        List<List<Long>> splitToSubLongList = ListUtils.EMPTY_LIST;

        public PicDeleteApi(User user, Collection<Long> ids) {
            super(user.getSessionKey());
            this.user = user;
            this.ids = ids;
            this.splitToSubLongList = SplitUtils.splitToSubLongList(ids, 50);
            this.iteratorTime = splitToSubLongList.size();
            this.retryTime = 1;
        }

        @Override
        public PictureDeleteRequest prepareRequest() {
            PictureDeleteRequest req = new PictureDeleteRequest();
            String numIids = StringUtils.join(splitToSubLongList.get(iteratorTime), ',');
            req.setPictureIds(numIids);
            return req;
        }

        @Override
        public Boolean validResponse(PictureDeleteResponse resp) {
            if (resp == null) {
                log.error("Null Resp Returned");
                return null;
            }
//            log.info("[resp:]" + new Gson().toJson(resp));
            ErrorHandler.validTaoBaoResp(resp);
            return resp.getSuccess();
        }

        @Override
        public Boolean applyResult(Boolean res) {
            return res;
        }

    }

    public Picture singlePicture(User user, Long pictureId) {
        GetPicApi api = new GetPicApi(user);
        api.setPictureId(pictureId);
        List<Picture> call = api.call();
        return NumberUtil.first(call);
    }

    public Set<Long> listPicIds(User user, Long catId) {
        List<Picture> pics = new GetPicApi(user, catId).call();
//        log.info("[get pics:]" + pics);
        Set<Long> ids = new HashSet<Long>();
        for (Picture picture : pics) {
            ids.add(picture.getPictureId());
        }
        return ids;
    }

    public boolean clearDir(User user, Long catId) {
        log.error(" >>>>>>  do clear :" + catId);

        List<Picture> pics = new GetPicApi(user, catId).call();
        if (CommonUtils.isEmpty(pics)) {
            return true;
        }
        Set<Long> toRemoveIds = new HashSet<Long>();
        for (Picture picture : pics) {
//            log.info("[is referenced:]" + picture.getReferenced());
            if (picture.getReferenced()) {
                continue;
            }

            toRemoveIds.add(picture.getPictureId());
        }

        Boolean call = new PicDeleteApi(user, toRemoveIds).call();
        if (call == null) {
            return false;
        }
        return call.booleanValue();
    }

    static PicApi _instance = new PicApi();

    public static PicApi get() {
        return _instance;
    }

    public Picture uploadPic(User user, String title, byte[] b, Long catId) {
        Picture pic = new AddPicture(user, b, title, catId).call();
        return pic;
    }

    public Picture uploadPcClientPic(User user, String title, byte[] b, Long catId) {
        long start = System.currentTimeMillis();
        AddPicture api = new AddPicture(user, b, title, catId);
        api.setClientType(AddPicture.PCClientType);
        Picture pic = api.call();
        long end = System.currentTimeMillis();
        log.info("[upload  uploadPcClientPic took :]" + (end - start) + "ms for user:" + user + " with file :" + title);
        return pic;
    }

    public Picture uploadPcClientPic(User user, String title, File f, Long catId) {
        long start = System.currentTimeMillis();
        AddPicture api = new AddPicture(user, f, title, catId);
        api.setClientType(AddPicture.PCClientType);
        Picture pic = api.call();
        long end = System.currentTimeMillis();
        log.info("[upload  uploadPcClientPic took :]" + (end - start) + "ms for user:" + user + " with file :" + title);
        return pic;
    }
    
	/**
	 * 返回TMResult 追踪图片空间报错
	 */
	public TMResult<Picture> newUploadPcClientPic(User user, String title, File f, Long catId) {
		// 检查图片空间中待上传的图片是否已存在
		List<Picture> existList = new pictureGetApi(user, catId, title).call();
		if(!CommonUtils.isEmpty(existList)) {
			log.info("pic [" + title + "] already exist");
			return new TMResult<Picture>(true, StringUtils.EMPTY, existList.get(0));
		}
		
		long start = System.currentTimeMillis();
		AddPicture api = new AddPicture(user, f, title, catId);
		api.setClientType(AddPicture.PCClientType);
		Picture pic = api.call();
		long end = System.currentTimeMillis();
		log.info("[upload newUploadPcClientPic took :]" + (end - start) + "ms for user:" + user + " with file :" + title);
		if(pic == null) {
			// 图片空间 容量不足报错
			if("isv.pictureServiceClient-service-error:PICTURE_OVER_AVAILSPACE".equalsIgnoreCase(api.getSubErrorCode())) {
				return new TMResult<Picture>(false, api.getSubErrorMsg(), null);
			}
			// 目录不存在
			if("isv.pictureServiceClient-service-error:CATEGORY_NOTEXIST".equalsIgnoreCase(api.getSubErrorCode())) {
				PicApi.removePicCat(user);
				return new TMResult<Picture>(false, "图片空间异常，未找到默认的图片目录。正在整理中，请稍后重试", null);
			}
		}
		return new TMResult<Picture>(true, StringUtils.EMPTY, pic);
	}

    public Picture uploadPic(User user, String title, File file, Long catId) {

        if (file == null) {
            return null;
        }

        long start = System.currentTimeMillis();
        Picture picPath = new AddPicture(user, file, title, catId).call();
        long end = System.currentTimeMillis();
        log.info("[upload uploadPic took :]" + (end - start) + "ms for user:" + user + " with file :" + file);
        return picPath;
    }

    public List<Picture> ensureOriginPicture(User user, Set<String> imgUrls) {
        if (CommonUtils.isEmpty(imgUrls)) {
            return ListUtils.EMPTY_LIST;
        }
        List<Picture> pictures = new GetPicApi(user, StringUtils.join(imgUrls, ',')).call();
        return pictures;
    }

    public Picture findOriginPicture(User user, String url) {
        if (StringUtils.isEmpty(url)) {
            return null;
        }
        List<Picture> pictures = new GetPicApi(user, url).call();
        if (pictures == null || pictures.size() == 0) {
            return null;
        } else {
            return pictures.get(0);
        }
    }
}
