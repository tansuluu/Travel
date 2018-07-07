package com.example.try4.dao;

import com.example.try4.entity.Application;
import com.example.try4.entity.Place;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.Calendar;
import java.util.List;

@Repository
@Transactional
public class PlaceDAO {

    @Autowired
    private EntityManager entityManager;

    public Place addPlace(Place app) {
        app.setDate(Calendar.getInstance().getTime());
        this.entityManager.persist(app);
        this.entityManager.flush();
        return app;
    }
    public List<Place> getPlace() {
        try {
            String sql = "Select e from " + Place.class.getName() + " e ";
            Query query = entityManager.createQuery(sql, Place.class);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }
    public Place findPlaceId(long id) {
        try {
            String sql = "Select e from " + Place.class.getName() + " e " //
                    + " Where e.id = :id ";
            Query query = entityManager.createQuery(sql, Place.class);
            query.setParameter("id", id);
            Place application=(Place) query.getSingleResult();
            return  application;
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<Application> findFoundLost(String status) {
        try {
            String sql = "Select e from " + Application.class.getName() + " e " //
                    + " Where e.status= :status ";

            Query query = entityManager.createQuery(sql, Application.class);
            query.setParameter("status", status);

            return  query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }
    public List<Application> findByCategory(String status) {
        try {
            String sql = "Select e from " + Application.class.getName() + " e " //
                    + " Where e.category= :category ";

            Query query = entityManager.createQuery(sql, Application.class);
            query.setParameter("category", status);

            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }
    public List<Place> findPopular() {
        try {
            String sql = "Select e from " + Place.class.getName() + " e " //
                    + "order by e.view desc ";
            Query query = entityManager.createQuery(sql, Place.class);
            return (List<Place>) query.setFirstResult(0).setMaxResults(3).getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }
    public List<Place> search(String word) {
        try {
            String sql = "Select e from " + Place.class.getName() + " e " //
                    + "where concat(e.title,e.category,e.status) like '%"+word+"%'";
            Query query = entityManager.createQuery(sql, Place.class);
            return (List<Place>) query.setFirstResult(0).setMaxResults(3).getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<Place> getUsersPlace(String username) {
        try {
            String sql = "Select e from " + Place.class.getName() + " e " //
                    + "where e.usarname= :username";
            Query query = entityManager.createQuery(sql, Place.class);
            query.setParameter("username",username);
            return  query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }
    public void deleteApp(long id){
        this.entityManager.remove(findPlaceId(id));
        this.entityManager.flush();
        this.entityManager.clear();
    }
    public Place updateView(long id,int n){
        Place place=findPlaceId(id);
        place.setView(place.getView()+n);
        return addPlace(place);
    }


}
