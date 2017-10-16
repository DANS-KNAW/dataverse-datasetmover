package nl.knaw.dans.dataverse.tools.datasetmover.dvnconnect;
import javax.ws.rs.*;

@Path("/")
public class DatasetMoverTool {

    @GET
    @Produces("text/html")
    public String help() {
        return "<h1>TODO : HELP</h1>";
    }
}
