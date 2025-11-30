package org.machanism.machai.core.bindex;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.DefaultModelBuilder;
import org.apache.maven.model.building.DefaultModelBuildingRequest;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.building.ModelBuildingResult;
import org.apache.maven.model.interpolation.StringSearchModelInterpolator;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.model.path.DefaultPathTranslator;
import org.apache.maven.model.path.DefaultUrlNormalizer;
import org.apache.maven.model.resolution.ModelResolver;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectModelResolver;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RequestTrace;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.impl.RemoteRepositoryManager;
import org.eclipse.aether.internal.impl.DefaultRepositorySystem;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.LocalRepositoryManager;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.transport.wagon.WagonTransporterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EffectivePomReader {

	private static Logger logger = LoggerFactory.getLogger(EffectivePomReader.class);

	public static Model getEffectivePom(File pomFile) {
		ModelBuildingRequest request = new DefaultModelBuildingRequest();
		request.setPomFile(pomFile);

		Model model = null;
		try {
			DefaultModelBuilder modelBuilder = getModelBuilder(request);
			ModelBuildingResult result = modelBuilder.build(request);
			model = result.getEffectiveModel();

		} catch (Exception e) {
			logger.warn("Unable to obtain effective pom: " + StringUtils.abbreviate(e.getLocalizedMessage(), 60));
			MavenXpp3Reader reader = new MavenXpp3Reader();
			try {
				model = reader.read(new FileReader(pomFile));
			} catch (IOException | XmlPullParserException e1) {
				e1.printStackTrace();
			}
		}

		return model;
	}

	private static DefaultModelBuilder getModelBuilder(ModelBuildingRequest request)
			throws PlexusContainerException, ComponentLookupException {
		DefaultServiceLocator locator = serviceLocator();
		RepositorySystem system = locator.getService(RepositorySystem.class);
		LocalRepository localRepo = new LocalRepository("C:\\Users\\vikto\\.m2\\repository2");

		DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
		LocalRepositoryManager repManager = system.newLocalRepositoryManager(session, localRepo);
		session.setLocalRepositoryManager(repManager);

		RequestTrace requestTrace = new RequestTrace(null);
		DefaultRepositorySystem repositorySystem = new DefaultRepositorySystem();
		repositorySystem.initService(locator);

		RemoteRepositoryManager remoteRepositoryManager = locator.getService(RemoteRepositoryManager.class);
		List<RemoteRepository> repos = Arrays.asList(new RemoteRepository.Builder("central", "default",
				"https://repo.maven.apache.org/maven2/").build());

		ModelResolver modelResolver = new ProjectModelResolver(session, requestTrace,
				repositorySystem, remoteRepositoryManager, repos,
				ProjectBuildingRequest.RepositoryMerging.POM_DOMINANT,
				null);
		request.setModelResolver(modelResolver);
		request.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL);
		request.setProcessPlugins(false);
		request.setTwoPhaseBuilding(false);

		PlexusContainer container = new DefaultPlexusContainer();
		DefaultModelBuilder modelBuilder = (DefaultModelBuilder) container
				.lookup(org.apache.maven.model.building.ModelBuilder.class);

		StringSearchModelInterpolator modelInterpolator = new StringSearchModelInterpolator();
		modelInterpolator.setPathTranslator(new DefaultPathTranslator());
		modelInterpolator.setUrlNormalizer(new DefaultUrlNormalizer());
		modelBuilder.setModelInterpolator(modelInterpolator);
		return modelBuilder;
	}

	public static String printModel(Model model) throws IOException {
		MavenXpp3Writer writer = new MavenXpp3Writer();
		Writer stringWriter = new StringWriter();
		writer.write(stringWriter, model);
		return stringWriter.toString();
	}

	private static DefaultServiceLocator serviceLocator() {
		DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
		locator.addService(RepositoryConnectorFactory.class,
				BasicRepositoryConnectorFactory.class);
		locator.addService(
				TransporterFactory.class, FileTransporterFactory.class);
		locator.addService(TransporterFactory.class,
				HttpTransporterFactory.class);
		locator.addService(TransporterFactory.class,
				WagonTransporterFactory.class);
		return locator;
	}

}