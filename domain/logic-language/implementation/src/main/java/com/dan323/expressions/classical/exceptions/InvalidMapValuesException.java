package com.dan323.expressions.classical.exceptions;

/**
 * In case a map has not all the necessary values
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
