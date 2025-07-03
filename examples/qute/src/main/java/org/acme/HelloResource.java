package org.acme;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.jboss.resteasy.reactive.RestQuery;

import io.quarkus.qute.TemplateExtension;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;

@Path("/hello")
public class HelloResource {

    record hello(String name, List<String> headers)
            implements TemplateInstance {
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get(@RestQuery String name,
            HttpHeaders headers) {
        return new hello(name, headers.getRequestHeaders().entrySet()
                .stream().map(e -> e.getKey() + ": " + e.getValue())
                .toList());
    }

    @TemplateExtension
    static List<String> sorted(List<String> list) {
        List<String> sorted = new ArrayList<>(list);
        sorted.sort(null);
        return sorted;
    }

    @TemplateExtension
    static <T> Iterator<T> reversed(List<T> list) {
        ListIterator<T> it = list.listIterator(list.size());
        return new Iterator<T>() {
            public boolean hasNext() {
                return it.hasPrevious();
            }
            public T next() {
                return it.previous();
            }
        };
    }
}
