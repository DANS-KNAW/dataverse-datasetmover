package nl.knaw.dans.dataverse.tools.datasetmover.api;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.*;
import java.util.List;

@Path("/")
public class DatasetMoverTool {

    @GET
    @Produces("text/html")
    public String help() {
        return "<h1>TODO : HELP</h1>";
    }
}
