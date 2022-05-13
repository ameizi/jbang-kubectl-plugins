// #!/usr/bin/env jbang
///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS info.picocli:picocli:4.6.3
//DEPS info.picocli:picocli-codegen:4.6.3
//DEPS io.fabric8:kubernetes-client:5.12.2
//DEPS com.massisframework:j-text-utils:0.3.4
//DEPS org.slf4j:slf4j-simple:1.7.30

import dnl.utils.text.table.TextTable;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.utils.PodStatusUtil;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * list useage image
 */
@Command(name = "lsimage", mixinStandardHelpOptions = true, version = "lsimage 0.1",
        description = "list useage image made with jbang")
class lsimage implements Callable<Integer> {

    // https://www.fileformat.info/info/unicode/char/search.htm?
    private static final String CHECK_MARK = "\u2705";
    private static final String FIRE = "\uD83D\uDD25";

    /**
     * namespace
     */
    // @Parameters(index = "0", description = "namespace", defaultValue = "default")
    @Option(names = {"-n", "--namespace"}, description = "namespace", defaultValue = "default")
    private String ns;

    public static void main(String... args) {
        int exitCode = new CommandLine(new lsimage()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        printTable(getPodImages(ns));
        return 0;
    }

    private static List<PodImageInfo> getPodImages(String ns) {
        KubernetesClient kubernetesClient;
        try {
            kubernetesClient = new DefaultKubernetesClient();
        } catch (Exception e) {
            throw new RuntimeException("Unable to create default Kubernetes client", e);
        }
        return kubernetesClient.pods().inNamespace(ns).list().getItems().stream().map(pod -> {
            ObjectMeta metadata = pod.getMetadata();
            Container container = pod.getSpec().getContainers().get(0);
            PodInfoState state = PodStatusUtil.isRunning(pod) ? PodInfoState.RUNNING : PodInfoState.FAILING;
            String message = null;
            if (!state.equals(PodInfoState.RUNNING)) {
                message = PodStatusUtil.getContainerStatus(pod).get(0).getState().getWaiting().getMessage();
            }
            return new PodImageInfo(metadata.getName(), state, message, container.getName(), container.getImage());
        }).collect(Collectors.toList());
    }

    static class PodImageInfo {

        private final String name;
        private final PodInfoState state;
        private final String message;
        private final String container;
        private final String image;

        public PodImageInfo(String name, PodInfoState state, String message, String container, String image) {
            this.name = name;
            this.state = state;
            this.message = message;
            this.container = container;
            this.image = image;
        }

        public String getName() {
            return name;
        }

        public PodInfoState getState() {
            return state;
        }

        public String getMessage() {
            return message;
        }

        public String getImage() {
            return image;
        }

        public String getContainer() {
            return container;
        }
    }

    enum PodInfoState {
        RUNNING,
        FAILING
    }

    private static void printTable(List<PodImageInfo> list) {
        final Object[][] tableData = list.stream()
                .map(podInfo -> new Object[]{
                        // podInfo.getState().equals(PodInfoState.RUNNING) ? CHECK_MARK : FIRE,
                        podInfo.getName(),
                        // podInfo.getState(),
                        // podInfo.getMessage(),
                        podInfo.getContainer(),
                        podInfo.getImage()
                })
                .toArray(Object[][]::new);
        // String[] columnNames = {"", "PodName", "PodState", "Cause", "ContainerName", "ImageName"};
        String[] columnNames = {"PodName", "ContainerName", "ImageName"};
        new TextTable(columnNames, tableData).printTable();
    }
}
