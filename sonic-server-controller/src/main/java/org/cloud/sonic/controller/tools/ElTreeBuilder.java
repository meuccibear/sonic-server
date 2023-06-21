package org.cloud.sonic.controller.tools;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ElTreeBuilder {


    List<Node> nodes = new ArrayList<Node>();

    public ElTreeBuilder(List<Node> nodes) {
        super();
        this.nodes = nodes;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Node {

        private Integer id;
        private Integer pid;
        private String label;

    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class NodeBuild {

        private String value;

        private String label;

        private List<NodeBuild> children;

    }
}
