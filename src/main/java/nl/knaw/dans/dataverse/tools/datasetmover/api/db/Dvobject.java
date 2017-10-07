package nl.knaw.dans.dataverse.tools.datasetmover.api.db;


import javax.persistence.*;
import java.io.Serializable;


//update dvobject set owner_id=(select id from dataverse where alias='socialpsychology') where id in (select id from dataset where identifier='YOSOSF');
@Entity
@Table (name="dvobject")
public class Dvobject implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    public Long getId() {
        return id;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    @Column(name="owner_id")
    Long ownerId;
}
