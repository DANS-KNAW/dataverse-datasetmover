package nl.knaw.dans.dataverse.tools.datasetmover.web;

import nl.knaw.dans.dataverse.tools.datasetmover.dvnconnect.DvnApiConnector;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import java.util.ArrayList;
import java.util.List;

@ManagedBean
@SessionScoped
public class DmtBean {

    private String userName;
    private String password;
    private DvnApiConnector ldac;
    private List<String> aliases = new ArrayList<String>();
    private String dataset; // contains the Persistent ID with 'hdl:' or 'doi:' and prefix

    public int getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(int datasetId) {
        this.datasetId = datasetId;
    }

    private int datasetId;
    private String alias;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String validateUserLogin() {
        clear();
        String navResult = "success";

        ldac = new DvnApiConnector(userName, password);

        boolean superUserAuthenticated = ldac.isSuperUser();
        if ( ! superUserAuthenticated ) {
            errorMsg("Invalid Credentials or not super user");
            navResult = "index";
        }
        return navResult;
    }

    public void setAliases(List<String> aliases) {
        this.aliases = aliases;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public String getDataset() {
        return dataset;
    }

    public void setDataset(String dataset) {
        this.dataset = dataset;
    }

    public void validateDataset(ActionEvent actionEvent) {
        if (dataset != null && !dataset.trim().isEmpty())
            datasetId = ldac.findDatasetIdByIdentifier(dataset);

        if (datasetId > 0) {
            aliases = ldac.getAllDataverseAliases();
            infoMsg(dataset + " has id: " + datasetId);
        } else {
            aliases = new ArrayList<String>();
            errorMsg("No dataset '" + dataset + "' found.");
        }
    }

    public void move(ActionEvent actionEvent){
        if (alias != null && datasetId > 0) {
            String persistentId = dataset;
            boolean success = ldac.updateDatasetOwner(datasetId, persistentId, alias);
            if (success)
                infoMsg("The dataset PID: " + dataset + " is moved to '" + alias + "'.");
            else
                errorMsg("FATAL ERROR! Dataverse alias: " + alias + ". DatasetId: " + datasetId + " Dataset: " + dataset);
        } else
            errorMsg("Error! No dataset found and/or no target dataverse is selected.");
    }

    private void errorMsg(String msg) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, ""));
    }

    private void infoMsg(String msg) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, msg, ""));
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public void handleChange(final ValueChangeEvent event){
        alias = (String)event.getNewValue();
        if (dataset != null && alias != null)
            infoMsg("The dataset PID: " + dataset + " will be moved to '" + alias + "'.");
    }

    private void clear(){
        dataset = "";
        alias = "";
        aliases = new ArrayList<String>();
    }

    public String logout() {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "/index.xhtml?faces-redirect=true";
    }
}