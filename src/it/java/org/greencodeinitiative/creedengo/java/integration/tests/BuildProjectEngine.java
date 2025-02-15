package org.greencodeinitiative.creedengo.java.integration.tests;

import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.build.MavenBuild;
import com.sonar.orchestrator.container.Server;
import com.sonar.orchestrator.junit5.OrchestratorExtension;
import com.sonar.orchestrator.junit5.OrchestratorExtensionBuilder;
import com.sonar.orchestrator.locator.FileLocation;
import com.sonar.orchestrator.locator.Location;
import com.sonar.orchestrator.locator.MavenLocation;
import com.sonar.orchestrator.locator.URLLocation;
import lombok.Getter;
import org.greencodeinitiative.creedengo.java.integration.tests.profile.ProfileBackup;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.sonarqube.ws.Issues;
import org.sonarqube.ws.Measures;
import org.sonarqube.ws.client.HttpConnector;
import org.sonarqube.ws.client.WsClient;
import org.sonarqube.ws.client.WsClientFactories;
import org.sonarqube.ws.client.issues.SearchRequest;
import org.sonarqube.ws.client.measures.ComponentRequest;

import static java.lang.System.Logger.Level.INFO;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;

abstract class BuildProjectEngine {

	private static final System.Logger LOGGER = System.getLogger(BuildProjectEngine.class.getName());

	protected static OrchestratorExtension orchestrator;
	protected static List<ProjectToAnalyze> analyzedProjects;

	@BeforeAll
	static void setup() {
		LOGGER.log(
				INFO,
				"\n" +
						"====================================================================================================\n" +
						"Launching SonarQube server with following JAVA System properties: {0}\n" +
						"====================================================================================================\n"
				,
				Stream
						.of(
								"test-it.sonarqube.keepRunning",
								"test-it.orchestrator.artifactory.url",
								"test-it.sonarqube.version",
								"test-it.plugins",
								"test-it.additional-profile-uris",
								"test-it.test-projects",
								"test-it.test-project-profile-by-language"
						)
						.filter(k -> System.getProperty(k) != null)
						.map(k -> MessageFormat
								.format(
										"-D{0}=\"{1}\"",
										k,
										System.getProperty(k).replaceAll("\\s+", " ")
								)
						)
						.collect(Collectors.joining("\n", "\n\n", "\n\n"))
		);
		launchSonarqube();
		launchAnalysis();
	}

	@AfterAll
	static void tearDown() {
		if ("true".equalsIgnoreCase(System.getProperty("test-it.sonarqube.keepRunning"))) {
			try (Scanner in = new Scanner(System.in)) {
				LOGGER.log(INFO, () ->
						MessageFormat.format(
								"\n" +
										"\n====================================================================================================" +
										"\nSonarQube available at: {0} (to login: admin/admin)" +
										"\n====================================================================================================" +
										"\n",
								orchestrator.getServer().getUrl()
						)
				);
				do {
					LOGGER.log(INFO, "✍ Please press CTRL+C to stop");
				}
				while (!in.nextLine().isEmpty());
			}
		}
		if (orchestrator != null) {
			orchestrator.stop();
		}
	}

	private static void launchSonarqube() {
		String orchestratorArtifactoryUrl = systemProperty("test-it.orchestrator.artifactory.url");
		String sonarqubeVersion = systemProperty("test-it.sonarqube.version");
		Optional<String> sonarqubePort = ofNullable(System.getProperty("test-it.sonarqube.port")).map(String::trim).filter(not(String::isEmpty));

		OrchestratorExtensionBuilder orchestratorExtensionBuilder = OrchestratorExtension
				.builderEnv()
				.useDefaultAdminCredentialsForBuilds(true)
				.setOrchestratorProperty("orchestrator.artifactory.url", orchestratorArtifactoryUrl)
				.setSonarVersion(sonarqubeVersion)
				.setServerProperty("sonar.forceAuthentication", "false")
				.setServerProperty("sonar.web.javaOpts", "-Xmx1G");

		sonarqubePort.ifPresent(s -> orchestratorExtensionBuilder.setServerProperty("sonar.web.port", s));

		additionalPluginsToInstall().forEach(orchestratorExtensionBuilder::addPlugin);
		additionalProfiles().forEach(orchestratorExtensionBuilder::restoreProfileAtStartup);

		orchestrator = orchestratorExtensionBuilder.build();
		orchestrator.start();
		LOGGER.log(INFO, () -> MessageFormat.format("SonarQube server available on: {0}", orchestrator.getServer().getUrl()));
	}

