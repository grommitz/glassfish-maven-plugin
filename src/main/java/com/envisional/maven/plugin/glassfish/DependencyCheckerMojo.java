package com.envisional.maven.plugin.glassfish;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.ZipInputStream;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.UnzipParameters;

import org.apache.commons.io.IOUtils;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.ExceptionUtils;

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
						<phase>validate</phase>
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
		getLog().info("Looking for " + dep.toString() + "...");

		String base = System.getenv().get("GLASSFISH_HOME")+"/glassfish";
		final List<String> locations = Lists.newArrayList(
				base+"/lib/",
				base+"/modules/",
				base+"/domains/domain1/lib/");

		String fileName = dep.getArtifactId() + "-" + dep.getVersion() + ".jar";

		for (String location : locations) {
			File jar = new File(location + fileName);
			if (jar.exists()) {
				getLog().info("Found "+jar.toString());
				return true;
			}
		}

		fileName = dep.getArtifactId() + ".jar";
		File jar = new File(base + "/modules/" + fileName);
		if (jar.exists()) {
			getLog().debug("Examining contents of "+jar.toString() + " to try to determine the version...");
			if (correctVersionInsideJar(jar, dep.getVersion())) {
				getLog().info("Found " + jar.toString() + " with the correct version");
				return true;
			}
		}

		return false;
	}

	public void setProject(MavenProject project) {
		this.project = project;
	}

	// TODO!
	private boolean correctVersionInsideJar(File jarFile, String version) {
		// unzip -l $jar | awk -F' ' '{print $4}' | grep pom.properties > loc
		// unzip -p $jar `cat loc` | grep version | awk -F'=' '{print $2}' > ver
		// if [ `cat ver` = $version ]; then ...
		
		try {
			ZipFile jar = new ZipFile(jarFile.toString());
			if (jar.isEncrypted()) {
				getLog().error(jarFile.toString() + " is encrypted, cannot get version information");
				return false;
			}
			List<FileHeader> fileHeaderList = jar.getFileHeaders();
			for (FileHeader fileHeader : fileHeaderList) {
				if (fileHeader.getFileName().endsWith("pom.properties")) {
					ZipInputStream is = jar.getInputStream(fileHeader);
					String contents = IOUtils.toString(is, "UTF-8");
					getLog().debug("pom.properties = " + contents);
					Pattern p = Pattern.compile("[Vv]ersion=(.*)");
					Matcher m = p.matcher(contents);
					if (m.find()) {
						String ver = m.group(1);
						getLog().debug("Project has dependency on version '"+version+"', jar file is version = '" + ver + "'");
						if (ver.equals(version)) {
							return true;
						}
					}
					getLog().error("version is incorrect or not found in " + fileHeader.getFileName());
				}
			}
		} catch (ZipException e) {
			getLog().error(ExceptionUtils.getStackTrace(e));
		} catch (IOException e) {
			getLog().error(ExceptionUtils.getStackTrace(e));
		}
		return false;
	}
	

}
