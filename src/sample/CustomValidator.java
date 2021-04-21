package sample;

/** Simple class designed to be inherited from
 * to wrap a custom validation method.
 * In other languages I'd just be able to pass around
 * method references, but having trouble figuring that
 * out in java... hope this works!
 */
public abstract class CustomValidator {

    public abstract boolean checkValidity();

}
