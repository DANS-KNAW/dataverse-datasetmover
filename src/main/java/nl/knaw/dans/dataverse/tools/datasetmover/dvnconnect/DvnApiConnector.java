package nl.knaw.dans.dataverse.tools.datasetmover.dvnconnect;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DvnApiConnector {

    private static final Logger LOG = LoggerFactory.getLogger(DvnApiConnector.class);

    private String username;
    private String apiToken;

    public boolean isSuperUser() {
        return superUser;
    }

    private void setSuperUser(boolean superUser) {
        this.superUser = superUser;
    }

    private boolean superUser;


    private String HOST = "http://localhost:8080/api/";

    public DvnApiConnector(String username, String password) {
        this.username = username;
        apiToken = getApiToken(username, password);
        //only superUser may use this service
        validateSuperUser();

    }
    private void validateSuperUser() {
        if (apiToken != null) {
            JsonObject jo = getResponseAsJsonObject(HOST + "admin/authenticatedUsers/" + username);
            if (jo != null) {
                JsonObject dataJsonObject = jo.getJsonObject("data");
                setSuperUser(dataJsonObject.getBoolean("superuser"));
            }
        }
    }


    private JsonObject getResponseAsJsonObject(String url) {
        JsonObject jsonObject = null;
        try {
            URL validUrl = getValidApiUrl(url);
            if (validUrl != null ) {
                JsonReader reader = Json.createReader(
                        new StringReader(IOUtils.toString(validUrl, "UTF-8")));
                jsonObject = reader.readObject();
                reader.close();
            } else
                LOG.error(url + " is not valid.");
        } catch (IOException e) {
            LOG.error("Failed to connect, for url: " + url + ". Message: " + e.getMessage());
        }
        return jsonObject;
    }

    private String getApiToken(String username, String password) {
        JsonObject jo = getResponseAsJsonObject(HOST + "builtin-users/" + username + "/api-token?password=" + password);
        if (jo != null && jo.getString("status").equals("OK")) {
            JsonObject dataJObject = jo.getJsonObject("data");
            if (dataJObject != null)
                return dataJObject.getString("message");
        }
        LOG.warn("No api token found for username '" + username + "' and password '" + password + "'.");
        return null;
    }


    public List<String> getAllDataverseAliases() {
        //There is no dataverse native api for retrieving a list of dataverses. So, we use solr search
        JsonObject jObject = getResponseAsJsonObject(HOST + "search?q=*&type=dataverse&key=" + apiToken + "&per_page=1000&sort=identifier&order=asc");
        if (jObject != null && jObject.getJsonObject("data") != null) {
            JsonArray jArrayItems = jObject.getJsonObject("data").getJsonArray("items");
            if (jArrayItems !=null)
                return jArrayItems.stream().map(json -> ((JsonObject) json).getString("identifier")).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public int findDatasetIdByIdentifier(String dataset) {
        String[] hdl = dataset.split("/");
        JsonObject jo = getResponseAsJsonObject(HOST + "datasets/:persistentId/?persistentId=hdl%3A" + hdl[0] + "%2F" + hdl[1] + "&key=" + apiToken);
        if (jo != null && jo.getString("status").equals("OK"))
            return jo.getJsonObject("data").getInt("id");
        LOG.warn("No dataset found. Dataset: " + dataset);
        return -1;
    }

    public int findDataverseIdByAlias(String alias) {
        JsonObject jo = getResponseAsJsonObject(HOST + "dataverses/" + alias + "?key=" + apiToken);
        if (jo != null && jo.getString("status").equals("OK"))
            return jo.getJsonObject("data").getInt("id");

        return -1;
    }

    public boolean updateDatasetOwner(int datasetId, String persistentId, String targetDataverseAlias) {

        JsonObject jsonObject;
        try {
            URL obj = new URL(HOST + "datasets/"+ datasetId + "/move/" + targetDataverseAlias + "?key=" + apiToken);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            con.setRequestMethod("POST");
            con.setDoOutput(true);
            if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                JsonReader reader = Json.createReader(
                        new StringReader(IOUtils.toString(con.getInputStream(), "UTF-8")));

                jsonObject = reader.readObject();
                reader.close();

                if (jsonObject != null && jsonObject.getString("status").equals("OK")) {
                    //run index
                    String[] hdl = persistentId.split("/");
                    JsonObject joi = getResponseAsJsonObject(HOST + "admin/index/dataset?persistentId=hdl%3A" + hdl[0] + "%2F" + hdl[1]);
                    return (joi != null && joi.getString("status").equals("OK"));
                }
                return false;
            }


        } catch (IOException e) {
            LOG.error("Failed to connect, for url: " + targetDataverseAlias + ". Message: " + e.getMessage());
        }

        return false;
    }

    private URL getValidApiUrl(String url) {
        URL validUrl;
        try {
            validUrl = new URL(url);
            HttpURLConnection huc = (HttpURLConnection)validUrl.openConnection();
            int rc = huc.getResponseCode();
            if (rc == HttpURLConnection.HTTP_OK )
                return validUrl;
            else
                LOG.error(url + " response gives status other 200 (HTTP_OK). Response code: " + rc);
        } catch (MalformedURLException e) {
            LOG.error("MalformedURLException, message: " + e.getMessage());
        } catch (IOException e) {
            LOG.error("Failed to connect, for url: " + url + ". Message: " + e.getMessage());
        }
        return null;
    }

}


