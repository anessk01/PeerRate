package com.dsproject.opinions.controller;

import com.dsproject.opinions.entity.Opinion;
import com.dsproject.opinions.repository.OpinionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import javax.servlet.http.HttpSession;

@Controller
public class OpinionController {
    String gatewayUrl = "http://localhost:8000";
    String loginUrl = "redirect:" + gatewayUrl + "/accounts/login";

    @Autowired
    private OpinionRepository repository;

    @GetMapping("/opinions/dashboard")
    public String dashboard(HttpSession session, Model model) {
        String currentUser = (String) session.getAttribute("username");
        if(currentUser == null){
            return loginUrl;
        }
        ArrayList<Opinion> opinionsByUser = repository.findOpinionBySenderEmail(currentUser);
        model.addAttribute("opinionsByUser", opinionsByUser);
        
        ArrayList<Opinion> opinionsOfUserViewed = repository.findOpinionByReceiverEmailViewed(currentUser, true);
        ArrayList<Opinion> opinionsOfUserNotViewed = repository.findOpinionByReceiverEmailViewed(currentUser, false);
        model.addAttribute("opinionsOfUserViewed", opinionsOfUserViewed);
        model.addAttribute("opinionsOfUserNotViewed", opinionsOfUserNotViewed);
        
        //(JMS): await notifications to be fetched from accounts service
        return "dashboard";
    }

    @GetMapping("/opinions/addForm")
    public String addOpinionForm(
        @RequestParam("receiverName") String receiverName,
        @RequestParam("receiverEmail") String receiverEmail,
        HttpSession session, 
        Model model) 
    {
        String currentUser = (String) session.getAttribute("username");
        if(currentUser == null){
            return loginUrl;
        }
        //check if the person is trying to review themself
        if(receiverEmail.toLowerCase().equals(currentUser.toLowerCase())){
            System.out.println("cannot review yourself.");
            return "cannotPost";
        }
        //check if the current user reviewed the receiver before (repo query)
        if(repository.checkIfReviewed(receiverEmail, currentUser).size() != 0){
            System.out.println("you already reviewed this user: " + repository.checkIfReviewed(receiverEmail, currentUser));
            return "cannotPost";
        }
        model.addAttribute("receiverName", receiverName);
        model.addAttribute("receiverEmail", receiverEmail);
        return "addOpinionForm";
    }


    @PostMapping("/opinions/add")
    public String addOpinion(
        @RequestParam("receiverEmail") String receiverEmail,
        @RequestParam("likes") String likes,
        @RequestParam("dislikes") String dislikes, 
        HttpSession session, 
        Model model) 
    {
        String currentUser = (String) session.getAttribute("username");
        if(currentUser == null){
            return loginUrl;
        }
        
        LocalDateTime timestamp = LocalDateTime.now();
        //check if the person is trying to review themself
        if(receiverEmail.toLowerCase().equals(currentUser.toLowerCase())){
            System.out.println("cannot review yourself.");
            return "cannotPost";
        }
        //check if the current user reviewed the receiver before (repo query)
        if(repository.checkIfReviewed(receiverEmail, currentUser).size() != 0){
            System.out.println("you already reviewed this user: " + repository.checkIfReviewed(receiverEmail, currentUser));
            return "cannotPost";
        }
        //if not, make sure that the receiver email is a valid user and increment credits
        //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        //if valid, go ahead (save to repo)
        model.addAttribute("temp", repository.checkIfReviewed(receiverEmail, currentUser).size());
        Opinion opinion = new Opinion();
        opinion.setTimestamp(timestamp);
        opinion.setSenderEmail(currentUser);
        opinion.setReceiverEmail(receiverEmail);
        opinion.setLikes(likes);
        opinion.setDislikes(dislikes);
        opinion.setViewed(false);
        repository.save(opinion);
        model.addAttribute("temp1", repository.checkIfReviewed(receiverEmail, currentUser).size());
        return "success";
    }

    @GetMapping("/opinions/get")
    public String getOpinion(
        @RequestParam("timestamp") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime timestamp,
        HttpSession session, 
        Model model) 
    {
        String currentUser = (String) session.getAttribute("username");
        if(currentUser == null){
            return loginUrl;
        }
        
        //check that a post with this timestamp and (receiveEmail in session) exists
        Optional<Opinion> optional = repository.findByTimestamp(timestamp);
        if(optional.isPresent()){
            Opinion opinion = optional.get();
            if(opinion.getSenderEmail().equals(currentUser)){
                //check that post was not viewed before. if it was then just show it again for free
                if(opinion.getViewed()){
                    model.addAttribute("opinion", opinion);
                    return "expandedOpinion";
                }
                else{
                    //if not, check if the user has enough credits (JMS) and deduct automatically on accounts service if they do
                    //if true returned, show post and mark it as viewed. save to repo.
                }
            }
            else{
                return "invalidPost";
            }
        }
        else{
            return "invalidPost";
        }
        
        return "stub";
    }


    @GetMapping("/opinions/updateForm")
    public String goUpdateForm(
        @RequestParam("timestamp") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime timestamp,
        Model model,
        HttpSession session) 
    {
        String currentUser = (String)session.getAttribute("username");
        if(currentUser == null){
            return loginUrl;
        }
        System.out.println("Reached!");
        //check that timestamp refers to post made by currentUser
        //if so, add to model the email, timestamp, likes and dislikes
        Optional<Opinion> optional = repository.findByTimestamp(timestamp);
        if(optional.isPresent()){
            Opinion opinion = optional.get();
            if(opinion.getSenderEmail().equals(currentUser)){
                model.addAttribute("receiverEmail", opinion.getReceiverEmail());
                model.addAttribute("timestamp", opinion.getTimestamp());
                model.addAttribute("oldLikes", opinion.getLikes());
                model.addAttribute("oldDislikes", opinion.getDislikes());
            }
            else{
                return "invalidPost";
            }
        }
        else{
            return "invalidPost";
        }
        return "update";
    }

    // Should be PUT ideally but HTML does not support PUT requests
    @PostMapping("/opinions/update")
    public String update(
        @RequestParam("timestamp") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime timestamp,
        @RequestParam("likes") String likes,
        @RequestParam("dislikes") String dislikes, 
        HttpSession session) 
    {
        String currentUser = (String)session.getAttribute("username");
        if(currentUser == null){
            return loginUrl;
        }
        Optional<Opinion> optional = repository.findByTimestamp(timestamp);
        if(optional.isPresent()){
            Opinion opinion = optional.get();
            if(opinion.getSenderEmail().equals(currentUser)){
                //check that timestamp refers to post made by currentUser before updating
                opinion.setLikes(likes);
                opinion.setDislikes(dislikes);
                repository.save(opinion);
            }
            else{
                return "invalidPost";
            }
        }
        else{
            return "invalidPost";
        }
        return "success";
    }
}
