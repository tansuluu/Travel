package com.example.try4.controller;

import com.example.try4.dao.AppUserDAO;
import com.example.try4.dao.ApplicationDAO;
import com.example.try4.dao.LikeDAO;
import com.example.try4.dao.PlaceDAO;
import com.example.try4.entity.AppRole;
import com.example.try4.entity.AppUser;
import com.example.try4.entity.Application;
import com.example.try4.entity.Place;
import com.example.try4.form.AppUserForm;
import com.example.try4.service.EmailService;
import com.example.try4.service.StorageService;
import com.example.try4.utils.SecurityUtil;
import com.example.try4.validator.AppUserValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.connect.web.ProviderSignInUtils;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
@Transactional
public class MainController {

    @Autowired
    private ApplicationDAO applicationDAO;

    @Autowired
    private StorageService storageService;

    @Autowired
    private AppUserDAO appUserDAO;

    @Autowired
    private ConnectionFactoryLocator connectionFactoryLocator;

    @Autowired
    private UsersConnectionRepository connectionRepository;

    @Autowired
    private EmailService emailService;
    @Autowired
    private AppUserValidator appUserValidator;
    @Autowired
    private PlaceDAO placeDAO;

    @InitBinder
    protected void initBinder(WebDataBinder dataBinder) {

        // Form target
        Object target = dataBinder.getTarget();
        if (target == null) {
            return;
        }
        if (target.getClass() == AppUserForm.class) {
            dataBinder.setValidator(appUserValidator);
        }
        // ...
    }


    @RequestMapping(value = { "/", "/welcome" }, method = RequestMethod.GET)
    public String welcomePage(Model model) {

        List<Place> popular=placeDAO.findPopular4();
        model.addAttribute("popular",popular);
        return "index";
    }
    @RequestMapping(value = { "/about" }, method = RequestMethod.GET)
    public String welcomeP(Model model) {

        return "about";
    }
    @RequestMapping(value = { "/offers" }, method = RequestMethod.GET)
    public String welcome(Model model) {
        List<Place> list=placeDAO.getPlace();
        model.addAttribute("places",list);
        return "offers";
    }
    @RequestMapping(value = { "/blog" }, method = RequestMethod.GET)
    public String welcoeP(Model model) {

        return "blog";
    }
    @RequestMapping(value = { "/contact" }, method = RequestMethod.GET)
    public String welcomP(Model model) {

        return "contact";
    }



    @RequestMapping(value = { "/login" }, method = RequestMethod.GET)
    public String login(Model model) {
        return "login";
    }

    // User login with social networking,
    // but does not allow the app to view basic information
    // application will redirect to page / signin.
    @RequestMapping(value = { "/signin" }, method = RequestMethod.GET)
    public String signInPage(Model model)
    {
        return "redirect:/login";
    }

    @RequestMapping(value = { "/signup" }, method = RequestMethod.GET)
    public String signupPage(WebRequest request, Model model) {

        ProviderSignInUtils providerSignInUtils //
                = new ProviderSignInUtils(connectionFactoryLocator, connectionRepository);

        // Retrieve social networking information.
        Connection<?> connection = providerSignInUtils.getConnectionFromSession(request);
        //
        AppUserForm myForm = null;
        //
        if (connection != null) {
            myForm = new AppUserForm(connection);
        } else {
            myForm = new AppUserForm();
        }
        model.addAttribute("myForm", myForm);
        return "signup";
    }

    @RequestMapping(value = { "/signup" }, method = RequestMethod.POST)
    public String signupSave(WebRequest request, //
                             Model model, @RequestParam("imageUrl") String image,//
                             @ModelAttribute("myForm") @Validated AppUserForm appUserForm, //
                             BindingResult result) {

        if (result.hasErrors()) {
            return "signup";
        }

        List<String> roleNames = new ArrayList<String>();
        roleNames.add(AppRole.ROLE_USER);
        AppUser registered = null;

        try {
            AppUser last=appUserDAO.findAppUserNotEnabled(appUserForm.getEmail());
            if(last!=null) {
                appUserDAO.delete(last.getUserId());
            }
            appUserForm.setEnabled(true);
            appUserForm.setUrlImage(image);
            registered = appUserDAO.registerNewUserAccount(appUserForm, roleNames);
        } catch (Exception ex) {
            ex.printStackTrace();
            model.addAttribute("errorMessage", "Error " + ex.getMessage());
            return "signup";
        }

        if (appUserForm.getSignInProvider() != null) {
            ProviderSignInUtils providerSignInUtils //
                    = new ProviderSignInUtils(connectionFactoryLocator, connectionRepository);

            // (Spring Social API):
            // If user login by social networking.
            // This method saves social networking information to the UserConnection table.
            providerSignInUtils.doPostSignUp(registered.getUserName(), request);
        }

        // After registration is complete, automatic login.
        SecurityUtil.logInUser(registered, roleNames);

        return "redirect:/";
    }
    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public String viewRegister(Model model) {
        AppUserForm form = new AppUserForm();
        model.addAttribute("user", form);

        return "register";
    }

