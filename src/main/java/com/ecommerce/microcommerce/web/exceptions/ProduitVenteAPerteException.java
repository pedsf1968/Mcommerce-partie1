package com.ecommerce.microcommerce.web.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class ProduitVenteAPerteException extends RuntimeException {

    public ProduitVenteAPerteException(String message){
        super( message);
    }
}
