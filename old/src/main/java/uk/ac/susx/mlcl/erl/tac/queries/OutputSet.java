package uk.ac.susx.mlcl.erl.tac.queries;

import com.beust.jcommander.internal.Maps;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An <tt>OutputSet</tt> represents a collection of entity linking annotations, either produced by the prediction
 * system, or from gold standard annotation.
 */
public class OutputSet extends AbstractList<Output> {

    private final String name;
    private final List<Output> links;
    private final Map<String, Set<Output>> kbIdClusters;
    private final Map<String, Output> mentionIndex;

    OutputSet(final String name,
              final List<Output> links,
              final Map<String, Set<Output>> kbIdClusters,
              final Map<String, Output> mentionIndex) {
        this.name = checkNotNull(name, "name");
        this.links = checkNotNull(links, "links");
        this.kbIdClusters = checkNotNull(kbIdClusters, "kbIdClusters");
        this.mentionIndex = checkNotNull(mentionIndex, "mentionIndex");
    }

    public static OutputSet newInstance(String name, List<Output> links) {
        final Map<String, Set<Output>> clusters = Maps.newHashMap();
        final ImmutableMap.Builder<String, Output> el2kbid = ImmutableMap.builder();
        for (Output link : links) {
            if (clusters.containsKey(link.getKbId())) {
                clusters.get(link.getKbId()).add(link);
            } else {
                clusters.put(link.getKbId(), Sets.newHashSet(link));
            }

            el2kbid.put(link.getMentionId(), link);
        }
        return new OutputSet(name, ImmutableList.copyOf(links), clusters, el2kbid.build());
    }

    public String getName() {
        return name;
    }

    public boolean inSameCluster(String el_a, String el_b) {
        return getKbIdForMention(el_a).equals(getKbIdForMention(el_b));
    }

    public List<Output> getLinks() {
        return links;
    }

    public String getKbIdForMention(String mentionId) {
        return mentionIndex.get(mentionId).getKbId();
    }

    public Map<String, Set<Output>> getKbIdClusters() {
        return kbIdClusters;
    }

    public Map<String, Output> getMentionIndex() {
        return mentionIndex;
    }

    public Set<String> getMentionIds() {
        return mentionIndex.keySet();
    }

    public int getMentionCount() {
        return mentionIndex.keySet().size();
    }

    public Set<String> getKbIds() {
        return kbIdClusters.keySet();
    }

    @Override
    public Output get(int index) {
        return links.get(index);
    }

    @Override
    @Nonnull
    public Iterator<Output> iterator() {
        return links.iterator();
    }

    @Override
    @Nonnegative
    public int size() {
        return links.size();
    }

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(this)
                .add("links", links)
                .toString();
    }

    public void appendResultsTable(Appendable appendable) throws IOException {
        for (Output link : links) {
            appendable.append(link.getMentionId());
            appendable.append('\t');
            appendable.append(link.getKbId());
            appendable.append('\t');
            appendable.append(Double.toString(link.getConfidence()));
            appendable.append(System.getProperty("line.separator"));
        }
    }

    public void appendKbIdClusters(Appendable appendable) throws IOException {
        final Map<String, Set<Output>> clusters = getKbIdClusters();
        for (Map.Entry<String, Set<Output>> cluster : clusters.entrySet()) {
            appendable.append(cluster.getKey());
            appendable.append(" => {");
            boolean first = true;
            for (Output elo : cluster.getValue()) {
                if (!first) {
                    appendable.append(", ");
                } else {
                    first = false;
                }
                appendable.append(elo.getMentionId());
            }
            appendable.append("}");
            appendable.append(System.getProperty("line.separator"));
        }
    }
}
