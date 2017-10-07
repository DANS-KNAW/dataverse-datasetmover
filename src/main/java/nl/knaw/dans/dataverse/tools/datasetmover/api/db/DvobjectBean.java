package nl.knaw.dans.dataverse.tools.datasetmover.api.db;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Persistence;
import java.util.List;

@Stateless
public class DvobjectBean {

    private EntityManager em;
    public DvobjectBean() {
        em = Persistence.createEntityManagerFactory("VDCNet-ejbPU").createEntityManager();
    }

    public boolean updateOwnerId(int ownerId, int id) {
        //update dvobject set owner_id=(select id from dataverse where alias='socialpsychology') where id in (select id from dataset where identifier='YOSOSF');
        try {
            em.getTransaction().begin();
            em.createQuery("UPDATE Dvobject d SET d.ownerId=" + ownerId + " WHERE d.id =" + id)
                    .executeUpdate();
            em.getTransaction().commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
