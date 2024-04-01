package com.java.kfn.study.gcp.cloudfunction;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.logging.Logger;

import org.apache.http.client.ClientProtocolException;

import com.google.cloud.functions.CloudEventsFunction;

import com.java.kfn.study.gcp.cloudfunction.pojos.CloudStorageBody;
import com.java.kfn.study.gcp.cloudfunction.pojos.CloudStorageBody.CloudStorageBodyBuilder;

import io.cloudevents.CloudEvent;

public class CloudStorageEventHandling implements CloudEventsFunction {
	  private static final Logger logger = Logger.getLogger(CloudStorageEventHandling.class.getName());

	  @Override
	  public void accept(CloudEvent event) throws ClientProtocolException, IOException, StudyPipelineException {

		  
		  String reusable_pipeline_runtime_args = new String( Base64.getDecoder().decode(System.getenv("reusable_pipeline_runtime_args")));
		  String data_fusion_pipeline_name = System.getenv("data_fusion_pipeline_name");
		 
		//  logger.info(reusable_pipeline_runtime_args);
		 
		 if (event.getData() == null) {
		      logger.warning("No data found in cloud event payload!");
		      return;
		 } else  {
			 String cloudEventData = new String(event.getData().toBytes(), StandardCharsets.UTF_8);
			 //logger.info(cloudEventData);
			
			 CloudStorageBody cloudStorageBody= new CloudStorageBodyBuilder(cloudEventData, reusable_pipeline_runtime_args, data_fusion_pipeline_name).build();
			 
			 
			 logger.info("Event: " + event.getId() +", Event Type: " + event.getType() +  "  :ouputBQTable:"+cloudStorageBody.getOuputBQTable() +   "  :ouputBQDataset:"+cloudStorageBody.getOuputBQDataset() +  "	CBucket: " + cloudStorageBody.getBucket() +", File: " + cloudStorageBody.getFileName()+", InputPath: " + cloudStorageBody.getInputPath()+", TableViewName: " + cloudStorageBody.getTableViewName());
			 
			 new CallCloudFunctionPipeline().call(cloudStorageBody);
	     
	    }
	  }
	}

