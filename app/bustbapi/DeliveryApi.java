package bustbapi;

import com.taobao.api.domain.DeliveryTemplate;
import com.taobao.api.request.DeliveryTemplatesGetRequest;
import com.taobao.api.response.DeliveryTemplatesGetResponse;
import models.user.User;

import java.util.List;

/**
 * Created by User on 2017/11/11.
 */
public class DeliveryApi {

    public static class DeliveryTemplatesGet extends TBApi<DeliveryTemplatesGetRequest, DeliveryTemplatesGetResponse, List<DeliveryTemplate>> {

        public DeliveryTemplatesGet(User user) {
            super(user);
        }

        @Override
        public DeliveryTemplatesGetRequest prepareRequest() {
            DeliveryTemplatesGetRequest request = new DeliveryTemplatesGetRequest();
            request.setFields("template_id,template_name,created,modified,supports,assumer,valuation,query_express,query_ems,query_cod,query_post");
            return request;
        }

        @Override
        public List<DeliveryTemplate> validResponse(DeliveryTemplatesGetResponse resp) {
            List<DeliveryTemplate> deliveryTemplates = resp.getDeliveryTemplates();

            return deliveryTemplates;
        }

        @Override
        public List<DeliveryTemplate> applyResult(List<DeliveryTemplate> res) {
            return res;
        }
    }

}
