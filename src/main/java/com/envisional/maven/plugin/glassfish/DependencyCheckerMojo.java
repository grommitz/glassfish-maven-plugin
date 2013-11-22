package com.envisional.maven.plugin.glassfish;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import com.google.common.collect.Lists;

/**
 * A mojo to check that all the provided dependencies are installed in glassfish lib. Usage:
 * 
 * <build>
	    <plugins>
			<plugin>
				<groupId>com.envisional</groupId>
				<artifactId>glassfish-maven-plugin</artifactId>
				<version>1.0-SNAPSHOT</version>
				<executions>
					<execution>
						<phase>install</phase>
						<goals>
							<goal>check-dependencies-installed</goal>
						</goals>
					</execution>
				</executions>
			</plugin>
		</plugins>	
    </build>
 *
 * @author martin
 */
@Mojo(name = "check-dependencies-installed")
public class DependencyCheckerMojo extends AbstractMojo {

	@Parameter (property="my.project", defaultValue="${project}")
	private MavenProject project;
	
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("Checking domain1/lib folder...");
		@SuppressWarnings("unchecked")
		List<Dependency> dependencies = project.getDependencies();
		if (!System.getenv().containsKey("GLASSFISH_HOME")) {
			throw new MojoFailureException("GLASSFISH_HOME not set");
		}
		for (Dependency dep : dependencies) {
			if (dep.getScope().equals("provided") && dep.getType().equals("jar")) {
				if (!installed(dep)) {
					throw new MojoFailureException("Provided dependency not installed: "+dep.toString());
				}
			}
		}
	}

	private boolean installed(Dependency dep) {
		final List<String> locations = Lists.newArrayList(
					System.getenv().get("GLASSFISH_HOME")+"/glassfish/lib/",
					System.getenv().get("GLASSFISH_HOME")+"/glassfish/domains/domain1/lib/");
		String fileName = dep.getArtifactId() + "-" + dep.getVersion() + ".jar";

		for (String location : locations) {
			File jar = new File(location + fileName);
			getLog().info("Looking for " + dep.toString() + " at " + jar.toString());
			if (jar.exists()) {
				return true;
			}
		}
		return false;
	}

	public void setProject(MavenProject project) {
		this.project = project;
	}
	

}
