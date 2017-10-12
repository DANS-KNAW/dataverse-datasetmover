package nl.knaw.dans.dataverse.tools.datasetmover.api;

import nl.knaw.dans.dataverse.tools.datasetmover.api.db.DvobjectBean;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.*;
import java.io.*;
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
            LOG.error("IOException, message: " + e.getMessage());
        }
        return jsonObject;
    }

    private String getApiToken(String username, String password) {
        JsonObject jo = getResponseAsJsonObject(HOST + "/builtin-users/" + username + "/api-token?password=" + password);
        if (jo != null && jo.getString("status").equals("OK")) {
            JsonObject dataJObject = jo.getJsonObject("data");
            if (dataJObject != null)
                return dataJObject.getString("message");
        }
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
        return new ArrayList<String>();
    }

    public int findDatasetIdByIdentifier(String dataset) {
        String[] hdl = dataset.split("/");
        //http://ddvn.dans.knaw.nl:8080/api/datasets/:persistentId/?persistentId=hdl:10411/10021
        JsonObject jo = getResponseAsJsonObject(HOST + "datasets/:persistentId/?persistentId=hdl%3A" + hdl[0] + "%2F" + hdl[1] + "&key=" + apiToken);
        if (jo != null && jo.getString("status").equals("OK"))
            return jo.getJsonObject("data").getInt("id");

        return -1;
    }

    public int findDataverseIdByAlias(String alias) {
        //http://ddvn.dans.knaw.nl:8080/api/dataverses/4tu?key=c7c57eaa-a133-4f2d-976a-5a40e69e7435
        JsonObject jo = getResponseAsJsonObject(HOST + "dataverses/" + alias + "?key=" + apiToken);
        if (jo != null && jo.getString("status").equals("OK"))
            return jo.getJsonObject("data").getInt("id");

        return -1;
    }

    public boolean updateDatasetOwner(String dataverseAlias, int datasetId, String persistentId) {
        //Unfortunately, there is no api for updating dataset owner
        DvobjectBean dob = new DvobjectBean();
        boolean succes = dob.updateOwnerId(findDataverseIdByAlias(dataverseAlias), datasetId);
        if (succes) {
            //run index
            //curl http://localhost:8080/api/admin/index/dataset?persistentId=hdl:10411/YOSOSF
            String[] hdl = persistentId.split("/");
            JsonObject jo = getResponseAsJsonObject(HOST + "admin/index/dataset?persistentId=hdl%3A" + hdl[0] + "%2F" + hdl[1]);
            succes = (jo != null && jo.getString("status").equals("OK"));
        }
        return succes;
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
            LOG.error("IOException, message: " + e.getMessage());
        }
        return null;
    }

}


