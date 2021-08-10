### 1. Configure plugin 
```xml
    <build>
        <plugins>
            <plugin>
                <groupId>com.epam.healenium</groupId>
                <artifactId>hlm-report-mvn</artifactId>
                <version>1.1</version>
                <executions>
                    <execution>
                        <id>hlmReport</id>
                        <phase>compile</phase>
                        <goals>
                            <goal>initReport</goal>
                        </goals>
                    </execution>
                    <execution>
                        <id>hlmReportB</id>
                        <phase>test</phase>
                        <goals>
                            <goal>buildReport</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
```

initReport goal will generate sessionKey after compile phase before test run
buildReport goal will generate link to the healing report after test run

### 2. Run tests 
```
mvn clean test
```

Report link will be available in console like
```
Report available at http://localhost:7878/healenium/report/0cdcb909-73c2-4e74-a7f2-d46c06929a8f
```
