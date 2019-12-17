// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

import workspaceActivate from "./workspaceActivate";
import workspaceDeactivate from "./workspaceDeactivate";

/**
 Send the actions to activate a sequence and deactivate the current sequence pattern for all voices

 @param dispatch{Function} to send actions with
 @param programId{uuid} of program
 @param programSequenceId{uuid} of program sequence
 @param voices{[Object]} array of voices to deactive current sequence pattern of
 */
export default (dispatch, programId, programSequenceId, voices) => {
  dispatch(workspaceActivate(programId, "program sequence", programSequenceId));
  voices.forEach(voice => dispatch(workspaceDeactivate(voice.id, "program sequence pattern")));
}
