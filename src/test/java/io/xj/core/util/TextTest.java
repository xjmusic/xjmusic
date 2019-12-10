// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.util;

import com.google.common.collect.ImmutableMap;
import io.xj.core.model.Account;
import io.xj.core.model.AccountUser;
import io.xj.core.model.Chain;
import io.xj.core.model.ChainConfig;
import io.xj.core.model.ChainState;
import io.xj.core.model.InstrumentAudioChord;
import io.xj.core.model.InstrumentAudioEvent;
import io.xj.core.model.InstrumentMeme;
import io.xj.core.model.ProgramMeme;
import io.xj.core.model.ProgramSequenceChord;
import io.xj.core.model.ProgramSequencePatternEvent;
import io.xj.core.model.SegmentChord;
import io.xj.core.model.SegmentMeme;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class TextTest  {


  @Test
  public void formatSimpleTrace() {
    // FUTURE test Text.formatSimpleTrace()
  }

  @Test
  public void formatStackTrace() {
    assertEquals("", Text.formatStackTrace(null));
  }

  @Test
  public void getSimpleName() {
    assertEquals("ChainConfig", Text.getSimpleName(new ChainConfig()));
    assertEquals("ChainConfig", Text.getSimpleName(ChainConfig.class));
    assertEquals("Account", Text.getSimpleName(Account.class));
    assertEquals("Chain", Text.getSimpleName(Chain.create(ChainState.Fabricate)));
    assertEquals("Chain", Text.getSimpleName(Chain.class));
    assertEquals("InstrumentAudioChord", Text.getSimpleName(InstrumentAudioChord.class));
    assertEquals("InstrumentAudioEvent", Text.getSimpleName(InstrumentAudioEvent.class));
    assertEquals("InstrumentMeme", Text.getSimpleName(InstrumentMeme.class));
    assertEquals("ProgramSequencePatternEvent", Text.getSimpleName(ProgramSequencePatternEvent.class));
    assertEquals("ProgramMeme", Text.getSimpleName(ProgramMeme.class));
    assertEquals("SegmentChord", Text.getSimpleName(SegmentChord.class));
    assertEquals("SegmentMeme", Text.getSimpleName(SegmentMeme.class));
    assertEquals("ProgramSequenceChord", Text.getSimpleName(ProgramSequenceChord.class));
  }

  @Test
  public void toAlphabetical() {
    assertEquals("Pajamas", Text.toAlphabetical("Pajamas"));
    assertEquals("Pajamas", Text.toAlphabetical("1P34aj2a3ma321s"));
    assertEquals("Pajamas", Text.toAlphabetical("  P#$ aj#$@a   @#$$$$ma         s"));
    assertEquals("Pajamas", Text.toAlphabetical("P_+_+_+_+_(@(#%&!&&&@&%!@)_$*(!_)@()_#()(((a j a m a s "));
    assertEquals("Pajamas", Text.toAlphabetical("Pajamas"));
  }

  @Test
  public void toAlphanumeric() {
    assertEquals("Pajamas", Text.toAlphanumeric("Pajamas!!!!!!"));
    assertEquals("17Pajamas", Text.toAlphanumeric("17 Pajamas?"));
    assertEquals("Pajamas5", Text.toAlphanumeric("  P#$ aj#$@a   @#$$$$ma         s5"));
    assertEquals("Pajamas25", Text.toAlphanumeric("P_+_+_+_+_(@(#%&!&&&@&%!@)_$*(!_)@()_#()(((a j a m a s 2    5"));
    assertEquals("Pajamas", Text.toAlphanumeric("Pajamas"));
  }

  @Test
  public void toAlphaSlug() {
    assertEquals("THIS_THING", Text.toAlphaSlug("--- ---  T@@@HIS_@THIN!4 G"));
  }

  @Test
  public void toLowerScored() {
    assertEquals("hammy_jammy", Text.toLowerScored("HAMMY jaMMy"));
    assertEquals("jammy", Text.toLowerScored("jaMMy"));
    assertEquals("jam_42", Text.toLowerScored("jaM &&$ 42"));
    assertEquals("jam_42", Text.toLowerScored("  ## jaM &&$ 42"));
    assertEquals("jam_42", Text.toLowerScored("jaM &&$ 42 !!!!"));
    assertEquals("jmmy", Text.toLowerScored("j#MMy", "neuf"));
    assertEquals("neuf", Text.toLowerScored(null, "neuf"));
    assertEquals("neuf", Text.toLowerScored("%&(#", "neuf"));
    assertEquals("hammy_jammy_bunbuns", Text.toLowerScored("HAMMY $%& jaMMy bun%buns"));
    assertEquals("p", Text.toLowerScored("%&(#p"));
    assertEquals("", Text.toLowerScored("%&(#"));
  }

  @Test
  public void toLowerSlug() {
    assertEquals("hammyjammy", Text.toLowerSlug("H4AMMY jaMMy"));
    assertEquals("jammy", Text.toLowerSlug("jaMMy"));
    assertEquals("jmmy", Text.toLowerSlug("j#MMy", "neuf"));
    assertEquals("neuf", Text.toLowerSlug(null, "neuf"));
    assertEquals("neuf", Text.toLowerSlug("%&(#", "neuf"));
    assertEquals("p", Text.toLowerSlug("%&(#p"));
    assertEquals("", Text.toLowerSlug("%&(#"));
  }

  @Test
  public void toNote() {
    assertEquals("C# major", Text.toNote("   C# m___ajor "));
  }

  @Test
  public void toNumeric() {
    assertEquals("2", Text.toNumeric("Pajamas!!2!!!!"));
    assertEquals("17", Text.toNumeric("17 Pajamas?"));
    assertEquals("5", Text.toNumeric("  P#$ aj#$@a   @#$$$$ma         s5"));
    assertEquals("25", Text.toNumeric("P_+_+_+_+_(@(#%&!&&&@&%!@)_$*(!_)@()_#()(((a j a m a s 2    5"));
    assertEquals("2", Text.toNumeric("Paja2mas"));
  }

  @Test
  public void toPlural() {
    assertEquals("i", Text.toPlural("i"));
    assertEquals("basses", Text.toPlural("bass"));
    assertEquals("bases", Text.toPlural("base"));
    assertEquals("flasks", Text.toPlural("flask"));
    assertEquals("kitties", Text.toPlural("kitty"));
    assertEquals("cats", Text.toPlural("cat"));
    assertEquals("libraries", Text.toPlural("library"));
    assertEquals("account-users", Text.toPlural("account-user"));
    assertEquals("accounts", Text.toPlural("account"));
  }

  @Test
  public void toSingular() {
    assertEquals("i", Text.toSingular("i"));
    assertEquals("base", Text.toSingular("bases"));
    assertEquals("flask", Text.toSingular("flasks"));
    assertEquals("kitty", Text.toSingular("kitties"));
    assertEquals("cat", Text.toSingular("cats"));
    assertEquals("library", Text.toSingular("libraries"));
    assertEquals("account-user", Text.toSingular("account-users"));
    assertEquals("account", Text.toSingular("accounts"));
  }

  @Test
  public void toProper() {
    assertEquals("Jammy biscuit", Text.toProper("jaMMy bISCUIT"));
    assertEquals("Jammy", Text.toProper("jaMMy"));
    assertEquals("J#mmy", Text.toProper("j#MMy"));
    assertEquals("%&(#", Text.toProper("%&(#"));
  }

  @Test
  public void toProperSlug() {
    assertEquals("Jammybiscuit", Text.toProperSlug("jaMMy bISCUIT"));
    assertEquals("Jammy", Text.toProperSlug("jaMMy"));
    assertEquals("Jmmy", Text.toProperSlug("j#MMy", "neuf"));
    assertEquals("Neuf", Text.toProperSlug("%&(#", "neuf"));
    assertEquals("P", Text.toProperSlug("%&(#p"));
    assertEquals("", Text.toProperSlug("%&(#"));
  }

  @Test
  public void toResourceBelongsTo() {
    assertEquals("library", Text.toResourceBelongsTo("Library"));
    assertEquals("accountUser", Text.toResourceBelongsTo("AccountUser"));
    assertEquals("accountUser", Text.toResourceBelongsTo("accountUser"));
    assertEquals("accountUser", Text.toResourceBelongsTo("accountUser"));
    assertEquals("chain", Text.toResourceBelongsTo(Chain.create(ChainState.Fabricate)));
    assertEquals("chain", Text.toResourceBelongsTo(Chain.class));
    assertEquals("accountUser", Text.toResourceBelongsTo(new AccountUser()));
    assertEquals("accountUser", Text.toResourceBelongsTo(AccountUser.class));
    assertEquals("account", Text.toResourceBelongsTo("Account"));
  }

  @Test
  public void toResourceHasMany() {
    assertEquals("libraries", Text.toResourceHasMany("Library"));
    assertEquals("accountUsers", Text.toResourceHasMany("AccountUser"));
    assertEquals("accountUsers", Text.toResourceHasMany("accountUser"));
    assertEquals("chains", Text.toResourceHasMany(Chain.create(ChainState.Fabricate)));
    assertEquals("chains", Text.toResourceHasMany(Chain.class));
    assertEquals("accountUsers", Text.toResourceHasMany(new AccountUser()));
    assertEquals("accountUsers", Text.toResourceHasMany(AccountUser.class));
    assertEquals("accounts", Text.toResourceHasMany("Account"));
  }

  @Test
  public void toResourceHasManyFromType() {
    assertEquals("libraries", Text.toResourceHasManyFromType("libraries"));
    assertEquals("accountUsers", Text.toResourceHasManyFromType("account-users"));
    assertEquals("programSequenceBindingMemes", Text.toResourceHasManyFromType("program-sequence-binding-memes"));
    assertEquals("accountUsers", Text.toResourceHasManyFromType("account-users"));
    assertEquals("accounts", Text.toResourceHasManyFromType("accounts"));
  }

  @Test
  public void toResourceType() {
    assertEquals("libraries", Text.toResourceType("Library"));
    assertEquals("account-users", Text.toResourceType("AccountUser"));
    assertEquals("account-users", Text.toResourceType("accountUser"));
    assertEquals("account-users", Text.toResourceType("accountUser"));
    assertEquals("chains", Text.toResourceType(Chain.create(ChainState.Fabricate)));
    assertEquals("chains", Text.toResourceType(Chain.class));
    assertEquals("account-users", Text.toResourceType(new AccountUser()));
    assertEquals("account-users", Text.toResourceType(AccountUser.class));
    assertEquals("accounts", Text.toResourceType("Account"));
  }

  @Test
  public void toScored() {
    assertEquals("", Text.toScored(null));
    assertEquals("HAMMY_jaMMy", Text.toScored("HAMMY jaMMy"));
    assertEquals("jaMMy", Text.toScored("jaMMy"));
    assertEquals("jaM_42", Text.toScored("jaM &&$ 42"));
    assertEquals("jaM_42", Text.toScored("  ## jaM &&$ 42"));
    assertEquals("jaM_42", Text.toScored("jaM &&$ 42 !!!!"));
    assertEquals("HAMMY_jaMMy_bunbuns", Text.toScored("HAMMY $%& jaMMy bun%buns"));
    assertEquals("p", Text.toScored("%&(#p"));
    assertEquals("", Text.toScored("%&(#"));
  }

  @Test
  public void toSingleQuoted() {
    assertEquals("'stones'", Text.toSingleQuoted("stones"));
  }


  @Test
  public void toSlug() {
    assertEquals("jim", Text.toSlug("jim"));
    assertEquals("jim", Text.toSlug("jim-251"));
    assertEquals("jim", Text.toSlug("j i m - 2 5 1"));
    assertEquals("jim", Text.toSlug("j!i$m%-^2%5*1"));
  }

  @Test
  public void toStrings() {
    // FUTURE test Text.toStrings()
  }

  @Test
  public void toUpperScored() {
    assertEquals("JAMMY_BUNS", Text.toUpperScored("jaMMy b#!uns"));
    assertEquals("JAMMY_BUNS", Text.toUpperScored("  jaMMy    b#!uns   "));
    assertEquals("JAMMY", Text.toUpperScored("jaMMy"));
    assertEquals("JMMY", Text.toUpperScored("j#MMy", "neuf"));
    assertEquals("NEUF", Text.toUpperScored(null, "neuf"));
    assertEquals("NEUF", Text.toUpperScored("%&(#", "neuf"));
    assertEquals("P", Text.toUpperScored("%&(#p"));
    assertEquals("", Text.toUpperScored("%&(#"));
  }

  @Test
  public void toUpperSlug() {
    assertEquals("JAMMYBUNS", Text.toUpperSlug("jaMMy b#!uns"));
    assertEquals("JAMMY", Text.toUpperSlug("jaMMy"));
    assertEquals("JMMY", Text.toUpperSlug("j#MMy", "neuf"));
    assertEquals("NEUF", Text.toUpperSlug(null, "neuf"));
    assertEquals("NEUF", Text.toUpperSlug("%&(#", "neuf"));
    assertEquals("P", Text.toUpperSlug("%&(#p"));
    assertEquals("", Text.toUpperSlug("%&(#"));
  }

  @Test
  public void toIdAttribute() {
    assertEquals("bilgeWaterId", Text.toIdAttribute("BilgeWater"));
    assertEquals("accountId", Text.toIdAttribute(new Account()));
    assertEquals("accountId", Text.toIdAttribute(Account.class));
    assertEquals("chainId", Text.toIdAttribute(Chain.create(ChainState.Fabricate)));
    assertEquals("chainId", Text.toIdAttribute(Chain.class));
  }

  @Test
  public void keyValueString() {
    assertEquals("Test{one=1,two=2,three=3}",
      Text.toKeyValueString("Test", ImmutableMap.of("one", "1", "two", "2", "three", "3")));
  }

}
