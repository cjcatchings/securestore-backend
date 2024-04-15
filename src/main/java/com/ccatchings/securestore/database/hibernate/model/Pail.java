package com.ccatchings.securestore.database.hibernate.model;

import com.ccatchings.securestore.database.hibernate.model.DBBackedModel;
import jakarta.annotation.Nonnull;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name="pail")
public class Pail implements DBBackedModel {

    @Id
    @GeneratedValue
    @Column(name="id")
    private Long id;

    @Column(name="name")
    @Nonnull
    private String name;

    @Column(name="owner_login")
    @Nonnull
    private String ownerLogin;

    @Column(name="publicid")
    @Nonnull
    private String publicid;

    public Pail(){

    }

    public Pail(String name, String ownerLogin){
        this.setName(name);
        this.setOwnerLogin(ownerLogin);
        this.setPublicid(UUID.randomUUID().toString());
    }

    public String getOwnerLogin() {
        return ownerLogin;
    }

    public void setOwnerLogin(String owner_login) {
        this.ownerLogin = owner_login;
    }

    public String getPublicid() {
        return publicid;
    }

    public void setPublicid(String publicid) {
        this.publicid = publicid;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
