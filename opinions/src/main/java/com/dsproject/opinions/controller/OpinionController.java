package com.dsproject.opinions.controller;

import com.dsproject.opinions.entity.Opinion;
import com.dsproject.opinions.repository.OpinionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.util.Optional;

import javax.servlet.http.HttpSession;

@Controller
public class OpinionController {
    private static final String OpinionPK = null;
    String gatewayUrl = "https://localhost:8000";
    String loginUrl = "redirect:" + gatewayUrl + "/accounts/login";

    @Autowired
    private OpinionRepository repository;

    @GetMapping("/opinions/dashboard")
    public String dashboard(HttpSession session) {
        String currentUser = (String) session.getAttribute("username");
        if(currentUser == null){
            return loginUrl;
        }
        //(JMS): await notifications to be fetched from accounts service
        return "dashboard";
    }

    @GetMapping("/opinions/addForm")
    public String addOpinionForm(HttpSession session) {
        String currentUser = (String) session.getAttribute("username");
        if(currentUser == null){
            return loginUrl;
        }
        return "addOpinionForm";
    }


    @PostMapping("/opinions/add")
    public String addOpinion(
        @RequestParam("receiverEmail") String receiverEmail,
        @RequestParam("likes") String likes,
        @RequestParam("dislikes") String dislikes, 
        HttpSession session) 
    {
        String currentUser = (String) session.getAttribute("username");
        if(currentUser == null){
            return loginUrl;
        }
        
        LocalDateTime timestamp = LocalDateTime.now();
        //check if the current user reviewed the receiver before (repo query)
        //if not, make sure that the receiver email is a valid user (JMS queues)
        //if valid, go ahead (save to repo)
        return "stub";
    }

    @GetMapping("/opinions/get")
    public String getOpinion(
        @RequestParam("timestamp") LocalDateTime timestamp,
        HttpSession session) 
    {
        String currentUser = (String) session.getAttribute("username");
        if(currentUser == null){
            return loginUrl;
        }
        
        //check that a post with this timestamp and receiveEmail in session exists
        //check that post was not viewed before. if it was then just show it again for free
        //if not, check if the user has enough credits (JMS) and deduct automatically on accounts service if they do
        //if true returned, show post and mark it as viewed.
        //if valid, go ahead (save to repo)
        return "stub";
    }


    @GetMapping("/opinions/updateForm")
    public String goUpdateForm(
        @RequestParam("timestamp") LocalDateTime timestamp,
        Model model,
        HttpSession session) 
    {
        String currentUser = (String)session.getAttribute("username");
        if(currentUser == null){
            return loginUrl;
        }
        System.out.println("Reached!");
        //USE SESSION TO AUTOFILL FIELDS
        // Optional<Account> optional = repository.findById(currentUser);
        // if (optional.isPresent()) {
        //     Account account = optional.get();
        //     model.addAttribute("oldName", account.getName());
        //     model.addAttribute("oldPassword", account.getPassword());
        //     model.addAttribute("oldCompany", account.getLastCompanyWorked());
        // }
        // else{
        //     return loginUrl;
        // }
        return "update";
    }

    @PostMapping("/opinions/update")
    public String update(
        @RequestParam("timestamp") LocalDateTime timestamp,
        @RequestParam("likes") String likes,
        @RequestParam("dislikes") String dislikes, 
        HttpSession session) 
    {
        String currentUser = (String)session.getAttribute("username");
        if(currentUser == null){
            return loginUrl;
        }
        //update 
        return "stub";
    }

    @RequestMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return loginUrl;
    }
}
