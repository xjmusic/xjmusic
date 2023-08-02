package io.xj.lib.analysis.util;

import java.util.HashSet;
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
    programIds = new HashSet<>();
    instrumentIds = new HashSet<>();
  }

  public void addProgramId(UUID programId) {
    programIds.add(programId);
    total++;
  }
}
