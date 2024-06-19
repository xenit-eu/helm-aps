package eu.xenit.testing.k8s.kind;

import eu.xenit.testing.k8s.PodTests;
import eu.xenit.testing.k8s.cluster.Cluster;
import eu.xenit.testing.k8s.helm.HelmCommander;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;


class HelmActivitiTest {

    @Test
    void smallSetup() throws IOException {
        var kindConfiguration = """
                kind: Cluster
                apiVersion: kind.x-k8s.io/v1alpha4
                nodes:
                  - role: control-plane
                    kubeadmConfigPatches:
                      - |
                        kind: InitConfiguration
                        nodeRegistration:
                          kubeletExtraArgs:
                            node-labels: "ingress-ready=true"
                    extraPortMappings:
                      - containerPort: 80
                        hostPort: 8099
                        protocol: TCP
                        """;

        var values = """
                ingress:
                  host: test
                  protocol: http
                  kubernetes.io/ingress.class: {}
                """;

        var clusterProvisioner = new KindClusterProvisioner();
        clusterProvisioner.setConfiguration(kindConfiguration);

        Cluster cluster = null;

        try {
            cluster = clusterProvisioner.provision();

            var tempFile = Files.createTempFile("values", ".yaml");
            Files.writeString(tempFile, values);

            var helmCommander = new HelmCommander(cluster);
            var namespace = "mynamespace";
            helmCommander.commandAndPrint("install",
                    "testinstall", "../xenit-aps",
                    "-f", tempFile.toAbsolutePath().toString(),
                    "-n", namespace, "--create-namespace");

            PodTests.checkPodsReady(cluster, namespace, "app = activiti", 1, 600);
            PodTests.checkPodsReady(cluster, namespace, "app = activiti-admin", 1, 600);
        } finally {
            if (cluster != null) {
                cluster.destroy();
            }
        }

    }
}