	private static void launchAnalysis() {
		Server server = orchestrator.getServer();
		Map<String, String> qualityProfileByLanguage = testProjectProfileByLanguage();

		analyzedProjects = getProjectsToAnalyze();

		analyzedProjects
				.stream()
				// - Prepare/create SonarQube project for the test project
				.peek(projectToAnalyze -> projectToAnalyze.provisionProjectIntoServer(server))
				// - Configure the test project
				.peek(projectToAnalyze -> projectToAnalyze.associateProjectToQualityProfile(server, qualityProfileByLanguage))
				.map(ProjectToAnalyze::createMavenBuild)
				// - Run SonarQube Scanner on test project
				.peek(p -> LOGGER.log(INFO, () -> MessageFormat.format("Running SonarQube Scanner on project: {0}", p.getPom())))
				.forEach(orchestrator::executeBuild);
	}

	private static String systemProperty(String propertyName) {
		return ofNullable(System.getProperty(propertyName))
				.orElseThrow(() -> new IllegalStateException(
						String.format(
								"System property `%s` must be defined. See `%s` (in section: `plugin[maven-failsafe-plugin]/systemPropertyVariables`) for sample value.",
								propertyName,
								Path.of("pom.xml").toAbsolutePath()
						)
				));
	}

	/**
	 * Projects to analyze
	 */
	private static List<ProjectToAnalyze> getProjectsToAnalyze() {
		return commaSeparatedValues(systemProperty("test-it.test-projects"))
				.map(projectToAnalyzeDefinition -> pipeSeparatedValues(projectToAnalyzeDefinition).collect(toList()))
				.filter(projectToAnalyzeDefinition -> projectToAnalyzeDefinition.size() == 3)
				.map(projectToAnalyzeDefinition -> {
					// Project Key
					String projectKey = projectToAnalyzeDefinition.get(0);
					// Project Name
					String projectName = projectToAnalyzeDefinition.get(1);
					// Project POM URI
					URI projectPom = URI.create(projectToAnalyzeDefinition.get(2));
					return new ProjectToAnalyze(projectPom, projectKey, projectName);
				})
				.collect(toList());
	}

	private static Stream<String> commaSeparatedValues(String value) {
		return splitAndTrim(value, "\\s*,\\s*");
	}

	private static Stream<String> pipeSeparatedValues(String value) {
		return splitAndTrim(value, "\\s*\\|\\s*");
	}

	private static Stream<String> colonSeparatedValues(String value) {
		return splitAndTrim(value, "\\s*\\:\\s*");
	}

	private static Stream<String> splitAndTrim(String value, String regexSeparator) {
		return Stream
				.of(value.split(regexSeparator))
				.map(String::trim)
				.filter(not(String::isEmpty));
	}

	private static Set<Location> additionalPluginsToInstall() {
		Set<Location> plugins = commaSeparatedValues(systemProperty("test-it.plugins"))
				.map(BuildProjectEngine::toPluginLocation)
				.collect(Collectors.toSet());
		commaSeparatedValues(System.getProperty("test-it.additional-plugins", ""))
				.map(BuildProjectEngine::toPluginLocation)
				.forEach(plugins::add);
		return plugins;
	}

	private static Set<URLLocation> additionalProfiles() {
		return commaSeparatedValues(systemProperty("test-it.additional-profile-uris"))
				.map(URI::create)
				.map(ProfileBackup::new)
				.map(ProfileBackup::profileDataUri)
				.map(URLLocation::create)
				.collect(Collectors.toSet());
	}

	private static Map<String, String> testProjectProfileByLanguage() {
		// Comma separated list of profiles to associate to each "test project"
		// Syntaxe: `language:profileName`
		return commaSeparatedValues(systemProperty("test-it.test-project-profile-by-language"))
				.map(languageAndProfileDefinitions -> pipeSeparatedValues(languageAndProfileDefinitions).collect(toList()))
				.filter(languageAndProfile -> languageAndProfile.size() == 2)
				.collect(toMap(
						// Language
						languageAndProfile -> languageAndProfile.get(0),
						// Profile name
						languageAndProfile -> languageAndProfile.get(1)
				));
	}

	private static Location toPluginLocation(String location) {
		if (location.startsWith("file://")) {
			try {
				return FileLocation.of(URI.create(location).toURL());
			} catch (MalformedURLException e) {
				throw new IllegalArgumentException(e);
			}
		}
		List<String> pluginGAVvalues = colonSeparatedValues(location).collect(toList());
		if (pluginGAVvalues.size() != 3) {
			throw new IllegalArgumentException("Invalid plugin GAV definition (`groupId:artifactId:version`): " + location);
		}
		return MavenLocation.of(
				// groupId
				pluginGAVvalues.get(0),
				// artifactId
				pluginGAVvalues.get(1),
				// version
				pluginGAVvalues.get(2)
		);
	}

	protected static List<Issues.Issue> issuesForFile(String projectKey, String file) {
		return issuesForComponent(projectKey + ":" + file);
	}

