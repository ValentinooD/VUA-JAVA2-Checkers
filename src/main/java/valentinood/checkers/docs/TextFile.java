package valentinood.checkers.docs;

import valentinood.checkers.docs.data.IDocumentable;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class TextFile {
    protected final String resource;
    protected String content;

    public TextFile(String resource) throws Exception {
        this.resource = resource;
        load();
    }

    public void load() throws Exception {
        URL url = ProjectDocumentation.class.getResource(resource);
        assert url != null;
        content = Files.readString(Paths.get(url.toURI()));
    }

    public String getResource() {
        return resource;
    }

    public String getContent() {
        return content;
    }

    public void replace(String from, String to) {
        content = content.replace(from, to);
    }

    public void remove(String str) {
        replace(str, "");
    }

    public void clear() {
        content = "";
    }

    public void insert(String tag, IDocumentable doc) throws Exception {
        insert(tag, List.of(doc));
    }

    public void insert(String tag, List<IDocumentable> docs) throws Exception {
        StringBuilder builder = new StringBuilder();
        for (IDocumentable doc : docs) {
            builder.append(doc.createDocs().getContent());
        }

        replace(tag, builder.toString());
    }
}
