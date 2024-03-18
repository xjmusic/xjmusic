package io.xj.gui.nav;

import java.util.Set;

public enum Route {
  ContentInstrumentAudioEditor,
  ContentInstrumentBrowser,
  ContentInstrumentEditor,
  ContentLibraryBrowser,
  ContentLibraryEditor,
  ContentProgramBrowser,
  ContentProgramEditor,
  FabricationSegment,
  FabricationTimeline,
  TemplateBrowser,
  TemplateEditor;

  /**
   Set of routes with the main nav 'content'
   */
  private static final Set<Route> contentRoutes = Set.of(
    ContentInstrumentAudioEditor,
    ContentInstrumentBrowser,
    ContentInstrumentEditor,
    ContentLibraryBrowser,
    ContentLibraryEditor,
    ContentProgramBrowser,
    ContentProgramEditor
  );

  /**
   @return the default route
   */
  public static Route getDefault() {
    return ContentLibraryBrowser;
  }

  /**
   @return true if this is a content route
   */
  public boolean isContent() {
    return contentRoutes.contains(this);
  }

  /**
   Set of routes with the main nav 'fabrication'
   */
  private static final Set<Route> fabricationRoutes = Set.of(
    FabricationSegment,
    FabricationTimeline
  );

  /**
   @return true if this is a fabrication route
   */
  public boolean isFabrication() {
    return fabricationRoutes.contains(this);
  }

  /**
   Set of routes with the main nav 'template'
   */
  private static final Set<Route> templateRoutes = Set.of(
    TemplateBrowser,
    TemplateEditor
  );

  /**
   @return true if this is a template route
   */
  public boolean isTemplate() {
    return templateRoutes.contains(this);
  }

}
