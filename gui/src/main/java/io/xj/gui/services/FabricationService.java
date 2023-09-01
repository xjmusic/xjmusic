// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.services;

import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.Program;
import io.xj.nexus.InputMode;
import io.xj.nexus.OutputFileMode;
import io.xj.nexus.OutputMode;
import io.xj.nexus.model.*;
import io.xj.nexus.work.WorkFactory;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Worker;
import javafx.event.EventTarget;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface FabricationService extends Worker<Boolean>, EventTarget {

  ObjectProperty<FabricationStatus> statusProperty();

  StringProperty inputTemplateKeyProperty();

  StringProperty outputPathPrefixProperty();

  ObjectProperty<InputMode> inputModeProperty();

  ObjectProperty<OutputFileMode> outputFileModeProperty();

  ObjectProperty<OutputMode> outputModeProperty();

  StringProperty outputSecondsProperty();

  WorkFactory getWorkFactory();

  Collection<SegmentMeme> getSegmentMemes(Segment segment);

  Collection<SegmentChord> getSegmentChords(Segment segment);

  Collection<SegmentChoice> getSegmentChoices(Segment segment);

  void start();

  void reset();

  Optional<Program> getProgram(UUID programId);

  Optional<Instrument> getInstrument(UUID instrumentId);

  Collection<SegmentChoiceArrangement> getArrangements(SegmentChoice choice);

  Collection<SegmentChoiceArrangementPick> getPicks(SegmentChoiceArrangement arrangement);
}
