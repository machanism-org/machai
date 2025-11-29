package org.machanism.machai.core.bindex;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.maven.model.Model;
import org.apache.maven.model.building.DefaultModelBuilder;
import org.apache.maven.model.building.DefaultModelBuildingRequest;
import org.apache.maven.model.building.ModelBuildingException;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.building.ModelBuildingResult;
import org.apache.maven.model.interpolation.StringSearchModelInterpolator;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.model.path.DefaultPathTranslator;
import org.apache.maven.model.path.DefaultUrlNormalizer;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

public class EffectivePomReader {

	public static Model getEffectivePom(File pomFile) {
		try {
			PlexusContainer container = new DefaultPlexusContainer();
			DefaultModelBuilder modelBuilder = (DefaultModelBuilder) container
					.lookup(org.apache.maven.model.building.ModelBuilder.class);

			StringSearchModelInterpolator modelInterpolator = new StringSearchModelInterpolator();
			modelInterpolator.setPathTranslator(new DefaultPathTranslator());
			modelInterpolator.setUrlNormalizer(new DefaultUrlNormalizer());
			modelBuilder.setModelInterpolator(modelInterpolator);

			ModelBuildingRequest request = new DefaultModelBuildingRequest();
			request.setPomFile(pomFile);
			request.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL);
			request.setProcessPlugins(true);
			request.setTwoPhaseBuilding(false);

			ModelBuildingResult result = modelBuilder.build(request);

			return result.getEffectiveModel();

		} catch (PlexusContainerException | ComponentLookupException | ModelBuildingException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public static String printModel(Model model) throws IOException {
		MavenXpp3Writer writer = new MavenXpp3Writer();
		Writer stringWriter = new StringWriter();
		writer.write(stringWriter, model);
		return stringWriter.toString();
	}
}