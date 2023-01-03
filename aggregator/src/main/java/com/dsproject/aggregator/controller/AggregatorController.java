package com.dsproject.aggregator.controller;

import com.dsproject.core.MessageTypeC;
import com.dsproject.core.MessageTypeD;
import com.dsproject.aggregator.entity.Aggregator;
import com.dsproject.aggregator.helpers.API;
import com.dsproject.aggregator.repository.AggregatorRepository;


import org.apache.activemq.command.ActiveMQObjectMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.JmsException;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
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

    public void returnUnsuccesfulMessage(){
        MessageTypeC errorMessage = new MessageTypeC(false, null);
        jmsTemplate.convertAndSend(queue, errorMessage);
    }

    public void returnSuccessMessage(){
        MessageTypeC successMessage = new MessageTypeC(true, null);
        jmsTemplate.convertAndSend(queue, successMessage);
    }

    @JmsListener(destination = "queueD")
    public void consume(Object received) throws JmsException{
        //should ideally be handled using threads, but JPA is not thread safe.
        ActiveMQObjectMessage receivedConverted = (ActiveMQObjectMessage) received;
        Object message;
        try {
            message = receivedConverted.getObject();
        } catch (JMSException e) {
            e.printStackTrace();
            returnUnsuccesfulMessage();
            return;
        }
        if (message instanceof MessageTypeD) {
            MessageTypeD contents = (MessageTypeD) message;
            if(contents.reaggregate == true){
                //scenario alpha: user has just read a new piece of feedback, we mark 
                //the reaggregate flag as true for this user, so that the aggregation 
                //is performed again when requested next time, instead of simply displaying
                //the results from the previous aggregation
                Optional<Aggregator> optional = repository.findById(contents.receiverEmail);
                if (optional.isPresent()) {
                    Aggregator aggregator = optional.get();
                    aggregator.setReaggregate(true);
                    repository.save(aggregator);
                }
                else{
                    Aggregator aggregator = new Aggregator();
                    aggregator.setEmail(contents.receiverEmail);
                    aggregator.setReaggregate(true);
                    aggregator.setResults(new LinkedHashMap<String, Double>());
                    repository.save(aggregator);
                }
                returnSuccessMessage();
                return;
            }
            else{
                returnUnsuccesfulMessage();
                return;
            }
        }
        else{
            System.out.println("Unknown message type: " + message.getClass().getCanonicalName());
            returnUnsuccesfulMessage();
        }
    }

    @GetMapping("/aggregator")
    public String aggregator(Model model, HttpSession session) {
        String currentUser = (String)session.getAttribute("username");
        if(currentUser == null){
            return loginUrl;
        }

        //if the user has no reviews and is trying to access this page, show a message that informs them 
        //that they have no reviews
        Optional<Aggregator> optional = repository.findById(currentUser);
        if (optional.isPresent()) {
            Aggregator aggregator = optional.get();
            if(aggregator.getResults().size() == 0 && aggregator.getReaggregate() == false){
                return "noReviews";
            }
            else{
                //the user has previous reviews and/or needs to be reaggregated. we check if reaggregate is set
                if(aggregator.getReaggregate()){
                    //if reaggregate is true, new data needs to be fetched from the opinions service
                    //this data is then passed through the aggregator and if all is well, reaggregate is set to false.
                    MessageTypeC requestAllOpinions = new MessageTypeC(null, currentUser);
                    jmsTemplate.convertAndSend(queue, requestAllOpinions);

                    //await response from opinions microservice
                    Object message = jmsTemplate.receiveAndConvert("queueD");
                    if (message instanceof MessageTypeD) {
                        MessageTypeD contents = (MessageTypeD) message;
                        if(contents.allLikes != null){
                            //if valid, go ahead (aggregate)
                            String combinedFeedback = "";
                            for(int i = 0; i< contents.allLikes.size(); i++){
                                combinedFeedback += contents.allLikes.get(i);
                                combinedFeedback += contents.allDislikes.get(i);
                            }
                            System.out.println("RAKE will aggregate: " + combinedFeedback);
                            API rakeAPI = new API();
                            LinkedHashMap<String, Double> results = rakeAPI.extract(combinedFeedback); 
                            aggregator.setReaggregate(false);
                            aggregator.setResults(results);
                            repository.save(aggregator);
                            model.addAttribute("results", results);
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
                else{
                    //if not, we simply display the previously stored data
                    model.addAttribute("results", aggregator.getResults());
                }
            }
        }
        else{
            return "noReviews";
        }
        return "aggregator";
    }
}
