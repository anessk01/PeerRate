package com.dsproject.accounts.controller;

import com.dsproject.accounts.entity.Account;
import com.dsproject.accounts.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Optional;
import javax.swing.*;

@RestController
public class AccountController {
    @Autowired
    private AccountRepository repository;

    @GetMapping("/accounts")
    public ModelAndView goAccount() {
        return new ModelAndView("index");
    }

    @GetMapping("/accounts/getall")
    public List<Account> getAll() {
        return repository.findAll();
    }

    @PostMapping("/accounts/getbyid")
    public Optional<Account> getById(@RequestParam("email") String email
    ) {
        return repository.findById(email);
    }


    @PostMapping("/accounts/create")
    public Account create(@RequestParam("email") String email,
                          @RequestParam("name") String name,
                          @RequestParam("company") String company
                          ) {
        if (repository.findById(email).isPresent()) {

            return null;} else {
            Account account = new Account();
            account.setEmail(email);
            account.setCredits(0);
            account.setLastCompanyWorked(company);
            account.setName(name);
            return repository.save(account);
        }
    }

    @PostMapping("/accounts/update")
    public Account update(@RequestParam("email") String email,
                          @RequestParam("name") String name,
                          @RequestParam("company") String company,
                          @RequestParam("credits") Integer credits
                          ) {
        Optional<Account> optional = repository.findById(email);
        if (optional.isPresent()) {
            Account account = optional.get();
            account.setName(name);
            account.setLastCompanyWorked(company);
            account.setCredits(credits);
            return repository.save(account);
        } else {
            return null;
        }
    }

    @GetMapping("/accounts/createform")
    public ModelAndView goCreateForm() {
        return new ModelAndView("create");
    }

    @GetMapping("/accounts/updateform")
    public ModelAndView goUpdateForm() {
        return new ModelAndView("update");
    }

    @GetMapping("/accounts/queryform")
    public ModelAndView goQueryForm() {
        return new ModelAndView("query");
    }

    @PostMapping("/accounts/showaccount")
    public ModelAndView goToAccount(@RequestParam("email") String email) {
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
