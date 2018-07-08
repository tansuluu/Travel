package com.example.try4.controller;


import com.example.try4.dao.AppUserDAO;
import com.example.try4.dao.ApplicationDAO;
import com.example.try4.dao.CommentDAO;
import com.example.try4.dao.LikeDAO;
import com.example.try4.entity.Application;
import com.example.try4.entity.Comment;
import com.example.try4.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.security.Principal;
import java.util.List;

@Controller
@Transactional
public class ApplicationController {

    @Autowired
    StorageService storageService;

    @Autowired
    private ApplicationDAO applicationDAO;

    @Autowired
    private AppUserDAO appUserDAO;

    @Autowired
    private CommentDAO commentDAO;

    @Autowired
    private LikeDAO likeDAO;

    @RequestMapping(value = "/newApplication", method = RequestMethod.GET)
    public String viewRegister(Model model) {
        Application form = new Application();
        model.addAttribute("app", form);
        List<String> list = form.getCategoties();
        model.addAttribute("categories", list);

        return "newPost";
    }

    @RequestMapping(value = "/newApplication", method = RequestMethod.POST)
    public String saveRegister(@ModelAttribute("app")@Valid Application app,
                               BindingResult result, Model model, //
                               Principal principal, @RequestParam(name = "file1", required = false)MultipartFile file1,
                               @RequestParam(name = "file2", required = false)MultipartFile file2, @RequestParam(name = "file3", required = false)MultipartFile file3) {
        if (result.hasErrors()) {
            model.addAttribute("app", app);
            List<String> list = app.getCategoties();
            model.addAttribute("categories", list);
            return "newPost";
        }
        try {
            //app=storageService.preStore(file1,file2,file3,app);
            appUserDAO.findAppUserByUserName(principal.getName());
            app.setUsarname(principal.getName());
            app.setImage(appUserDAO.findAppUserByUserName(principal.getName()).getUrlImage());
            applicationDAO.addApp(app);
        } catch (Exception e) {

            model.addAttribute("errorMessage", "Error: " + e.getMessage());
            model.addAttribute("app", app);
            List<String> list = app.getCategoties();
            model.addAttribute("categories", list);
            model.addAttribute("message","there is already exist such image");
            return "newPost";
        }

        return "redirect:/";
    }


    @RequestMapping("/deleteComment")
    public String deleteComment(@RequestParam("id") long id, @RequestParam("apId") long appId){
        applicationDAO.updateCommentNum(appId,-1);
        commentDAO.deleteComment(id);
        return "redirect:/appInfo?id="+appId+"#comment";
    }

    @RequestMapping("/deleteApp")
    public String deleteApp(@RequestParam("id") long id){
        applicationDAO.deleteApp(id);
        return "redirect:/";
    }
}
