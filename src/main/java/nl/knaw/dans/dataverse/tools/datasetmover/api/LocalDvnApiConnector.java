package nl.knaw.dans.dataverse.tools.datasetmover.api;

import nl.knaw.dans.dataverse.tools.datasetmover.api.db.DvobjectBean;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LocalDvnApiConnector {

    private static final Logger LOG = LoggerFactory.getLogger(LocalDvnApiConnector.class);

    private final String username;
    private String apiToken;
    public static final String STATUS_ERROR = "Bad username or password";
    public static final String ERROR_DATASET_NOT_FOUND = "Dataset not found";
    public static final String ERROR_DATASET_MORE_THAN_ONE = "More than one dataset found.";
    public static final String ERROR_INVALID_PREFIX = "DANS Handle prefixes are 10695 and 10411";

    protected String HOST = "http://localhost:8080/api/";

    public LocalDvnApiConnector(String username, String password) {
        this.username = username;
        apiToken = getApiToken(username, password);
    }

    private boolean isValidUser() {
        return apiToken != null;
    }

    public boolean isSuperUser(){
        if (isValidUser()) {
            JsonObject jo2 = getResponseAsJsonObject("http://localhost:8080/api/admin/authenticatedUsers/" + username);
            JsonObject dataJsonObject = jo2.getJsonObject("data");
            return dataJsonObject.getBoolean("superuser");
        }
        return false;
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
            }
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
        return jsonObject;
    }

    private String getApiToken(String username, String password) {

        String builinUserApiUrl = HOST + "/builtin-users/" + username + "/api-token?password=" + password;
        JsonObject jo = getResponseAsJsonObject(builinUserApiUrl);
        if (jo != null && jo.getString("status").equals("OK")) {
            JsonObject dataJObject = jo.getJsonObject("data");
            return dataJObject.getString("message");
        }
        return null;
    }


    public List<String> getAllDataverseAliases() {
        if (isSuperUser()) {
            String url = HOST + "search?q=*&type=dataverse&key=" + apiToken + "&per_page=1000&sort=identifier&order=asc";
            JsonObject jObject = getResponseAsJsonObject(url);
            JsonObject dataJObject = jObject.getJsonObject("data");
            JsonArray jArrayItems = dataJObject.getJsonArray("items");
            return jArrayItems.stream().map(json -> ((JsonObject) json).getString("identifier")).collect(Collectors.toList());
        }
        return new ArrayList<String>();
    }

    public int findDatasetIdByIdentifier(String dataset) {
        String[] hdl = dataset.split("/");
        //http://ddvn.dans.knaw.nl:8080/api/datasets/:persistentId/?persistentId=hdl:10411/10021
        String url = HOST + "datasets/:persistentId/?persistentId=hdl%3A" + hdl[0] + "%2F" + hdl[1] + "&key=" + apiToken;
        JsonObject jo = getResponseAsJsonObject(url);
        if (jo != null && jo.getString("status").equals("OK"))
            return jo.getJsonObject("data").getInt("id");

        return -1;
    }

    public int findDataverseIdByAlias(String alias) {
        //http://ddvn.dans.knaw.nl:8080/api/dataverses/4tu?key=c7c57eaa-a133-4f2d-976a-5a40e69e7435
        if (isSuperUser()) {
            String url = HOST + "dataverses/" + alias + "?key=" + apiToken;
            JsonObject jo = getResponseAsJsonObject(url);
            if (jo != null && jo.getString("status").equals("OK"))
                return jo.getJsonObject("data").getInt("id");
        }
        return -1;
    }

    public boolean updateDatasetOwner(String dataverseAlias, int datasetId, String persistentId) {
        DvobjectBean dob = new DvobjectBean();
        boolean succes = dob.updateOwnerId(findDataverseIdByAlias(dataverseAlias), datasetId);
        if (succes) {
            //curl http://localhost:8080/api/admin/index/dataset?persistentId=hdl:10411/YOSOSF
            String[] hdl = persistentId.split("/");
            String url = HOST + "datasets/:persistentId/?persistentId=hdl%3A" + hdl[0] + "%2F" + hdl[1];
            JsonObject jo = getResponseAsJsonObject(url);
            succes = (jo != null && jo.getString("status").equals("OK"));
        }
        return succes;
    }

    private URL getValidApiUrl(String url) throws IOException{
       URL validUrl = new URL(url);
       if (((HttpURLConnection)validUrl.openConnection()).getResponseCode() == HttpURLConnection.HTTP_OK)
            return validUrl;
        return null;
    }

}


