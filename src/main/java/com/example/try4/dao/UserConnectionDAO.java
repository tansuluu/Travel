package com.example.try4.dao;

import com.example.try4.entity.UserConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.List;

@Repository
@Transactional
public class UserConnectionDAO {

    @Autowired
    private EntityManager entityManager;

    public UserConnection findUserConnectionByUserProviderId(String userProviderId) {
        try {
            String sql = "Select e from " + UserConnection.class.getName() + " e " //
                    + " Where e.userProviderId = :userProviderId ";

            Query query = entityManager.createQuery(sql, UserConnection.class);
            query.setParameter("userProviderId", userProviderId);

            List<UserConnection> list = query.getResultList();

            return list.isEmpty() ? null : list.get(0);
        } catch (NoResultException e) {
            return null;
        }
    }    public UserConnection findByUsername(String username) {
        try {
            String sql = "Select e from " + UserConnection.class.getName() + " e " //
                    + " Where e.userId = :userid";

            Query query = entityManager.createQuery(sql, UserConnection.class);
            query.setParameter("userid", username);

            List<UserConnection> list = query.getResultList();

            return list.isEmpty() ? null : list.get(0);
        } catch (NoResultException e) {
            return null;
        }
    }
    public void deleteConnectionUser(String username){
        this.entityManager.remove(findByUsername(username));
        this.entityManager.flush();
        this.entityManager.clear();
    }

}
