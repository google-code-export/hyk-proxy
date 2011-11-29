package javax.persistence;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies the primary key property or field of an entity<br/>
 * <a
 * href="http://java.sun.com/javaee/5/docs/api/javax/persistence/Id.html">http
 * ://java.sun.com/javaee/5/docs/api/javax/persistence/Id.html</a> <br/>
 * Reproduced from the java.persistence API
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target( {ElementType.METHOD, ElementType.FIELD})
public @interface Id {
}
