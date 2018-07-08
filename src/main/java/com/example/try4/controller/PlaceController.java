package com.example.try4.controller;



import com.example.try4.dao.AppUserDAO;
import com.example.try4.dao.CommentDAO;
import com.example.try4.dao.LikeDAO;
import com.example.try4.dao.PlaceDAO;
import com.example.try4.entity.Comment;
import com.example.try4.entity.Place;
import com.example.try4.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;

@Controller
@Transactional

public class PlaceController {

    @Autowired
    private LikeDAO likeDAO;

    @Autowired
    private CommentDAO commentDAO;

    @Autowired
    StorageService storageService;

    @Autowired
    private AppUserDAO appUserDAO;

    @Autowired
    private PlaceDAO placeDAO;



    @RequestMapping(value = "/newPlace", method = RequestMethod.GET)
    public String newPlace(Model model) {
        Place form = new Place();
        model.addAttribute("place", form);
        return "newPlace";
    }
    @RequestMapping(value = "/newPlace", method = RequestMethod.POST)
    public String saveRegister(@ModelAttribute("place")@Valid Place app,
                               BindingResult result, Model model, //
                               Principal principal, @RequestParam(name = "file1", required = false)MultipartFile file1,
                               @RequestParam(name = "file2", required = false)MultipartFile file2, @RequestParam(name = "file3", required = false)MultipartFile file3) {
        if (result.hasErrors()) {
            model.addAttribute("place", app);
            return "newPlace";
        }
        try {
            app=storageService.preStore(file1,file2,file3,app);
            appUserDAO.findAppUserByUserName(principal.getName());
            app.setUsarname(principal.getName());
            placeDAO.addPlace(app);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error: " + e.getMessage());
            model.addAttribute("place", app);
            model.addAttribute("message","There is already exist such image");
            return "newPlace";
        }

        return "redirect:/userPage?username="+principal.getName();
    }

    @RequestMapping("/placeInfo")
    public String showApplications(Model model, @RequestParam("id")long id, Principal principal){
        Place ap=placeDAO.updateView(id,1);
        List<Comment> list=commentDAO.findComment(id);
        List<Place> popular=placeDAO.findPopular();
        model.addAttribute("comments",list);
        model.addAttribute("app",ap);
        model.addAttribute("popular",popular);
        Comment comment=new Comment();
        model.addAttribute("comment",comment);
        try {
            System.out.println(principal.getName());
            if (principal.getName() != null) {
                if (likeDAO.hasPut(principal.getName(),id)) {
                    System.out.println("VFvdv");
                    model.addAttribute("trueFalse", "yes");
                    System.out.println("fevdvfvfg");
                    System.out.println(likeDAO.hasPut(principal.getName(),id));
                }
                else model.addAttribute("trueFalse", "no" );
            }
        }catch (Exception e){
            System.out.println("error");
        }
        return "places";
    }
    @RequestMapping(value = "/newComment", method = RequestMethod.POST)
    public String saveComment(@ModelAttribute("comment") @Valid Comment comment, BindingResult result, Principal principal, @RequestParam("appId") long appId){
        if (result.hasErrors()) {
            return "redirect:/placeInfo?id="+appId+"#comment";
        }
        comment.setUsername(principal.getName());
        comment.setId_place(appId);
        comment.setImage(appUserDAO.findAppUserByUserName(principal.getName()).getUrlImage());
        commentDAO.addComment(comment);
        placeDAO.updateCommentNum(comment.getId_place(),1);
        return "redirect:/placeInfo?id="+appId+"#comment";
    }
}
