package core;

import api.Cabinet;
import api.Folder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class FileCabinetTest {

    private Cabinet sampleCabinet() {
        // leaves
        Folder fA = new FileCabinet.BasicFolder("A-small", "50MB");     // SMALL (<100MB)
        Folder fB = new FileCabinet.BasicFolder("B-medium", "850MB");   // MEDIUM (>=100MB && <1GB)
        Folder fC = new FileCabinet.BasicFolder("C-large", "2GB");      // LARGE (>=1GB)
        Folder fX = new FileCabinet.BasicFolder("Xbad", "oops");       // non parseable

        // Group g1 contains fA & fB
        Folder g1 = new FileCabinet.GroupFolder("G1", "900MB", List.of(fA, fB)); // MEDIUM (>=100MB && <1GB)

        // Group ROOT contains g1 & fC & fX
        return new FileCabinet(List.of(g1, fC, fX));
    }

    private Cabinet dagCabinet() {
        Folder fA = new FileCabinet.BasicFolder("A-small",  "50MB");   // SMALL
        Folder fB = new FileCabinet.BasicFolder("B-medium", "850MB");  // MEDIUM
        Folder fC = new FileCabinet.BasicFolder("C-large",  "2GB");    // LARGE
        Folder fD = new FileCabinet.BasicFolder("B-small", "72MB");    // SMALL

        Folder g1 = new FileCabinet.GroupFolder("G1", "900MB", List.of(fA, fB)); // MEDIUM
        Folder g2 = new FileCabinet.GroupFolder("G2", "972MB", List.of(fA, fB, fD)); // MEDIUM

        return new FileCabinet(List.of(g1, g2, fC));
    }

    @Test
    @DisplayName("findFolderByName — finds A-small (leaf), G1 (group), and C-large (root)")
    void shouldFindLeafAndNested_whenFindFolderByName() {
        // given
        Cabinet cab = sampleCabinet();

        // when & then
        assertTrue(cab.findFolderByName("A-small").isPresent(), "Should find leaf: A-small (50MB).");
        assertTrue(cab.findFolderByName("G1").isPresent(),       "Should find group: G1 (900MB).");
        assertTrue(cab.findFolderByName("C-large").isPresent(),  "Should find root: C-large (2GB).");
        assertFalse(cab.findFolderByName("non-existing").isPresent(), "Should not find: non-existing.");
    }

    @Test
    @DisplayName("findFoldersBySize — SMALL={A-small}, MEDIUM={G1,B-medium}, LARGE={C-large}")
    void shouldReturnExpectedSetsForSmallMediumLarge_whenFindFoldersBySize() {
        // given
        Cabinet cab = sampleCabinet();

        // when
        var small = cab.findFoldersBySize("SMALL");
        var medium = cab.findFoldersBySize("medium");
        var mediumNames = medium.stream().map(Folder::name).collect(java.util.stream.Collectors.toSet());
        var large = cab.findFoldersBySize("LARGE");

        // then
        assertEquals(1, small.size(), "SMALL should contain exactly: A-small(50 MB).");
        assertEquals("A-small", small.get(0).name());

        assertEquals(2, medium.size(), "MEDIUM should contain exactly: G1 (900 MB), B-medium (850 MB).");
        assertEquals(Set.of("G1", "B-medium"), mediumNames);

        assertEquals(1, large.size(), "LARGE should contain exactly: C-large(2GB).");
        assertEquals("C-large", large.get(0).name());
    }

    @Test
    @DisplayName("findFoldersBySize — ignores unparseable sizes")
    void shouldIgnoreUnparseableSizes_whenFindFoldersBySize() {
        // given
        Cabinet cab = sampleCabinet();

        // when
        List<Folder> filteredSmallSizeFilesWithXbadName = cab.findFoldersBySize("SMALL").stream()
                .filter(f -> f.name().equals("Xbad"))
                .toList();
        List<Folder> filteredMediumSizeFilesWithXbadName = cab.findFoldersBySize("MEDIUM").stream()
                .filter(f -> f.name().equals("Xbad"))
                .toList();
        List<Folder> filteredLargeSizeFilesWithXbadName = cab.findFoldersBySize("LARGE").stream()
                .filter(f -> f.name().equals("Xbad"))
                .toList();

        // then
        assertEquals(0, filteredSmallSizeFilesWithXbadName.size(),  "Xbad should not appear in SMALL.");
        assertEquals(0, filteredMediumSizeFilesWithXbadName.size(), "Xbad should not appear in MEDIUM.");
        assertEquals(0, filteredLargeSizeFilesWithXbadName.size(),  "Xbad should not appear in LARGE.");
    }

    @Test
    @DisplayName("findFoldersBySize — returns empty for unknown/blank/null label")
    void shouldReturnEmpty_whenFindFoldersBySizeWithUnknownOrBlankOrNullLabel() {
        // given
        Cabinet cab = sampleCabinet();

        // when
        List<Folder> foldersWithSizeHUGE = cab.findFoldersBySize("HUGE");
        List<Folder> foldersWithEmptySize = cab.findFoldersBySize("");
        List<Folder> foldersWithNullSize = cab.findFoldersBySize(null);

        // then
        assertTrue(foldersWithSizeHUGE.isEmpty(),  "Unknown label (HUGE) => empty list.");
        assertTrue(foldersWithEmptySize.isEmpty(), "Blank label => empty list.");
        assertTrue(foldersWithNullSize.isEmpty(),  "Null label => empty list.");
    }

    @Test
    @DisplayName("findFolderByName (DAG) — finds shared leaf")
    void shouldFindSharedLeafInDAG_whenFindFolderByName() {
        // given
        Cabinet cab = dagCabinet();

        // when & then
        assertTrue(cab.findFolderByName("A-small").isPresent(), "Should find shared leaf: A-small.");
        assertTrue(cab.findFolderByName("G1").isPresent(),      "Should find group: G1.");
        assertTrue(cab.findFolderByName("G2").isPresent(),      "Should find group: G2.");
        assertTrue(cab.findFolderByName("C-large").isPresent(), "Should find root: C-large.");
        assertFalse(cab.findFolderByName("NOPE").isPresent(),   "Should not find: NOPE.");
    }

    @Test
    @DisplayName("findFoldersBySize (DAG) — deduplicates shared nodes")
    void shouldDeduplicateSharedNodesInDAG_whenFindFoldersBySize() {
        // given
        Cabinet cab = dagCabinet();

        // when
        var small = cab.findFoldersBySize("SMALL");
        var medium = cab.findFoldersBySize("MEDIUM");
        var mediumNames = medium.stream().map(Folder::name).collect(Collectors.toSet());
        var large = cab.findFoldersBySize("LARGE");

        // then
        assertEquals(2, small.size(), "SMALL should contain: A-small, B-small (no duplicates).");
        assertEquals("A-small", small.get(0).name());
        assertEquals(3, medium.size(), "MEDIUM should contain: B-medium, G1, G2 (deduplicated).");
        assertEquals(Set.of("B-medium", "G1", "G2"), mediumNames);
        assertEquals(1, large.size(), "LARGE should contain: C-large.");
        assertEquals("C-large", large.get(0).name());
    }

    @Test
    @DisplayName("count — counts all unique nodes in sample tree (G1, A-small, B-medium, C-large, Xbad)")
    void shouldCountAllNodes_whenCountOnSampleCabinet() {
        // given
        Cabinet cab = sampleCabinet();

        // when
        int total = cab.count();

        // then
        assertEquals(5, total, "Expected 5 nodes: G1, A-small, B-medium, C-large, Xbad");
    }

    @Test
    @DisplayName("count (DAG) — counts unique nodes without duplicates (G1, G2, C-large, A-small, B-medium, B-small)")
    void shouldCountAllNodes_whenCountOnDagCabinet() {
        // given
        Cabinet cab = dagCabinet();

        // when
        int total = cab.count();

        // then
        assertEquals(6, total, "Expected 6 unique nodes: G1, G2, C-large, A-small, B-medium, B-small");
    }

    @Test
    @DisplayName("count — returns 0 for empty cabinet")
    void shouldReturnZero_whenCabinetIsEmpty() {
        // given
        Cabinet cab = new FileCabinet(List.of());

        // when
        int total = cab.count();

        // then
        assertEquals(0, total, "Expected 0 nodes for an empty cabinet");
    }
}