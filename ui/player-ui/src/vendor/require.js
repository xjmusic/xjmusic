// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

// NOTE this is a custom version of RequireJS that uses a path prefix
const pathPrefix = "js/";

;(function(root) {

  function require(p) {
    const path = pathPrefix + require.resolve(p)
    ;let mod = require.modules[path];

    // weepy: following line added
    if (!mod) mod = require.load(path);

    if (!mod) throw new Error('failed to require "' + p + '"');

    if (!mod.exports) {
      mod.exports = {};

      // weepy: added __filename and __dirname
      const bits = path.split('/')
        , __filename = bits.pop() + '.js'
        , __dirname = bits.join('/');


      mod.call(mod.exports, mod, mod.exports, require.relative(path), __filename, __dirname);
    }
    return mod.exports;
  }

  require.modules = {};

  require.prefix = "";

  require.resolve = function (path){
    path = require.prefix + path;
    const reg = path + '.js'
      , index = path + '/index.js';
    return require.modules[reg] && reg
      || require.modules[index] && index
      || path;
  };

  require.register = function (path, fn){
    require.modules[path] = fn;
  };

  require.relative = function (parent) {
    return function(p){
      if ('.' !== p.charAt(0)) return require(p);

      const path = parent.split('/')
        , segs = p.split('/');
      path.pop();


      const relative_path = path.concat(segs);
      const normalized = [];

      for (let i = 0; i < relative_path.length; i++) {
        const seg = relative_path[i];

        if ('..' === seg && normalized.length && normalized[normalized.length-1].charAt(0) !== ".") {
          normalized.pop();
        }
        else if('.' === seg && normalized.length) {
          //
        }
        else
          normalized.push(seg)
      }

      return require(normalized.join('/'));
    };
  };

  // weepy: following added
  require.load = function( path ) {

    const orig_path = path;
    const request = new XMLHttpRequest();

    if(path.match(/\.[^./]+$/)) {
      // has extension

    } else {
      path += ".js"
    }

    const ext = path.split(".").pop();

    request.open('GET', path, false);
    request.send();
    let data = request.responseText;

    if(!request.responseText || !request.responseText.length)
      console.log("FAILED to load ", path);

    console.log("xhr: ", path, " loaded ", request.responseText.length, " bytes");

    if(ext === "json") {
      try {
        JSON.parse(data)
      } catch(e) {
        console.error("BAD JSON @ " + path, e);
        return
      }
      data = "module.exports = " + data
    }
    else if(ext === "js") {

    }
    else {
      // STRING
      data = "module.exports = " + JSON.stringify(data)
    }

    const text = "require.register('" + orig_path + "', \
              function(module, exports, require, __filename, __dirname) {\n\n"
      + data + "\n\n\
            }); //@ sourceURL=" + path;

    try {
      (window.execScript || function(text) {
        return window["eval"].call(window, text)
      })(text)

    }
    catch(e) {
      if(root.PARSEJS) {
        try {
          root.PARSEJS.parse(text)
        } catch(e) {
          console.error('Syntax error in file ' + path + ' at line:' + (e.line-2) + ', col:' + e.col + '. ' + e.platform_message) //, e.stack) //, e)
        }
      } else {
        console.error( "Syntax Error in file " + path + ": " + e.toString() )
      }
    }

    return require.modules[ orig_path ];
  };

  root.require = require;

})(window);
