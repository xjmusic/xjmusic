# Templates in this folder are STATIC

That means:

  * They DO NOT use Handlebars, even though they have the .hbs extension.
  * They have the .hbs extension because they need to, in order to be loaded as partials
  * They have NO Handlebars tags, because they are used as static content by `ember-cli-inline-content`

See:

  * [#314] Placeholder content index.html page should not have handlebars blocks
  * [#318] Static content is injected into index.html via {{content-for "static"}}

