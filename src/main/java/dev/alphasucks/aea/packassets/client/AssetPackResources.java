package dev.alphasucks.aea.packassets.client;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.class_2960;
import net.minecraft.class_3262;
import net.minecraft.class_3264;
import net.minecraft.class_3270;
import net.minecraft.class_7367;

/** A resource pack whose files are the downloaded data pack assets, read straight from the cache folder. */
final class AssetPackResources implements class_3262 {
    private final String name;
    private final Map<class_2960, Path> files;
    private final Set<String> namespaces = new HashSet<>();

    AssetPackResources(String name, Map<class_2960, Path> files) {
        this.name = name;
        this.files = files;
        for (class_2960 id : files.keySet()) {
            namespaces.add(id.method_12836());
        }
    }

    @Override
    public class_7367<InputStream> method_14410(String... segments) {
        return null;
    }

    @Override
    public class_7367<InputStream> method_14405(class_3264 type, class_2960 id) {
        Path file = type == class_3264.field_14188 ? files.get(id) : null;
        return file == null ? null : class_7367.create(file);
    }

    @Override
    public void method_14408(class_3264 type, String namespace, String prefix, class_3262.class_7664 consumer) {
        if (type != class_3264.field_14188 || !namespaces.contains(namespace)) {
            return;
        }
        String start = prefix + "/";
        for (Map.Entry<class_2960, Path> entry : files.entrySet()) {
            class_2960 id = entry.getKey();
            if (id.method_12836().equals(namespace) && id.method_12832().startsWith(start)) {
                consumer.accept(id, class_7367.create(entry.getValue()));
            }
        }
    }

    @Override
    public Set<String> method_14406(class_3264 type) {
        return type == class_3264.field_14188 ? namespaces : Set.of();
    }

    @Override
    public <T> T method_14407(class_3270<T> reader) {
        return null;
    }

    @Override
    public String method_14409() {
        return name;
    }

    @Override
    public void close() {
    }
}
