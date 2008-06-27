package org.objectstyle.woproject.maven2.wolifecycle;

//org.apache.maven.plugins:maven-compiler-plugin:compile
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.BooleanUtils;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

public abstract class DefineResourcesMojo extends AbstractWOMojo {

	/**
	 * Flatten all Resources and WebServerResouces into the Resources folder of
	 * WO application/framework package.
	 * 
	 * @parameter expression="flattenResources"
	 */
	private Boolean flattenResources;

	public DefineResourcesMojo() {
		super();
	}

	private void addResources(final List<Resource> resources) {
		for (Resource resource : resources) {
			getProject().addResource(resource);
		}
	}

	private Resource createResources(final List<String> resourcesInclude, final List<String> resourcesExclude, final String targetPath) {
		Resource resource = new Resource();

		resource.setDirectory(getProjectFolder().getAbsolutePath());

		if (resourcesInclude != null) {
			for (String resourceInclude : resourcesInclude) {
				resource.addInclude(resourceInclude);
			}
		}

		if (resourcesExclude != null) {
			for (String resourceExclude : resourcesExclude) {
				resource.addExclude(resourceExclude);
			}

			resource.addExclude("build/**");
			resource.addExclude("dist/**");
			resource.addExclude("target/**");
		}

		String fullTargetPath = getFullTargetPath(targetPath);

		resource.setTargetPath(fullTargetPath);

		return resource;
	}

	private List<Resource> createResources(final String directory, final String targetPath) {
		String fullTargetPath = getFullTargetPath(targetPath);

		List<Resource> resources = new ArrayList<Resource>();

		Resource resource = new Resource();

		File resourcesDirectory = new File(getProjectFolder(), directory);

		resource.setDirectory(resourcesDirectory.getAbsolutePath());
		resource.addExclude("*.lproj/**");

		Boolean shouldFlattenResources = BooleanUtils.toBooleanDefaultIfNull(flattenResources(), false);

		if (shouldFlattenResources) {
			resource.addInclude("*");
			resource.addInclude("*.wo/**");
			resource.addInclude("*.eomodeld/**");
		}

		resource.setTargetPath(fullTargetPath);
		resources.add(resource);

		File[] files = resourcesDirectory.listFiles();

		for (int i = 0; i < files.length; i++) {
			File file = files[i];

			String filename = file.getName();

			if (filename != null && filename.endsWith(".lproj")) {
				resource = new Resource();

				resource.setDirectory(file.getAbsolutePath());

				if (filename.equalsIgnoreCase("Nonlocalized.lproj")) {
					resource.setTargetPath(fullTargetPath);
				} else {
					resource.setTargetPath(fullTargetPath + File.separator + filename);
				}

				resources.add(resource);

				continue;
			}

			if (shouldFlattenResources && file.isDirectory() && !filename.endsWith(".wo") && !filename.endsWith(".eomodeld")) {
				List<Resource> additionalResources = createResources(directory + File.separator + filename, targetPath);

				resources.addAll(additionalResources);
			}
		}

		return resources;
	}

	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("Creating output folders");

		executeCreateFolders();

		getLog().info("Defining WO resources");

		executeExistingComponents();
		executeExistingResources();
		executeExistingWebServerResources();

		Boolean readPatternsets = this.readPatternsets();

		if (readPatternsets != null && readPatternsets.booleanValue()) {
			executeResourcesPatternsetFiles();
			executeWebServerResourcesPatternsetFiles();
		}

