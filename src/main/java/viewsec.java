// #!/usr/bin/env jbang
///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS info.picocli:picocli:4.6.3
//DEPS info.picocli:picocli-codegen:4.6.3
//DEPS io.fabric8:kubernetes-client:5.12.2
//DEPS com.massisframework:j-text-utils:0.3.4
//DEPS org.slf4j:slf4j-simple:1.7.30


import dnl.utils.text.table.TextTable;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.apache.commons.lang.StringUtils;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * view secret info
 */
@Command(name = "viewsec", mixinStandardHelpOptions = true, version = "viewsec 0.1",
        description = "view secret info made with jbang")
class viewsec implements Callable<Integer> {

    @Option(names = {"-n", "--namespace"}, description = "namespace", defaultValue = "default")
    private String ns;

    @Option(names = {"-sec", "--secret"}, description = "secret name", defaultValue = "")
    private String secretName;

    public static void main(String... args) {
        int exitCode = new CommandLine(new viewsec()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        printTable(viewSecret(ns, secretName));
        return 0;
    }

    private static List<SecretInfo> viewSecret(String ns, String secretName) {
        List<SecretInfo> secretInfos = new ArrayList<>();
        if (StringUtils.isEmpty(secretName)) {
            return secretInfos;
        }
        KubernetesClient kc;
        try {
            kc = new DefaultKubernetesClient();
        } catch (Exception e) {
            throw new RuntimeException("Unable to create default Kubernetes client", e);
        }
        Secret secret = kc.secrets()
                .inNamespace(ns)
                .withName(secretName)
                .get();
        if (secret != null) {
            secret.getData().forEach((k, v) -> {
                SecretInfo secretInfo = new SecretInfo();
                secretInfo.setName(k);
                secretInfo.setValue(new String(Base64.getDecoder().decode(v)));
                secretInfos.add(secretInfo);
            });
        }
        return secretInfos;
    }

    static class SecretInfo {

        private String name;
        private String value;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    private static void printTable(List<SecretInfo> list) {
        final Object[][] tableData = list.stream()
                .map(secretInfo -> new Object[]{
                        secretInfo.getName(),
                        secretInfo.getValue()
                })
                .toArray(Object[][]::new);
        String[] columnNames = {"SecretKey", "SecretValue"};
        new TextTable(columnNames, tableData).printTable();
    }
}
