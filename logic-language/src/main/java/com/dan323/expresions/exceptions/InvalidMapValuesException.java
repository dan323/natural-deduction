package com.dan323.expresions.exceptions;

/**
 * In case a map has no all the necessary values
 * @author danco
 */
public class InvalidMapValuesException extends IllegalArgumentException {

    public InvalidMapValuesException(){
        super();
    }

    public InvalidMapValuesException(String mess){
        super(mess);
    }
}
