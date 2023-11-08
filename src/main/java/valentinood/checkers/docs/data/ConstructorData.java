package valentinood.checkers.docs.data;

import valentinood.checkers.docs.TextFile;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

public class ConstructorData implements IDocumentable {
    private final Class<?> parent;
    private final Constructor<?> constructor;

    private final List<AnnotationData> annotations;
    private final List<ParameterData> params;
    private ExceptionData exceptions;

    public ConstructorData(Class<?> parent, Constructor<?> constructor) {
        this.parent = parent;
        this.constructor = constructor;

        this.annotations = new ArrayList<>();
        this.params = new ArrayList<>();
        load();
    }

    private void load() {
        exceptions = new ExceptionData(parent, constructor);

        for (Annotation annotation : constructor.getAnnotations()) {
            annotations.add(new AnnotationData(annotation));
        }

        for (Parameter param : constructor.getParameters()) {
            params.add(new ParameterData(parent, constructor, param));
        }
    }

    @Override
    public TextFile createDocs() throws Exception {
        TextFile file = new TextFile("constructor.html");
        file.replace("%NAME%", parent.getSimpleName());
        file.replace("%MOD%", Modifier.toString(constructor.getModifiers()));

        file.replace("%THROWS%", exceptions.createDocs().getContent());

        // Parameters
        List<String> list = new ArrayList<>();
        for (ParameterData param : params) {
            list.add(param.createDocs().getContent());
        }
        file.replace("%PARAMS%", String.join(", ", list));

        file.insert("%ANNOTATIONS%", annotations.stream().map(a -> (IDocumentable) a).toList());

        return file;
    }

    public Class<?> getParent() {
        return parent;
    }

    public Constructor<?> getConstructor() {
        return constructor;
    }

    public List<AnnotationData> getAnnotations() {
        return new ArrayList<>(annotations);
    }

    public List<ParameterData> getParams() {
        return new ArrayList<>(params);
    }

    public ExceptionData getExceptions() {
        return exceptions;
    }
}
