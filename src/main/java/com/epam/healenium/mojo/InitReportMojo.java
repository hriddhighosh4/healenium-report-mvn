package com.epam.healenium.mojo;

import com.epam.healenium.client.HealingClient;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

import static com.epam.healenium.extension.ReportHelper.loadConfig;

@Mojo(name = "initReport", defaultPhase = LifecyclePhase.TEST)
public class InitReportMojo extends AbstractMojo {

    private final String configFile = "/healenium.properties";

    @Parameter(readonly = true, defaultValue = "${project}")
    private MavenProject project;

    public InitReportMojo() {
    }

    @Override
    public void execute() {
        Model model = project.getModel();
        Build build = model.getBuild();
        String targetDir = build.getDirectory() + "/classes";
        String sourceDir = project.getBasedir().getPath() + "/src/test/resources";

        findConfig(targetDir, sourceDir).ifPresent(it -> {
            try {
                Properties configProperty = loadConfig(it);
                // normalize
                normalizeProperties(configProperty);

                //build remote
                String serverHost = configProperty.getProperty("serverHost", "localhost");
                Integer serverPort = stringToInteger(configProperty.getProperty("serverPort", "7878"));
                getLog().info("Healenium server Port = " + serverPort);
                getLog().info("Healenium server Host = " + serverHost);

                HealingClient client = new HealingClient(serverHost, serverPort);

                // get session key
                String sessionKey = client.initReport();
                getLog().info("sessionKey= " + sessionKey);
                if (sessionKey == null || sessionKey.isEmpty()) {
                    getLog().warn("Couldn't obtain session key from server!");
                    return;
                }
                configProperty.setProperty("sessionKey", sessionKey);
                configProperty.setProperty("serverHost", serverHost);
                configProperty.setProperty("serverPort", serverPort.toString());
                // append key info
                uploadConfig(it, configProperty);
            } catch (Exception ex) {
                getLog().error("Failed to perform init action", ex);
            }
        });
    }

    private Optional<File> findConfig(String targetDir, String sourceDir) {
        Optional<String> result = Optional.empty();
        try (Stream<Path> walk = Files.walk(Paths.get(targetDir.toString()))) {
            result = walk.filter(Files::isRegularFile)
                    .map(Path::toString)
                    .filter(it -> it.endsWith(configFile))
                    .findFirst();
        } catch (Exception ex) {
            // no logging
        }
        File file = result.map(File::new)
                .orElseGet(() -> {
                    Path fromPath = Paths.get(String.valueOf(sourceDir), configFile);
                    Path toPath = Paths.get(String.valueOf(targetDir), configFile);
                    try {
                        Files.createDirectories(toPath.getParent());
                        if (Files.exists(Paths.get(sourceDir + configFile))) {
                            Files.copy(fromPath, toPath, StandardCopyOption.REPLACE_EXISTING);
                        } else {
                            Files.createFile(toPath);
                        }
                        return Paths.get(targetDir + configFile).toFile();
                    } catch (IOException ex) {
                        getLog().error("Failed to create config file", ex);
                        return null;
                    }
                });
        return Optional.ofNullable(file);
    }

    private void normalizeProperties(Properties configProperty) {
        for (String name : configProperty.stringPropertyNames()) {
            String value = configProperty.getProperty(name);
            if (value == null || value.isEmpty()) {
                configProperty.remove(name);
                continue;
            }
            if (name.equals(";")) {
                configProperty.remove(name);
                configProperty.setProperty(name.concat(value.substring(0, value.indexOf("=")).replaceAll("\\s+", "")), value.substring(value.indexOf("=") + 1).trim());
            }
        }
    }

    /**
     * @param file
     * @param properties
     */
    private void uploadConfig(File file, Properties properties) {
        try {
            FileOutputStream fileOut = new FileOutputStream(file);
            properties.store(fileOut, "\n");
//            PropertiesUtils.store(properties, fileOut, null, StandardCharsets.ISO_8859_1, "\n");
            fileOut.close();
        } catch (IOException ex) {
            getLog().error("Failed to append data", ex);
        }
    }

    public Integer stringToInteger(String inputString) {
        return (inputString == null || inputString.isEmpty()) ? null : Integer.parseInt(inputString);
    }
}
