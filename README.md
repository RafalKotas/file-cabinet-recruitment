Implementation of `Cabinet` / `Folder` / `MultiFolder` with BFS traversal (handles `MultiFolder`) and
reference-based de-duplication (DAG-safe). Size filter supports `SMALL` / `MEDIUM` / `LARGE`.

## Features
- `findFolderByName(String)` — returns the first matching folder (BFS, early exit).
- `findFoldersBySize(String)` — returns all folders that fall into a size bucket (`SMALL`/`MEDIUM`/`LARGE`).
- `count()` — counts **unique** nodes (reference de-duplication; safe on DAGs).
- Thresholds: `< 100MB` → SMALL, `100MB–<1GB` → MEDIUM, `≥ 1GB` → LARGE.
- Accepted size formats: `123`, `123KB`, `10MB`, `2GB` (no spaces). Unparseable sizes are ignored.

## Requirements
- Java 17+
- Maven 3.9+

## Run tests
```bash
mvn -DskipTests=false test