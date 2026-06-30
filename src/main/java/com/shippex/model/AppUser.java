package com.shippex.model;

import com.shippex.constants.Designation;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "app_user")
@NoArgsConstructor
public class AppUser extends BaseUser{

    public AppUser(String name, String username, String password) {
        super(name, username, password);
    }

    public AppUser(String name, String username, String password, String email) {
        super(name, username, password, email);
    }

    private String whatsappContactNo;
    private Designation designation;
    private String shipName;
    private String ShipIMONumber;

    private Boolean verified;
}
