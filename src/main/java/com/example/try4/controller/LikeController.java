package com.example.try4.controller;


import com.example.try4.dao.LikeDAO;
import com.example.try4.dao.PlaceDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LikeController {

    @Autowired
    private LikeDAO likeDAO;
    @Autowired
    private PlaceDAO placeDAO;

    @GetMapping(value = "/addLike")
    public String getLike(@RequestParam("id") long id, @RequestParam("username") String username) {
        likeDAO.addLike(id,username);
        placeDAO.updateLikes(id,1);
        return "redirect:/placeInfo?id="+id+"#likes";
    }
    @GetMapping(value = "/deleteLike")
    public String deletLike(@RequestParam("id") long id, @RequestParam("username") String username) {
        likeDAO.deleteLike(username,id);
        placeDAO.updateLikes(id,-1);
        return "redirect:/placeInfo?id="+id+"#likes";
    }

}