		executeDefineProjectWonderStyleFolders();
		executeDefineMavenStyleFolders();
	}

	private void executeCreateFolders() {
		File target = new File(getProject().getBuild().getOutputDirectory());

		if (!target.exists()) {
			target.mkdirs();
		}
	}

	private void executeDefineMavenStyleFolders() {
		getLog().debug("Patching resources for WO Maven style");

		String pathPrefix = "src" + File.separator + "main" + File.separator;

		String resourcesFolder = pathPrefix + "resources";

		File resourcesFile = new File(getProjectFolder(), resourcesFolder);

		if (resourcesFile.exists()) {
			getLog().debug("Patching " + resourcesFolder + " folder. Adding include...");

			List<Resource> resourcesFromResourcesFolder = createResources(resourcesFolder, "Resources");

			addResources(resourcesFromResourcesFolder);
		} else {
			getLog().debug("No " + resourcesFolder + " folder found within project. Skipping include...");
		}

		String componentsFolder = pathPrefix + "components";

		File componentsFile = new File(getProjectFolder(), componentsFolder);

		if (componentsFile.exists()) {
			getLog().debug("Patching " + componentsFolder + " folder. Adding include...");

			List<Resource> resourcesFromResourcesFolder = createResources(componentsFolder, "Resources");

			addResources(resourcesFromResourcesFolder);
		} else {
			getLog().debug("No " + componentsFolder + " folder found within project. Skipping include...");
		}

		String webServerResourcesFolder = pathPrefix + "webserver-resources";

		File webServerResourcesFile = new File(getProjectFolder(), webServerResourcesFolder);

		if (webServerResourcesFile.exists()) {
			getLog().debug("Patching " + webServerResourcesFolder + " folder. Adding include...");

			List<Resource> webServerResourcesFromWebServerResourcesFolder = createResources(webServerResourcesFolder, "WebServerResources");

			addResources(webServerResourcesFromWebServerResourcesFolder);
		} else {
			getLog().debug("No " + webServerResourcesFolder + " folder found within project. Skipping include...");
		}
	}

	private void executeDefineProjectWonderStyleFolders() {
		getLog().debug("Patching resources for WOnder style");

		File componentsFile = new File(getProjectFolder(), "Components");

		if (componentsFile.exists()) {
			getLog().debug("Patching \"Components\" folder. Adding include...");

			List<Resource> resourcesFromComponentsFolder = createResources("Components", "Resources");

			addResources(resourcesFromComponentsFolder);
		} else {
			getLog().debug("No \"Components\" folder found within project. Skipping include...");
		}

		File resourcesFile = new File(getProjectFolder(), "Resources");

		if (resourcesFile.exists()) {
			getLog().debug("Patching \"Resources\" folder. Adding include...");

			List<Resource> resourcesFromResourcesFolder = createResources("Resources", "Resources");

			addResources(resourcesFromResourcesFolder);
		} else {
			getLog().debug("No \"Resources\" folder found within project. Skipping include...");
		}

		File webServerResourcesFile = new File(getProjectFolder(), "WebServerResources");

		if (webServerResourcesFile.exists()) {
			getLog().debug("Patching \"WebServerResources\" folder. Adding include...");

			List<Resource> webServerResourcesFromWebServerResourcesFolder = createResources("WebServerResources", "WebServerResources");

			addResources(webServerResourcesFromWebServerResourcesFolder);
		} else {
			getLog().debug("No \"WebServerResources\" folder found within project. Skipping include...");
		}
	}

	private void executeExistingComponents() {
		executePatchResources("Components", getFullTargetPath("Resources"));
	}

	private void executeExistingResources() {
		this.executePatchResources("WebServerResources", this.getFullTargetPath("WebServerResources"));
	}

	private void executeExistingWebServerResources() {
		this.executePatchResources("Resources", getFullTargetPath("Resources"));
	}

	private void executePatchResources(final String existingTargetPath, final String newTargetPath) {
		@SuppressWarnings("unchecked")
		List<Resource> resources = getProject().getResources();

		for (Resource resource : resources) {
			if (resource.getTargetPath() != null && resource.getTargetPath().equals(existingTargetPath)) {
				getLog().debug("Patching target path of resource " + resource);

				resource.setTargetPath(newTargetPath);
			}
		}
	}

	private void executeResourcesPatternsetFiles() {
		getLog().debug("Loading Pattern Sets for Resources");

		File woProjectFile = getWOProjectFolder();

		if (woProjectFile == null) {
			getLog().debug("No \"woproject\" folder found within project. Skipping patternsets...");

			return;
		}

		getLog().debug("\"woproject\" folder found within project. Reading patternsets...");

		List<String> resourcesIncludeFromAntPatternsetFiles = getResourcesInclude();
		List<String> resourcesExcludeFromAntPatternsetFiles = getResourcesExclude();

		if (resourcesIncludeFromAntPatternsetFiles != null && resourcesExcludeFromAntPatternsetFiles != null && (resourcesIncludeFromAntPatternsetFiles.size() > 0 || resourcesExcludeFromAntPatternsetFiles.size() > 0)) {
			Resource resourcesFromAntPatternsetFiles = createResources(resourcesIncludeFromAntPatternsetFiles, resourcesExcludeFromAntPatternsetFiles, "Resources");

			getProject().addResource(resourcesFromAntPatternsetFiles);
		}
	}

	private void executeWebServerResourcesPatternsetFiles() {
		getLog().debug("Loading Pattern Sets for Resources");
		File woProjectFile = getWOProjectFolder();
		if (woProjectFile == null) {
			getLog().debug("No \"woproject\" folder found within project. Skipping patternsets...");

			return;
		}

		getLog().debug("\"woproject\" folder found within project. Reading patternsets...");

		List<String> webserverResourcesIncludeFromAntPatternsetFiles = getWebserverResourcesInclude();
		List<String> webserverResourcesExcludeFromAntPatternsetFiles = getWebserverResourcesExclude();

		if (webserverResourcesIncludeFromAntPatternsetFiles != null && webserverResourcesExcludeFromAntPatternsetFiles != null && (webserverResourcesIncludeFromAntPatternsetFiles.size() > 0 || webserverResourcesExcludeFromAntPatternsetFiles.size() > 0)) {
			Resource webserverResourcesFromAntPatternsetFiles = this.createResources(webserverResourcesIncludeFromAntPatternsetFiles, webserverResourcesExcludeFromAntPatternsetFiles, "WebServerResources");
			this.getProject().addResource(webserverResourcesFromAntPatternsetFiles);
		}
	}

	protected Boolean flattenResources() {
		return flattenResources;
	}

	private String getFullTargetPath(final String targetPath) {
		String fullTargetPath = "../" + getProject().getArtifactId();

		if (this.includesVersionInArtifactName()) {
			fullTargetPath = fullTargetPath + "-" + this.getProject().getVersion();
		}

		fullTargetPath = fullTargetPath + "." + getProductExtension();

		if (this.hasContentsFolder()) {
			fullTargetPath = fullTargetPath + File.separator + "Contents";
		}

		return fullTargetPath + File.separator + targetPath;
	}

	private List<String> getResourcesExclude() {
		String patternsetFileName = "resources.exclude.patternset";
		return readPatternset(patternsetFileName);
	}

	private List<String> getResourcesInclude() {
		String patternsetFileName = "resources.include.patternset";
		return readPatternset(patternsetFileName);
	}

	private List<String> getWebserverResourcesExclude() {
		String patternsetFileName = "wsresources.exclude.patternset";
		return readPatternset(patternsetFileName);
	}

	private List<String> getWebserverResourcesInclude() {
		String patternsetFileName = "wsresources.include.patternset";
		return readPatternset(patternsetFileName);
	}

	public abstract boolean hasContentsFolder();

	public abstract boolean includesVersionInArtifactName();

	private List<String> readPatternset(final String patternsetFileName) {
		getLog().debug("Reading Pattern Sets file \"" + patternsetFileName + "\"");

		File file = new File(getWOProjectFolder(), patternsetFileName);

		if (!file.exists()) {
			return null;
		}

		PatternsetReader patternsetReader;

		List<String> pattern = null;

		try {
			patternsetReader = new PatternsetReader(file);

			pattern = patternsetReader.getPattern();
		} catch (IOException e) {
			getLog().error("Exception while loading \"" + patternsetFileName + "\"", e);
		}

		return pattern;
	}

	protected abstract Boolean readPatternsets();
}
