package com.dsproject.accounts.controller;

import com.dsproject.accounts.entity.Account;
import com.dsproject.accounts.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Optional;

@Controller
public class AccountController {
    @Autowired
    private AccountRepository repository;

    @GetMapping("/accounts/search")
    public String search(
        @RequestParam("company") String company, 
        Model model) 
    {
        model.addAttribute("searchResults", repository.findIdByCompany(company));
        return "searchResults";
    }

    @GetMapping("/accounts/signup")
    public String signup() {
        return "signup";
    }

    @PostMapping("/accounts/create")
    public String create(
        @RequestParam("email") String email,
        @RequestParam("password") String password,
        @RequestParam("name") String name,
        @RequestParam("company") String company) 
    {
        if (repository.findById(email).isPresent()) {
            return "AccountExists";
        } 
        else {
            Account account = new Account();
            account.setEmail(email);
            account.setCredits(3);
            account.setLastCompanyWorked(company);
            account.setName(name);
            account.setPassword(password);
            repository.save(account);
            //ADD TO SESSION
            return "success"; //TO DO: REDIRECT TO DASH
        }
    }

    @GetMapping("/accounts/login")
    public String login() {
        //IF USER IN SESSION THEN REDIRECT TO DASH
        return "login";
    }

    @PostMapping("/accounts/authenticate")
    public String authenticate(
        @RequestParam("email") String email,
        @RequestParam("password") String password) 
    {
        if (!repository.findById(email).isPresent()) {
            System.out.println("no email");
            return "AccountDoesNotExist";
        } 
        else {
            if(repository.findPasswordByEmail(email).equals(password)){
                //ADD TO SESSION
                return "success"; //TO DO: REDIRECT TO DASH
            }
            else{
                System.out.println(email);
                System.out.println(repository.findPasswordByEmail(email));
                return "AccountDoesNotExist";
            }
        }
    }

    @GetMapping("/accounts/updateAccount")
    public String goUpdateForm(Model model) {
        //CHECK SESSION AND REDIRECT IF NOT LOGGED IN
        //USE SESSION TO AUTOFILL FIELDS
        return "update";
    }

    @PostMapping("/accounts/update")
    public String update(@RequestParam("password") String password,
                          @RequestParam("name") String name,
                          @RequestParam("company") String company
                          ) 
    {
        //GET EMAIL FROM SESSION
        String email = "STUB";  //NEEDS TO BE FROM SESSION
        Optional<Account> optional = repository.findById(email);
        if (optional.isPresent()) {
            Account account = optional.get();
            account.setName(name);
            account.setLastCompanyWorked(company);
            repository.save(account);
            return "success";
        } 
        else {
            return "AccountDoesNotExist";
        }
    }

    @GetMapping("/accounts/showaccount")
    public ModelAndView goToAccount() {
        String email = "STUB";  //NEEDS TO BE FROM SESSION
        Optional<Account> optional = repository.findById(email);
        if (optional.isPresent()) {
            Account account = optional.get();
            String t_email = account.getEmail();
            Integer t_credits = account.getCredits();
            String t_name = account.getName();
            String t_company = account.getLastCompanyWorked();
            ModelAndView mv = new ModelAndView("account");
            mv.getModel().put("name", t_name);
            mv.getModel().put("credits", t_credits);
            mv.getModel().put("company", t_company);
            mv.getModel().put("email", t_email);
            return mv;
        } else {
            return null;
        }
    }
}
