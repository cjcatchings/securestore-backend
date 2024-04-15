package com.ccatchings.securestore.database.hibernate.model;

import jakarta.annotation.Nonnull;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name="pail_folder")
public class PailFolder implements DBBackedModel {
    @Id
    @GeneratedValue
    @Column(name="id")
    private Long id;

    @JoinColumn(name="parent")
    @ManyToOne(targetEntity=PailFolder.class)
    private PailFolder parent;

    @JoinColumn(name="pail_id")
    @ManyToOne(targetEntity=Pail.class)
    @Nonnull
    private Pail pail;

    @Column(name="publicid")
    @Nonnull
    private String publicid;
    private String folderName;

    public PailFolder(){}

    public PailFolder(Pail pail, String folderName, PailFolder parent ){
        this.setPublicid(UUID.randomUUID().toString());
        this.setFolderName(folderName);
        this.setPail(pail);
        this.setParent(parent);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PailFolder getParent() {
        return parent;
    }

    public void setParent(PailFolder parent) {
        this.parent = parent;
    }

    public Pail getPail() {
        return pail;
    }

    public void setPail(Pail pail) {
        this.pail = pail;
    }

    public String getPublicid() {
        return publicid;
    }

    public void setPublicid(String publicid) {
        this.publicid = publicid;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

}