	protected static List<Issues.Issue> issuesForComponent(String componentKey) {
		return newWsClient(orchestrator)
				.issues()
				.search(new SearchRequest().setComponentKeys(Collections.singletonList(componentKey)))
				.getIssuesList();
	}

	protected static Map<String, Measures.Measure> getMeasures(String componentKey) {
		List<String> metricKeys = List.of(
				"alert_status",
				"blocker_violations",
				"branch_coverage",
				"bugs",
//				"class_complexity", // suppr en 25.1
				"classes",
				"code_smells",
				"cognitive_complexity",
				"comment_lines",
				"comment_lines_data",
				"comment_lines_density",
				"complexity",
//				"complexity_in_classes", // suppr en 25.1
//				"complexity_in_functions", // suppr en 25.1
				"conditions_to_cover",
				"confirmed_issues",
				"coverage",
				"critical_violations",
				"development_cost",
//				"directories", // suppr en 10.2
				"duplicated_blocks",
				"duplicated_files",
				"duplicated_lines",
				"duplicated_lines_density",
				"duplications_data",
				"effort_to_reach_maintainability_rating_a",
				"executable_lines_data",
				"false_positive_issues",
//				"file_complexity", // suppr en 25.1
//				"file_complexity_distribution", // suppr en 25.1
				"files",
//				"function_complexity", // suppr en 25.1
//				"function_complexity_distribution", // suppr en 25.1
				"functions",
				"generated_lines",
				"generated_ncloc",
				"info_violations",
				"last_commit_date",
				"line_coverage",
				"lines",
				"lines_to_cover",
				"major_violations",
				"minor_violations",
				"ncloc",
				"ncloc_data",
				"ncloc_language_distribution",
				"new_blocker_violations",
				"new_branch_coverage",
				"new_bugs",
				"new_code_smells",
				"new_conditions_to_cover",
				"new_coverage",
				"new_critical_violations",
				"new_development_cost",
				"new_duplicated_blocks",
				"new_duplicated_lines",
				"new_duplicated_lines_density",
				"new_info_violations",
				"new_line_coverage",
				"new_lines",
				"new_lines_to_cover",
				"new_maintainability_rating",
				"new_major_violations",
				"new_minor_violations",
				"new_reliability_rating",
				"new_reliability_remediation_effort",
				"new_security_hotspots",
				"new_security_hotspots_reviewed",
				"new_security_hotspots_reviewed_status",
				"new_security_hotspots_to_review_status",
				"new_security_rating",
				"new_security_remediation_effort",
				"new_security_review_rating",
				"new_technical_debt",
				"new_violations",
				"new_vulnerabilities",
				"open_issues",
				"projects",
				"public_api",
				"public_documented_api_density",
				"public_undocumented_api",
				"quality_gate_details",
				"quality_profiles",
				"reliability_rating",
				"reliability_remediation_effort",
				"reopened_issues",
				"security_hotspots",
				"security_hotspots_reviewed",
				"security_hotspots_reviewed_status",
				"security_hotspots_to_review_status",
				"security_rating",
				"security_remediation_effort",
				"security_review_rating",
				"skipped_tests",
				"sqale_rating",
				"statements",
				"unanalyzed_c",
				"unanalyzed_cpp",
				"violations"
		);
		return newWsClient(orchestrator)
				.measures()
				.component(
						new ComponentRequest()
								.setComponent(componentKey)
								.setMetricKeys(metricKeys)
				)
				.getComponent().getMeasuresList()
				.stream()
				.collect(Collectors.toMap(Measures.Measure::getMetric, Function.identity()));
	}

	protected static WsClient newWsClient(Orchestrator orchestrator) {
		return WsClientFactories.getDefault().newClient(HttpConnector.newBuilder()
		                                                             .url(orchestrator.getServer().getUrl())
		                                                             .build());
	}

	@Getter
	protected static class ProjectToAnalyze {
		private final Path pom;
		private final String projectKey;
		private final String projectName;

		private ProjectToAnalyze(URI pom, String projectKey, String projectName) {
			this.pom = Path.of(pom);
			assertThat(this.pom).isRegularFile();
			this.projectKey = projectKey;
			this.projectName = projectName;
		}

		public MavenBuild createMavenBuild() {
			return MavenBuild.create(pom.toFile())
					.setCleanPackageSonarGoals()
					.setProperty("sonar.projectKey", projectKey)
					.setProperty("sonar.projectName", projectName)
					.setProperty("sonar.scm.disabled", "true");
		}

		private void provisionProjectIntoServer(Server server) {
			server.provisionProject(projectKey, projectName);

		}

		private void associateProjectToQualityProfile(Server server, Map<String, String> qualityProfileByLanguage) {
			qualityProfileByLanguage.forEach((language, profileName) -> server.associateProjectToQualityProfile(projectKey, language, profileName));
		}
	}

}
