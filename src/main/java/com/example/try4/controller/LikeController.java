package com.example.try4.controller;

import com.example.try4.dao.ApplicationDAO;
import com.example.try4.dao.LikeDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LikeController {

    @Autowired
    private LikeDAO likeDAO;
    @Autowired
    private ApplicationDAO applicationDAO;

    @GetMapping(value = "/addLike")
    public String getLike(@RequestParam("id") long id, @RequestParam("username") String username) {
        likeDAO.addLike(id,username);
        applicationDAO.updateLikes(id,1);
        return "redirect:/appInfo?id="+id+"#likes";
    }
    @GetMapping(value = "/deleteLike")
    public String deletLike(@RequestParam("id") long id, @RequestParam("username") String username) {
        likeDAO.deleteLike(username,id);
        applicationDAO.updateLikes(id,-1);
        return "redirect:/appInfo?id="+id+"#likes";
    }

}