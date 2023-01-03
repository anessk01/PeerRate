package com.dsproject.core;

import java.io.Serializable;

//message type C: sent from aggregator microservice to opinions microservice
public class MessageTypeC  implements Serializable {
    public Boolean aggregate;
    public String receiverEmail;

    public MessageTypeC(Boolean aggregate, String receiverEmail){
        this.aggregate = aggregate;
        this.receiverEmail = receiverEmail;
    }
}
