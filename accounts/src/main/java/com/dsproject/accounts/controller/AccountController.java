package com.dsproject.accounts.controller;

import com.dsproject.core.MessageTypeA;
import com.dsproject.accounts.entity.Account;
import com.dsproject.accounts.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Optional;

import javax.jms.Queue;
import javax.servlet.http.HttpSession;

@Controller
public class AccountController {
    String gatewayUrl = "http://localhost:8000";
    String dashUrl = "redirect:" + gatewayUrl + "/opinions/dashboard";

    @Autowired
    private AccountRepository repository;

    @Autowired
	JmsTemplate jmsTemplate;
	
	@Autowired
	Queue queue;

    @GetMapping("/accounts")
    public String homePage() {
        MessageTypeA messageTypeA = new MessageTypeA("sender", "receiver", "increment");
		jmsTemplate.convertAndSend(queue, messageTypeA);
        System.out.println("sent");
        return "home";
    }

    @GetMapping("/accounts/search")
    public String search(
        @RequestParam("company") String company, 
        Model model, 
        HttpSession session) 
    {
        String currentUser = (String)session.getAttribute("username");
        if(currentUser == null || !repository.findById(currentUser).isPresent()){
            return "redirect:/accounts/login";
        }
        model.addAttribute("searchResults", repository.findIdByCompany(company, currentUser));
        return "searchResults";
    }

    @GetMapping("/accounts/signup")
    public String signup(HttpSession session) {
        String currentUser = (String)session.getAttribute("username");
        if(currentUser != null && repository.findById(currentUser).isPresent()){
            return dashUrl;
        }
        return "signup";
    }

    @PostMapping("/accounts/create")
    public String create(
        @RequestParam("email") String email,
        @RequestParam("password") String password,
        @RequestParam("name") String name,
        @RequestParam("company") String company, 
        HttpSession session) 
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
            session.setAttribute("username", email);
            return dashUrl; 
        }
    }

    @GetMapping("/accounts/login")
    public String login(HttpSession session) {
        String currentUser = (String) session.getAttribute("username");
        if(currentUser != null && repository.findById(currentUser).isPresent()){
            return dashUrl;
        }
        return "login";
    }

    @PostMapping("/accounts/authenticate")
    public String authenticate(
        @RequestParam("email") String email,
        @RequestParam("password") String password, 
        HttpSession session) 
    {
        String currentUser = (String) session.getAttribute("username");
        if(currentUser != null && repository.findById(currentUser).isPresent()){
            return dashUrl;
        }
        if (!repository.findById(email).isPresent()) {
            System.out.println("no email");
            return "AccountDoesNotExist";
        } 
        else {
            if(repository.findPasswordByEmail(email).equals(password)){
                session.setAttribute("username", email);
                return dashUrl;
            }
            else{
                System.out.println(email);
                System.out.println(repository.findPasswordByEmail(email));
                return "AccountDoesNotExist";
            }
        }
    }

    @GetMapping("/accounts/updateAccount")
    public String goUpdateForm(Model model, HttpSession session) {
        String currentUser = (String)session.getAttribute("username");
        if(currentUser == null || !repository.findById(currentUser).isPresent()){
            return "redirect:/accounts/login";
        }
        System.out.println("Reached!");
        //USE SESSION TO AUTOFILL FIELDS
        Optional<Account> optional = repository.findById(currentUser);
        if (optional.isPresent()) {
            Account account = optional.get();
            model.addAttribute("oldName", account.getName());
            model.addAttribute("oldPassword", account.getPassword());
            model.addAttribute("oldCompany", account.getLastCompanyWorked());
        }
        else{
            return "redirect:/accounts/login";
        }
        return "update";
    }

    // Should be PUT ideally but HTML does not support PUT requests
    @PostMapping("/accounts/update")
    public String update(@RequestParam("password") String password,
                          @RequestParam("name") String name,
                          @RequestParam("company") String company,
                          HttpSession session) 
    {
        String currentUser = (String)session.getAttribute("username");
        if(currentUser == null || !repository.findById(currentUser).isPresent()){
            return "redirect:/accounts/login";
        }
        Optional<Account> optional = repository.findById(currentUser);
        if (optional.isPresent()) {
            Account account = optional.get();
            account.setName(name);
            account.setPassword(password);
            account.setLastCompanyWorked(company);
            repository.save(account);
            return dashUrl;
        } 
        else {
            return "AccountDoesNotExist";
        }
    }

    @RequestMapping("/accounts/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/accounts/login";
    }

    @GetMapping("/accounts/account")
    public ModelAndView goToAccount(HttpSession session) {
        String currentUser = (String)session.getAttribute("username");
        if(currentUser == null || !repository.findById(currentUser).isPresent()){
            return null;
        }
        String email = currentUser; 
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

    //NOTIFICATION LOGIC: SET AND GET GOES HERE
}
