package valentinood.checkers.util;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public final class ReflectionUtils {
    private ReflectionUtils() {
    }

    public static String getType(Field field) {
        Class<?> type = field.getType();
        String typeName = type.getSimpleName();
        if (type.getTypeParameters().length > 0) {
            StringBuilder builder = new StringBuilder();
            builder.append(typeName);
            builder.append("&lt;");

            Type t = field.getGenericType();
            List<String> types = new ArrayList<>();

            if (t instanceof ParameterizedType pt) {
                for (Type t2 : pt.getActualTypeArguments()) {
                    types.add(t2.getTypeName().substring(t2.getTypeName().lastIndexOf(".") + 1));
                }
            }
            builder.append(String.join(", ", types));
            builder.append("&gt;");
            typeName = builder.toString();
        }
        return typeName;
    }
}
