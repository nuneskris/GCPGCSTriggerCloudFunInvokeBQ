package com.java.kfn.study.gcp.cloudfunction;

import java.io.IOException;
import java.util.logging.Logger;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.java.kfn.study.gcp.cloudfunction.pojos.CloudStorageBody;

public class CallCloudFunctionPipeline {
	private static final Logger logger = Logger.getLogger(CloudStorageEventHandling.class.getName());
	
	public void call(CloudStorageBody cloudStorageBody) throws ClientProtocolException, IOException {
		
		
		
		
		//curl -X POST -H "Authorization: Bearer ${AUTH_TOKEN}" "${CDAP_ENDPOINT}/v3/namespaces/default/apps/kfnstudy/workflows/DataPipelineWorkflow/start" '{ "arguments" : [{"name": "input.path", "value": "gs://ingest_study/cricket/testNew/*.csv" } ] }'
		
		//curl -X POST -H "Authorization: Bearer ${AUTH_TOKEN}"  -H "Content-Type: application/json" -d '{"arguments" : [{"name": "input.path", "value": "gs://ingest_study/cricket/testNew/*.csv"}]}' "${CDAP_ENDPOINT}/v3/namespaces/default/apps/kfnstudy/workflows/DataPipelineWorkflow/start"
		//curl -X POST -H "Authorization: Bearer ${AUTH_TOKEN}"  -H "Content-Type: application/json" -d '{"name": "input.path", "value": "gs://ingest_study/cricket/testNew/*.csv"}' "${CDAP_ENDPOINT}/v3/namespaces/default/apps/kfnstudy/workflows/DataPipelineWorkflow/start"
		HttpClient httpClient = HttpClientBuilder.create().build();
		// Create an instance of HttpPost with the desired URL
		//String postUrl = "https://study-kfn-possible-haven-418421-dot-usw1.datafusion.googleusercontent.com/api/v3/namespaces/default/apps/kfnstudy/workflows/DataPipelineWorkflow/start";
		String postUrl = cloudStorageBody.getData_fusion_pipeline_name();
		HttpPost httpPost = new HttpPost(postUrl);
		logger.info("postUrl:"+postUrl);
		// Add headers to the request
		
	    httpPost.setHeader("Authorization", "Bearer "+ getToken());

		httpPost.setHeader("Content-type", "application/json");

		// Set the request body
		String request = 
				 "{\"input.path\": \""+cloudStorageBody.getInputPath()+"\","+
				 "\"ouput.bqdataset\": \""+cloudStorageBody.getOuputBQDataset()+"\","+
				 "\"ouput.bqtable\": \""+cloudStorageBody.getOuputBQTable()+"\"}";
		
		
		StringEntity entity = new StringEntity(request);
		httpPost.setEntity(entity);
		// Execute the request and obtain the response
	
		HttpResponse httpResponse = httpClient.execute(httpPost);
		logger.info("Event: Calling Function:"+httpResponse.getStatusLine().getReasonPhrase());

		
	}
	
	private String getToken() throws ClientProtocolException, IOException {
		HttpClient httpClient = HttpClientBuilder.create().build();
		String scopes="https://www.googleapis.com/auth/cloud-platform";
		HttpGet httpGet= new HttpGet("http://metadata.google.internal/computeMetadata/v1/instance/service-accounts/default/token?scopes=" + scopes);
		httpGet.setHeader("Metadata-Flavor", "Google");
		ResponseHandler<String> handler = new BasicResponseHandler();
		JsonObject jsonObject = JsonParser.parseString( httpClient.execute(httpGet, handler)).getAsJsonObject();
		String accesstoken = jsonObject.get("access_token").getAsString();
		return accesstoken;
	}

}
