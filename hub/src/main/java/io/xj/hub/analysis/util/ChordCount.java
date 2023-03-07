package io.xj.hub.analysis.util;

import com.google.api.client.util.Sets;

import java.util.Set;
import java.util.UUID;

/**
 * Representation of the count of usages for one chord
 */
public class ChordCount {
  Set<UUID> programIds;
  Set<UUID> instrumentIds;
  Integer total;


  public Set<UUID> getProgramIds() {
    return programIds;
  }

  public Set<UUID> getInstrumentIds() {
    return instrumentIds;
  }

  public Integer getTotal() {
    return total;
  }

  public ChordCount() {
    total = 0;
    programIds = Sets.newHashSet();
    instrumentIds = Sets.newHashSet();
  }

  public void addProgramId(UUID programId) {
    programIds.add(programId);
    total++;
  }
}
