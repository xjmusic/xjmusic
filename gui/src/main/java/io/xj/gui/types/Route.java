package io.xj.gui.types;

import java.util.Collection;
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
   Routes with parent content
   */
  private static final Collection<Route> routesWithParentContent = Set.of(
    ContentProgramBrowser,
    ContentProgramEditor,
    ContentInstrumentBrowser,
    ContentInstrumentEditor,
    ContentInstrumentAudioEditor,
    ContentLibraryEditor,
    TemplateEditor
  );

  /**
   Set of routes with the main nav 'content'
   */
  private static final Collection<Route> routesContent = Set.of(
    ContentInstrumentAudioEditor,
    ContentInstrumentBrowser,
    ContentInstrumentEditor,
    ContentLibraryBrowser,
    ContentLibraryEditor,
    ContentProgramBrowser,
    ContentProgramEditor
  );

  /**
   Set of routes which are an editor
   */
  private static final Collection<Route> routesEditor = Set.of(
    ContentLibraryEditor,
    ContentProgramEditor,
    ContentInstrumentEditor,
    ContentInstrumentAudioEditor,
    TemplateEditor
  );

  /**
   Set of routes which are a browser
   */
  private static final Collection<Route> routesBrowser = Set.of(
    ContentInstrumentBrowser,
    ContentLibraryBrowser,
    ContentProgramBrowser,
    TemplateBrowser
  );

  /**
   Set of routes with the main nav 'fabrication'
   */
  private static final Collection<Route> routesFabrication = Set.of(
    FabricationSegment,
    FabricationTimeline
  );

  /**
   Set of routes with the main nav 'template'
   */
  private static final Collection<Route> routesTemplate = Set.of(
    TemplateBrowser,
    TemplateEditor
  );

  /**
   @return true if this route has a parent content route
   */
  public boolean hasParentContent() {
    return routesWithParentContent.contains(this);
  }

  /**
   @return true if this is a content route
   */
  public boolean isContent() {
    return routesContent.contains(this);
  }

  /**
   @return true if this is a editor route
   */
  public boolean isEditor() {
    return routesEditor.contains(this);
  }

  /**
   @return true if this is a browser route
   */
  public boolean isBrowser() {
    return routesBrowser.contains(this);
  }

  /**
   @return true if this is a fabrication route
   */
  public boolean isFabrication() {
    return routesFabrication.contains(this);
  }

  /**
   @return true if this is a template route
   */
  public boolean isTemplate() {
    return routesTemplate.contains(this);
  }
}
