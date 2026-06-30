package com.shippex.dto.whatsapp;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class WhatsAppApiResponse {

    private List<Message> messages;

    @Getter
    @Setter
    public static class Message {

        private String id;

    }

}