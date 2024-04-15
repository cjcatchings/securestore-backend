package com.ccatchings.securestore.database.hibernate.model;

import jakarta.annotation.Nonnull;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name="pail_file")
public class PailFile implements DBBackedModel {

    @Id
    @GeneratedValue
    @Column(name="id")
    private Long id;

    @JoinColumn(name="pail")
    @ManyToOne(targetEntity=Pail.class)
    @Nonnull
    private Pail pail;

    @JoinColumn(name="folder")
    @ManyToOne(targetEntity=PailFolder.class)
    private PailFolder folder;

    @Column(name="filename")
    @Nonnull
    private String fileName;

    @Column(name="stlocation")
    private String storageLocation;

    @Column(name="mediatype")
    @Nonnull
    private String mediaType;

    @Column(name="createdtime")
    @CreationTimestamp
    private Date createdTime;

    @Column(name="lastupdatedtime")
    @UpdateTimestamp
    private Date lastUpdatedTime;

    @Column(name="publicid")
    @Nonnull
    private String publicId;

    public PailFile(){}

    public PailFile(String fileName, Pail pail, PailFolder folder, String mediaType){
        this.setFileName(fileName);
        this.setPail(pail);
        this.setFolder(folder);
        this.setMediaType(mediaType);
        Date createdTime = Calendar.getInstance().getTime();
        this.setCreatedTime(createdTime);
        this.setLastUpdatedTime(createdTime);
        this.setPublicId(UUID.randomUUID().toString());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Pail getPail() {
        return pail;
    }

    public void setPail(Pail pail) {
        this.pail = pail;
    }

    public PailFolder getFolder() {
        return folder;
    }

    public void setFolder(PailFolder folder) {
        this.folder = folder;
    }

    public String getStorageLocation() {
        return storageLocation;
    }

    public void setStorageLocation(String storageLocation) {
        this.storageLocation = storageLocation;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Date getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    public void setLastUpdatedTime(Date lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public String getFileName(){
        return fileName;
    }

    public void setFileName(String fileName){
        this.fileName = fileName;
    }
}
