package com.ares.urlshortening.enumeration;

import lombok.Getter;

public enum VerificationType {

    ACCOUNT,
    PASSWORD;

    public String getType(){
        return this.name().toLowerCase();
    }


}
