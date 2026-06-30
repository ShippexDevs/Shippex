package com.shippex.dto.whatsapp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WhatsAppTextMessageRequest {

    @JsonProperty("messaging_product")
    private String messagingProduct = "whatsapp";

    @JsonProperty("to")
    private String to;

    @JsonProperty("type")
    private String type = "text";

    @JsonProperty("text")
    private WhatsAppText text;

}