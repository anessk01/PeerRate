package com.dsproject.aggregator.controller;

import com.dsproject.core.MessageTypeC;
import com.dsproject.core.MessageTypeD;
import com.dsproject.aggregator.entity.Aggregator;
import com.dsproject.aggregator.repository.AggregatorRepository;

import com.linguistic.rake.API;

import org.apache.activemq.command.ActiveMQObjectMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.JmsException;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.servlet.http.HttpSession;

@Controller
public class AggregatorController{
    String gatewayUrl = "http://localhost:8000";
    String loginUrl = "redirect:" + gatewayUrl + "/accounts/login";
    

    @Autowired
    private AggregatorRepository repository;

    @Autowired
	JmsTemplate jmsTemplate;
	
	@Autowired
	Queue queue;


    // @JmsListener(destination = "queueA")
    // public void consume(Object received) throws JmsException{
    //     //should ideally be handled using threads, but JPA is not thread safe.
    //     ActiveMQObjectMessage receivedConverted = (ActiveMQObjectMessage) received;
    //     Object message;
    //     try {
    //         message = receivedConverted.getObject();
    //     } catch (JMSException e) {
    //         e.printStackTrace();
    //         returnUnsuccesfulMessage();
    //         return;
    //     }
    //     if (message instanceof MessageTypeA) {
    //         MessageTypeA contents = (MessageTypeA) message;
    //         if(contents.type.equals("increment")){
    //             //the user has just tried to add a review. opinions service will await accounts' response to confirm information consistency.
    //             //on accounts microservice, we validate recepient email
    //             if(repository.findById(contents.receiverEmail).isPresent() && repository.findById(contents.senderEmail).isPresent()){
    //                 //we increment sender user credits
    //                 Account senderAccount = repository.findById(contents.senderEmail).get();
    //                 Account receiverAccount = repository.findById(contents.receiverEmail).get();
    //                 senderAccount.setCredits(senderAccount.getCredits() + 1);

    //                 LinkedList<String> notificationsSender = senderAccount.getNotifications();
    //                 if(notificationsSender == null){
    //                     notificationsSender = new LinkedList<String>();
    //                 }
    //                 //we add a notification to sender that they have a new credit
    //                 senderAccount.setNotifications(NotificationManager.addNotification(notifNewCredit, notificationsSender));

    //                 LinkedList<String> notificationsReceiver = receiverAccount.getNotifications();
    //                 if(notificationsReceiver == null){
    //                     notificationsReceiver = new LinkedList<String>();
    //                 }
    //                 //we add a notification to receiver that they have been reviewed
    //                 receiverAccount.setNotifications(NotificationManager.addNotification(notifNewReview, notificationsReceiver));
    //                 repository.save(senderAccount);
    //                 repository.save(receiverAccount);
    //                 returnSuccessMessage();
    //             }
    //             else{
    //                 System.out.println("failed: fake receiver or sender");
    //                 returnUnsuccesfulMessage();
    //             }
    //         }
    //         else if(contents.type.equals("decrement")){
    //             //the user has just attempted to read a review they hadn't read before. opinions service depends on response to either show opinion or not.
    //             //we check that they have enough credits (1 or more)
    //             if(repository.findById(contents.receiverEmail).isPresent() && repository.findById(contents.senderEmail).isPresent()){
    //                 //if so, allowed is true, and we reduce credits by 1
    //                 Account receiverAccount = repository.findById(contents.receiverEmail).get();
    //                 if(receiverAccount.getCredits() > 0){
    //                     receiverAccount.setCredits(receiverAccount.getCredits() - 1);

    //                     LinkedList<String> notificationsReceiver = receiverAccount.getNotifications();
    //                     if(notificationsReceiver == null){
    //                         notificationsReceiver = new LinkedList<String>();
    //                     }
    //                     //we add a notification to reader informing of the consumption of a credit
    //                     receiverAccount.setNotifications(NotificationManager.addNotification(notifLessCredit(receiverAccount.getCredits()), notificationsReceiver));
                        
    //                     repository.save(receiverAccount);
    //                     //we inform opinions service to go ahead
    //                     returnSuccessMessage();
    //                 }
    //                 else{
    //                     System.out.println("failed: not enough credits");
    //                     returnUnsuccesfulMessage();
    //                 }
    //             }
    //             else{
    //                 //otherwise it is false
    //                 System.out.println("failed: fake receiver or sender");
    //                 returnUnsuccesfulMessage();
    //             }
    //         }
    //         else if(contents.type.equals("fetch")){
    //             //the user has opened the dashboard
    //             //we return their current notifications
    //             //we tell them how many credits they have
    //             Account senderAccount = repository.findById(contents.senderEmail).get();
    //             MessageTypeB successMessage = new MessageTypeB(true, true, senderAccount.getNotifications(), senderAccount.getCredits());
    //             jmsTemplate.convertAndSend(queue, successMessage);
    //         }
    //         else{
    //             System.out.println("Unknown message type: " + message.getClass().getCanonicalName());
    //             returnUnsuccesfulMessage();
    //         }
    //     }
    //     else{
    //         System.out.println("Unknown message type: " + message.getClass().getCanonicalName());
    //         returnUnsuccesfulMessage();
    //     }
    // }

    @GetMapping("/aggregator")
    public String homePage(Model model) {
        API rakeAPI = new API();
        LinkedHashMap<String, Double> results = rakeAPI.extract("This is a test input to see if the entire flow works or is utter crap"); 
        model.addAttribute("results", results);
        return "home";
    }
}
