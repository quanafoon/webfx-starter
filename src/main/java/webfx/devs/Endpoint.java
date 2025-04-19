package webfx.devs;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.ElementType;


/*
 * The Annotation interface that is meant for establishing endpoints endpoints
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Endpoint {
    
    //The name of the endpoint
    public String name();

}
