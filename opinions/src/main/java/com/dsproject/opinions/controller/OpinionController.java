package com.dsproject.opinions.controller;

import com.dsproject.core.*;
import com.dsproject.opinions.entity.Opinion;
import com.dsproject.opinions.repository.OpinionRepository;

import org.apache.activemq.command.ActiveMQObjectMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.jms.JmsException;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.servlet.http.HttpSession;

@Controller
public class OpinionController {
    String gatewayUrl = "http://host.docker.internal:8000";
    String loginUrl = "redirect:" + gatewayUrl + "/accounts/login";

    @Autowired
    private OpinionRepository repository;

    @Autowired
	JmsTemplate jmsTemplate;
	
	@Autowired
    @Qualifier("queueA")
	Queue queueA;

	@Autowired
    @Qualifier("queueD")
	Queue queueD;

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
        MessageTypeA messageTypeA = new MessageTypeA(currentUser, null, "fetch");
        jmsTemplate.convertAndSend(queueA, messageTypeA);

        //await response from accounts microservice
        Object message = jmsTemplate.receiveAndConvert("queueB");
        if (message instanceof MessageTypeB) {
            MessageTypeB contents = (MessageTypeB) message;
            if(contents.allowed){
                LinkedList<String> notifications = contents.notifications;
                try{
                    Collections.reverse(notifications);
                }
                catch(Exception e){
                    //in case notifications is empty
                }
                model.addAttribute("notifications", notifications);
                model.addAttribute("credits", contents.credits);
                return "dashboard";
            }
            else{
                return "error";
            }
        }
        else{
            System.out.println("Unknown message type: " + message.getClass().getCanonicalName());
            return "error";
        }
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
        MessageTypeA messageTypeA = new MessageTypeA(currentUser, receiverEmail, "increment");
        jmsTemplate.convertAndSend(queueA, messageTypeA);
        
        //await response from accounts microservice
        Object message = jmsTemplate.receiveAndConvert("queueB");
        if (message instanceof MessageTypeB) {
            MessageTypeB contents = (MessageTypeB) message;
            if(contents.allowed){
                //if valid, go ahead (save to repo)
                Opinion opinion = new Opinion();
                opinion.setTimestamp(timestamp);
                opinion.setSenderEmail(currentUser);
                opinion.setReceiverEmail(receiverEmail);
                opinion.setLikes(likes);
                opinion.setDislikes(dislikes);
                opinion.setViewed(false);
                repository.save(opinion);
                return "success";
            }
            else{
                return "error";
            }
        }
        else{
            System.out.println("Unknown message type: " + message.getClass().getCanonicalName());
            return "error";
        }
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
            if(opinion.getReceiverEmail().equals(currentUser)){
                //check that post was not viewed before. if it was then just show it again for free
                if(opinion.getViewed()){
                    model.addAttribute("opinion", opinion);
                    return "expandedOpinion";
                }
                else{
                    //if not, check if the user has enough credits (JMS) and deduct automatically on accounts service if they do
                    MessageTypeA messageTypeA = new MessageTypeA(opinion.getSenderEmail(), currentUser, "decrement");
		            jmsTemplate.convertAndSend(queueA, messageTypeA);
                    
                    //await response from accounts microservice
                    Object message = jmsTemplate.receiveAndConvert("queueB");
                    if (message instanceof MessageTypeB) {
                        MessageTypeB contents = (MessageTypeB) message;
                        if(contents.allowed){
                            opinion.setViewed(true);

                            //also, trigger the aggregator service to set the reaggregate flag to true
                            //as this newly viewed opinion should also be taken into account now
                            MessageTypeD messageTypeD = new MessageTypeD(null, null, true, currentUser);
                            System.out.println("sending: " + messageTypeD.receiverEmail + ", " + messageTypeD.reaggregate);
                            jmsTemplate.convertAndSend(queueD, messageTypeD);

                            // //await response to ensure aggregator service is on the same page
                            // Object aggregatorResponse = jmsTemplate.receiveAndConvert("queueC");
                            // if (aggregatorResponse instanceof MessageTypeC) {
                            //     MessageTypeC aggregatorResponseContents = (MessageTypeC) aggregatorResponse;
                            //     //if the aggregator could not make sense of the sent message
                            //     if(!aggregatorResponseContents.aggregate){
                            //         System.out.println("Aggregator cannot parse");
                            //         return "error";
                            //     }
                            // }
                            // else{
                            //     System.out.println("Unknown message type: " + aggregatorResponse.getClass().getCanonicalName());
                            //     return "error";
                            // }

                            //if all is well, show post and mark it as viewed. save to repo.
                            repository.save(opinion);
                            model.addAttribute("opinion", opinion);
                            return "expandedOpinion";
                        }
                        else{
                            return "noCredits";
                        }
                    }
                    else{
                        System.out.println("Unknown message type: " + message.getClass().getCanonicalName());
                        return "error";
                    }
                }
            }
            else{
                return "invalidPost";
            }
        }
        else{
            return "invalidPost";
        }
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

    @JmsListener(destination = "queueC")
    public void consume(Object received) throws JmsException{
        //should ideally be handled using threads, but JPA is not thread safe.
        ActiveMQObjectMessage receivedConverted = (ActiveMQObjectMessage) received;
        Object message;
        try {
            message = receivedConverted.getObject();
        } catch (JMSException e) {
            e.printStackTrace();
            return;
        }
        if (message instanceof MessageTypeC) {
            MessageTypeC contents = (MessageTypeC) message;
            //scenario beta: the aggregator service is looking to aggregate the opinions of a user
            //opinions service returns all the opinions where this user is the receiver
            Optional<ArrayList<String>> optional = repository.findViewedLikesByReceiverEmail(contents.receiverEmail, true);
            if(optional.isPresent()){
                ArrayList<String> likes = optional.get();
                ArrayList<String> dislikes = repository.findViewedDislikesByReceiverEmail(contents.receiverEmail, true).get();
                System.out.println("CALLED");
                MessageTypeD messageTypeD = new MessageTypeD(likes, dislikes, false, contents.receiverEmail);
                jmsTemplate.convertAndSend(queueD, messageTypeD);
            }
            else{
                System.out.println("Unknown message type: " + message.getClass().getCanonicalName());
                return;
            }
        }
        else{
            System.out.println("Unknown message type: " + message.getClass().getCanonicalName());
            return;
        }
    }
}
