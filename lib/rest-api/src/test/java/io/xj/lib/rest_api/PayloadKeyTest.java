// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.rest_api;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 Tests for text utilities
 <p>
 Created by Charney Kaye on 2020/03/09
 */
public class PayloadKeyTest extends TestTemplate {


  @Test
  public void formatSimpleTrace() {
    // FUTURE test Text.formatSimpleTrace()
  }

  @Test
  public void getSimpleName() {
    assertEquals("MockEntity", PayloadKey.getSimpleName(new MockEntity()));
    assertEquals("null", PayloadKey.getSimpleName((Object) null));
  }

  @Test
  public void toAlphabetical() {
    assertEquals("Pajamas", PayloadKey.toAlphabetical("Pajamas"));
    assertEquals("Pajamas", PayloadKey.toAlphabetical("1P34aj2a3ma321s"));
    assertEquals("Pajamas", PayloadKey.toAlphabetical("  P#$ aj#$@a   @#$$$$ma         s"));
    assertEquals("Pajamas", PayloadKey.toAlphabetical("P_+_+_+_+_(@(#%&!&&&@&%!@)_$*(!_)@()_#()(((a j a m a s "));
    assertEquals("Pajamas", PayloadKey.toAlphabetical("Pajamas"));
  }

  @Test
  public void toAlphanumeric() {
    assertEquals("Pajamas", PayloadKey.toAlphanumeric("Pajamas!!!!!!"));
    assertEquals("17Pajamas", PayloadKey.toAlphanumeric("17 Pajamas?"));
    assertEquals("Pajamas5", PayloadKey.toAlphanumeric("  P#$ aj#$@a   @#$$$$ma         s5"));
    assertEquals("Pajamas25", PayloadKey.toAlphanumeric("P_+_+_+_+_(@(#%&!&&&@&%!@)_$*(!_)@()_#()(((a j a m a s 2    5"));
    assertEquals("Pajamas", PayloadKey.toAlphanumeric("Pajamas"));
  }

  @Test
  public void toAlphaSlug() {
    assertEquals("THIS_THING", PayloadKey.toAlphaSlug("--- ---  T@@@HIS_@THIN!4 G"));
  }

  @Test
  public void toLowerScored() {
    assertEquals("hammy_jammy", PayloadKey.toLowerScored("HAMMY jaMMy"));
    assertEquals("jammy", PayloadKey.toLowerScored("jaMMy"));
    assertEquals("jam_42", PayloadKey.toLowerScored("jaM &&$ 42"));
    assertEquals("jam_42", PayloadKey.toLowerScored("  ## jaM &&$ 42"));
    assertEquals("jam_42", PayloadKey.toLowerScored("jaM &&$ 42 !!!!"));
    assertEquals("jmmy", PayloadKey.toLowerScored("j#MMy", "neuf"));
    assertEquals("neuf", PayloadKey.toLowerScored(null, "neuf"));
    assertEquals("neuf", PayloadKey.toLowerScored("%&(#", "neuf"));
    assertEquals("hammy_jammy_bunbuns", PayloadKey.toLowerScored("HAMMY $%& jaMMy bun%buns"));
    assertEquals("p", PayloadKey.toLowerScored("%&(#p"));
    assertEquals("", PayloadKey.toLowerScored("%&(#"));
  }

  @Test
  public void toLowerSlug() {
    assertEquals("hammyjammy", PayloadKey.toLowerSlug("H4AMMY jaMMy"));
    assertEquals("jammy", PayloadKey.toLowerSlug("jaMMy"));
    assertEquals("jmmy", PayloadKey.toLowerSlug("j#MMy", "neuf"));
    assertEquals("neuf", PayloadKey.toLowerSlug(null, "neuf"));
    assertEquals("neuf", PayloadKey.toLowerSlug("%&(#", "neuf"));
    assertEquals("p", PayloadKey.toLowerSlug("%&(#p"));
    assertEquals("", PayloadKey.toLowerSlug("%&(#"));
  }

