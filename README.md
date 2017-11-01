# Dataverse Datasetmover Tool 
Replacing dataset to another subdataverse [JIRA-DDN-314](https://drivenbydata.atlassian.net/browse/DDN-314)

The tool uses "datasets/{id}/moveTo/{newDataverseAlias}" api that is available on dans-master dataverse.

How to install:
1. Download from https://github.com/DANS-KNAW/dataverse-datasetmover
2. mvn clean install
3. Deploy the dmt.war file to glassfish
4. Open http://<domain>/dmt/faces/index.html
