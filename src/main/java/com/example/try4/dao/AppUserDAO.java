package com.example.try4.dao;

import com.example.try4.entity.*;
import com.example.try4.form.AppUserForm;
import com.example.try4.utils.EncrytedPasswordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionKey;
import org.springframework.social.connect.UserProfile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
@Transactional
public class AppUserDAO  {


    @Autowired
    private EntityManager entityManager;

    @Autowired
    private AppRoleDAO appRoleDAO;

    @Autowired
    private ApplicationDAO applicationDAO;

    @Autowired
    private  UserConnectionDAO userConnectionDAO;

    @Autowired
    private CommentDAO commentDAO;

    @Autowired
    private PostDAO postDAO;

    public AppUser findAppUserByUserId(Long userId) {
        try {
            String sql = "Select e from " + AppUser.class.getName() + " e " //
                    + " Where e.userId = :userId ";

            Query query = entityManager.createQuery(sql, AppUser.class);
            query.setParameter("userId", userId);

            return (AppUser) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public AppUser findAppUserByUserName(String userName) {
        try {
            String sql = "Select e from " + AppUser.class.getName() + " e " //
                    + " Where e.userName = :userName ";

            Query query = entityManager.createQuery(sql, AppUser.class);
            query.setParameter("userName", userName);

            return (AppUser) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    public AppUser findAppUserNotEnabled(String email) {
        try {
            String sql = "Select e from " + AppUser.class.getName() + " e " //
                    + " Where e.enabled=false  and e.email = :email ";

            Query query = entityManager.createQuery(sql, AppUser.class);
            query.setParameter("email", email);

            return (AppUser) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    public AppUser findByEmail(String email) {
        try {
            String sql = "Select e from " + AppUser.class.getName() + " e " //
                    + " Where e.enabled=true and e.email = :email ";

            Query query = entityManager.createQuery(sql, AppUser.class);
            query.setParameter("email", email);

            return (AppUser) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    public AppUser findUserconfirm(String confirm) {
        try {
            String sql = "Select e from " + AppUser.class.getName() + " e " //
                    + " Where e.confirm = :confirm ";

            Query query = entityManager.createQuery(sql, AppUser.class);
            query.setParameter("confirm", confirm);

            return (AppUser) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    private String findAvailableUserName(String userName_prefix) {
        AppUser account = this.findAppUserByUserName(userName_prefix);
        if (account == null) {
            return userName_prefix;
        }
        int i = 0;
        while (true) {
            String userName = userName_prefix + "_" + i++;
            account = this.findAppUserByUserName(userName);
            if (account == null) {
                return userName;
            }
        }
    }

    // Auto create App User Account.
    public AppUser createAppUser(Connection<?> connection) {

        ConnectionKey key = connection.getKey();
        // (facebook,12345), (google,123) ...

        System.out.println("key= (" + key.getProviderId() + "," + key.getProviderUserId() + ")");

        UserProfile userProfile = connection.fetchUserProfile();

        String email = userProfile.getEmail();
        AppUser appUser = this.findByEmail(email);
        if (appUser != null) {
            return appUser;
        }
        String userName_prefix = userProfile.getFirstName().trim().toLowerCase()//
                + "_" + userProfile.getLastName().trim().toLowerCase();

        String userName = this.findAvailableUserName(userName_prefix);
        //
        // Random Password! TODO: Need send email to User!
        //
        String randomPassword = UUID.randomUUID().toString().substring(0, 5);
        String encrytedPassword = EncrytedPasswordUtils.encrytePassword(randomPassword);
        //
        appUser = new AppUser();
        appUser.setEnabled(true);
        appUser.setEncrytedPassword(encrytedPassword);
        appUser.setUserName(userName);
        appUser.setEmail(email);
        appUser.setName(userProfile.getFirstName() + " "+userProfile.getLastName());

        this.entityManager.persist(appUser);

        // Create default Role
        List<String> roleNames = new ArrayList<String>();
        roleNames.add(AppRole.ROLE_USER);
        this.appRoleDAO.createRoleFor(appUser, roleNames);

        return appUser;
    }

    public AppUser registerNewUserAccount(AppUserForm appUserForm, List<String> roleNames) {
        AppUser appUser = new AppUser();
        appUser.setUserName(appUserForm.getUserName());
        appUser.setEmail(appUserForm.getEmail());

        appUser.setName(appUserForm.getName());
        appUser.setEnabled(appUserForm.isEnabled());
        appUser.setConfirm(appUserForm.getConfirm());
        System.out.println(appUserForm.getUrlImage());
        appUser.setUrlImage(appUserForm.getUrlImage());
        appUser.setCountry(appUserForm.getCountry());
        appUser.setAge(appUserForm.getAge());
        appUser.setGender(appUserForm.getGender());
        String encrytedPassword = EncrytedPasswordUtils.encrytePassword(appUserForm.getPassword());
        appUser.setEncrytedPassword(encrytedPassword);
        this.entityManager.persist(appUser);
        this.entityManager.flush();
        this.appRoleDAO.createRoleFor(appUser, roleNames);
        return appUser;
    }
    public void setEnable(long id){
        AppUser user=findAppUserByUserId(id);
        user.setEnabled(true);
        this.entityManager.persist(user);
        this.entityManager.flush();
    }
    public void delete(long id) {
        AppUser user = findAppUserByUserId(id);
        for (Comment com : commentDAO.findCommentByUsername(user.getUserName())) {
            applicationDAO.updateCommentNum(com.getId_app(),-1);
            commentDAO.deleteComment(com.getId());
        }
        for (Application app : applicationDAO.getUsersApplication(user.getUserName())) {
            applicationDAO.deleteApp(app.getApId());
        }
        for (Post post:postDAO.findPostByUsername(user.getUserName()))
        {
            postDAO.deletePost(post.getId());
        }
        if (user.getConfirm()==null) {
            userConnectionDAO.deleteConnectionUser(user.getUserName());
        }
        entityManager.remove(entityManager.contains(user) ? user : entityManager.merge(user));
        this.entityManager.flush();
        this.entityManager.clear();
    }

    public List<AppUser> searchW(String word) {
        try {
            String sql = "Select e from " + AppUser.class.getName() + " e " //
                    + "where e.enabled=true and concat(e.userName,e.firstName,e.lastName) like '%"+word+"%'";
            Query query = entityManager.createQuery(sql, AppUser.class);
            return (List<AppUser>) query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }
    public void updateUser(AppUser user){
        AppUser usera=findAppUserByUserName(user.getUserName());
        usera.setCountry(user.getCountry());
        usera.setName(user.getName());
        usera.setAge(user.getAge());
        this.entityManager.persist(usera);
        this.entityManager.flush();
    }
    public void addUser(AppUser user){
        this.entityManager.persist(user);
        this.entityManager.flush();
    }
}