package de.bas.bodo.genai;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

@DisplayName("Modularity")
class ModularityTests {
	@Test
	void verifiesModuleDependencies() {
		ApplicationModules.of(GenaiApplication.class).verify();
	}

	@Test
	void writesPlantUmlDocumentation() throws Exception {
		Path output = Path.of("docs", "modulith");
		Files.createDirectories(output);
		ApplicationModules modules = ApplicationModules.of(GenaiApplication.class);
		new Documenter(modules, Documenter.Options.defaults().withOutputFolder(output.toString()))
				.writeModulesAsPlantUml()
				.writeIndividualModulesAsPlantUml();
	}
}
