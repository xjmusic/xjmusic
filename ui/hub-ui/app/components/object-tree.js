//  Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
/*global Symbol*/
import Component from '@ember/component';

/**
 * Displays an object as pretty HTML
 */
const JsonPrettyComponent = Component.extend({
  didReceiveAttrs() {
    this._super(...arguments);
    const data = this.data;
    if (typeof data === 'string') {
      this.set('content', data);
    } else if (data) {
      this.set('content', prettyPrint(data));
    }
  }
});

/**
 * Usage (e.g, in Handlebars):
 *
 *   {{object-tree object}}
 */
JsonPrettyComponent.reopenClass({
  positionalParams: ['data']
});

export default JsonPrettyComponent;

let _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) {
  return typeof obj;
} : function (obj) {
  return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj;
};

let matchHtmlRegExp = /["'&<>]/;
let escapeHtml = function escapeHtml(str) {
  let match = matchHtmlRegExp.exec(str);
  if (match !== null) {
    let escape = void 0;
    let html = '';
    let index;
    let lastIndex = 0;
    for (index = match.index; index < str.length; index++) {
      switch (str.charCodeAt(index)) {
        case 34:
          escape = '&quot;';
          break;
        case 38:
          escape = '&amp;';
          break;
        case 39:
          escape = '&#39;';
          break;
        case 60:
          escape = '&lt;';
          break;
        case 62:
          escape = '&gt;';
          break;
        default:
          continue;
      }
      if (lastIndex !== index) {
        html += str.substring(lastIndex, index);
      }
      lastIndex = index + 1;
      html += escape;
    }
    return lastIndex !== index ? html + str.substring(lastIndex, index) : html;
  }
  return str;
};
let PrintWriter = function () {
  function PrintWriter(indentString) {
    this.buffer = [];
    this.indentString = indentString;
    this.objects = [];
  }

  PrintWriter.prototype.checkCircular = function (object) {
    for (let _i = 0, _a = this.objects; _i < _a.length; _i++) {
      let obj = _a[_i];
      if (object === obj) {
        throw new Error('Cannot pretty print object with circular reference');
      }
    }
    this.objects.push(object);
  };
  PrintWriter.prototype.print = function (str) {
    this.buffer.push(str);
  };
  PrintWriter.prototype.newLine = function () {
    if (this._printSelectionEndAtNewLine) {
      this.printSelectionEnd();
      this._printSelectionEndAtNewLine = false;
    } else {
      this.buffer.push('<br>');
    }
  };
  PrintWriter.prototype.space = function () {
    this.buffer.push('&nbsp;');
  };
  PrintWriter.prototype.indent = function (len) {
    if (len > 0) {
      let res = '';
      for (let i = 0; i < len; i++) {
        res += this.indentString;
      }
      this.buffer.push(res);
    }
  };
  PrintWriter.prototype.printKey = function (key) {
    this.buffer.push('"');
    this.buffer.push("<span class=\"json-key\">" + escapeHtml(key) + "</span>");
    this.buffer.push('"');
  };
  PrintWriter.prototype.printString = function (value) {
    this.buffer.push('"');
    this.buffer.push("<span class=\"json-string\">" + escapeHtml(value) + "</span>");
    this.buffer.push('"');
  };
  PrintWriter.prototype.printBoolean = function (value) {
    this.buffer.push("<span class=\"json-boolean\">" + value + "</span>");
  };
  PrintWriter.prototype.printNumber = function (value) {
    this.buffer.push("<span class=\"json-number\">" + value + "</span>");
  };
  PrintWriter.prototype.printSelectionStart = function () {
    this.buffer.push("</div>");
    this.buffer.push("<div class=\"json-pretty json-selected\">");
  };
  PrintWriter.prototype.printSelectionEnd = function () {
    this.buffer.push("</div>");
    this.buffer.push("<div class=\"json-pretty\">");
  };
  Object.defineProperty(PrintWriter.prototype, "printSelectionEndAtNewLine", {
    set: function set(value) {
      this._printSelectionEndAtNewLine = value;
    },
    enumerable: true,
    configurable: true
  });
  PrintWriter.prototype.toString = function () {
    return this.buffer.join('');
  };
  return PrintWriter;
}();
let printObject = function printObject(object, out, idt, selection, options) {
  out.checkCircular(object);
  out.print('{');
  out.newLine();
  let keys = Object.keys(object);
  for (let i = 0; i < keys.length; i++) {
    let key = keys[i];
    let value = object[key];
    if (selection === value) {
      out.printSelectionStart();
    }
    out.indent(idt + 1);
    out.printKey(key);
    out.print(':');
    out.space();
    switch (typeof value === "undefined" ? "undefined" : _typeof(value)) {
      case 'number':
        out.printNumber(value);
        break;
      case 'boolean':
        out.printBoolean(value);
        break;
      case 'string':
        out.printString(value);
        break;
      case 'object':
        if (value === null) {
          out.print('null');
        } else if (Array.isArray(value)) {
          printArray(value, out, idt + 1, selection, options);
        } else {
          printObject(value, out, idt + 1, selection, options);
        }
        break;
      case 'undefined':
        out.print('undefined');
        break;
      default:
        throw new Error("Don''t know what to do with " + (typeof value === "undefined" ? "undefined" : _typeof(value)));
    }
    if (i < keys.length - 1) {
      out.print(',');
    }
    out.newLine();
  }
  out.indent(idt);
  out.print('}');
  if (selection === object) {
    out.printSelectionEndAtNewLine = true;
  }
};
let printArray = function printArray(array, out, idt, selection, options) {
  out.checkCircular(array);
  out.print('[');
  out.newLine();
  for (let i = 0; i < array.length; i++) {
    let value = array[i];
    if (selection === value) {
      out.printSelectionStart();
    }
    out.indent(idt + 1);
    switch (typeof value === "undefined" ? "undefined" : _typeof(value)) {
      case 'number':
        out.printNumber(value);
        break;
      case 'boolean':
        out.printBoolean(value);
        break;
      case 'string':
        out.printString(value);
        break;
      case 'object':
        if (value === null) {
          out.print('null');
        } else {
          printObject(value, out, idt + 1, selection, options);
        }
        break;
      case 'undefined':
        out.print('undefined');
        break;
      default:
        throw new Error("Don''t know what to do with " + (typeof value === "undefined" ? "undefined" : _typeof(value)));
    }
    if (i < array.length - 1) {
      out.print(',');
    }
    out.newLine();
  }
  out.indent(idt);
  out.print(']');
  if (selection === array) {
    out.printSelectionEndAtNewLine = true;
  }
};
let prettyPrint = function prettyPrint(object, selection, options) {
  if ((typeof object === "undefined" ? "undefined" : _typeof(object)) !== undefined && object !== null) {
    let opts = Object.assign({indent: '&nbsp;&nbsp;'}, options);
    let out = new PrintWriter(opts.indent);
    if (object === selection) {
      out.print("<div class=\"json-pretty json-selected\">");
    } else {
      out.print("<div class=\"json-pretty\">");
    }
    if (Array.isArray(object)) {
      printArray(object, out, 0, selection, opts);
    } else {
      printObject(object, out, 0, selection, opts);
    }
    out.print("</div>");
    return out.toString();
  }
  return '';
};
