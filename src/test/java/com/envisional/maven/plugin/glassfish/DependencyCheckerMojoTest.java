package com.envisional.maven.plugin.glassfish;

import static org.junit.Assert.*;
import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.DefaultMavenProjectBuilder;
import org.apache.maven.project.DefaultProjectBuilderConfiguration;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.context.DefaultContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class DependencyCheckerMojoTest {

	@Before
	public void setUp() throws Exception {

	}

	@After
	public void tearDown() throws Exception {
	}
	
	//@Test
	@Ignore
	public void run() throws MojoExecutionException, MojoFailureException, ProjectBuildingException, ContextException, PlexusContainerException {
		MavenProject project = buildProject();
		DependencyCheckerMojo mojo = new DependencyCheckerMojo();
		mojo.setProject(project);
		mojo.execute();
	}

	private MavenProject buildProject() throws ProjectBuildingException, ContextException, PlexusContainerException {
		DefaultMavenProjectBuilder mavenProjectBuilder = new DefaultMavenProjectBuilder();
		mavenProjectBuilder.initialize();
		DefaultContext context = new DefaultContext();
		context.put("plexus", new DefaultPlexusContainer());
		mavenProjectBuilder.contextualize(context);
		File pom = new File("pom.xml");
		assertTrue(pom.exists());
		return mavenProjectBuilder.build(pom, new DefaultProjectBuilderConfiguration(), false);
	}
	
}
