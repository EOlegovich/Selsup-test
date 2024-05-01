package org.example;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.http.client.HttpClient;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;

public class CrptApi {
  private static final String DOCUMENT_TEST_DATA = "{\"description\":{\"participantInn\":\"string\"},\"doc_id\":\"string\",\"doc_status\":\"string\",\"doc_type\":\"LP_INTRODUCE_GOODS\",\"importRequest\":true,\"owner_inn\":\"string\",\"participant_inn\":\"string\",\"producer_inn\":\"string\",\"production_date\":\"2020-01-23\",\"production_type\":\"string\",\"products\":[{\"certificate_document\":\"string\",\"certificate_document_date\":\"2020-01-23\",\"certificate_document_number\":\"string\",\"owner_inn\":\"string\",\"producer_inn\":\"string\",\"production_date\":\"2020-01-23\",\"tnved_code\":\"string\",\"uit_code\":\"string\",\"uitu_code\":\"string\"}],\"reg_date\":\"2020-01-23\",\"reg_number\":\"string\"}";
  private static final String TEST_SIGNATURE = "111test-signature111";
  private final TimeUnit timeUnit;
  private final int requestLimit;
  private int requestCount;
  private long lastRequestTimeMillis;

  public CrptApi(TimeUnit timeUnit, int requestLimit) {
    this.timeUnit = timeUnit;
    this.requestLimit = requestLimit;
    this.lastRequestTimeMillis = System.currentTimeMillis();
  }

  public synchronized void callApi() throws InterruptedException {
    long currentTimeMillis = System.currentTimeMillis();
    long elapsedTime = currentTimeMillis - lastRequestTimeMillis;

    if (elapsedTime >= timeUnit.toMillis(1)) {
      requestCount = 0;
      lastRequestTimeMillis = currentTimeMillis;
    }

    if (requestCount >= requestLimit) {
      long sleepTime = (lastRequestTimeMillis + timeUnit.toMillis(1)) - currentTimeMillis;
      if (sleepTime > 0) {
        Thread.sleep(sleepTime);
      }

      requestCount = 0;
      lastRequestTimeMillis = System.currentTimeMillis();
    }

    Document document;
    ObjectMapper mapper = new ObjectMapper();
    try {
      document = mapper.readValue(DOCUMENT_TEST_DATA, Document.class);
    }
    catch (JsonProcessingException e) {
      throw new RuntimeException("Failed serialize Document data", e);
    }
    ApiProvider.createDocument(document, TEST_SIGNATURE);
    requestCount++;
  }

  public static class ApiProvider {
    private static final String BASE_URL = "https://ismp.crpt.ru/api/v3/";

    public static void createDocument(Document document, String signature) {
      try {
        ObjectMapper mapper = new ObjectMapper();
        String jsonDoc = mapper.writeValueAsString(document);
        String requestJson = "{\"document\": " + jsonDoc + ", \"signature\": \"" + signature + "\"}";
        StringEntity entity = new StringEntity(requestJson);
        executePOST("lk/documents/create", entity);
      }
      catch (JsonProcessingException | UnsupportedEncodingException e) {
        throw new RuntimeException("Failed create document", e);
      }
    }

    private static void executePOST(String methodUrl, StringEntity requestBody) {
      HttpClient client = HttpClients.createDefault();
      HttpPost httpPost = new HttpPost(BASE_URL + methodUrl);
      httpPost.setEntity(requestBody);
      httpPost.setHeader("Content-Type", "application/json");
      try {
        client.execute(httpPost);
      }
      catch (IOException e) {
        throw new RuntimeException("Failed execute POST request", e);
      }
    }
  }

  public static class Document {
    @JsonProperty("description")
    private Description description;
    @JsonProperty("doc_id")
    private String docId;
    @JsonProperty("doc_status")
    private String docStatus;
    @JsonProperty("doc_type")
    private String docType;
    @JsonProperty("importRequest")
    private boolean importRequest;
    @JsonProperty("owner_inn")
    private String ownerInn;
    @JsonProperty("participant_inn")
    private String participantInn;
    @JsonProperty("producer_inn")
    private String producerInn;
    @JsonProperty("production_date")
    private String productionDate;
    @JsonProperty("production_type")
    private String productionType;
    @JsonProperty("products")
    private List<Product> products;
    @JsonProperty("reg_date")
    private String regDate;
    @JsonProperty("reg_number")
    private String regNumber;

    public static class Description {
      @JsonProperty("participantInn")
      private String participantInn;
    }

    public static class Product {
      @JsonProperty("certificate_document")
      private String certificateDocument;
      @JsonProperty("certificate_document_date")
      private String certificateDocumentDate;
      @JsonProperty("certificate_document_number")
      private String certificateDocumentNumber;
      @JsonProperty("owner_inn")
      private String ownerInn;
      @JsonProperty("producer_inn")
      private String producerInn;
      @JsonProperty("production_date")
      private String productionDate;
      @JsonProperty("tnved_code")
      private String tnvedCode;
      @JsonProperty("uit_code")
      private String uitCode;
      @JsonProperty("uitu_code")
      private String uituCode;
    }
  }
}