package api;

import java.util.List;

public interface MultiFolder extends Folder {
    List<Folder> getFolders();
}
