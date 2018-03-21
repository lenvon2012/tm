
package dao.word;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import models.word.ElasticRawWord;

import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import search.SearchManager;

import com.ciaosir.client.CommonUtils;
import com.google.gson.Gson;

public class WordSearch {

    /**
     * @param bd
     * @return 
     * @throws IOException
     */
    public static XContentBuilder buildDataMapping(XContentBuilder bd) throws IOException {
        bd.startObject("word").field("type", "string").field("store", "yes")
                .field("indexAnalyzer", "paoding").field("searchAnalyzer", "paoding").endObject();
        bd.startObject("price").field("type", "integer").field("index", "not_analyzed").endObject();
        bd.startObject("click").field("type", "integer").field("index", "not_analyzed").endObject();
        bd.startObject("competition").field("type", "integer").field("index", "not_analyzed")
                .endObject();
        bd.startObject("pv").field("type", "integer").field("index", "not_analyzed").endObject();
        bd.startObject("strikeFocus").field("type", "integer").field("index", "not_analyzed")
                .endObject();
        bd.startObject("searchFocus").field("type", "integer").field("index", "not_analyzed")
                .endObject();
        bd.startObject("status").field("type", "integer").field("index", "not_analyzed")
                .endObject();
        bd.startObject("scount").field("type", "integer").field("index", "not_analyzed")
                .endObject();
        bd.startObject("score").field("type", "integer").field("store", "yes").endObject();
        bd.startObject("cid").field("type", "integer").field("store", "yes").endObject();

        return bd;
    }

    public static void createMapping(Client client) {

        ElasticRawWord.log.info("[index name :]" + ElasticRawWord.indexName);
        try {
            client.admin().indices().prepareCreate(ElasticRawWord.indexName).execute().actionGet();

            XContentBuilder builder = XContentFactory.jsonBuilder().startObject()
                    .startObject(ElasticRawWord.indexName).startObject("properties");

            XContentBuilder mapping = buildDataMapping(builder).endObject().endObject().endObject();

            PutMappingRequest mappingRequest = Requests.putMappingRequest(ElasticRawWord.indexName)
                    .type(ElasticRawWord.indexType).source(mapping);
            PutMappingResponse resp = client.admin().indices().putMapping(mappingRequest).actionGet();
            ElasticRawWord.log.error("Client :" + new Gson().toJson(resp));
        } catch (Exception e) {
            ElasticRawWord.log.warn(e.getMessage(), e);
        }

        ElasticRawWord.log.error("Client :" + client);
    }

    static void execBulk(BulkRequestBuilder bulkRequest) {
        BulkResponse bulkResponse = bulkRequest.execute().actionGet();
        if (bulkResponse.hasFailures()) {
            Iterator<BulkItemResponse> its = bulkResponse.iterator();
            while (its.hasNext()) {
                BulkItemResponse next = its.next();
                ElasticRawWord.log.error("[next respose]" + next);
            }
        }
    }

    public static void callSync(Collection<ElasticRawWord> words) throws IOException {
        callSync(SearchManager.getIntance().getClient(), words);
    }

    public static void callSync(Client client, Collection<ElasticRawWord> words) throws IOException {
        if (CommonUtils.isEmpty(words)) {
            return;
        }

        BulkRequestBuilder bulkRequest = client.prepareBulk();
        for (ElasticRawWord elasticRawWord : words) {
            bulkRequest.add(elasticRawWord.parepareClientIndex(client));
        }

        WordSearch.execBulk(bulkRequest);
    }

    public static void callIndexDelete(Collection<ElasticRawWord> words) {
        if (CommonUtils.isEmpty(words)) {
            return;
        }

        Client client = SearchManager.getIntance().getClient();
        BulkRequestBuilder bulkRequest = client.prepareBulk();

        for (ElasticRawWord elasticRawWord : words) {
            bulkRequest.add(client.prepareDelete(ElasticRawWord.indexName,
                    ElasticRawWord.indexType, elasticRawWord.getId().toString()));
        }

        execBulk(bulkRequest);
    }

}
