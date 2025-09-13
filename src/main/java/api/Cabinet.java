package api;

import java.util.List;
import java.util.Optional;

public interface Cabinet {

    // returns any element with the given name
    Optional<Folder> findFolderByName(String name);

    // returns all folders of the given size (SMALL/MEDIUM/LARGE)
    List<Folder> findFoldersBySize(String size);

    // returns the number of all objects that make up the structure
    int count();
}