  @Test
  public void toNumeric() {
    assertEquals("2", PayloadKey.toNumeric("Pajamas!!2!!!!"));
    assertEquals("17", PayloadKey.toNumeric("17 Pajamas?"));
    assertEquals("5", PayloadKey.toNumeric("  P#$ aj#$@a   @#$$$$ma         s5"));
    assertEquals("25", PayloadKey.toNumeric("P_+_+_+_+_(@(#%&!&&&@&%!@)_$*(!_)@()_#()(((a j a m a s 2    5"));
    assertEquals("2", PayloadKey.toNumeric("Paja2mas"));
  }

  @Test
  public void toPlural() {
    assertEquals("i", PayloadKey.toPlural("i"));
    assertEquals("basses", PayloadKey.toPlural("bass"));
    assertEquals("bases", PayloadKey.toPlural("base"));
    assertEquals("flasks", PayloadKey.toPlural("flask"));
    assertEquals("kitties", PayloadKey.toPlural("kitty"));
    assertEquals("cats", PayloadKey.toPlural("cat"));
    assertEquals("entities", PayloadKey.toPlural("entity"));
    assertEquals("library-programs", PayloadKey.toPlural("library-program"));
    assertEquals("libraries", PayloadKey.toPlural("library"));
  }

  @Test
  public void toSingular() {
    assertEquals("i", PayloadKey.toSingular("i"));
    assertEquals("base", PayloadKey.toSingular("bases"));
    assertEquals("flask", PayloadKey.toSingular("flasks"));
    assertEquals("kitty", PayloadKey.toSingular("kitties"));
    assertEquals("cat", PayloadKey.toSingular("cats"));
    assertEquals("entity", PayloadKey.toSingular("entities"));
    assertEquals("library-program", PayloadKey.toSingular("library-programs"));
    assertEquals("library", PayloadKey.toSingular("libraries"));
  }

  @Test
  public void toProper() {
    assertEquals("Jammy biscuit", PayloadKey.toProper("jaMMy bISCUIT"));
    assertEquals("Jammy", PayloadKey.toProper("jaMMy"));
    assertEquals("J#mmy", PayloadKey.toProper("j#MMy"));
    assertEquals("%&(#", PayloadKey.toProper("%&(#"));
  }

  @Test
  public void toProperSlug() {
    assertEquals("Jammybiscuit", PayloadKey.toProperSlug("jaMMy bISCUIT"));
    assertEquals("Jammy", PayloadKey.toProperSlug("jaMMy"));
    assertEquals("Jmmy", PayloadKey.toProperSlug("j#MMy", "neuf"));
    assertEquals("Neuf", PayloadKey.toProperSlug("%&(#", "neuf"));
    assertEquals("P", PayloadKey.toProperSlug("%&(#p"));
    assertEquals("", PayloadKey.toProperSlug("%&(#"));
  }

  @Test
  public void toResourceBelongsTo() {
    assertEquals("entity", PayloadKey.toResourceBelongsTo("Entity"));
    assertEquals("libraryProgram", PayloadKey.toResourceBelongsTo("LibraryProgram"));
    assertEquals("mockEntity", PayloadKey.toResourceBelongsTo("mockEntity"));
    assertEquals("mockEntity", PayloadKey.toResourceBelongsTo("mockEntities"));
    assertEquals("mockEntity", PayloadKey.toResourceBelongsTo(createMockEntity("Ding")));
    assertEquals("mockEntity", PayloadKey.toResourceBelongsTo(MockEntity.class));
    assertEquals("library", PayloadKey.toResourceBelongsTo("Library"));
  }

  @Test
  public void toResourceHasMany() {
    assertEquals("entities", PayloadKey.toResourceHasMany("Entity"));
    assertEquals("libraryPrograms", PayloadKey.toResourceHasMany("LibraryProgram"));
    assertEquals("mockEntities", PayloadKey.toResourceHasMany("mockEntity"));
    assertEquals("mockEntities", PayloadKey.toResourceHasMany(createMockEntity("Ding")));
    assertEquals("mockEntities", PayloadKey.toResourceHasMany(MockEntity.class));
    assertEquals("libraries", PayloadKey.toResourceHasMany("Library"));
  }

  @Test
  public void toResourceHasManyFromType() {
    assertEquals("entities", PayloadKey.toResourceHasManyFromType("entities"));
    assertEquals("libraryPrograms", PayloadKey.toResourceHasManyFromType("library-programs"));
    assertEquals("libraryPrograms", PayloadKey.toResourceHasManyFromType("library-program"));
    assertEquals("libraries", PayloadKey.toResourceHasManyFromType("library"));
    assertEquals("libraries", PayloadKey.toResourceHasManyFromType("Libraries"));
  }

