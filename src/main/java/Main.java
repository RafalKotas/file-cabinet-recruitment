import api.Folder;
import core.FileCabinet;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        Folder fA = new FileCabinet.BasicFolder("A-small", "50MB");
        Folder fB = new FileCabinet.BasicFolder("B-medium", "850MB");
        Folder fC = new FileCabinet.BasicFolder("C-large", "2GB");
        Folder g1 = new FileCabinet.GroupFolder("G1", "900MB", List.of(fA, fB));
        FileCabinet cab = new FileCabinet(List.of(g1, fC));

        System.out.println("SMALL: " + cab.findFoldersBySize("SMALL").stream().map(Folder::name).toList());
        System.out.println("MEDIUM: " + cab.findFoldersBySize("MEDIUM").stream().map(Folder::name).toList());
        System.out.println("LARGE: " + cab.findFoldersBySize("LARGE").stream().map(Folder::name).toList());
        System.out.println("count = " + cab.count());
    }
}
