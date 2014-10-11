package com.manning.neo4jia.chapter09.simple.indexbasedtrs.domain.dodgy;


import org.springframework.data.neo4j.annotation.GraphId;

/**
 * User which does not have the @NodeEntity annotation.
 */
public class UnannotatedUser {

    @GraphId
    Long nodeId;

    DodgyRelDef dodgyRel;

    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    public DodgyRelDef getDodgyRel() {
        return dodgyRel;
    }

    public void setDodgyRel(DodgyRelDef dodgyRel) {
        this.dodgyRel = dodgyRel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UnannotatedUser that = (UnannotatedUser) o;

        if (nodeId != null ? !nodeId.equals(that.nodeId) : that.nodeId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return nodeId != null ? nodeId.hashCode() : 0;
    }
}
