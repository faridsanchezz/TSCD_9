package test;

import software.ulpgc.pathfinder.GraphContainer;
import software.ulpgc.pathfinder.FileGraphLoader;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileGraphLoaderTest {

	@Test
	void load_buildsGraphFromValidFile() throws Exception {
		Path temp = Files.createTempFile("graph", ".csv");
		Files.write(temp, List.of(
				"A,B,1.5",
				"B,C,2.0"
		));

		FileGraphLoader loader = new FileGraphLoader(temp.toFile());
		GraphContainer container = loader.load();

		assertEquals(List.of("A", "B", "C"), container.shortestPathBetween("A", "C"));
		assertEquals(3.5, container.pathWeightBetween("A", "C"), 1e-9);
	}

	@Test
	void load_ignoresInvalidLinesAndKeepsValidOnes() throws Exception {
		Path temp = Files.createTempFile("graph-invalid", ".csv");
		Files.write(temp, List.of(
				"A,B,1.0",
				"BAD,LINE,WITH,EXTRA",  // parts.length != 3 -> ParseException -> catch
				"B,C,2.0",
				"C,D,NaNxx"            // NumberFormatException -> ParseException -> catch
		));

		FileGraphLoader loader = new FileGraphLoader(temp.toFile());
		GraphContainer container = loader.load();

		assertEquals(List.of("A", "B", "C"), container.shortestPathBetween("A", "C"));
		assertEquals(3.0, container.pathWeightBetween("A", "C"), 1e-9);

		// D no se añade por línea inválida
		assertThrows(IllegalArgumentException.class, () -> container.shortestPathBetween("A", "D"));
	}

	@Test
	void load_emptyFile_createsEmptyGraph() throws Exception {
		Path temp = Files.createTempFile("graph-empty", ".csv");
		Files.write(temp, List.of());

		FileGraphLoader loader = new FileGraphLoader(temp.toFile());
		GraphContainer container = loader.load();

		assertThrows(IllegalArgumentException.class, () -> container.shortestPathBetween("A", "B"));
		assertThrows(IllegalArgumentException.class, () -> container.pathWeightBetween("A", "B"));
	}
}
