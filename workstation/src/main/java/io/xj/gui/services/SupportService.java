package io.xj.gui.services;

public interface SupportService {
  /**
   Launch the guide in a browser
   */
  void launchGuideInBrowser();

  /**
   Launch the tutorial in a browser
   */
  void launchTutorialInBrowser();

  /**
    Launch the discord in a browser
   */
  void launchDiscordInBrowser();

  /**
    Launch the website in a browser
   */
  void launchWebsiteInBrowser();

  /**
   Launch the privacy policy's section on the workstation, and its usage reporting, in a browser
   */
  void launchPrivacyPolicyInBrowser();

  /**
   Launch PRIVACY.md, which says exactly what usage reporting sends, metric by metric, in a browser
   */
  void launchUsageReportingDocInBrowser();
}
