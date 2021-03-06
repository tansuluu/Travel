package com.example.try4.dao;

import com.example.try4.entity.Likes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.List;

@Repository
@Transactional
public class LikeDAO {

    @Autowired
    private EntityManager entityManager;

    public Likes addLike(long id, String username) {
        Likes likes = new Likes(username, id);
        this.entityManager.persist(likes);
        this.entityManager.flush();
        return likes;
    }
    public  boolean hasPut(String name,long app_id) {
        try {
            String sql = "Select e from " + Likes.class.getName() + " e " //
                    + " Where e.username = :username and e.app_id=:id";

            Query query = entityManager.createQuery(sql, Likes.class);
            query.setParameter("username", name);
            query.setParameter("id", app_id);
            Likes likes = (Likes) query.getSingleResult();
                return true;
        } catch (NoResultException e) {
            return false;

        }
    }
    public int countLike(long id) {
        try {
            String sql = "Select e from " + Likes.class.getName() + " e " //
                    + " Where e.app_id = :id_app ";

            Query query = entityManager.createQuery(sql, Likes.class);
            query.setParameter("id_app", id);

            List<Likes> likes = query.getResultList();
            if (!likes.isEmpty())
                return likes.size();
            return 0 ;
        } catch (NoResultException e) {
            return 0;
        }
    }
    public void deleteLike(String  name,long id){
        this.entityManager.remove(findLike(name,id));
        this.entityManager.flush();
        this.entityManager.clear();
    }
    public Likes findLike(String name, long app_id) {
        try {
            String sql = "Select e from " + Likes.class.getName() + " e " //
                    + " Where e.username = :username and e.app_id=:id";

            Query query = entityManager.createQuery(sql, Likes.class);
            query.setParameter("username", name);
            query.setParameter("id", app_id);
            return (Likes) query.getSingleResult();
        } catch (NoResultException e) {
            return null; }
    }
}