package com.novoda.simple.medium;

import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

public class SimpleFrameworkMediumXmlBenchmark {

    public void parse(String xml) throws Exception {
        Serializer serializer = new Persister();
        Feed feed = serializer.read(Feed.class, xml);
        System.out.println(getClass().getSimpleName() + " " + feed);
    }

    @Root(name = "employee", strict = false)
    public static class Feed {
        @Element
        public String id;

        @Element
        public String title;

        @Element
        public String updated;

        @Path("author")
        @Element(name = "name")
        public String author;

        @Element
        public String logo;

        @Element
        public Link link;

        @Element
        public String generator;

        @ElementList(name = "entry", inline = true)
        public List<Entry> entries;

        public String toString() {
            return "Feed{" +
                    "id='" + id + '\'' +
                    ", title='" + title + '\'' +
                    ", updated='" + updated + '\'' +
                    ", author=" + author +
                    ", logo='" + logo + '\'' +
                    ", link='" + link + '\'' +
                    ", generator='" + generator + '\'' +
                    ", entries=" + entries +
                    '}';
        }
    }

    public static class Entry {
        @Element
        public String id;
        @Element
        public String title;
        @Element
        public String summary;
        @Element
        public String updated;
        @ElementList(name = "link", inline = true)
        public List<Link> links;

        @Override
        public String toString() {
            return "Entry{" +
                    "id='" + id + '\'' +
                    ", title='" + title + '\'' +
                    ", summary='" + summary + '\'' +
                    ", updated='" + updated + '\'' +
                    ", links=" + links +
                    '}';
        }
    }

    public static class Link {
        @Attribute
        public String href;
        @Attribute(required = false)
        public String title;
        @Attribute
        public String rel;
        @Attribute
        public String type;

        @Override
        public String toString() {
            return "Link{" +
                    "url='" + href + '\'' +
                    ", title='" + title + '\'' +
                    ", rel='" + rel + '\'' +
                    ", type='" + type + '\'' +
                    '}';
        }
    }

}
