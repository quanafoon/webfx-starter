package webfx.devs;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.ElementType;


/**
 * The Annotation interface that is meant for establishing endpoints
 * <p> Methods using this endpoint must be static to be reached by the {@link Router} class</p>
 * <p> All Methods using this Annotation is made public </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Endpoint {
    
    /**
     * The name that it used to identify endpoints
     * @return {@code String} name
     */
    public String name();

}
