# Dataverse Datasetmover Tool 
Move a dataset to another dataverse.

The tool uses "datasets/{id}/move/{newDataverseAlias}" API that is available on Dataverse 4.8.6 and later.

The Handle prefixes are still hardcoded, so if you have DOIs or other prefixes it's not usable yet!


How to install:
1. Download from [https://github.com/DANS-KNAW/dataverse-datasetmover](https://github.com/DANS-KNAW/dataverse-datasetmover).
2. Either use the released war file, or download the sources and build with `mvn clean install`.
3. Deploy the `dmt.war` file to Glassfish where Dataverse is also deployed.
4. Open `http://<domain>/dmt/faces/index.html`

Instructions for usage:
1. Browse to `dmt/faces/index.xhtml` on the same host as you have the Dataverse application instance running.
2. Login as dataverse admin user (only works with admin rights)
3. Select the prefix (must be handle) and fill out the handle 'id' of the dataset you want to move.
4. Click 'Validate'. If the handle is correct it will show the dataset id (dataverse specific number).
5. Select the target (sub)dataverse where the dataset should be moved to. 
   Use the dataverse alias, which is visible in the dataverse page URL and also shown as 'identifier' under the General information'. 
   Note that the dataverse should be published.
6. Click the 'Move' button.
7. A dialog pups up, click 'Yes' if sure about this move.
8. A message should show up indicating the move succeeded.
