package io.xj.nexus.craft.detail;

import io.xj.SegmentChoice;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.fabricator.NameIsometry;

/**
 Score a candidate for an inertial choice, based on its similarity score to the source choice
 */
public class InertialCandidate {
  double score;
  SegmentChoice target;

  public InertialCandidate(Fabricator fabricator, SegmentChoice target, SegmentChoice source) {
    this.target = target;
    var candidateVoice = fabricator.sourceMaterial().getProgramVoice(target.getProgramVoiceId());
    var voice = fabricator.sourceMaterial().getProgramVoice(source.getProgramVoiceId());
    score = candidateVoice.isPresent() && voice.isPresent() ?
      NameIsometry.similarity(candidateVoice.get().getName(), voice.get().getName()) : 0;
  }

  public double getScore() {
    return score;
  }

  public SegmentChoice getTarget() {
    return target;
  }

  public boolean isValid() {
    return 0 < score;
  }
}
