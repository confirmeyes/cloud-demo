import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lpx .
 * @create 2020-04-23-16:02 .
 * @description .
 */


public class EsClient {

    private static String ip = "192.168.88.134:9200";

    public RestHighLevelClient getEsClient() {

        HttpHost httpHost = new HttpHost(ip);

        RestClientBuilder builder = RestClient.builder(httpHost);

        RestHighLevelClient client = new RestHighLevelClient(builder);

        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("user", "kimchy");
        jsonMap.put("postDate", new Date());
        jsonMap.put("message", "trying out Elasticsearch");
        /*IndexRequest indexRequest = new IndexRequest("posts")
                .id("1").source(jsonMap);*/
        return null;
    }

}
