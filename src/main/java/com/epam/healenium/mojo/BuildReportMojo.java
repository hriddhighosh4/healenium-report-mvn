package com.epam.healenium.mojo;

import com.epam.healenium.client.HealingClient;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.net.URI;
import java.nio.file.Paths;
import java.util.Properties;

import static com.epam.healenium.extension.ReportHelper.loadConfig;

@Mojo(name = "buildReport")
public class BuildReportMojo extends AbstractMojo {
    private final String configFile = "/healenium.properties";
    @Parameter(readonly = true, defaultValue = "${project}")
    private MavenProject project;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            Model model = project.getModel();
            Build build = model.getBuild();
            String targetDir = build.getDirectory() + "/classes";
            Properties configProperty = loadConfig(Paths.get(targetDir + configFile).toFile());
            String sessionKey = configProperty.getProperty("sessionKey");
            String serverHost = configProperty.getProperty("serverHost");
            int serverPort = Integer.parseInt(configProperty.getProperty("serverPort"));
            HealingClient client = new HealingClient(serverHost, serverPort);
            String reportUrlResponse = client.buildReport(sessionKey);
            String reportUrl = new URI("http", null, serverHost, serverPort, "/" + reportUrlResponse, null, null).normalize().toString();
            getLog().info("Report available at " + reportUrl);
        } catch (
            Exception ex) {
            getLog().error("Failed to perform init action", ex);
        }
    }

}
