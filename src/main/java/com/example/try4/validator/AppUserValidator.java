package com.example.try4.validator;

import com.example.try4.dao.AppUserDAO;
import com.example.try4.entity.AppUser;
import com.example.try4.form.AppUserForm;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class AppUserValidator implements Validator {

    private Pattern pattern;
    private Matcher matcher;

    String STRING_PATTERN = "[a-zA-Z]+";


    // common-validator library.
    private EmailValidator emailValidator = EmailValidator.getInstance();

    @Autowired
    private AppUserDAO appUserDAO;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == AppUserForm.class;
    }

    @Override
    public void validate(Object target, Errors errors) {

        AppUserForm form = (AppUserForm) target;
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "country", "", "Country is required");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email", "", "Email is required");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "userName", "", "User name is required");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "", "name is required");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "", "Password is required");

        AppUser userAccount = appUserDAO.findAppUserByUserName( form.getUserName());
        if (userAccount != null) {
            if (form.getUserId() == null) {
                errors.rejectValue("userName", "", "User name is not available");
                return;
            } else if (!form.getUserId().equals(userAccount.getUserId() )) {
                errors.rejectValue("userName", "", "User name is not available");
                return;
            }
        }

        if (!emailValidator.isValid(form.getEmail())) {

            errors.rejectValue("email", "", "Please write valid email");
            return;
        }
        userAccount = appUserDAO.findByEmail(form.getEmail());
        if (userAccount != null) {
                errors.rejectValue("email", "", "Email is not available");

            } }

}