    // This method is called to save the registration information.
    // @Validated: To ensure that this Form
    // has been Validated before this method is invoked.
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String saveRegister(Model model, //
                               @ModelAttribute("user") @Validated AppUserForm user, //
                               BindingResult result, //
                               final RedirectAttributes redirectAttributes, HttpServletRequest request, @RequestParam(name = "file",required = false)MultipartFile file) {

        if (result.hasErrors()) {
            return "register";
        }
        user.setUrlImage("gmail.png");

        if (!file.isEmpty()){
            storageService.saveAvatar(file);
            user.setUrlImage(file.getOriginalFilename());
        }

        try {
            user.setConfirm(UUID.randomUUID().toString());

            String appUrl = request.getScheme() + "://" + request.getServerName();

            SimpleMailMessage registrationEmail = new SimpleMailMessage();
            registrationEmail.setTo(user.getEmail());
            registrationEmail.setSubject("Registration Confirmation");
            registrationEmail.setText("To confirm your e-mail address, please click the link below:\n"
                    + appUrl + ":8080/confirm?token=" + user.getConfirm());
            registrationEmail.setFrom("noreply@domain.com");
            emailService.sendEmail(registrationEmail);
            model.addAttribute("successMessage", "A confirmation e-mail has been sent to " + user.getEmail());
            List<String> roleNames = new ArrayList<String>();
            roleNames.add(AppRole.ROLE_USER);
            AppUser last=appUserDAO.findAppUserNotEnabled(user.getEmail());
            if(last!=null) {
                appUserDAO.delete(last.getUserId());
            }
            appUserDAO.registerNewUserAccount(user, roleNames);
        } catch (Exception e) {

            model.addAttribute("errorMessage", "Error: " + e.getMessage());
            return "register";
        }

        redirectAttributes.addFlashAttribute("flashUser", user);

        return "register";
    }

    @RequestMapping(value = "/confirm")
    public String showConfirmationPage(Model model, @RequestParam("token") String token) {
        AppUser user =appUserDAO.findUserconfirm(token);
        if (user == null) { // No token found in DB
            model.addAttribute("invalidToken", "Oops!  This is an invalid confirmation link.");
        }
        else{ model.addAttribute("confirm", user.getConfirm());
            // Set user to enabled
            appUserDAO.setEnable(user.getUserId());}
        List<String> roleNames = new ArrayList<String>();
        roleNames.add(AppRole.ROLE_USER);
        SecurityUtil.logInUser(user, roleNames);
        return "redirect:/";
    }

    @RequestMapping("/saved")
    public void handleFileUpload(@RequestParam("file") MultipartFile file, Model model) {
        try {
            storageService.store(file);

            model.addAttribute("message", "You successfully uploaded " + file.getOriginalFilename() + "!");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            model.addAttribute("message", "FAIL to upload " + file.getOriginalFilename() + "!");
        }

    }
    @RequestMapping("/found")
    public String showFound(Model model){
        List<Application> list= applicationDAO.findFoundLost("found");
        model.addAttribute("list",list);
        return "search";
    }
    @RequestMapping("/lost")
    public String showLost(Model model){
        List<Application> list= applicationDAO.findFoundLost("lost");
        model.addAttribute("list",list);
        return "search";
    }
    @RequestMapping("/category")
    public String showLost(Model model, @RequestParam("category")String category){
        List<Application> list= applicationDAO.findByCategory(category);
        model.addAttribute("list",list);
        return "search";
    }
    @RequestMapping("/searchW")
    public String showSearch(Model model, @RequestParam("word")String word){
        List<Application> list= applicationDAO.search(word);
        model.addAttribute("list",list);
        List<AppUser> users=appUserDAO.searchW(word);
        if(!users.isEmpty())
            model.addAttribute("users",users);
        model.addAttribute("word",word);
        return "search";
    }
    @GetMapping("/image/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        Resource file = storageService.loadFile(filename);
        String mimeType = "";
        try {
            mimeType = Files.probeContentType(file.getFile().toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "filename=\"" + file.getFilename() + "\"")

                .body(file);
    }
    @GetMapping("/up-avatar/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> getAvatar(@PathVariable String filename) {
        Resource file = storageService.loadAvatar(filename);
        String mimeType = "";
        try {
            mimeType = Files.probeContentType(file.getFile().toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "filename=\"" + file.getFilename() + "\"")

                .body(file);
    }



}