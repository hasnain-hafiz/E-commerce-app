package com.Ecom.E_commerce.app.model;

import com.Ecom.E_commerce.app.model.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;
    private boolean revoked;
    private boolean expired;

    @ManyToOne
    @JoinColumn(name= "user_id")
    @JsonIgnore
    private User user;

}
