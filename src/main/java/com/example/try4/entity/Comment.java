package com.example.try4.entity;

import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "comment")
public class Comment {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    private long id;
    @Column(name = "username")
    private String username;
    @Column(name = "id_place")
    private long id_place;
    @Column(name = "text")
    @NotEmpty(message = "*Please write comment")
    private String comentText;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "dateCom", nullable = false)
    private Date dateCom;
    @Column(name = "image", length = 136)
    private String image;

    public Comment(String username, long id_place, String comentText) {
        this.username = username;
        this.id_place = id_place;
        this.comentText = comentText;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setId_app(long id_app) {
        this.id_place = id_place;
    }

    public Date getDateCom() {
        return dateCom;
    }

    public void setDateCom(Date dateCom) {
        this.dateCom = dateCom;
    }

    public Comment() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String id_user) {
        this.username = id_user;
    }

    public long getId_app() {
        return id_place;
    }

    public void setId_app(int id_app) {
        this.id_place = id_app;
    }

    public String getComentText() {
        return comentText;
    }

    public void setComentText(String comentText) {
        this.comentText = comentText;
    }
}
