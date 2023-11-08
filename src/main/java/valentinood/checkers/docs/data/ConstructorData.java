package valentinood.checkers.docs.data;

import valentinood.checkers.docs.TextFile;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConstructorData implements IDocumentable {
    private final Class<?> parent;
    private final Constructor<?> constructor;

    private final List<Annotation> annotations;
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
        annotations.addAll(Arrays.stream(constructor.getAnnotations()).toList());

        exceptions = new ExceptionData(parent, constructor);

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


        // TODO: annotation
        file.replace("%ANNOTATIONS%", "");

        return file;
    }
}
