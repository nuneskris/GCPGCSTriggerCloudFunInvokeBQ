NOTES again

gcloud init

export ORAC_GCP_ETLPIPELINE_ENV=development

export ORAC_GCP_ETLPIPELINE_BUCKET_BASE=trigger_cloud_function_kfn_study

export ORAC_GCP_ETLPIPELINE_BUCKET_NAME=${ORAC_GCP_ETLPIPELINE_BUCKET_BASE}_${ORAC_GCP_ETLPIPELINE_ENV}
echo ${ORAC_GCP_ETLPIPELINE_BUCKET_NAME}
export ORAC_GCP_ETLPIPELINE_BUCKET_FOLDER_RUN=$(date +%Y-%m-%d)
echo ${ORAC_GCP_ETLPIPELINE_BUCKET_FOLDER_RUN}
export ORAC_GCP_ETLPIPELINE_BUCKET_FOLDER=gs://${ORAC_GCP_ETLPIPELINE_BUCKET_NAME}/landing/${ORAC_GCP_ETLPIPELINE_BUCKET_FOLDER_RUN}
echo ${ORAC_GCP_ETLPIPELINE_BUCKET_FOLDER}


export AUTH_TOKEN=$(gcloud auth print-access-token)
export INSTANCE_ID=study-kfn
export CDAP_ENDPOINT=$(gcloud beta data-fusion instances describe \
    --location=us-west1 \
    --format="value(apiEndpoint)" \
  ${INSTANCE_ID})
  
export ORAC_GCP_ETLPIPELINE_CALL_DF_PIPELINE_NAME=kfnstudy
  
echo ${ORAC_GCP_ETLPIPELINE_CALL_DF_PIPELINE_NAME}
  
echo ${CDAP_ENDPOINT}

export ORAC_GCP_ETLPIPELINE_CALL_DF_URL=${CDAP_ENDPOINT}/v3/namespaces/default/apps/${ORAC_GCP_ETLPIPELINE_CALL_DF_PIPELINE_NAME}/workflows/DataPipelineWorkflow/start
  
echo ${ORAC_GCP_ETLPIPELINE_CALL_DF_URL}

	    
#1: CloudFunction Triggered by CloudStorage

## we need encode the runtime arguments which are in JSON
export ORAC_GCP_ETLPIPELINE_RUNTIME_ENV_VAR=WwogIHsKICAgICJ0YWJsZVZpZXdOYW1lIjogIlRURElOIiwKICAgICJvdXB1dEJRRGF0YXNldCI6ICJPcmEyR1NDMkJRIiwKICAgICJvdXB1dEJRVGFibGUiOiAiVFRESU5WMDAxMTAwIgogIH0sCiAgewogICAgInRhYmxlVmlld05hbWUiOiAiVFRJSVRNIiwKICAgICJvdXB1dEJRRGF0YXNldCI6ICJPcmEyR1NDMkJRIiwKICAgICJvdXB1dEJRVGFibGUiOiAiVFRJSVRNMDAxMTAwIgogIH0KXQ==


gcloud functions delete ora-gcp-ingest-finalize-function-ETL-${ORAC_GCP_ETLPIPELINE_ENV} --gen2 --region us-west1 


gcloud functions deploy ora-gcp-ingest-finalize-function-ETL-${ORAC_GCP_ETLPIPELINE_ENV} \
--gen2 \
--runtime=java17 \
--region=us-west1 \
--source=. \
--entry-point=com.java.kfn.study.gcp.cloudfunction.CloudStorageEventHandling \
--memory=512MB \
--trigger-event-filters="type=google.cloud.storage.object.v1.finalized" \
--trigger-event-filters="bucket=${ORAC_GCP_ETLPIPELINE_BUCKET}" \
--set-env-vars reusable_pipeline_runtime_args=${ORAC_GCP_ETLPIPELINE_RUNTIME_ENV_VAR},data_fusion_pipeline_name=${ORAC_GCP_ETLPIPELINE_CALL_DF_URL}

# 2 Ingest a file into GCS

## 2.1 Using Metadata to on GCS File. Use x-goog-meta- prefix for the same. ex:gsutil -h x-goog-meta-reviewer:jane cp mycode.java gs://bucket/reviews
## 2.2 Using Folder Landing Foilder Structure: /landing/<extract-name>/<tablename>


gsutil -h x-goog-meta-table_view_name:TTDIN \
	   -h x-goog-meta-job_name:oracle_extract \
	   -h x-goog-meta-extract_last_modified_date:2024-03-07T14:47:24.899+00:00 \
	    cp TTDINV001100NEW.csv ${ORAC_GCP_ETLPIPELINE_BUCKET_FOLDER}/TTDIN/TTDINV001100Y.csv
	    

gsutil -h x-goog-meta-table_view_name:TTIITM\
	   -h x-goog-meta-job_name:oracle_extract \
	   -h x-goog-meta-extract_last_modified_date:2024-03-07T14:47:24.899+00:00 \
	    cp TTIITM001100.csv ${ORAC_GCP_ETLPIPELINE_BUCKET_FOLDER}/TTIITM/TTIITM001100T.csv
	    
## 2.3 Read the logs
gcloud functions logs read ora-gcp-ingest-finalize-function-ETL-${ORAC_GCP_ETLPIPELINE_ENV} --region us-west1 --gen2 --limit=20





//curl -X POST -H "Authorization: Bearer ${AUTH_TOKEN}" \
		 -H "Content-Type: application/json"  \
		 -d '{"input.path":"gs://trigger_cloud_function_kfn_study_development/landing/2024-03-30/TTDIN/TTDINV0011003.csv","ouput.bqdataset": "Ora2GSC2BQ","ouput.bqtable": "TTDINV001100"}' \
		 "${CDAP_ENDPOINT}/v3/namespaces/default/apps/kfnstudy/workflows/DataPipelineWorkflow/start"

