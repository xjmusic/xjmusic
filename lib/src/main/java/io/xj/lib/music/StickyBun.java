// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.music;

import com.google.api.client.util.Maps;
import com.google.common.collect.Lists;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 Sticky buns v2 https://www.pivotaltracker.com/story/show/179153822 persisted for each randomly selected note in the series for any given pattern
 - key on program-sequence-pattern-event id, persisting only the first value seen for any given event
 - super-key on program-sequence-pattern id, measuring delta from the first event seen in that pattern
 */
public class StickyBun {
  private final UUID parentId;
  private final Note root;
  private final Map<UUID, Member> members;
  private @Nullable
  Double earliestPosition;

  /**
   Prepare a sticky bun for members of the given parent if

   @param parentId id of parent
   @param root     note
   */
  public StickyBun(UUID parentId, Note root) {
    this.parentId = parentId;
    this.root = root;
    members = Maps.newHashMap();
  }

  /**
   Get the id of the parent of all members of this sticky bun

   @return parent id
   */
  public UUID getParentId() {
    return parentId;
  }

  /**
   Add a note by member id. Update the earliest lowest note if qualified

   @param memberId id of member
   @param position of note
   @param note     to add
   */
  public void put(UUID memberId, Double position, Note note) {
    if (Objects.isNull(earliestPosition) || position < earliestPosition)
      earliestPosition = position;

    if (!members.containsKey(memberId))
      members.put(memberId, new Member(memberId));
    members.get(memberId).add(position, note);
  }

  /**
   Get a list of offsets (relative to earliest lowest note) for given member id

   @param memberId for which to get offsets
   @return offsets for member id
   */
  public List<Integer> getOffsets(UUID memberId) {
    if (!members.containsKey(memberId)) return List.of();
    return members.get(memberId).getOffsetsFrom(root);
  }

  /**
   Replace the atonal notes in the given list with our offsets from the given target root note

   @param memberId to use
   @param targetRoot   root note for interpretation of our offsets
   @param from     list of notes from which to replace atonal notes with our offset notes
   @return notes with atonal notes replaced
   */
  public List<Note> replaceAtonal(UUID memberId, Note targetRoot, List<Note> from) {
    var targets =
      getOffsets(memberId).stream()
        .map(targetRoot::shift)
        .toList();

    List<Note> notes = Lists.newArrayList(from);
    if (targets.isEmpty()) return notes;

    for (var i = 0; i < notes.size(); i++)
      if (notes.get(i).isAtonal())
        notes.set(i, targets.get(Math.min(i, targets.size() - 1)));

    return notes;
  }

  /**
   @param memberId to check for tonality
   @return true if any of the specified member's notes are tonal
   */
  public boolean isTonal(UUID memberId) {
    return members.containsKey(memberId) && !members.get(memberId).isAtonal();
  }

  /**
   A sticky bun member is a group of notes at one position
   */
  static class Member {
    UUID id;
    List<Note> notes;
    @Nullable
    Double position;

    /**
     Create a member with the given id

     @param id of member
     */
    public Member(UUID id) {
      this.id = id;
    }

    /**
     @return member id
     */
    public UUID getId() {
      return id;
    }

    /**
     @return notes at earliest available position
     */
    public List<Note> getNotes() {
      return notes;
    }

    /**
     Add notes to the member at a known position

     @param notePos position
     @param note    to add
     */
    public void add(Double notePos, Note note) {
      if (Objects.isNull(position) || notePos < position) {
        position = notePos;
        notes = Lists.newArrayList(note);
      } else if (Objects.equals(position, notePos)) {
        notes.add(note);
      }
    }

    /**
     Get the list of offsets of this member's notes from the given root
     */
    public List<Integer> getOffsetsFrom(Note root) {
      return getNotes().stream()
        .map(root::delta)
        .toList();
    }

    /**
     @return true if none of these notes are tonal
     */
    public boolean isAtonal() {
      return notes.stream().allMatch(Note::isAtonal);
    }
  }
}
