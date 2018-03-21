
package controllers;

import java.util.ArrayList;
import java.util.List;

import models.item.ItemPlay;
import models.user.User;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;

import play.mvc.Before;
import actions.DiagAction;
import actions.DiagAction.BatchInserter;
import actions.DiagAction.BatchReplaceCaller;
import actions.DiagAction.BatchResultMsg;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.JsonUtil;

import dao.item.ItemDao;

public class BatchOp extends TMController {

    @Before
    static void sync() {
//        new ItemUpdateJob(getUser().getId()).doJob();
    }

    public static void removeAll(String src, String numIidList) {

        User user = getUser();
        List<BatchResultMsg> msgs;
        if (numIidList.equals("")) {
            msgs = new BatchReplaceCaller(user, ItemDao.findByUserId(user.getId()), src, StringUtils.EMPTY)
                    .call();
        } else {
            List<ItemPlay> items = ItemDao.findByNumIidList(user, numIidList);
            msgs = new BatchReplaceCaller(user, items, src, StringUtils.EMPTY).call();
        }

        DiagAction.refreshByUpdateMsgs(user, msgs);
        renderOpResults(msgs);

    }

    public static void replaceAll(String src, String target, String numIidList) {

        User user = getUser();
        List<BatchResultMsg> msgs;
        if (numIidList.equals("")) {
            msgs = new BatchReplaceCaller(user, ItemDao.findByUserId(user.getId()), src, target).call();
        } else {
            List<ItemPlay> items = ItemDao.findByNumIidList(user, numIidList);
            msgs = new BatchReplaceCaller(user, items, src, target).call();
        }
        DiagAction.refreshByUpdateMsgs(user, msgs);
        renderOpResults(msgs);
    }

    public static void appendHead(String target, String numIidList) {

        User user = getUser();
        List<BatchResultMsg> msgs;
        if (numIidList.equals("")) {
            msgs = new BatchInserter(user, ItemDao.findByUserId(user.getId()), target, 0).call();
        } else {
            List<ItemPlay> items = ItemDao.findByNumIidList(user, numIidList);
            msgs = new BatchInserter(user, items, target, 0).call();
        }

        DiagAction.refreshByUpdateMsgs(user, msgs);
        renderOpResults(msgs);
    }

    public static void appendTail(String target, String numIidList) {

        User user = getUser();
        List<BatchResultMsg> msgs;
        if (numIidList.equals("")) {
            msgs = new BatchInserter(user, ItemDao.findByUserId(user.getId()), target, -1).call();
        } else {
            List<ItemPlay> items = ItemDao.findByNumIidList(user, numIidList);
            msgs = new BatchInserter(user, items, target, -1).call();
        }

        DiagAction.refreshByUpdateMsgs(user, msgs);
        renderOpResults(msgs);
    }

    protected static void renderOpResults(List<BatchResultMsg> list) {
        renderJSON(JsonUtil.getJson(new BatchOpResult(list)));
    }

    @JsonAutoDetect
    public static class BatchOpResult {

        @JsonProperty
        List<BatchResultMsg> errorList = ListUtils.EMPTY_LIST;

        @JsonProperty
        List<BatchResultMsg> okList = new ArrayList<BatchResultMsg>();

        @JsonProperty
        int successNum = 0;

        @JsonProperty
        int failNum = 0;

        public List<BatchResultMsg> getOkList() {
            return okList;
        }

        public void setOkList(List<BatchResultMsg> okList) {
            this.okList = okList;
        }

        public BatchOpResult() {
            super();
            this.errorList = new ArrayList<BatchResultMsg>();
        }

        public BatchOpResult(List<BatchResultMsg> errorList, int successNum, int failNum) {
            super();
            this.errorList = errorList;
            this.successNum = successNum;
            this.failNum = failNum;
        }

        public BatchOpResult(List<BatchResultMsg> list) {
            super();
            if (CommonUtils.isEmpty(list)) {
                this.errorList = ListUtils.EMPTY_LIST;
            }

            this.errorList = new ArrayList<DiagAction.BatchResultMsg>();
            int size = list.size();
            for (int i = size - 1; i >= 0; i--) {
                BatchResultMsg msg = list.get(i);
                if (msg == null) {
                } else if (msg.isOk()) {
                    successNum++;
                } else {
                    failNum++;
                    errorList.add(msg);
                }
            }

        }

        public static BatchOpResult makeEmpty() {
            return new BatchOpResult();
        }

        public List<BatchResultMsg> getErrorList() {
            return errorList;
        }

        public void setErrorList(List<BatchResultMsg> errorList) {
            this.errorList = errorList;
        }

        public int getSuccessNum() {
            return successNum;
        }

        public void setSuccessNum(int successNum) {
            this.successNum = successNum;
        }

        public int getFailNum() {
            return failNum;
        }

        public void setFailNum(int failNum) {
            this.failNum = failNum;
        }

        public void addAllList(List<BatchResultMsg> msgs) {
            if (CommonUtils.isEmpty(msgs)) {
                return;
            }

            int size = msgs.size();
            for (int i = size - 1; i >= 0; i--) {
                BatchResultMsg msg = msgs.get(i);
                if (msg == null) {

                } else if (msg.isOk()) {
                    successNum++;
                    this.okList.add(msg);
                } else {
                    failNum++;
                    errorList.add(msg);
                }
            }
        }

        @Override
        public String toString() {
            return "BatchOpResult [errorList=" + errorList.size() + ", successNum=" + successNum + ", failNum="
                    + failNum
                    + "]";
        }

    }
}
