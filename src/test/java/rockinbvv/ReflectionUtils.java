package rockinbvv;

import java.lang.reflect.Field;

public class ReflectionUtils {
    /**
     * Injects a value into a private final field of an enum instance.
     */
    public static void setBalanceStrategyFiled(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);

        field.set(target, value);
    }
}
