package core;

import api.Cabinet;
import api.Folder;
import api.MultiFolder;

import java.util.*;
import java.util.function.Predicate;

import static java.util.Collections.newSetFromMap;
import static util.SizeUtil.parseBytesSafe;

public class FileCabinet implements Cabinet {
    private final List<Folder> folders;

    public FileCabinet(List<Folder> folders) {
        this.folders = List.copyOf(Objects.requireNonNull(folders));
    }

    @Override
    public Optional<Folder> findFolderByName(String name) {
        if (name == null) return Optional.empty();
        List<Folder> hits = bfsFilter(f -> name.equals(f.name()), true);
        return hits.isEmpty() ? Optional.empty() : Optional.of(hits.get(0));
    }

    @Override
    public List<Folder> findFoldersBySize(String size) {
        var targetOpt = FolderSize.fromString(size);
        if (targetOpt.isEmpty()) return List.of();
        var target = targetOpt.get();

        return bfsFilter(f -> {
            Long bytes = parseBytesSafe(f.size());
            return bytes != null && FolderSize.classify(bytes) == target;
        }, false);
    }

    @Override
    public int count() {
        return bfsFilter(f -> true, false).size();
    }

    private List<Folder> bfsFilter(Predicate<Folder> predicate, boolean stopOnFirst) {
        List<Folder> result = new ArrayList<>();
        var visited = newSetFromMap(new java.util.IdentityHashMap<Folder, Boolean>());
        List<Folder> queue = new ArrayList<>(folders);
        int i = 0;

        while (i < queue.size()) {
            Folder f = queue.get(i++);
            if (!visited.add(f)) continue; // in case of DAG

            if (predicate.test(f)) {
                result.add(f);
                if (stopOnFirst) break;
            }
            if (f instanceof MultiFolder mf) {
                queue.addAll(mf.getFolders());
            }
        }
        return result;
    }

    public record BasicFolder(String name, String size) implements Folder {
        public BasicFolder {
            Objects.requireNonNull(name, "name");
            Objects.requireNonNull(size, "size");
        }
    }

    public record GroupFolder(String name, String size, List<Folder> folders)
            implements MultiFolder {
        public GroupFolder {
            Objects.requireNonNull(name, "name");
            Objects.requireNonNull(size, "size");
            folders = List.copyOf(Objects.requireNonNull(folders));
        }
        @Override public List<Folder> getFolders() { return folders; }
    }

    private enum FolderSize { SMALL, MEDIUM, LARGE;

        static Optional<FolderSize> fromString(String s) {
            if (s == null) return Optional.empty();
            String v = s.trim().toUpperCase(Locale.ROOT);
            switch (v) {
                case "S": return Optional.of(SMALL);
                case "M": return Optional.of(MEDIUM);
                case "L": return Optional.of(LARGE);
                default:
                    try { return Optional.of(FolderSize.valueOf(v)); }
                    catch (IllegalArgumentException ignored) { return Optional.empty(); }
            }
        }

        // Thresholds: <100MB, [100MB,1GB), ≥1GB
        static FolderSize classify(long bytes) {
            long KB = 1024L, MB = 1024L * KB, GB = 1024L * MB;
            if (bytes < 100 * MB) return SMALL;
            if (bytes < GB)       return MEDIUM;
            return LARGE;
        }
    }
}
