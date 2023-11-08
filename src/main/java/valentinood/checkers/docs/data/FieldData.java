package valentinood.checkers.docs.data;

import valentinood.checkers.docs.TextFile;
import valentinood.checkers.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class FieldData implements IDocumentable {
    private final Class<?> parent;
    private final Field field;

    private final String name;
    private final Class<?> type;
    private final String modifier;
    private final List<AnnotationData> annotations;

    public FieldData(Class<?> parent, Field field) {
        this.parent = parent;
        this.field = field;

        this.name = field.getName();
        this.type = field.getType();
        this.modifier = Modifier.toString(field.getModifiers());
        this.annotations = new ArrayList<>();

        for (Annotation annotation : field.getAnnotations()) {
            annotations.add(new AnnotationData(parent, annotation));
        }
    }

    @Override
    public TextFile createDocs() throws Exception {
        TextFile file = new TextFile("field.html");
        file.replace("%NAME%", name);
        file.replace("%TYPE%", ReflectionUtils.getType(field));
        file.replace("%MOD%", modifier);

        file.insert("%ANNOTATIONS%", annotations.stream().map(a -> (IDocumentable) a).toList());

        return file;
    }

    public Class<?> getParent() {
        return parent;
    }

    public Field getField() {
        return field;
    }

    public String getName() {
        return name;
    }

    public Class<?> getType() {
        return type;
    }

    public String getModifier() {
        return modifier;
    }

    public List<AnnotationData> getAnnotations() {
        return new ArrayList<>(annotations);
    }
}
