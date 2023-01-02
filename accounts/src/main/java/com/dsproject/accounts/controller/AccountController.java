package com.dsproject.accounts.controller;

import com.dsproject.core.MessageTypeA;
import com.dsproject.core.MessageTypeB;
import com.dsproject.accounts.entity.Account;
import com.dsproject.accounts.helpers.NotificationManager;
import com.dsproject.accounts.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.jms.Queue;
import javax.servlet.http.HttpSession;

@Controller
public class AccountController {
    String gatewayUrl = "http://localhost:8000";
    String dashUrl = "redirect:" + gatewayUrl + "/opinions/dashboard";
    final String notifNewCredit = "You have a new credit because of your latest review! Check it out!";
    final String notifNewReview = "Someone has reviewed you! Check it out!";
    private String notifNewPeer(String peerName){
        return peerName + " has just joined PeerRate who works in Your Company. Check it out!";
    }
    private String notifLessCredit(Integer newCreditCount){
        return "You have consumed a credit by viewing the review. Review others to get more! New Count: " + newCreditCount;
    }

    @Autowired
    private AccountRepository repository;

    @Autowired
	JmsTemplate jmsTemplate;
	
	@Autowired
	Queue queue;

    public void returnUnsuccesfulMessage(){
        MessageTypeB errorMessage = new MessageTypeB(false, false, null, null);
        jmsTemplate.convertAndSend(queue, errorMessage);
    }

    @JmsListener(destination = "queueA")
    public void consume(Object message){
		System.out.println("CALLED");
        if (message instanceof MessageTypeA) {
            MessageTypeA contents = (MessageTypeA) message;
            if(contents.type.equals("increment")){
                //the user has just tried to add a review.
                //on accounts microservice, we validate recepient email
                if(repository.findById(contents.receiverEmail).isPresent() && repository.findById(contents.senderEmail).isPresent()){
                    //we increment sender user credits
                    Account senderAccount = repository.findById(contents.senderEmail).get();
                    Account receiverAccount = repository.findById(contents.receiverEmail).get();
                    senderAccount.setCredits(senderAccount.getCredits() + 1);
                    repository.save(senderAccount);
                    //we add a notification to sender that they have a new credit
                    senderAccount.setNotifications(NotificationManager.addNotification(notifNewCredit, senderAccount.getNotifications()));
                    //we add a notification to receiver that they have been reviewed
                    receiverAccount.setNotifications(NotificationManager.addNotification(notifNewReview, receiverAccount.getNotifications()));
                }
                else{
                    System.out.println("failed: fake receiver or sender");
                    returnUnsuccesfulMessage();
                }
            }
            else if(contents.type.equals("decrement")){
                //the user has just attempted to read a review they hadn't read before
                //we check that they have enough credits (1 or more)
                if(repository.findById(contents.receiverEmail).isPresent() && repository.findById(contents.senderEmail).isPresent()){
                    //if so, allowed is true, and we reduce credits by 1. we return the new credit count
                    Account receiverAccount = repository.findById(contents.receiverEmail).get();
                    if(receiverAccount.getCredits() > 0){
                        receiverAccount.setCredits(receiverAccount.getCredits() - 1);
                        repository.save(receiverAccount);
                        //we add a notification to reader of the consumption of a credit
                        receiverAccount.setNotifications(NotificationManager.addNotification(notifLessCredit(receiverAccount.getCredits()), receiverAccount.getNotifications()));
                        
                        //we inform opinions service to go ahead
                        MessageTypeB returnMessage = new MessageTypeB(true, true, null, null);
                    }
                    else{
                        System.out.println("failed: not enough credits");
                        returnUnsuccesfulMessage();
                    }
                }
                else{
                    //otherwise it is false
                    System.out.println("failed: fake receiver or sender");
                    returnUnsuccesfulMessage();
                }
            }
            else if(contents.type.equals("fetch")){
                //the user has opened the dashboard
                //we return their current notifications
                //we tell them how many credits they have
            }
            else{
                System.out.println("Unknown message type: " + message.getClass().getCanonicalName());
                returnUnsuccesfulMessage();
            }
        }
        else{
            System.out.println("Unknown message type: " + message.getClass().getCanonicalName());
            returnUnsuccesfulMessage();
        }

		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		try{
			IncrementCheck incrementCheck = new IncrementCheck(attemptIncrement((String) message), jmsTemplate, queue);
			executor.execute(incrementCheck);
		}
		catch(Exception e){
			e.printStackTrace();
		}
    }

    static class IncrementCheck implements Runnable{		
        private boolean allowed;
		private JmsTemplate jmsTemplate;
		private Queue queue;

        public IncrementCheck(boolean allowed, JmsTemplate jmsTemplate, Queue queue){
            this.allowed = allowed;
			this.jmsTemplate = jmsTemplate;
			this.queue = queue;
        }

        public void run(){
            try{
				jmsTemplate.convertAndSend(queue, allowed);
				System.out.println("SENT: " + allowed);
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    } 

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
            session.setAttribute("username", email);
            
            //notify all people in this person's company of their joining
            List<Account> companyProfiles = repository.returnAllInCompany(company);
            for(Account companyProfile: companyProfiles){
                companyProfile.setNotifications(NotificationManager.addNotification(notifNewPeer(name), companyProfile.getNotifications()));
                repository.save(companyProfile);
            }

            repository.save(account);
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
