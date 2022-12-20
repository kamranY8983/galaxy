package com.kam.galaxy.common.exception;

/**
 * @author Kamran Y. Khan
 * @since 18-Dec-2022
 */
public class GalaxyException extends RuntimeException{

    public GalaxyException(){
        super();
    }
    public GalaxyException(String message){
        super(message);
    }
    public GalaxyException(String message, Throwable cause){
        super(message, cause);
    }
    public GalaxyException(Throwable cause){
        super(cause);
    }
}
