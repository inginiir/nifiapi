package api.nifi.models.processgroup;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Status {

    private AggregateSnapshot aggregateSnapshot;

    public AggregateSnapshot getAggregateSnapshot() {
        return aggregateSnapshot;
    }

    public void setAggregateSnapshot(AggregateSnapshot aggregateSnapshot) {
        this.aggregateSnapshot = aggregateSnapshot;
    }
}
