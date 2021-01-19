package com.aralapps;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class Main {

    //Constants
    public static String accessTokenBaseUrl = "https://connect-api.cloud.huawei.com/api/oauth2/v1/token";
    public static String reportsApiBaseUrl = "https://connect-api.cloud.huawei.com/api/report/distribution-operation-quality/v1/";

    public static String grantType = "client_credentials"; //Fixed value.
    public static String clientId = "Your Client ID";
    public static String clientSecret = "Your Client Secret";

    public static String downloadAndInstallationBodyUrl = "appDownloadExport/";
    public static String iapBodyUrl = "IAPExport/";
    public static String paidDownloadBodyUrl = "orderDetailExport/";
    public static String installationFailureDataBodyUrl = "appDownloadFailExport/";

    public static void main(String[] args) {

        //Mandatory input parameters for all report types of Reports API for API Client mode.
        String accessToken = getAccessToken();
        String appId = "YOUR APP ID";
        String language = "LANGUAGE"; // i.e "en-US"
        String startTime = "START DATE OF THE REPORT"; // i.e. "20201107"
        String endTime = "END DATE OF THE REPORT"; // i.e. "20201125"
        //--------------------------------------------------------------------------------------------

        getDownloadAndInstallationReport(downloadAndInstallationBodyUrl, accessToken, appId, language, startTime, endTime);
        getIapReport(iapBodyUrl, accessToken, appId, language, startTime, endTime);
        getPaidDownloadReport(paidDownloadBodyUrl, accessToken, appId, language, startTime, endTime);
        getInstallationFailureDataReport(installationFailureDataBodyUrl, accessToken, appId, language, startTime, endTime);
    }


    /**
     * Obtain Installation Failure Data Report download URL.
     */
    private static void getInstallationFailureDataReport(String bodyUrl, String token, String appId, String language, String startTime, String endTime) {

        //Outputs:
        String fileUrl = null;
        String packageName = null;
        String ret = null;

        //Optional input parameters:
        List<String> filterCondition = new ArrayList<>(16);
        List<String> filterConditionValue = new ArrayList<>(16);
        List<String> groupBy = new ArrayList<>(16);

        //Filters are deviceName, downloadType and countryId.
        filterCondition.add(0, "deviceName");
        filterConditionValue.add(0, "P30");
        filterCondition.add(1, "downloadType");
        filterConditionValue.add(1, "1"); // 1 (new app package download) and 2 (updated app package download).

        groupBy.add("date"); //Statistics are grouped by date, deviceName, downloadType, or countryId.
        groupBy.add("deviceName");

        String filter = IntStream.range(0, filterCondition.size()).mapToObj(i -> "&filterCondition=" + filterCondition.get(i) + "&filterConditionValue=" + filterConditionValue.get(i)).collect(Collectors.joining());
        String group = groupBy.stream().map(s -> "&groupBy=" + s).collect(Collectors.joining());

        HttpGet get = new HttpGet(reportsApiBaseUrl + bodyUrl + appId + "?" +
                "language=" + language + "&" +
                "startTime=" + startTime + "&" +
                "endTime=" + endTime + "&" +
                filter +
                group);

        get.setHeader("Authorization", "Bearer " + token);
        get.setHeader("client_id", clientId);

        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            CloseableHttpResponse httpResponse = httpClient.execute(get);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                BufferedReader br =
                        new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent(), Consts.UTF_8));
                String result = br.readLine();
                JSONObject object = JSON.parseObject(result);
                fileUrl = object.getString("fileURL");
                ret = object.getString("ret");
                packageName = object.getString("packName");

                System.out.println("Server Message for Installation Failure Data Report: " + ret);
                System.out.println("Package name: " + packageName);
                System.out.println("Installation Failure Data Report Download URL: " + fileUrl);
            }
            get.releaseConnection();
            httpClient.close();
        } catch (Exception e) {
            System.out.println("Failed to get download URL for Installation Failure Data Report.");
        }
    }

    /**
     * Obtain Paid Download Report download URL.
     */
    private static void getPaidDownloadReport(String bodyUrl, String token, String appId, String language, String startTime, String endTime) {

        //Outputs:
        String fileUrl = null;
        String ret = null;

        //Mandatory input parameters:
        List<String> filterCondition = new ArrayList<>(16);
        List<String> filterConditionValue = new ArrayList<>(16);

        //Filters are countryId, orderID and orderStatus. countryId is mandatory.
        filterCondition.add(0, "countryId");
        filterConditionValue.add(0, "TR");

        String filter = IntStream.range(0, filterCondition.size()).mapToObj(i -> "&filterCondition=" + filterCondition.get(i) + "&filterConditionValue=" + filterConditionValue.get(i)).collect(Collectors.joining());

        HttpGet get = new HttpGet(reportsApiBaseUrl + bodyUrl + appId + "?" +
                "language=" + language + "&" +
                "startTime=" + startTime + "&" +
                "endTime=" + endTime + "&" +
                filter);

        get.setHeader("Authorization", "Bearer " + token);
        get.setHeader("client_id", clientId);

        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            CloseableHttpResponse httpResponse = httpClient.execute(get);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                BufferedReader br =
                        new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent(), Consts.UTF_8));
                String result = br.readLine();
                JSONObject object = JSON.parseObject(result);
                fileUrl = object.getString("fileURL");
                ret = object.getString("ret");

                System.out.println("Server Message for Paid Download Report: " + ret);
                System.out.println("Paid Download Report Download URL: " + fileUrl);
            }
            get.releaseConnection();
            httpClient.close();
        } catch (Exception e) {
            System.out.println("Failed to get download URL for Paid Download Report.");
        }
    }

    /**
     * Obtain In-App Payment Report download URL.
     */
    private static void getIapReport(String bodyUrl, String token, String appId, String language, String startTime, String endTime) {

        //Outputs:
        String fileUrl = null;
        String packageName = null;
        String ret = null;

        //Mandatory input parameter:
        String currency = "USD"; //Only CNY, EUR, USD, GBP, and JPY are supported.

        //Optional input parameters:
        List<String> filterCondition = new ArrayList<>(16);
        List<String> filterConditionValue = new ArrayList<>(16);
        String groupBy = "businessType"; //Options are date, countryId, businessType. date is default.
        String exportType = "EXCEL"; //Options are CVS and EXCEL. CVS is default.

        //Filters are countryId, businessType.
        filterCondition.add(0, "countryId");
        filterConditionValue.add(0, "TR");

        String filter = IntStream.range(0, filterCondition.size()).mapToObj(i -> "&filterCondition=" + filterCondition.get(i) + "&filterConditionValue=" + filterConditionValue.get(i)).collect(Collectors.joining());

        HttpGet get = new HttpGet(reportsApiBaseUrl + bodyUrl + appId + "?" +
                "language=" + language + "&" +
                "startTime=" + startTime + "&" +
                "endTime=" + endTime + "&" +
                "currency=" + currency + "&" +
                filter + "&" +
                "groupBy=" + groupBy + "&" +
                "exportType=" + exportType);

        get.setHeader("Authorization", "Bearer " + token);
        get.setHeader("client_id", clientId);

        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            CloseableHttpResponse httpResponse = httpClient.execute(get);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                BufferedReader br =
                        new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent(), Consts.UTF_8));
                String result = br.readLine();
                JSONObject object = JSON.parseObject(result);
                fileUrl = object.getString("fileURL");
                packageName = object.getString("packName");
                ret = object.getString("ret");

                System.out.println("Server Message for In-App Payment Report: " + ret);
                System.out.println("Package name: " + packageName);
                System.out.println("Download and In-App Payment Report Download URL: " + fileUrl);
            }
            get.releaseConnection();
            httpClient.close();
        } catch (Exception e) {
            System.out.println("Failed to get download URL for In-App Payment Report.");
        }
    }

    /**
     * Obtain Download and Installation Report download URL.
     */
    private static void getDownloadAndInstallationReport(String bodyUrl, String token, String appId, String language, String startTime, String endTime) {

        //Outputs:
        String fileUrl = null;
        String packageName = null;
        String ret = null;

        //Optional input parameters:
        List<String> filterCondition = new ArrayList<>(16);
        List<String> filterConditionValue = new ArrayList<>(16);
        String groupBy = "appVersion"; //Options are date, countryId, businessType, appVersion. date is default.
        String exportType = "EXCEL"; //Options are CVS and EXCEL. CVS is default.

        //Filters are countryId, businessType and appVersion.
        filterCondition.add(0, "countryId");
        filterConditionValue.add(0, "TR");

        String filter = IntStream.range(0, filterCondition.size()).mapToObj(i -> "&filterCondition=" + filterCondition.get(i) + "&filterConditionValue=" + filterConditionValue.get(i)).collect(Collectors.joining());

        HttpGet get = new HttpGet(reportsApiBaseUrl + bodyUrl + appId + "?" +
                "language=" + language + "&" +
                "startTime=" + startTime + "&" +
                "endTime=" + endTime + "&" +
                filter + "&" +
                "groupBy=" + groupBy + "&" +
                "exportType=" + exportType);

        get.setHeader("Authorization", "Bearer " + token);
        get.setHeader("client_id", clientId);

        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            CloseableHttpResponse httpResponse = httpClient.execute(get);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                BufferedReader br =
                        new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent(), Consts.UTF_8));
                String result = br.readLine();
                JSONObject object = JSON.parseObject(result);
                fileUrl = object.getString("fileURL");
                packageName = object.getString("packName");
                ret = object.getString("ret");

                System.out.println("Server Message for Download and Installation Report: " + ret);
                System.out.println("Package name: " + packageName);
                System.out.println("Download and Installation Report Download URL: " + fileUrl);
            }
            get.releaseConnection();
            httpClient.close();
        } catch (Exception e) {
            System.out.println("Failed to get download URL for Download and Installation Report.");
        }
    }


    /**
     * Obtains an access token for Client API.
     */
    private static String getAccessToken() {

        //Outputs:
        String token = null;
        Long expireDate;

        try {
            HttpPost post = new HttpPost(accessTokenBaseUrl);
            JSONObject keyString = new JSONObject();
            keyString.put("grant_type", grantType);
            keyString.put("client_id", clientId);
            keyString.put("client_secret", clientSecret);
            StringEntity entity = new StringEntity(keyString.toString(), StandardCharsets.UTF_8);
            entity.setContentEncoding("UTF-8");
            entity.setContentType("application/json");
            post.setEntity(entity);
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpResponse response = httpClient.execute(post);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                BufferedReader br =
                        new BufferedReader(new InputStreamReader(response.getEntity().getContent(), Consts.UTF_8));
                String result = br.readLine();
                JSONObject object = JSON.parseObject(result);
                token = object.getString("access_token");
                expireDate = object.getLong("expires_in");

                System.out.println("You have an access token and expires in " + expireDate + " : " + token);
            }
            post.releaseConnection();
            httpClient.close();
        } catch (Exception e) {
            System.out.println("Failed to get access token.");
        }
        return token;
    }
}