  @Test
  public void toResourceType() {
    assertEquals("entities", PayloadKey.toResourceType("Entity"));
    assertEquals("library-programs", PayloadKey.toResourceType("LibraryProgram"));
    assertEquals("library-programs", PayloadKey.toResourceType("libraryProgram"));
    assertEquals("library-programs", PayloadKey.toResourceType("libraryPrograms"));
    assertEquals("mock-entities", PayloadKey.toResourceType(createMockEntity("Ding")));
    assertEquals("mock-entities", PayloadKey.toResourceType(MockEntity.class));
    assertEquals("libraries", PayloadKey.toResourceType("Library"));
  }

  @Test
  public void toScored() {
    assertEquals("", PayloadKey.toScored(null));
    assertEquals("HAMMY_jaMMy", PayloadKey.toScored("HAMMY jaMMy"));
    assertEquals("jaMMy", PayloadKey.toScored("jaMMy"));
    assertEquals("jaM_42", PayloadKey.toScored("jaM &&$ 42"));
    assertEquals("jaM_42", PayloadKey.toScored("  ## jaM &&$ 42"));
    assertEquals("jaM_42", PayloadKey.toScored("jaM &&$ 42 !!!!"));
    assertEquals("HAMMY_jaMMy_bunbuns", PayloadKey.toScored("HAMMY $%& jaMMy bun%buns"));
    assertEquals("p", PayloadKey.toScored("%&(#p"));
    assertEquals("", PayloadKey.toScored("%&(#"));
  }

  @Test
  public void toSingleQuoted() {
    assertEquals("'stones'", PayloadKey.toSingleQuoted("stones"));
  }


  @Test
  public void toSlug() {
    assertEquals("jim", PayloadKey.toSlug("jim"));
    assertEquals("jim", PayloadKey.toSlug("jim-251"));
    assertEquals("jim", PayloadKey.toSlug("j i m - 2 5 1"));
    assertEquals("jim", PayloadKey.toSlug("j!i$m%-^2%5*1"));
  }

  @Test
  public void toStrings() {
    // FUTURE test Text.toStrings()
  }

  @Test
  public void toUpperScored() {
    assertEquals("JAMMY_BUNS", PayloadKey.toUpperScored("jaMMy b#!uns"));
    assertEquals("JAMMY_BUNS", PayloadKey.toUpperScored("  jaMMy    b#!uns   "));
    assertEquals("JAMMY", PayloadKey.toUpperScored("jaMMy"));
    assertEquals("JMMY", PayloadKey.toUpperScored("j#MMy", "neuf"));
    assertEquals("NEUF", PayloadKey.toUpperScored(null, "neuf"));
    assertEquals("NEUF", PayloadKey.toUpperScored("%&(#", "neuf"));
    assertEquals("P", PayloadKey.toUpperScored("%&(#p"));
    assertEquals("", PayloadKey.toUpperScored("%&(#"));
  }

  @Test
  public void toUpperSlug() {
    assertEquals("JAMMYBUNS", PayloadKey.toUpperSlug("jaMMy b#!uns"));
    assertEquals("JAMMY", PayloadKey.toUpperSlug("jaMMy"));
    assertEquals("JMMY", PayloadKey.toUpperSlug("j#MMy", "neuf"));
    assertEquals("NEUF", PayloadKey.toUpperSlug(null, "neuf"));
    assertEquals("NEUF", PayloadKey.toUpperSlug("%&(#", "neuf"));
    assertEquals("P", PayloadKey.toUpperSlug("%&(#p"));
    assertEquals("", PayloadKey.toUpperSlug("%&(#"));
  }

  @Test
  public void toIdAttribute() {
    assertEquals("bilgeWaterId", PayloadKey.toIdAttribute("BilgeWater"));
    assertEquals("mockEntityId", PayloadKey.toIdAttribute(createMockEntity("Ding")));
    assertEquals("mockEntityId", PayloadKey.toIdAttribute(MockEntity.class));
  }

  @Test
  public void toAttributeName() {
    assertEquals("dancingAbility", PayloadKey.toAttributeName("DancingAbility"));
  }
}
