package bustbapi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bustbapi.request.FuwuScoresGetRequest;
import bustbapi.response.FuwuScoresGetResponse;
import bustbapi.result.ScoreResult;

import com.ciaosir.client.CommonUtils;

import configs.TMConfigs.PageSize;

public class FuwuScoreGet extends
		TBApi<FuwuScoresGetRequest, FuwuScoresGetResponse, List<ScoreResult>> {

	public final static Logger log = LoggerFactory.getLogger(FuwuScoreGet.class);
	
	public Date date;

	public boolean hasInit = false;

	public long pageNo = 1;

	public List<ScoreResult> resList;

	public FuwuScoreGet(Date date) {
		super("");
		this.date = date;
		this.resList = new ArrayList<ScoreResult>();
	}

	@Override
	public FuwuScoresGetRequest prepareRequest() {
		FuwuScoresGetRequest req = new FuwuScoresGetRequest();
		
		req.setDate(date);
		req.setCurrentPage(pageNo++);
		req.setPageSize(PageSize.FUWU_SCORE_PAGE_SIZE);

		return req;
	}

	@Override
	public List<ScoreResult> validResponse(FuwuScoresGetResponse resp) {

		if (resp == null) {
			log.error("Null Resp Returned");
			return null;
		}

		if (!resp.isSuccess()) {
			log.error("resp submsg" + resp.getSubMsg());
			log.error("resp error code " + resp.getErrorCode());
			log.error("resp Mesg " + resp.getMsg());
			return null;
		}

		if (!hasInit) {
			long totalResult = 1000;

			this.iteratorTime = (int) CommonUtils.calculatePageCount(
					totalResult, PageSize.FUWU_SCORE_PAGE_SIZE) - 1;
			this.hasInit = true;
		}
		return resp.getScoreResult() == null ? ListUtils.EMPTY_LIST : resp.getScoreResult();
	}

	@Override
	public List<ScoreResult> applyResult(List<ScoreResult> res) {

		if (res == null) {
			return resList;
		}

		resList.addAll(res);
		// ItemWritter.addItemList(user.getId(), res);

		return resList;

	}

}