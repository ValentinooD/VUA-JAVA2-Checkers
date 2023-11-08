package valentinood.checkers.docs.data;

import valentinood.checkers.docs.TextFile;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

public class MethodData implements IDocumentable {
    private final Class<?> parent;
    private final Method method;

    private String name;
    private Class<?> returns;
    private String modifier;
    private final List<AnnotationData> annotations;
    private final List<ParameterData> params;
    private ExceptionData exceptions;

    public MethodData(Class<?> parent, Method method) {
        this.parent = parent;
        this.method = method;
        this.annotations = new ArrayList<>();
        this.params = new ArrayList<>();
        load();
    }

    private void load() {
        this.name = method.getName();
        this.returns = method.getReturnType();
        this.modifier = Modifier.toString(method.getModifiers());
        this.exceptions = new ExceptionData(parent, method);

        for (Annotation annotation : method.getAnnotations()) {
            annotations.add(new AnnotationData(annotation));
        }

        for (Parameter param : method.getParameters()) {
            params.add(new ParameterData(parent, method, param));
        }
    }

    @Override
    public TextFile createDocs() throws Exception {
        TextFile file = new TextFile("method.html");
        file.replace("%NAME%", name);

        if (returns.isPrimitive()) {
            file.replace("%TYPE%", "<span class=\"primitive\">" + returns.getSimpleName() + "</span>");
        } else {
            file.replace("%TYPE%", returns.getSimpleName());
        }

        file.replace("%MOD%", modifier);

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

    public Method getMethod() {
        return method;
    }

    public String getName() {
        return name;
    }

    public Class<?> getReturns() {
        return returns;
    }

    public String getModifier() {
        return modifier;
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
